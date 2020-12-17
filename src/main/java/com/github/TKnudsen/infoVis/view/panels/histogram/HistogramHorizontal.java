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

	public HistogramHorizontal(Collection<? extends T> data, Function<? super T, Number> worldToNumberMapping,
			Function<Number, Integer> aggregationFunction, Number maxGlobal, Color defaultColor, Color filterColor) {
		super(data, worldToNumberMapping, aggregationFunction, maxGlobal, false, defaultColor, filterColor);
	}

	/**
	 * 
	 * @param counts
	 * @param defaultColor
	 * @return
	 */
	protected BarChartPainter createAllDataDistributionBarchart(List<? extends Number> counts, Color defaultColor) {
		List<Color> colors = DataConversion.constantValueList(defaultColor, counts.size());

		BarChartPainter barChart = new BarChartHorizontalPainter(counts, colors);
		barChart.setBackgroundPaint(null);
		return barChart;
	}

	/**
	 * 
	 * @return
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
