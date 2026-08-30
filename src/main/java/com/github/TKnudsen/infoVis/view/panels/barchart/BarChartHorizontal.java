package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartHorizontalPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter;

/**
 * <p>
 * Horizontal categorical bar chart with filter + selection support.
 * 
 * <p>
 * In this orientation:
 * <ul>
 * <li>X-axis: Count values (bars extend rightwards)</li>
 * <li>Y-axis: Category bins (displayed as numeric indices 0..N, top to
 * bottom)</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * 
 * <pre>{@code
 * List<Person> people = ...;
 * BarChartHorizontal<Person> chart = new BarChartHorizontal<>(
 *     people, 
 *     Person::getCountry
 * );
 * }</pre>
 * </p>
 *
 * @version 2.02
 */
public class BarChartHorizontal<T> extends BarChart<T> {

	private static final long serialVersionUID = 1L;

	public BarChartHorizontal(Collection<? extends T> data, Function<? super T, String> worldToCategoryMapping) {
		super(data, worldToCategoryMapping);
	}

	public BarChartHorizontal(Collection<? extends T> data, Function<? super T, String> worldToCategoryMapping,
			CategoryOrder categoryOrder, List<String> customOrder, boolean includeMissingBin, String missingLabel,
			Color globalColor, Color filterColor) {

		super(data, worldToCategoryMapping, categoryOrder, customOrder, includeMissingBin, missingLabel, globalColor,
				filterColor);
	}

	@Override
	protected void initializeAxisPainters(List<? extends Number> globalCounts) {
		double maxCount = MathFunctions.getMax(globalCounts);

		// Counts on X axis
		initializeXAxisPainter(0.0, Math.max(1.0, maxCount));

		// Categories on Y axis as numeric index range
		initializeYAxisPainter(0.0, Math.max(1.0, (double) getBinCount()));
	}

	@Override
	public void initializeYAxisPainter(Number min, Number max) {
		YAxisNumericalPainter<Number> yAxis = new YAxisNumericalPainter<>(min, max);
		yAxis.setFlipAxisValues(true); // Categories top-to-bottom
		setYAxisPainter(yAxis);
	}

	@Override
	protected BarChartPainter createDistributionPainter(List<? extends Number> counts, Color color) {
		List<Color> colors = DataConversion.constantValueList(color, counts.size());

		BarChartHorizontalPainter barChart = new BarChartHorizontalPainter(counts, colors);
		barChart.setBackgroundPaint(null);
		barChart.setToolTipping(isShowingTooltips());

		return barChart;
	}

}