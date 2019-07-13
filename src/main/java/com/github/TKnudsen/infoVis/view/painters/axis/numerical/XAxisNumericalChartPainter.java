package com.github.TKnudsen.infoVis.view.painters.axis.numerical;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.chartLayouts.XAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.IXAxis;
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
public abstract class XAxisNumericalChartPainter<X extends Number> extends AxisPainter implements IXAxis<X> {

	protected XAxisChartRectangleLayout xAxisChartRectangleLayout = new XAxisChartRectangleLayout();

	protected XAxisNumericalPainter<X> xAxisPainter;

	private boolean drawTickLinesInChart = false;

	public abstract void initializeXAxisPainter(X min, X max);

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		double border = 0;
		if (rectangle.getWidth() > 0 && rectangle.getHeight() > 0)
			border = Math.max(1, Math.min(rectangle.getWidth(), rectangle.getHeight()) * 0.005);
		xAxisChartRectangleLayout.setBorder(border);

		xAxisChartRectangleLayout.setRectangle(rectangle);
		this.chartRectangle = xAxisChartRectangleLayout.getChartRectangle();
		xAxisPainter.setRectangle(xAxisChartRectangleLayout.getXAxisRectangle());
	}

	@Override
	public void draw(Graphics2D g2) {
		Color c = g2.getColor();

		super.draw(g2);

		// TODO validate that first drawing the chart and THEN the axes does not cause
		// confusion somewhere..

		if (drawTickLinesInChart)
			drawTickLinesInChart();

		drawChart(g2);

		if (xAxisChartRectangleLayout.isDrawXAxis())
			drawXAxis(g2);

		if (drawOutline)
			DisplayTools.drawRectangle(g2, rectangle, getBorderPaint());

		g2.setColor(c);
	}

	protected void drawTickLinesInChart() {
		// TODO
	}

	public void drawXAxis(Graphics2D g2) {
		xAxisPainter.draw(g2);
	}

	public abstract void drawChart(Graphics2D g2);

	// TODO remove respective functionality in all inheriting classes!
	protected final Double getXPixelValue(X worldCoordinate) {
		return this.xAxisPainter.getPositionEncodingFunction().apply(worldCoordinate);
	}

	@Override
	public void setXAxisPhysicalUnit(String physicalUnit) {
		this.xAxisPainter.setPhysicalUnit(physicalUnit);
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
		this.xAxisChartRectangleLayout.setDrawXAxis(drawXAxis);
		setRectangle(rectangle);
	}

	@Override
	public void setXAxisLegendHeight(double xAxisLegendHeight) {
		this.xAxisChartRectangleLayout.setXAxisLegendHeight(xAxisLegendHeight);
		setRectangle(rectangle);
	}

	@Override
	public void setXAxisMarkerDistanceInPixels(int markerDistanceInPixels) {
		this.xAxisPainter.setMarkerDistanceInPixels(markerDistanceInPixels);
		setRectangle(rectangle);
	}

	public XAxisChartRectangleLayout getXyAxisChartRectangleLayout() {
		return xAxisChartRectangleLayout;
	}

	public void setXyAxisChartRectangleLayout(XAxisChartRectangleLayout xyAxisChartRectangleLayout) {
		this.xAxisChartRectangleLayout = xyAxisChartRectangleLayout;
	}

	public XAxisNumericalPainter<X> getxAxisPainter() {
		return xAxisPainter;
	}

	public void setXAxisPainter(XAxisNumericalPainter<X> xAxisPainter) {
		if (this.xAxisPainter != null)
			if (this.xAxisPainter.isLogarithmicScale() != xAxisPainter.isLogarithmicScale())
				System.err.println(
						"XAxisNumericalPainter: set of axis painter implicitly switched between log and linear scale(!)");

		this.xAxisPainter = xAxisPainter;
	}

	public boolean isOverlayOfXAxis() {
		return this.xAxisChartRectangleLayout.isOverlayOfXAxis();
	}

	public void setOverlayOfXAxis(boolean overlayOfXAxis) {
		this.xAxisChartRectangleLayout.setOverlayOfXAxis(overlayOfXAxis);
	}

	public boolean isDrawTickLinesInChart() {
		return drawTickLinesInChart;
	}

	public void setDrawTickLinesInChart(boolean drawTickLinesInChart) {
		this.drawTickLinesInChart = drawTickLinesInChart;
	}
}
