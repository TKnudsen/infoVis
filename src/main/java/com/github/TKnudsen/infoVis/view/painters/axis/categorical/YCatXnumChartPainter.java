package com.github.TKnudsen.infoVis.view.painters.axis.categorical;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.TKnudsen.infoVis.view.chartLayouts.XYAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.IXAxis;
import com.github.TKnudsen.infoVis.view.painters.axis.IYAxisCategorical;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
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
 * @version 1.05
 */
public abstract class YCatXnumChartPainter<X extends Number, Y extends List<String>> extends AxisPainter
		implements IXAxis<X>, IYAxisCategorical<Y> {

	protected XYAxisChartRectangleLayout xyAxisChartRectangleLayout = new XYAxisChartRectangleLayout();
	protected XAxisNumericalPainter<X> xAxisPainter;
	protected YAxisCategoricalPainter<Y> yAxisPainter;

	protected abstract void initializeXAxisPainter(X min, X max);

	protected abstract void initializeYAxisPainter(Y labels);

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		double border = Math.max(1, Math.min(rectangle.getWidth(), rectangle.getHeight()) * 0.005);
		xyAxisChartRectangleLayout.setMargin(border);

		xyAxisChartRectangleLayout.setRectangle(rectangle);
		this.chartRectangle = xyAxisChartRectangleLayout.getChartRectangle();
		xAxisPainter.setRectangle(xyAxisChartRectangleLayout.getXAxisRectangle());
		yAxisPainter.setRectangle(xyAxisChartRectangleLayout.getYAxisRectangle());
	}

	@Override
	public void draw(Graphics2D g2) {
		Color c = g2.getColor();

		super.draw(g2);

		if (xyAxisChartRectangleLayout.isDrawXAxis())
			drawXAxis(g2);

		if (xyAxisChartRectangleLayout.isDrawYAxis())
			drawYAxis(g2);

		drawChart(g2);

		g2.setColor(color);

		if (drawOutline)
			DisplayTools.drawRectangle(g2, rectangle, getBorderPaint());

		g2.setColor(c);
	}

	public void drawXAxis(Graphics2D g2) {
		xAxisPainter.draw(g2);
	}

	public void drawYAxis(Graphics2D g2) {
		yAxisPainter.draw(g2);
	}

	public abstract void drawChart(Graphics2D g2);

	public X getXAxisMinValue() {
		return xAxisPainter.getMinValue();
	}

	@Override
	public void setXAxisMinValue(X xAxisMinValue) {
		xAxisPainter.setMinValue(xAxisMinValue);
		setRectangle(rectangle);
	}

	public X getXAxisMaxValue() {
		return xAxisPainter.getMaxValue();
	}

	@Override
	public void setXAxisMaxValue(X xAxisMaxValue) {
		xAxisPainter.setMaxValue(xAxisMaxValue);
		setRectangle(rectangle);
	}

	public boolean isDrawYAxis() {
		return this.xyAxisChartRectangleLayout.isDrawYAxis();
	}

	@Override
	public void setDrawYAxis(boolean drawYAxis) {
		this.xyAxisChartRectangleLayout.setDrawYAxis(drawYAxis);
		setRectangle(rectangle);
	}

	public double getYAxisLegendWidth() {
		return xyAxisChartRectangleLayout.getYAxisLegendWidth();
	}

	@Override
	public void setYAxisLegendWidth(double yAxisLegendWidth) {
		this.xyAxisChartRectangleLayout.setYAxisLegendWidth(yAxisLegendWidth);
		setRectangle(rectangle);
	}

	public boolean isDrawXAxis() {
		return this.xyAxisChartRectangleLayout.isDrawXAxis();
	}

	@Override
	public void setDrawXAxis(boolean drawXAxis) {
		this.xyAxisChartRectangleLayout.setDrawXAxis(drawXAxis);
		setRectangle(rectangle);
	}

	@Override
	public void setYAxisLabels(Y labels) {
		this.yAxisPainter.setMarkerLabels(labels);
	}

	@Override
	public void setXAxisMarkerDistanceInPixels(int markerDistanceInPixels) {
		this.xAxisPainter.setMarkerDistanceInPixels(markerDistanceInPixels);
		setRectangle(rectangle);
	}

	@Override
	public void setXAxisLegendHeight(double xAxisLegendHeight) {
		this.xyAxisChartRectangleLayout.setXAxisLegendHeight(xAxisLegendHeight);
		setRectangle(rectangle);
	}

	public XYAxisChartRectangleLayout getXyAxisChartRectangleLayout() {
		return xyAxisChartRectangleLayout;
	}

	public void setXyAxisChartRectangleLayout(XYAxisChartRectangleLayout xyAxisChartRectangleLayout) {
		this.xyAxisChartRectangleLayout = xyAxisChartRectangleLayout;
	}

	public XAxisNumericalPainter<X> getxAxisPainter() {
		return xAxisPainter;
	}

	public void setxAxisPainter(XAxisNumericalPainter<X> xAxisPainter) {
		this.xAxisPainter = xAxisPainter;
	}

	public YAxisCategoricalPainter<Y> getyAxisPainter() {
		return yAxisPainter;
	}

	public void setyAxisPainter(YAxisCategoricalPainter<Y> yAxisPainter) {
		this.yAxisPainter = yAxisPainter;
	}

	@Override
	public void setXAxisPhysicalUnit(String physicalUnit) {
		this.xAxisPainter.setPhysicalUnit(physicalUnit);
	}
}
