package com.github.TKnudsen.infoVis.view.panels.histogram;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRange;
import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRangeTools;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterChangedEvent;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.panels.bins.AbstractBinnedDistributionPanel;

/**
 * <p>
 * Numeric histogram (binned distribution) with filter + selection support.
 * 
 * Uses AbstractBinnedDistributionPanel for: - three painter layers (global /
 * filter / selection) - picking (point / rectangle) - filter + selection
 * lifecycle
 * 
 * Subclasses (HistogramVertical / HistogramHorizontal) only override
 * createDistributionPainter(...) if they want horizontal bar painters.
 * </p>
 *
 * @version 2.02
 */
public abstract class Histogram<T> extends AbstractBinnedDistributionPanel<T> {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_BIN_COUNT = 50;

	private final boolean vertical;
	private final int binCount;

	private final Function<? super T, Number> worldToNumberMapping;

	private final Number min;
	private final Number max;

	private final Function<Number, Integer> aggregationFunction;

	protected Histogram(Collection<? extends T> data, Function<? super T, Number> worldToNumberMapping,
			boolean vertical) {
		this(data, worldToNumberMapping, null, null, DEFAULT_BIN_COUNT, vertical, null, null);
	}

	protected Histogram(Collection<? extends T> data, Function<? super T, Number> worldToNumberMapping,
			Number minGlobal, Number maxGlobal, Integer binCount, boolean vertical, Color allDataColor,
			Color filterColor) {

		super(data, allDataColor != null ? allDataColor : Histograms.DEFAULT_COLOR,
				filterColor != null ? filterColor : Histograms.DEFAULT_FILTER_COLOR);

		Objects.requireNonNull(worldToNumberMapping, "worldToNumberMapping must not be null");

		this.vertical = vertical;
		this.worldToNumberMapping = worldToNumberMapping;

		this.binCount = (binCount != null) ? binCount.intValue() : DEFAULT_BIN_COUNT;
		if (this.binCount <= 0) {
			throw new IllegalArgumentException("binCount must be > 0, but was " + this.binCount);
		}

		NumericRange range = NumericRangeTools.computeFiniteRangeStrict(getData(), worldToNumberMapping, minGlobal,
				maxGlobal);

		this.min = range.getMin();
		this.max = range.getMax();

		this.aggregationFunction = Histograms.defaultAggregationFunction(min, max, this.binCount);

		// Everything required by AbstractBinnedDistributionPanel is now defined.
		init();

		// no spacing for histograms
		setGridSpacing(0.0);
	}

	@Override
	protected final int getBinCount() {
		return binCount;
	}

	@Override
	protected final Integer getBinIndex(T item) {
		if (item == null)
			return null;

		Number d = worldToNumberMapping.apply(item);
		if (d == null)
			return null;

		double v = d.doubleValue();
		if (Double.isNaN(v) || Double.isInfinite(v))
			return null;

		Integer idx = aggregationFunction.apply(d);
		return idx;
	}

	@Override
	protected final java.util.List<? extends Number> computeCounts(Collection<? extends T> items) {
		double[] counts = new double[binCount];

		if (items != null) {
			for (T t : items) {
				if (t == null)
					continue;

				Number d = worldToNumberMapping.apply(t);
				if (d == null)
					continue;

				double v = d.doubleValue();
				if (Double.isNaN(v) || Double.isInfinite(v))
					continue;

				Integer idx = aggregationFunction.apply(d);
				if (idx == null)
					continue;

				if (idx < 0 || idx >= counts.length) {
					throw new IllegalStateException("Histogram: aggregation produced invalid index " + idx
							+ " for value " + v + " (binCount=" + binCount + ", range=[" + min + "," + max + "])");
				}

				counts[idx]++;
			}
		}

		return DataConversion.doubleToList(counts);
	}

	@Override
	protected final void initializeAxisPainters(java.util.List<? extends Number> globalCounts) {
		double maxCount = MathFunctions.getMax(globalCounts);

		if (vertical) {
			initializeXAxisPainter(min, max);
			initializeYAxisPainter(0.0, maxCount);
		} else {
			initializeXAxisPainter(0.0, maxCount);
			initializeYAxisPainter(min, max);
		}
	}

	@Override
	public final void initializeXAxisPainter(Number min, Number max) {
		setXAxisPainter(new XAxisNumericalPainter<>(min, max));
	}

	@Override
	public final void initializeYAxisPainter(Number min, Number max) {
		YAxisNumericalPainter<Number> yAxis = new YAxisNumericalPainter<>(min, max);
		yAxis.setFlipAxisValues(true);
		yAxis.setDrawLabelsBetweenMarkers(true);
		setYAxisPainter(yAxis);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	@Override
	public void filterStatusChanged(FilterChangedEvent<T> filterChangedEvent) {
		super.filterStatusChanged(filterChangedEvent);

		// no spacing for histograms
		setGridSpacing(0.0);
	}

	@Override
	protected final void handleSelectionChanged() {
		super.handleSelectionChanged();

		// no spacing for histograms
		setGridSpacing(0.0);
	}

	public final boolean isVertical() {
		return vertical;
	}

	public final Number getMinWorldValue() {
		return min;
	}

	public final Number getMaxWorldValue() {
		return max;
	}
}