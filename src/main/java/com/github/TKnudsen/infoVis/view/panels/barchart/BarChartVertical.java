package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.painters.axis.categorical.XAxisCategoricalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartVerticalPainter;

/**
 * <p>
 * Vertical categorical bar chart with filter + selection support.
 * 
 * <p>
 * In this orientation:
 * <ul>
 * <li>X-axis: Category bins (displayed as numeric indices 0..N)</li>
 * <li>Y-axis: Count values (bars extend upward)</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * 
 * <pre>{@code
 * List<Person> people = ...;
 * BarChartVertical<Person> chart = new BarChartVertical<>(
 *     people, 
 *     Person::getCountry
 * );
 * }</pre>
 * </p>
 *
 * @version 2.02
 */
public class BarChartVertical<T> extends BarChart<T> {

	private static final long serialVersionUID = 1L;

	private XAxisCategoricalPainter<List<String>> xAxisLabelsPainter;

	public BarChartVertical(Collection<? extends T> data, Function<? super T, String> worldToCategoryMapping) {
		super(data, worldToCategoryMapping);
	}

	public BarChartVertical(Collection<? extends T> data, Function<? super T, String> worldToCategoryMapping,
			CategoryOrder categoryOrder, List<String> customOrder, boolean includeMissingBin, String missingLabel,
			Color globalColor, Color filterColor) {

		super(data, worldToCategoryMapping, categoryOrder, customOrder, includeMissingBin, missingLabel, globalColor,
				filterColor);
	}

	@Override
	protected void initializeAxisPainters(List<? extends Number> globalCounts) {
		double maxCount = MathFunctions.getMax(globalCounts);

		// Categories on X axis as numeric index range
		initializeXAxisPainter(0.0, Math.max(1.0, (double) getBinCount()));

		// Counts on Y axis
		initializeYAxisPainter(0.0, Math.max(1.0, maxCount));
	}

	@Override
	public void initializeXAxisPainter(Number min, Number max) {
		XAxisNumericalPainter<Number> painter = new XAxisNumericalPainter<>(min, max);
		painter.setDrawLabels(false);
		setXAxisPainter(painter);

		xAxisLabelsPainter = new XAxisCategoricalPainter<List<String>>(getBinLabels());
		xAxisLabelsPainter.setBackgroundPaint(null);
	}

	@Override
	public void initializeYAxisPainter(Number min, Number max) {
		YAxisNumericalPainter<Number> yAxis = new YAxisNumericalPainter<>(min, max);
		yAxis.setFlipAxisValues(true); // Counts grow upward
		setYAxisPainter(yAxis);
	}

	@Override
	protected void updatePainterRectangles() {
		if (xAxisPainter != null)
			xAxisPainter.setRectangle(xyAxisChartRectangleLayout.getXAxisRectangle());
		if (yAxisPainter != null)
			yAxisPainter.setRectangle(xyAxisChartRectangleLayout.getYAxisRectangle());

		super.updatePainterRectangles();

		if (xAxisLabelsPainter != null)
			xAxisLabelsPainter.setRectangle(xyAxisChartRectangleLayout.getXAxisRectangle());
	}

	@Override
	protected void drawChart(Graphics2D g2) {
		super.drawChart(g2);

		if (xAxisLabelsPainter != null) {
			xAxisLabelsPainter.setFont(this.getFont());
			xAxisLabelsPainter.draw(g2);
		}
	}

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);

		if (xAxisLabelsPainter != null)
			xAxisLabelsPainter.setFontColor(fg);
		
		repaint();
	}
	
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		
		if (xAxisLabelsPainter != null)
			xAxisLabelsPainter.setFont(font);
		
		repaint();
	}

	@Override
	protected BarChartPainter createDistributionPainter(List<? extends Number> counts, Color color) {
		List<Color> colors = DataConversion.constantValueList(color, counts.size());

		BarChartVerticalPainter barChart = new BarChartVerticalPainter(counts, colors);
		barChart.setBackgroundPaint(null);
		barChart.setToolTipping(isShowingTooltips());

		return barChart;
	}

	public XAxisCategoricalPainter<List<String>> getxAxisLabelsPainter() {
		return xAxisLabelsPainter;
	}

	public void setxAxisLabelsPainter(XAxisCategoricalPainter<List<String>> xAxisLabelsPainter) {
		this.xAxisLabelsPainter = xAxisLabelsPainter;

		// A painter built via initializeXAxisPainter() only gets its rectangle/font
		// wired up on the next layout pass (updatePainterRectangles) and repaint
		// (drawChart); applying the same wiring immediately here avoids a painter
		// swapped in through this setter rendering mis-sized/mis-fonted in the
		// meantime.
		if (xAxisLabelsPainter != null) {
			xAxisLabelsPainter.setFont(getFont());
			if (xyAxisChartRectangleLayout != null)
				xAxisLabelsPainter.setRectangle(xyAxisChartRectangleLayout.getXAxisRectangle());
		}

		repaint();
	}
}