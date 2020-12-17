package com.github.TKnudsen.infoVis.view.painters.axis.numerical;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.chartLayouts.YAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.IYAxis;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.05
 */

public abstract class YAxisNumericalChartPainter<Y extends Number> extends AxisPainter implements IYAxis<Y> {

	protected YAxisChartRectangleLayout yAxisChartRectangleLayout = new YAxisChartRectangleLayout();
	protected YAxisNumericalPainter<Y> yAxisPainter;

	public abstract void initializeYAxisPainter(Y min, Y max);

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		double border = 0;
		if (rectangle.getWidth() > 0 && rectangle.getHeight() > 0)
			border = Math.max(1, Math.min(rectangle.getWidth(), rectangle.getHeight()) * 0.005);
		yAxisChartRectangleLayout.setMargin(border);

		yAxisChartRectangleLayout.setRectangle(rectangle);
		this.chartRectangle = yAxisChartRectangleLayout.getChartRectangle();
		yAxisPainter.setRectangle(yAxisChartRectangleLayout.getYAxisRectangle());
	}

	@Override
	public void draw(Graphics2D g2) {
		Color c = g2.getColor();

		super.draw(g2);

		// TODO validate that first drawing the chart and THEN the axes does not cause
		// confusion somewhere..

		drawChart(g2);

		if (yAxisChartRectangleLayout.isDrawYAxis())
			drawYAxis(g2);

		g2.setColor(color);

		if (drawOutline)
			DisplayTools.drawRectangle(g2, rectangle, getBorderPaint());

		g2.setColor(c);
	}

	public void drawYAxis(Graphics2D g2) {
		yAxisPainter.draw(g2);
	}

	public abstract void drawChart(Graphics2D g2);

	// TODO remove respective functionality in all inheriting classes!
	protected final Double getYPixelValue(Y worldCoordinate) {
		return this.yAxisPainter.getPositionEncodingFunction().apply(worldCoordinate);
	}

	@Override
	public void setYAxisPhysicalUnit(String physicalUnit) {
		this.yAxisPainter.setPhysicalUnit(physicalUnit);
	}

	public Y getYAxisMinValue() {
		return yAxisPainter.getMinValue();
	}

	@Override
	public void setYAxisMinValue(Y yAxisMinValue) {
		yAxisPainter.setMinValue(yAxisMinValue);
		setRectangle(rectangle);
	}

	public Y getYAxisMaxValue() {
		return yAxisPainter.getMaxValue();
	}

	@Override
	public void setYAxisMaxValue(Y yAxisMaxValue) {
		yAxisPainter.setMaxValue(yAxisMaxValue);
		setRectangle(rectangle);
	}

	@Override
	public void setDrawYAxis(boolean drawYAxis) {
		this.yAxisChartRectangleLayout.setDrawYAxis(drawYAxis);
		setRectangle(rectangle);
	}

	public double getYAxisLegendWidth() {
		return yAxisChartRectangleLayout.getYAxisLegendWidth();
	}

	@Override
	public void setYAxisLegendWidth(double yAxisLegendWidth) {
		this.yAxisChartRectangleLayout.setYAxisLegendWidth(yAxisLegendWidth);
		setRectangle(rectangle);
	}

	@Override
	public void setYAxisMarkerDistanceInPixels(int markerDistanceInPixels) {
		this.yAxisPainter.setMarkerDistanceInPixels(markerDistanceInPixels);
		setRectangle(rectangle);
	}

	public YAxisNumericalPainter<Y> getyAxisPainter() {
		return yAxisPainter;
	}

	public void setYAxisPainter(YAxisNumericalPainter<Y> yAxisPainter) {
		if (this.yAxisPainter != null)
			if (this.yAxisPainter.isLogarithmicScale() != yAxisPainter.isLogarithmicScale())
				System.err.println(
						"YAxisNumericalPainter: set of axis painter implicitly switched between log and linear scale(!)");

		this.yAxisPainter = yAxisPainter;
	}
}
