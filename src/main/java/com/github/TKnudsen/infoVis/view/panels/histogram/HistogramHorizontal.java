package com.github.TKnudsen.infoVis.view.panels.histogram;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartHorizontalPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter;

public class HistogramHorizontal<T> extends Histogram<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * No global minimum and maximum any more. Interacts with aggregationFunction
	 * where this is already modeled.
	 * 
	 * @param data                 the data collection
	 * @param worldToNumberMapping mapping
	 * @param minGlobal            min
	 * @param maxGlobal            max
	 * @param binCount             null if internal default value shall be taken.
	 *                             Necessary because minimum and maximum in
	 *                             aggregation function may not be reached with the
	 *                             global value domain. As a result it is impossible
	 *                             to anticipate the number of bins required.
	 * 
	 * @param defaultColor         default color
	 * @param filterColor          default color histogram
	 */
	public HistogramHorizontal(Collection<? extends T> data, Function<? super T, Number> worldToNumberMapping,
			Number minGlobal, Number maxGlobal, Integer binCount, Color defaultColor, Color filterColor) {
		super(data, worldToNumberMapping, minGlobal, maxGlobal, binCount, false, defaultColor, filterColor);
	}

	/**
	 * 
	 * @param counts       the counts
	 * @param defaultColor default color
	 * @return bar chart
	 */
	protected BarChartPainter createAllDataDistributionBarchart(List<? extends Number> counts, Color defaultColor) {
		List<Color> colors = DataConversion.constantValueList(defaultColor, counts.size());

		BarChartPainter barChart = new BarChartHorizontalPainter(counts, colors);
		barChart.setBackgroundPaint(null);
		return barChart;
	}

	/**
	 * 
	 * @return bar chart
	 */
	protected BarChartPainter createFilterStatusDistributionBarchartPainter(Color filterColor) {
		List<? extends Number> counts = valuesToCounts.apply(filterStatusData);
		List<Color> colors = DataConversion.constantValueList(filterColor, counts.size());
		BarChartPainter barChart = new BarChartHorizontalPainter(counts, colors);
		barChart.setBackgroundPaint(null);

		return barChart;
	}

	/**
	 * 
	 */
	protected BarChartPainter createSelectionDistributionBarchartPainter(List<T> selection, Color color) {
		List<? extends Number> counts = valuesToCounts.apply(selection);
		List<Color> colors = DataConversion.constantValueList(color, counts.size());

		BarChartHorizontalPainter barChart = new BarChartHorizontalPainter(counts, colors);
		barChart.setBackgroundPaint(null);

		return barChart;
	}
}
