package com.github.TKnudsen.infoVis.view.panels.histogram;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartHorizontalPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter;

/**
 * <p>
 * Horizontal histogram variant.
 * 
 * Uses horizontal bar chart painters for all three layers (global, filter,
 * selection).
 * </p>
 *
 * @version 2.02 (revised)
 */
public class HistogramHorizontal<T> extends Histogram<T> {

	private static final long serialVersionUID = 1L;

	public HistogramHorizontal(Collection<? extends T> data, Function<? super T, Number> worldToNumberMapping,
			Number minGlobal, Number maxGlobal, Integer binCount, Color defaultColor, Color filterColor) {

		super(data, worldToNumberMapping, minGlobal, maxGlobal, binCount, false, defaultColor, filterColor);
	}

	@Override
	protected BarChartPainter createDistributionPainter(List<? extends Number> counts, Color color) {
		List<Color> colors = DataConversion.constantValueList(color, counts.size());

		BarChartHorizontalPainter barChart = new BarChartHorizontalPainter(counts, colors);
		barChart.setBackgroundPaint(null);
		barChart.setToolTipping(false);

		return barChart;
	}
}