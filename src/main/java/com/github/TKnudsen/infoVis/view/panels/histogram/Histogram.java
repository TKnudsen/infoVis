package com.github.TKnudsen.infoVis.view.panels.histogram;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterChangedEvent;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterStatusListener;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartVerticalPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.XYNumericalChartPanel;
import com.github.TKnudsen.infoVis.view.ui.InfoVisColors;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;

public abstract class Histogram<T> extends XYNumericalChartPanel<Number, Number>
		implements IClickSelection<T>, IRectangleSelection<T>, FilterStatusListener<T>, SelectionListener<T> {

	// Histogram is not a ISelectionVisualizer<T>, because it requires a
	// selection change event to re-create the selection bar chart. Instead
	// Histogram is a SelectionListener<T>.

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Collection<? extends T> data;
	protected Collection<? extends T> filterStatusData;

	private static final int DEFAULT_BIN_COUNT = 50;

	private static final Color DEFAULT_COLOR = Color.GRAY;
	private Color allDataColor;

	private static final Color DEFAULT_FILTER_COLOR = Color.DARK_GRAY;
	private Color filterColor;

	private Color selectionColor = InfoVisColors.SELECTION_COLOR;
	private Function<? super T, Boolean> selectedFunction;

	/**
	 * vertical or horizontal orientation?
	 */
	private final boolean vertical;
	private BarChartPainter globalDistributionBarchartPainter;
	private BarChartPainter filterDistributionBarchartPainter;
	private BarChartPainter selectionDistributionBarchartPainter;

	protected Function<Collection<? extends T>, List<? extends Number>> valuesToCounts;
	private Function<List<Integer>, List<T>> binsToValues;

	Histogram(Collection<? extends T> data, Function<? super T, Number> worldToNumberMapping, boolean vertical) {
		this(data, worldToNumberMapping, null, null, DEFAULT_BIN_COUNT, vertical, null, null);
	}

	/**
	 * No external aggregation functions any more. it interacts with the global
	 * minimum and maximum and thus can only be controlled safely in here.
	 * 
	 * @param data
	 * @param worldToNumberMapping
	 * @param minGlobal
	 * @param maxGlobal
	 * @param binCount             null if internal default value shall be taken
	 * @param vertical
	 * @param defaultColor
	 * @param filterColor
	 */
	Histogram(Collection<? extends T> data, Function<? super T, Number> worldToNumberMapping, Number minGlobal,
			Number maxGlobal, Integer binCount, boolean vertical, Color defaultColor, Color filterColor) {

		Objects.requireNonNull(data);
		Objects.requireNonNull(worldToNumberMapping);

		this.data = Collections.unmodifiableCollection(data);
		this.filterStatusData = Collections.unmodifiableCollection(data);

		this.vertical = vertical;
		this.filterColor = filterColor;

		Number min = Double.POSITIVE_INFINITY;
		Number max = Double.NEGATIVE_INFINITY;
		for (T t : data) {
			Number d = worldToNumberMapping.apply(t);
			min = Math.min(min.doubleValue(), d.doubleValue());
			max = Math.max(max.doubleValue(), d.doubleValue());
		}

		if (minGlobal != null && !Double.isNaN(minGlobal.doubleValue()) && minGlobal.doubleValue() < min.doubleValue())
			min = minGlobal;
		if (maxGlobal != null && !Double.isNaN(maxGlobal.doubleValue()) && maxGlobal.doubleValue() > max.doubleValue())
			max = maxGlobal;

		Function<Number, Integer> aggregation = Histograms.defaultAggregationFunction(min, max,
				binCount != null ? binCount : DEFAULT_BIN_COUNT);

		this.valuesToCounts = values -> {
			double[] counts = new double[binCount != null ? binCount : DEFAULT_BIN_COUNT];

			for (T t : values) {
				Number d = worldToNumberMapping.apply(t);
				Integer index = aggregation.apply(d);
				// necessary because the maxGlobal may not be the max value
				if (index < counts.length)
					counts[index]++;
			}

			return DataConversion.doublePrimitivesToList(counts);
		};

		List<? extends Number> counts = valuesToCounts.apply(data);

		// initialize axes - special case here: for the vertical variant the x Axis
		// shows values but is not synchronized with the bar chart painters.
		// Accordingly, for the horizontal variant the y Axis shows these values.

		// For both cases this is due to the fact that bar charts do not have a
		// numerical axis, still it would be nice here to see the value distribution
		initializeAxisPainters(min, max, counts);

		// initialize and register painters
		globalDistributionBarchartPainter = createAllDataDistributionBarchart(counts,
				defaultColor != null ? defaultColor : DEFAULT_COLOR);
		this.addChartPainter(globalDistributionBarchartPainter, false, true);

		filterDistributionBarchartPainter = createFilterStatusDistributionBarchartPainter(
				filterColor != null ? filterColor : DEFAULT_FILTER_COLOR);
		this.addChartPainter(filterDistributionBarchartPainter, false, true);

		this.binsToValues = bars -> {
			List<T> elements = new ArrayList<T>();
			for (T t : this.filterStatusData) {
				Number d = worldToNumberMapping.apply(t);
				if (bars.contains(aggregation.apply(d)))
					elements.add(t);
			}

			return elements;
		};
	}

	/**
	 * initialize axes - special case here: for the vertical variant the x Axis
	 * shows values but is not synchronized with the bar chart painters.
	 * Accordingly, for the horizontal variant the y Axis shows these values.
	 * 
	 * For both cases this is due to the fact that bar charts do not have a
	 * numerical axis, still it would be nice here to see the value distribution
	 */
	private void initializeAxisPainters(Number min, Number max, List<? extends Number> counts) {
		if (vertical) {
			initializeXAxisPainter(min, max);
			initializeYAxisPainter(0.0, MathFunctions.getMax(counts));
		} else {
			initializeXAxisPainter(0.0, MathFunctions.getMax(counts));
			initializeYAxisPainter(min, max);
		}
	}

	/**
	 * by default a vertical bar chart is created
	 * 
	 * @param counts
	 * @param defaultColor
	 * @return
	 */
	protected BarChartPainter createAllDataDistributionBarchart(List<? extends Number> counts, Color defaultColor) {
		List<Color> colors = DataConversion.constantValueList(defaultColor, counts.size());

		BarChartVerticalPainter barChart = new BarChartVerticalPainter(counts, colors);
		barChart.setBackgroundPaint(null);
		return barChart;
	}

	/**
	 * by default a vertical bar chart is created
	 * 
	 * @return
	 */
	protected BarChartPainter createFilterStatusDistributionBarchartPainter(Color filterColor) {
		List<? extends Number> counts = valuesToCounts.apply(filterStatusData);
		List<Color> colors = DataConversion.constantValueList(filterColor, counts.size());

		BarChartVerticalPainter barChart = new BarChartVerticalPainter(counts, colors);
		barChart.setBackgroundPaint(null);

		return barChart;
	}

	/**
	 * by default a vertical bar chart is created
	 * 
	 * @return
	 */
	protected BarChartPainter createSelectionDistributionBarchartPainter(List<T> selection, Color color) {
		List<? extends Number> counts = valuesToCounts.apply(selection);
		List<Color> colors = DataConversion.constantValueList(color, counts.size());

		BarChartVerticalPainter barChart = new BarChartVerticalPainter(counts, colors);
		barChart.setBackgroundPaint(null);

		return barChart;
	}

	@Override
	public void initializeXAxisPainter(Number min, Number max) {
		setXAxisPainter(new XAxisNumericalPainter<Number>(min, max));
	}

	@Override
	public void initializeYAxisPainter(Number min, Number max) {
		YAxisNumericalPainter<Number> yAxisNumericalPainter = new YAxisNumericalPainter<Number>(min, max);
		yAxisNumericalPainter.setFlipAxisValues(true);

		setYAxisPainter(yAxisNumericalPainter);
	}

	@Override
	public void setXAxisMinValue(Number minValue) {
		throw new UnsupportedOperationException(
				"Histogram: method call not supported - histograms axes min max shall not be parameterized, it interacts with the aggregation function defined a-priori. To keep the Histogram state model compact, the strategy is to create a new instance for axis (min/max) changes.");
	}

	@Override
	public void setXAxisMaxValue(Number maxValue) {
		throw new UnsupportedOperationException(
				"Histogram: method call not supported - histograms axes min max shall not be parameterized, it interacts with the aggregation function defined a-priori. To keep the Histogram state model compact, the strategy is to create a new instance for axis (min/max) changes.");
	}

	@Override
	public void setYAxisMinValue(Number minValue) {
		throw new UnsupportedOperationException(
				"Histogram: method call not supported - histograms axes min max shall not be parameterized, it interacts with the aggregation function defined a-priori. To keep the Histogram state model compact, the strategy is to create a new instance for axis (min/max) changes.");
	}

	@Override
	public void setYAxisMaxValue(Number maxValue) {
		throw new UnsupportedOperationException(
				"Histogram: method call not supported - histograms axes min max shall not be parameterized, it interacts with the aggregation function defined a-priori. To keep the Histogram state model compact, the strategy is to create a new instance for axis (min/max) changes.");
	}

	@Override
	/**
	 * checks if a point intersects with the bars showing the data distribution -
	 * the bars are used which show that data that is not filtered out. not filtered
	 * out.
	 */
	public List<T> getElementsAtPoint(Point p) {
		List<Integer> bars = filterDistributionBarchartPainter.getElementsAtPoint(p);

		return binsToValues.apply(bars);
	}

	@Override
	/**
	 * checks if a rectangle intersects with the bars showing the data distribution
	 * - the bars are used which show that data that is not filtered out. not
	 * filtered out.
	 */
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		List<Integer> bars = filterDistributionBarchartPainter.getElementsInRectangle(rectangle);

		return binsToValues.apply(bars);
	}

	/**
	 * the visual representation of selected elements is handled with a third bar
	 * char painter layer: the selectionDistributionBarchartPainter.
	 * 
	 * This and only this bar chart painter has to be recreated in case of three
	 * events:
	 * 
	 * 1) the filterStatusData has changed
	 * 
	 * 2) the selectedFunction has changed
	 * 
	 * 3) the selection has changed
	 */
	private void handleSelectionChanged() {
		if (selectionDistributionBarchartPainter != null)
			removeChartPainter(selectionDistributionBarchartPainter);

		if (selectedFunction == null)
			return;

		List<T> selection = new ArrayList<T>();
		for (T t : this.filterStatusData)
			if (selectedFunction.apply(t))
				selection.add(t);

		selectionDistributionBarchartPainter = createSelectionDistributionBarchartPainter(selection, selectionColor);

		this.addChartPainter(selectionDistributionBarchartPainter, false, true);

		repaint();
	}

	@Override
	public void filterStatusChanged(FilterChangedEvent<T> filterChangedEvent) {
		if (filterChangedEvent == null || filterChangedEvent.getFilterStatus() == null)
			return;

		List<T> filterStatusData = new ArrayList<T>();

		for (T t : this.data)
			if (filterChangedEvent.getFilterStatus().test(t))
				filterStatusData.add(t);
		this.filterStatusData = filterStatusData;

		removeChartPainter(filterDistributionBarchartPainter);

		filterDistributionBarchartPainter = createFilterStatusDistributionBarchartPainter(
				filterColor != null ? filterColor : DEFAULT_FILTER_COLOR);
		addChartPainter(1, filterDistributionBarchartPainter, false, true);

		handleSelectionChanged();
	}

	@Override
	public void selectionChanged(SelectionEvent<T> selectionEvent) {
		this.selectedFunction = t -> selectionEvent.getSelectionModel().isSelected(t);

		handleSelectionChanged();
	}

	public boolean isVertical() {
		return vertical;
	}

	public Color getAllDataColor() {
		return allDataColor;
	}

	public void setAllDataColor(Color allDataColor) {
		this.allDataColor = allDataColor;

		this.globalDistributionBarchartPainter.setColor(allDataColor);
	}

	public Color getFilterColor() {
		return filterColor;
	}

	public void setFilterColor(Color filterColor) {
		this.filterColor = filterColor;

		this.filterDistributionBarchartPainter.setColor(filterColor);
	}

	public Color getSelectionColor() {
		return selectionColor;
	}

	public void setSelectionColor(Color selectionColor) {
		this.selectionColor = selectionColor;

		if (this.selectionDistributionBarchartPainter != null)
			this.selectionDistributionBarchartPainter.setColor(selectionColor);
	}

	@Override
	public void setShowingTooltips(boolean showingTooltips) {
		super.setShowingTooltips(showingTooltips);

		if (this.xAxisPainter != null)
			this.xAxisPainter.setToolTipping(showingTooltips);
		if (this.yAxisPainter != null)
			this.yAxisPainter.setToolTipping(showingTooltips);
	}

}
