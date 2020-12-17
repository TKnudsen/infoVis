package com.github.TKnudsen.infoVis.view.painters.axis.numerical;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.chartLayouts.XYAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.IXAxis;
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
 * @version 2.01
 */
public abstract class XYAxisNumericalChartPainter<X extends Number, Y extends Number> extends AxisPainter
		implements IXAxis<X>, IYAxis<Y> {

	protected XYAxisChartRectangleLayout xyAxisChartRectangleLayout = new XYAxisChartRectangleLayout();
	protected XAxisNumericalPainter<X> xAxisPainter;
	protected YAxisNumericalPainter<Y> yAxisPainter;

	protected abstract void initializeXAxisPainter(X min, X max);

	protected abstract void initializeYAxisPainter(Y min, Y max);

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		double min = Math.min(rectangle.getWidth(), rectangle.getHeight());
		double border = 0;
		if (rectangle.getWidth() > 0 && rectangle.getHeight() > 0)
			border = Math.max(1, min / 200.0);

		xyAxisChartRectangleLayout.setMargin(border);

		xyAxisChartRectangleLayout.setRectangle(rectangle);
		this.chartRectangle = xyAxisChartRectangleLayout.getChartRectangle();
		xAxisPainter.setRectangle(xyAxisChartRectangleLayout.getXAxisRectangle());
		yAxisPainter.setRectangle(xyAxisChartRectangleLayout.getYAxisRectangle());
	}

	@Override
	public void draw(Graphics2D g2) {
		Color c = g2.getColor();
		Stroke s = g2.getStroke();

		super.draw(g2);

		// TODO validate that first drawing the chart and THEN the axes does not cause
		// confusion somewhere..
		drawChart(g2);

		if (xyAxisChartRectangleLayout.isDrawXAxis())
			drawXAxis(g2);

		if (xyAxisChartRectangleLayout.isDrawYAxis())
			drawYAxis(g2);

		g2.setPaint(getPaint());

		if (drawOutline)
			DisplayTools.drawRectangle(g2, rectangle, getBorderPaint());

		g2.setStroke(s);
		g2.setColor(c);
	}

	public void drawXAxis(Graphics2D g2) {
		xAxisPainter.draw(g2);
	}

	public void drawYAxis(Graphics2D g2) {
		yAxisPainter.draw(g2);
	}

	public abstract void drawChart(Graphics2D g2);

	// TODO remove respective functionality in all inheriting classes!
	protected final Double getXPixelValue(X worldCoordinate) {
		return this.xAxisPainter.getPositionEncodingFunction().apply(worldCoordinate);
	}

	// TODO remove respective functionality in all inheriting classes!
	protected final Double getYPixelValue(Y worldCoordinate) {
		return this.yAxisPainter.getPositionEncodingFunction().apply(worldCoordinate);
	}

	@Override
	public void setYAxisPhysicalUnit(String physicalUnit) {
		this.yAxisPainter.setPhysicalUnit(physicalUnit);
	}

	@Override
	public void setXAxisPhysicalUnit(String physicalUnit) {
		this.xAxisPainter.setPhysicalUnit(physicalUnit);
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

	@Override
	public void setDrawXAxis(boolean drawXAxis) {
		this.xyAxisChartRectangleLayout.setDrawXAxis(drawXAxis);
		setRectangle(rectangle);
	}

	@Override
	public void setYAxisMarkerDistanceInPixels(int markerDistanceInPixels) {
		this.yAxisPainter.setMarkerDistanceInPixels(markerDistanceInPixels);
		setRectangle(rectangle);
	}

	public double getXAxisLegendHeight() {
		return xyAxisChartRectangleLayout.getXAxisLegendHeight();
	}

	@Override
	public void setXAxisLegendHeight(double xAxisLegendHeight) {
		this.xyAxisChartRectangleLayout.setXAxisLegendHeight(xAxisLegendHeight);
		setRectangle(rectangle);
	}

	@Override
	public void setXAxisMarkerDistanceInPixels(int markerDistanceInPixels) {
		this.xAxisPainter.setMarkerDistanceInPixels(markerDistanceInPixels);
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

	public void setXAxisPainter(XAxisNumericalPainter<X> xAxisPainter) {
		if (this.xAxisPainter != null)
			if (this.xAxisPainter.isLogarithmicScale() != xAxisPainter.isLogarithmicScale())
				System.err.println(
						"XYAxisNumericalPainter: set of axis painter implicitly switched between log and linear scale(!)");

		this.xAxisPainter = xAxisPainter;

		setBackgroundPaint(getBackgroundPaint());
	}

	public YAxisNumericalPainter<Y> getyAxisPainter() {
		return yAxisPainter;
	}

	public void setYAxisPainter(YAxisNumericalPainter<Y> yAxisPainter) {
		if (this.yAxisPainter != null)
			if (this.yAxisPainter.isLogarithmicScale() != yAxisPainter.isLogarithmicScale())
				System.err.println(
						"XYAxisNumericalPainter: set of axis painter implicitly switched between log and linear scale(!)");

		this.yAxisPainter = yAxisPainter;

		setBackgroundPaint(getBackgroundPaint());
	}

	public boolean isXAxisOverlay() {
		return xyAxisChartRectangleLayout.isXAxisOverlay();
	}

	public void setXAxisOverlay(boolean overlayOfXAxis) {
		this.xyAxisChartRectangleLayout.setXAxisOverlay(overlayOfXAxis);
	}

	public boolean isYAxisOverlay() {
		return xyAxisChartRectangleLayout.isYAxisOverlay();
	}

	public void setYAxisOverlay(boolean overlayOfYAxis) {
		this.xyAxisChartRectangleLayout.setYAxisOverlay(overlayOfYAxis);
	}
}
