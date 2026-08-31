package com.github.TKnudsen.infoVis.view.panels.bins;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterChangedEvent;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterStatusListener;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartVerticalPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.XYNumericalChartPanel;
import com.github.TKnudsen.infoVis.view.ui.InfoVisColors;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;

/**
 * <p>
 * Base panel for binned distributions rendered as layered bar charts: - Global
 * distribution (all data) - Filter distribution (filtered-in data) - Selection
 * distribution (selected subset of filtered-in data)
 * 
 * Subclasses provide the binning strategy: - number of bins - counts
 * computation - mapping items to bin indices - axis setup (Histogram has
 * numeric axis; categorical usually does not)
 * 
 * Life-cycle: call {@link #dispose()} when no longer needed.
 * 
 * Threading model: this class assumes UI usage (Swing EDT). It is defensive
 * against inconsistent intermediate states by snapshot publishing.
 * 
 * IMPORTANT: Subclasses must call {@link #init()} after their binning logic is
 * ready.
 * </p>
 *
 * @version 1.01
 */
public abstract class AbstractBinnedDistributionPanel<T> extends XYNumericalChartPanel<Number, Number>
		implements IClickSelection<T>, IRectangleSelection<T>, FilterStatusListener<T>, SelectionListener<T> {

	private static final long serialVersionUID = 1L;

	// immutable input
	private final Collection<? extends T> data;

	// mutable state
	private volatile Collection<? extends T> filterStatusData;
	private volatile Function<? super T, Boolean> selectedFunction;

	private volatile boolean disposed = false;
	private volatile boolean initialized = false;

	// colors
	private Color globalColor;
	private Color filterColor;
	private Color selectionColor = InfoVisColors.SELECTION_COLOR;

	// painters (created in init())
	private BarChartPainter globalDistributionBarchartPainter;
	private BarChartPainter filterDistributionBarchartPainter;
	private BarChartPainter selectionDistributionBarchartPainter;

	private final int globalDistributionBarchartPainterIndex = 0;
	private final int filterDistributionBarchartPainterIndex = 1;
	private final int selectionDistributionBarchartPainterIndex = 2;

	// Bin index -> items mapping; published atomically as immutable snapshot
	private volatile List<Set<T>> binIndexToItems = Collections.emptyList();

	protected AbstractBinnedDistributionPanel(Collection<? extends T> data, Color globalColor, Color filterColor) {
		if (data == null) {
			throw new IllegalArgumentException("data must not be null");
		}
		this.data = Collections.unmodifiableCollection(data);
		this.filterStatusData = new ArrayList<>(data);

		this.globalColor = (globalColor != null) ? globalColor : Color.GRAY;
		this.filterColor = (filterColor != null) ? filterColor : Color.DARK_GRAY;

		setShowingTooltips(true);

		// NOTE: no painters are created here.
		// Subclasses must call init() once binning is defined.
	}

	// =========================
	// Subclass contract
	// =========================

	protected abstract int getBinCount();

	/**
	 * Must return a bin index in [0, getBinCount()-1], or null if the item is not
	 * representable.
	 */
	protected abstract Integer getBinIndex(T item);

	/**
	 * Must return a counts list of size getBinCount().
	 */
	protected abstract List<? extends Number> computeCounts(Collection<? extends T> items);

	/**
	 * Subclass decides which axes to show.
	 */
	protected abstract void initializeAxisPainters(List<? extends Number> globalCounts);

	/**
	 * Subclass may override to use a horizontal bar chart painter, etc.
	 */
	protected BarChartPainter createDistributionPainter(List<? extends Number> counts, Color color) {
		List<Color> colors = DataConversion.constantValueList(color, counts.size());

		BarChartVerticalPainter barChart = new BarChartVerticalPainter(counts, colors);
		barChart.setBackgroundPaint(null);
		barChart.setToolTipping(false);
		return barChart;
	}

	// =========================
	// Initialization
	// =========================

	/**
	 * Must be called exactly once by subclass after binning is fully defined.
	 */
	protected final void init() {
		if (disposed) {
			throw new IllegalStateException("Panel is disposed; cannot init()");
		}
		if (initialized) {
			throw new IllegalStateException("init() called twice");
		}

		final int bins = getBinCount();
		if (bins <= 0) {
			throw new IllegalStateException("getBinCount() must be > 0, but was " + bins);
		}

		// global counts + axis
		List<? extends Number> globalCounts = computeCounts(data);
		validateCounts(globalCounts, bins);

		initializeAxisPainters(globalCounts);

		// Batch the two addChartPainter() calls below into one layout pass instead
		// of two -- each would otherwise trigger its own full updateBounds().
		suspendLayoutUpdates();
		try {
			// global painter
			globalDistributionBarchartPainter = createDistributionPainter(globalCounts, globalColor);
			addChartPainter(globalDistributionBarchartPainterIndex, globalDistributionBarchartPainter, false, true);

			// filter painter
			List<? extends Number> filterCounts = computeCounts(filterStatusData);
			validateCounts(filterCounts, bins);

			filterDistributionBarchartPainter = createDistributionPainter(filterCounts, filterColor);

			// Ensure consistent scaling against global distribution
			if (filterDistributionBarchartPainter.getPositionEncodingFunction() != null
					&& globalDistributionBarchartPainter.getPositionEncodingFunction() != null) {
				filterDistributionBarchartPainter.getPositionEncodingFunction().setMaxWorldValue(
						globalDistributionBarchartPainter.getPositionEncodingFunction().getMaxWorldValue());
			}

			// filterDistributionBarchartPainter.setBorderPaint(selectionColor);

			addChartPainter(filterDistributionBarchartPainterIndex, filterDistributionBarchartPainter, false, true);
		} finally {
			resumeLayoutUpdates();
		}

		// build bin mapping
		rebuildBinIndexToItems(filterStatusData);

		initialized = true;

		// selection painter depends on selectedFunction and filtered data
		handleSelectionChanged();

		repaint();
	}

	private void validateCounts(List<? extends Number> counts, int expectedSize) {
		if (counts == null) {
			throw new IllegalStateException("computeCounts(...) returned null");
		}
		if (counts.size() != expectedSize) {
			throw new IllegalStateException(
					"computeCounts(...) returned " + counts.size() + " bins, expected " + expectedSize);
		}
	}

	protected final boolean isInitialized() {
		return initialized;
	}

	// =========================
	// Binning mapping
	// =========================

	protected final void rebuildBinIndexToItems(Collection<? extends T> items) {
		final int bins = getBinCount();
		List<Set<T>> map = new ArrayList<>(bins);
		for (int i = 0; i < bins; i++) {
			map.add(new HashSet<>());
		}

		if (items != null) {
			for (T t : items) {
				Integer idx = getBinIndex(t);
				if (idx == null)
					continue;
				if (idx < 0 || idx >= bins) {
					throw new IllegalStateException(
							"getBinIndex returned out-of-range index " + idx + " (bins=" + bins + ")");
				}
				map.get(idx).add(t);
			}
		}

		// publish immutable snapshot
		List<Set<T>> immutable = new ArrayList<>(bins);
		for (Set<T> s : map) {
			immutable.add(Collections.unmodifiableSet(s));
		}
		this.binIndexToItems = Collections.unmodifiableList(immutable);
	}

	protected final List<T> itemsForBins(List<Integer> bins) {
		if (bins == null || bins.isEmpty())
			return Collections.emptyList();

		List<Set<T>> mapping = this.binIndexToItems;
		if (mapping == null || mapping.isEmpty())
			return Collections.emptyList();

		Set<T> result = new HashSet<>();
		for (Integer idx : bins) {
			if (idx == null)
				continue;
			if (idx < 0 || idx >= mapping.size())
				continue;
			result.addAll(mapping.get(idx));
		}

		return new ArrayList<>(result);
	}

	// =========================
	// PAINTING AND DRAWING
	// =========================

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	// =========================
	// Public API
	// =========================

	public final Collection<? extends T> getData() {
		return data;
	}

	protected final Collection<? extends T> getFilterStatusData() {
		return filterStatusData;
	}

	public final boolean isDisposed() {
		return disposed;
	}

	public final void dispose() {
		if (disposed)
			return;

		disposed = true;

		// Remove painters
		if (globalDistributionBarchartPainter != null) {
			removeChartPainter(globalDistributionBarchartPainter);
			globalDistributionBarchartPainter = null;
		}
		if (filterDistributionBarchartPainter != null) {
			removeChartPainter(filterDistributionBarchartPainter);
			filterDistributionBarchartPainter = null;
		}
		if (selectionDistributionBarchartPainter != null) {
			removeChartPainter(selectionDistributionBarchartPainter);
			selectionDistributionBarchartPainter = null;
		}

		filterStatusData = null;
		selectedFunction = null;
		binIndexToItems = Collections.emptyList();
	}

	// =========================
	// Selection handling
	// =========================

	public final Function<? super T, Boolean> getSelectedFunction() {
		return selectedFunction;
	}

	public final void setSelectedFunction(Function<? super T, Boolean> selectedFunction) {
		this.selectedFunction = selectedFunction;

		if (initialized) {
			handleSelectionChanged();
		}
	}

	protected void handleSelectionChanged() {
		if (disposed || !initialized)
			return;

		if (selectionDistributionBarchartPainter != null) {
			removeChartPainter(selectionDistributionBarchartPainter);
			selectionDistributionBarchartPainter = null;
		}

		Function<? super T, Boolean> sf = this.selectedFunction;
		if (sf == null)
			return;

		List<T> selection = new ArrayList<>();
		Collection<? extends T> filtered = this.filterStatusData;
		if (filtered != null) {
			for (T t : filtered) {
				if (Boolean.TRUE.equals(sf.apply(t))) {
					selection.add(t);
				}
			}
		}

		List<? extends Number> counts = computeCounts(selection);
		validateCounts(counts, getBinCount());

		selectionDistributionBarchartPainter = createDistributionPainter(counts, selectionColor);

		// Align selection scaling to global distribution (same as filter painter)
		if (selectionDistributionBarchartPainter.getPositionEncodingFunction() != null
				&& globalDistributionBarchartPainter != null
				&& globalDistributionBarchartPainter.getPositionEncodingFunction() != null) {
			selectionDistributionBarchartPainter.getPositionEncodingFunction().setMaxWorldValue(
					globalDistributionBarchartPainter.getPositionEncodingFunction().getMaxWorldValue());
		}

		// selectionDistributionBarchartPainter.setBorderPaint(selectionColor);

		addChartPainter(selectionDistributionBarchartPainterIndex, selectionDistributionBarchartPainter, false, true);

		repaint();
	}

	@Override
	public final void selectionChanged(SelectionEvent<T> selectionEvent) {
		if (disposed || selectionEvent == null || selectionEvent.getSelectionModel() == null)
			return;

		setSelectedFunction(t -> selectionEvent.getSelectionModel().isSelected(t));
	}

	// =========================
	// Filter handling
	// =========================

	@Override
	public void filterStatusChanged(FilterChangedEvent<T> filterChangedEvent) {
		if (disposed || filterChangedEvent == null || filterChangedEvent.getFilterStatus() == null)
			return;

		// Update filtered snapshot
		List<T> filtered = new ArrayList<>();
		for (T t : data) {
			if (filterChangedEvent.getFilterStatus().test(t)) {
				filtered.add(t);
			}
		}
		this.filterStatusData = filtered;

		// If not initialized, just store filter status for later init().
		if (!initialized) {
			return;
		}

		// Recreate filter painter
		if (filterDistributionBarchartPainter != null) {
			removeChartPainter(filterDistributionBarchartPainter);
			filterDistributionBarchartPainter = null;
		}

		List<? extends Number> counts = computeCounts(filterStatusData);
		validateCounts(counts, getBinCount());

		filterDistributionBarchartPainter = createDistributionPainter(counts, filterColor);

		// Align max to global distribution
		if (filterDistributionBarchartPainter.getPositionEncodingFunction() != null
				&& globalDistributionBarchartPainter != null
				&& globalDistributionBarchartPainter.getPositionEncodingFunction() != null) {
			filterDistributionBarchartPainter.getPositionEncodingFunction().setMaxWorldValue(
					globalDistributionBarchartPainter.getPositionEncodingFunction().getMaxWorldValue());
		}

		// filterDistributionBarchartPainter.setBorderPaint(selectionColor);

		addChartPainter(filterDistributionBarchartPainterIndex, filterDistributionBarchartPainter, false, true);

		// Rebuild bin mapping for hit-testing / selection
		rebuildBinIndexToItems(filterStatusData);

		// Selection layer depends on filtered data
		handleSelectionChanged();
	}

	// =========================
	// Picking: shared for all binned distributions
	// =========================

	@Override
	public final List<T> getElementsAtPoint(Point p) {
		if (disposed || !initialized || p == null || filterDistributionBarchartPainter == null)
			return Collections.emptyList();

		List<Integer> bins = filterDistributionBarchartPainter.getElementsAtPoint(p);
		return itemsForBins(bins);
	}

	@Override
	public final List<T> getElementsInRectangle(RectangularShape rectangle) {
		if (disposed || !initialized || rectangle == null || filterDistributionBarchartPainter == null)
			return Collections.emptyList();

		List<Integer> bins = filterDistributionBarchartPainter.getElementsInRectangle(rectangle);
		return itemsForBins(bins);
	}

	public final Color getGlobalColor() {
		return globalColor;
	}

	public final void setGlobalColor(Color globalColor) {
		this.globalColor = globalColor;

		if (globalDistributionBarchartPainter != null) {
			globalDistributionBarchartPainter.setColor(globalColor);
			repaint();
		}
	}

	public final Color getFilterColor() {
		return filterColor;
	}

	public final void setFilterColor(Color filterColor) {
		this.filterColor = filterColor;

		if (filterDistributionBarchartPainter != null) {
			filterDistributionBarchartPainter.setColor(filterColor);
			repaint();
		}
	}

	public final Color getSelectionColor() {
		return selectionColor;
	}

	public final void setSelectionColor(Color selectionColor) {
		this.selectionColor = selectionColor;

		if (selectionDistributionBarchartPainter != null) {
			selectionDistributionBarchartPainter.setColor(selectionColor);
			repaint();
		}
	}

	public double getGridSpacing() {
		return getGlobalPainter() != null ? getGlobalPainter().getGridSpacing() : Double.NaN;
	}

	public void setGridSpacing(double gridSpacing) {
		if (getGlobalPainter() != null) {
			getGlobalPainter().setGridSpacing(gridSpacing);
		}
		if (getFilterPainter() != null) {
			getFilterPainter().setGridSpacing(gridSpacing);
		}
		if (getSelectionPainter() != null) {
			getSelectionPainter().setGridSpacing(gridSpacing);
		}
	}

	@Override
	public void setShowingTooltips(boolean showingTooltips) {
		super.setShowingTooltips(showingTooltips);

		// Update all existing painters
		if (getGlobalPainter() != null) {
			getGlobalPainter().setToolTipping(showingTooltips);
		}
		if (getFilterPainter() != null) {
			getFilterPainter().setToolTipping(showingTooltips);
		}
		if (getSelectionPainter() != null) {
			getSelectionPainter().setToolTipping(showingTooltips);
		}

		repaint();
	}

	public BarChartPainter getGlobalPainter() {
		return globalDistributionBarchartPainter;
	}

	public BarChartPainter getFilterPainter() {
		return filterDistributionBarchartPainter;
	}

	public BarChartPainter getSelectionPainter() {
		return selectionDistributionBarchartPainter;
	}
}