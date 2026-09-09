package com.github.TKnudsen.infoVis.view.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRange;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.controls.rangeSlider.InfoVisRangeSlider;
import com.github.TKnudsen.infoVis.view.interaction.controls.rangeSlider.InfoVisRangeSliderPanel;
import com.github.TKnudsen.infoVis.view.interaction.controls.rangeSlider.InfoVisRangeSliderPanels;
import com.github.TKnudsen.infoVis.view.interaction.controls.rangeSlider.InfoVisRangeSliders;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterChangedEvent;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterStatusListener;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.TitlePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanels;
import com.github.TKnudsen.infoVis.view.panels.histogram.Histogram;
import com.github.TKnudsen.infoVis.view.panels.histogram.Histograms;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctions;

import de.javagl.selection.SelectionModel;

/**
 * <p>
 * Dynamic query view with histogram and range slider for filtering data.
 * 
 * <h2>Lifecycle Management</h2> Always call {@link #dispose()} when this view
 * is no longer needed to prevent memory leaks. This clears all listeners and
 * releases resources.
 * </p>
 *
 * @version 2.0 (revised)
 * @since 2019
 */
public class DynamicQueryView<T> extends JPanel
		implements Predicate<T>, FilterStatusListener<T>, IClickSelection<T>, IRectangleSelection<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Function<T, Number> scaledToNumberFunction;

	public static final int Y_AXIS_WIDTH = 28;
	private final Histogram<T> histogram;

	// needed because the slider requires space left and right
	private final JPanel histogramEastSpacer;
	private final JPanel westSpacerYAxisForSlider;

	private final InfoVisRangeSliderPanel rangeSliderPanel;
	private final InfoVisRangeSlider rangeSlider;
	private static final int INTEGER_MULTIPLIER = 1000;

	// FIX: Instance field instead of static
	private final double largeValueMitigator;
	// private static double LARGE_VALUE_MITIGATOR = 1.0;

	private boolean missingValuesAreIn = false;

	// Store listener reference for removal
	private final ChangeListener rangeSliderChangeListener;

	// track disposed state
	private boolean disposed = false;

	/**
	 * listeners
	 */
	private final Collection<FilterStatusListener<T>> filterStatusListeners = new ArrayList<FilterStatusListener<T>>();

	/**
	 * 
	 * @param data             the data distribution
	 * @param toNumberFunction the function that maps the data to numbers
	 * @param selectionModel   the selection model is NOT meant to represent the
	 *                         internal (or external) filter status of the dynamic
	 *                         query (queries). It can be used to synchronize data
	 *                         selections within the data distribution chart.
	 */
	public DynamicQueryView(Collection<T> data, Function<T, Number> toNumberFunction,
			SelectionModel<T> selectionModel) {
		this(data, toNumberFunction, selectionModel, 25);
	}

	/**
	 * 
	 * @param data             the data distribution
	 * @param toNumberFunction the function that maps the data to numbers
	 * @param selectionModel   the selection model is NOT meant to represent the
	 *                         internal (or external) filter status of the dynamic
	 *                         query (queries). It can be used to synchronize data
	 *                         selections within the data distribution chart.
	 * @param binCount         the number of bins for the value histogram. Default:
	 *                         25
	 */
	public DynamicQueryView(Collection<T> data, Function<T, Number> toNumberFunction, SelectionModel<T> selectionModel,
			int binCount) {
		this(data, toNumberFunction, selectionModel, binCount, null, null);
	}

	/**
	 * 
	 * @param data             the data distribution
	 * @param toNumberFunction the function that maps the data to numbers
	 * @param selectionModel   the selection model is NOT meant to represent the
	 *                         internal (or external) filter status of the dynamic
	 *                         query (queries). It can be used to synchronize data
	 *                         selections within the data distribution chart.
	 * @param binCount         the number of bins for the value histogram. Default:
	 *                         25
	 * @param defaultColor     color of the overall data distribution
	 * @param filterColor      color of the data that is filtered out
	 */
	public DynamicQueryView(Collection<T> data, Function<T, Number> toNumberFunction, SelectionModel<T> selectionModel,
			int binCount, Color defaultColor, Color filterColor) {
		super(new BorderLayout());

		Objects.requireNonNull(data);
		Objects.requireNonNull(toNumberFunction);
		Objects.requireNonNull(selectionModel);

		if (binCount < 1)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + ": bin count must be creater zero");

		this.largeValueMitigator = calculateLargeValueMitigator(data, toNumberFunction);
		this.scaledToNumberFunction = scaledToNumberFunction(toNumberFunction, largeValueMitigator);

		// range slider
		rangeSliderPanel = createRangeSliderPanel(data, toNumberFunction, largeValueMitigator);
		rangeSlider = rangeSliderPanel.getRangeSlider();
		rangeSlider.getRangeSliderUI().setRangeColor(filterColor != null ? filterColor : Color.DARK_GRAY);
		rangeSlider.setMinimumSize(new Dimension(0, InfoVisRangeSlider.SLIDER_POINTER_WIDTH));
		rangeSlider.setPreferredSize(new Dimension(0, InfoVisRangeSlider.SLIDER_POINTER_WIDTH));
		rangeSlider.setMaximumSize(new Dimension(0, InfoVisRangeSlider.SLIDER_POINTER_WIDTH));

		JPanel southGrid = new JPanel(new GridLayout(1, 1));
		southGrid.add(rangeSliderPanel);

		JPanel south = new JPanel(new BorderLayout());
		westSpacerYAxisForSlider = new JPanel();
		westSpacerYAxisForSlider.setPreferredSize(
				new Dimension((int) (Y_AXIS_WIDTH - InfoVisRangeSlider.SLIDER_POINTER_WIDTH * 0.5), 0));
		south.add(westSpacerYAxisForSlider, BorderLayout.WEST);
		south.add(southGrid, BorderLayout.CENTER);

		add(south, BorderLayout.SOUTH);

		// This is for internal highlighting reasons
		// Store listener reference for later removal
		this.rangeSliderChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (disposed)
					return; // Guard against post-disposal events

				FilterChangedEvent<T> filterChangedEvent = new FilterChangedEvent<>(DynamicQueryView.this,
						DynamicQueryView.this);

				// Defensive copy to avoid ConcurrentModificationException
				for (FilterStatusListener<T> listener : new ArrayList<>(filterStatusListeners)) {
					listener.filterStatusChanged(filterChangedEvent);
				}

				if (rangeSlider.isInNeutralState())
					rangeSlider.getRangeSliderUI().setRangeColor(filterColor != null ? filterColor : Color.DARK_GRAY);
				else
					rangeSlider.getRangeSliderUI()
							.setRangeColor(filterColor != null ? filterColor : Histograms.DEFAULT_COLOR);

				repaint();
			}
		};
		rangeSlider.addChangeListener(rangeSliderChangeListener);

		// vertical histogram and spacing
		histogram = Histograms.create(data, scaledToNumberFunction, null, null, binCount, true, defaultColor,
				filterColor);
		// do not draw the histogram's x axis. it shows numbers according to the
		// INTEGER_MULTIPLIER. show the axis of the slider instead
		histogram.setDrawXAxis(false);
		histogram.setDrawYAxis(true);
		histogram.setYAxisOverlay(false);
		histogram.setYAxisLegendWidth(Y_AXIS_WIDTH);
		histogram.setXAxisLegendHeight(20); // has no effect, looks like as for 30
		histogram.setShowingTooltips(true);
		Histograms.addInteraction(histogram, selectionModel, true, true);
		addFilterStatusListener(histogram);

		JPanel histogramCanvas = new JPanel(new BorderLayout());
		histogramEastSpacer = new JPanel();
		histogramEastSpacer.setPreferredSize(new Dimension((int) (InfoVisRangeSlider.SLIDER_POINTER_WIDTH * 0.5), 0));
		histogramCanvas.add(histogramEastSpacer, BorderLayout.EAST);
		histogramCanvas.add(histogram, BorderLayout.CENTER);

		add(histogramCanvas, BorderLayout.CENTER);
	}

	/**
	 * Calculates the mitigator value for this instance based on data range.
	 * 
	 * @return mitigator value (1.0 for normal ranges, smaller for large values)
	 */
	private double calculateLargeValueMitigator(Collection<T> data, Function<T, Number> toNumberFunction) {
		double max = Double.NEGATIVE_INFINITY;

		for (T t : data) {
			Number n = toNumberFunction.apply(t);
			if (n == null)
				continue;

			double d = n.doubleValue();
			if (Double.isNaN(d))
				continue;

			max = Math.max(max, d);
		}

		if (max > Integer.MAX_VALUE / INTEGER_MULTIPLIER) {
			double dec = Math.ceil(Math.log10(max / (Integer.MAX_VALUE / INTEGER_MULTIPLIER)));
			return 1.0 / Math.pow(10, dec);
		}

		return 1.0;
	}

	private InfoVisRangeSliderPanel createRangeSliderPanel(Collection<T> data, Function<T, Number> toNumberFunction,
			double largeValueMitigator) {

		// tolerant: a filterable attribute can legitimately have zero variance in
		// the current data (e.g. every item is 0% for a given category) -- the
		// slider should render as a degenerate single-point range, not prevent the
		// whole view from being constructed
		NumericRange range = PositionEncodingFunctions.computeRangeTolerant(data, toNumberFunction,
				getClass().getSimpleName() + " range slider");

		double worldMin = range.getMin();
		double worldMax = range.getMax();
		if (worldMin == worldMax) {
			// InfoVisRangeSliderPanel itself requires min < max (a slider needs a
			// draggable range to render). Widen by an epsilon relative to the int
			// scale below so the floored/ceiled slider bounds always end up distinct,
			// regardless of the value's magnitude.
			double epsilon = 1.0 / (largeValueMitigator * INTEGER_MULTIPLIER);
			worldMin -= epsilon;
			worldMax += epsilon;
		}

		return new InfoVisRangeSliderPanel(
				(int) Math.floor(worldMin * (largeValueMitigator * INTEGER_MULTIPLIER)),
				(int) Math.ceil(worldMax * (largeValueMitigator * INTEGER_MULTIPLIER)),
				(int) Math.floor(worldMin * (largeValueMitigator * INTEGER_MULTIPLIER)),
				(int) Math.ceil(worldMax * (largeValueMitigator * INTEGER_MULTIPLIER)), worldMin, worldMax);
	}

	/**
	 * Releases all resources and removes all listeners.
	 * 
	 * <p>
	 * <b>CRITICAL:</b> Always call this method when the view is no longer needed to
	 * prevent memory leaks. After calling dispose(), this view should not be used.
	 * </p>
	 * 
	 * <p>
	 * This method is idempotent - safe to call multiple times.
	 * </p>
	 */
	public void dispose() {
		if (disposed)
			return;

		disposed = true;

		// Remove change listener from range slider
		if (rangeSlider != null && rangeSliderChangeListener != null)
			rangeSlider.removeChangeListener(rangeSliderChangeListener);

		// Clear all filter status listeners
		filterStatusListeners.clear();

		if (histogram != null)
			histogram.dispose();
	}

	/**
	 * Checks if this view has been disposed.
	 * 
	 * @return true if dispose() has been called
	 */
	public boolean isDisposed() {
		return disposed;
	}

	/**
	 * Guards against operations after disposal.
	 */
	private void checkNotDisposed() {
		if (disposed)
			throw new IllegalStateException("DynamicQueryView has been disposed");
	}

	public void setTitle(String title) {
		checkNotDisposed();

		Collection<TitlePainter> titlePainters = new ArrayList<>();
		for (ChartPainter painter : histogram.getChartPainters())
			if (painter instanceof TitlePainter)
				titlePainters.add((TitlePainter) painter);

		for (TitlePainter titlePainter : titlePainters)
			histogram.removeChartPainter(titlePainter);

		InfoVisChartPanels.addTitle(histogram, title);
	}

	public void addFilterStatusListener(FilterStatusListener<T> listener) {
		checkNotDisposed();
		Objects.requireNonNull(listener, "listener");

		this.filterStatusListeners.remove(listener);
		this.filterStatusListeners.add(listener);
	}

	public void removeFilterStatusListener(FilterStatusListener<T> listener) {
		this.filterStatusListeners.remove(listener);
	}

	/**
	 * Removes all filter status listeners.
	 */
	public void clearFilterStatusListeners() {
		this.filterStatusListeners.clear();
	}

	@Override
	public void filterStatusChanged(FilterChangedEvent<T> filterChangedEvent) {
		if (!disposed) {
			histogram.filterStatusChanged(filterChangedEvent);
		}
	}

	public void addChangeListener(ChangeListener l) {
		checkNotDisposed();
		this.rangeSlider.addChangeListener(l);
	}

	/**
	 * Removes a ChangeListener from the slider.
	 * 
	 * @param l the ChangeListener to remove
	 */
	public void removeChangeListener(ChangeListener l) {
		if (rangeSlider != null) {
			this.rangeSlider.removeChangeListener(l);
		}
	}

	@Override
	public boolean test(T t) {
		if (disposed)
			return true; // Neutral behavior after disposal

		// this is new: a range slider is only active if the two sliders are not in
		// minimum-maximum (default) constellation
		if (rangeSlider.isInNeutralState())
			return true;

		double d = scaledToNumberFunction.apply(t).doubleValue();
		if (Double.isNaN(d) && missingValuesAreIn)
			return true;

		return rangeSlider.inRange(d);
	}

	public IPositionEncodingFunction getXPositionEncodingFunction() {
		checkNotDisposed();

		System.err.println(
				"DynamicQueryView:getXPositionEncodingFunction returns the range slider position encoding function which has "
						+ (largeValueMitigator * INTEGER_MULTIPLIER) + " times scaled values");
		return rangeSlider.getXPositionEncodingFunction();
	}

	private Function<T, Number> scaledToNumberFunction(Function<T, Number> toNumberFunction,
			double largeValueMitigator) {
		return t -> toNumberFunction.apply(t).doubleValue() * (largeValueMitigator * INTEGER_MULTIPLIER);
	}

	public Histogram<T> getHistogram() {
		return histogram;
	}

	public boolean isShowingTooltips() {
		return histogram.isShowingTooltips();
	}

	public void setShowingTooltips(boolean showingTooltips) {
		checkNotDisposed();
		histogram.setShowingTooltips(showingTooltips);
		InfoVisRangeSliderPanels.setShowingTooltips(rangeSliderPanel, showingTooltips);
	}

	public Number getMinimumRangeBound() {
		checkNotDisposed();
		return InfoVisRangeSliders.getMinRangeBound(rangeSlider).doubleValue()
				/ (largeValueMitigator * INTEGER_MULTIPLIER);
	}

	public void setMinimumRangeBound(Number value) {
		checkNotDisposed();
		rangeSlider.setLowValue((int) (value.doubleValue() * (largeValueMitigator * INTEGER_MULTIPLIER)));
	}

	public Number getMaximumRangeBound() {
		checkNotDisposed();
		return InfoVisRangeSliders.getMaxRangeBound(rangeSlider).doubleValue()
				/ (largeValueMitigator * INTEGER_MULTIPLIER);
	}

	public void setMaximumRangeBound(Number value) {
		checkNotDisposed();
		rangeSlider.setHighValue((int) (value.doubleValue() * (largeValueMitigator * INTEGER_MULTIPLIER)));
	}

	public boolean isMissingValuesAreIn() {
		return missingValuesAreIn;
	}

	public void setMissingValuesAreIn(boolean missingValuesAreIn) {
		this.missingValuesAreIn = missingValuesAreIn;
	}

	/**
	 * Sets the background color of this component.
	 *
	 * @param bg the desired background <code>Color</code>
	 * @see java.awt.Component#getBackground
	 * @see #setOpaque
	 *
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);

		if (histogram != null)
			histogram.setBackground(bg);
		if (rangeSliderPanel != null)
			rangeSliderPanel.setBackground(bg);
		if (histogramEastSpacer != null)
			histogramEastSpacer.setBackground(bg);
		if (westSpacerYAxisForSlider != null)
			westSpacerYAxisForSlider.setBackground(bg);
	}

	/**
	 * Sets the foreground color of this component. It is up to the look and feel to
	 * honor this property, some may choose to ignore it.
	 * 
	 * The Histogram foreground will also be set, and within the font colors of all
	 * painters.
	 *
	 * @param fg the desired foreground <code>Color</code>
	 * @see java.awt.Component#getForeground
	 *
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);

		if (histogram != null)
			histogram.setForeground(fg);
		if (rangeSliderPanel != null)
			rangeSliderPanel.setForeground(fg);
	}

	public void setDrawYAxis(boolean drawYAxis) {
		this.histogram.setDrawYAxis(drawYAxis);

		if (drawYAxis)
			westSpacerYAxisForSlider.setPreferredSize(
					new Dimension((int) (Y_AXIS_WIDTH - InfoVisRangeSlider.SLIDER_POINTER_WIDTH * 0.5), 0));
		else
			westSpacerYAxisForSlider.setPreferredSize(new Dimension(0, 0));

		revalidate();
		repaint();
	}

	/**
	 * Returns whether the lower thumb is locked.
	 */
	public boolean isLowerThumbLocked() {
		return rangeSlider.isLowerThumbLocked();
	}

	/**
	 * Sets whether the lower thumb is locked. Note: Both thumbs cannot be locked
	 * simultaneously.
	 */
	public void setLowerThumbLocked(boolean locked) {
		rangeSlider.setLowerThumbLocked(locked);
	}

	/**
	 * Returns whether the upper thumb is locked.
	 */
	public boolean isUpperThumbLocked() {
		return rangeSlider.isUpperThumbLocked();
	}

	/**
	 * Sets whether the upper thumb is locked. Note: Both thumbs cannot be locked
	 * simultaneously.
	 */
	public void setUpperThumbLocked(boolean locked) {
		rangeSlider.setUpperThumbLocked(locked);
	}

	@Override
	public List<T> getElementsAtPoint(Point p) {
		return histogram.getElementsAtPoint(p);
	}

	@Override
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		return histogram.getElementsInRectangle(rectangle);
	}
}
