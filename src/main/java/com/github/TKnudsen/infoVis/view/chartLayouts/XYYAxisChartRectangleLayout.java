package com.github.TKnudsen.infoVis.view.chartLayouts;

import java.awt.geom.Rectangle2D;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * XYAxisChartRectangleLayout that includes a second Y-Axis (typically at the
 * right).
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class XYYAxisChartRectangleLayout extends XYAxisChartRectangleLayout {

	protected Rectangle2D yAxisRectangleRight;
	protected double yAxisLegendWidthRight = 40;
	protected boolean drawYAxisRight = true;
	private double minWidthToDrawYAxisRight = 100.0;

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		chartRectangle = null;
		xAxisRectangle = null;
		yAxisRectangle = null;
		yAxisRectangleRight = null;

		if (rectangle == null)
			return;

		double minX = rectangle.getMinX();
		double maxX = rectangle.getMaxX();
		double minY = rectangle.getMinY();
		double width = rectangle.getWidth();
		double height = rectangle.getHeight();

		double xAxisLegendHeight = this.xAxisLegendHeight;
		// TODO decide when a painter is allowed to perform LOD
		if (!drawXAxis)// || height < minHeightToDrawXAxis)
			xAxisLegendHeight = 0;
		if (height < xAxisLegendHeight * 4.0)
			xAxisLegendHeight = height / 4.0;

		double yAxisLegendWidth = this.yAxisLegendWidth;
		// TODO decide when a painter is allowed to perform LOD
		if (!drawYAxis)// || width < minWidthToDrawYAxis)
			yAxisLegendWidth = 0;
		if (width < yAxisLegendWidth * 4.0)
			yAxisLegendWidth = width / 4.0;

		// decide whether a second axis is created at the right
		double yAxisLegendWidthRight = 0;
		if (drawYAxisRight)
			yAxisLegendWidthRight = yAxisLegendWidth;

		// define chart sizes
		double chartMinX = minX + yAxisLegendWidth;
		if (isYAxisOverlay())
			chartMinX = minX;

		double chartWidth = width - yAxisLegendWidth - yAxisLegendWidthRight - 2 * margin;
		if (isYAxisOverlay())
			chartWidth = width - yAxisLegendWidth - yAxisLegendWidthRight - 2 * margin;

		double chartHeight = height - xAxisLegendHeight - 2 * margin;
		if (isXAxisOverlay())
			chartHeight = height - 2 * margin; // use more height

		xAxisRectangle = new Rectangle2D.Double(chartMinX + margin, rectangle.getMaxY() - margin - xAxisLegendHeight,
				chartWidth, xAxisLegendHeight);
		yAxisRectangle = new Rectangle2D.Double(minX + margin, minY + margin, yAxisLegendWidth, chartHeight);
		yAxisRectangleRight = new Rectangle2D.Double(maxX - margin - yAxisLegendWidthRight, minY + margin,
				yAxisLegendWidthRight, chartHeight);
		chartRectangle = new Rectangle2D.Double(chartMinX + margin, minY + margin, chartWidth, chartHeight);
	}

	public Rectangle2D getyAxisRectangleRight() {
		return yAxisRectangleRight;
	}

	public void setyAxisRectangleRight(Rectangle2D yAxisRectangleRight) {
		this.yAxisRectangleRight = yAxisRectangleRight;
	}

	public double getyAxisLegendWidthRight() {
		return yAxisLegendWidthRight;
	}

	public void setyAxisLegendWidthRight(double yAxisLegendWidthRight) {
		this.yAxisLegendWidthRight = yAxisLegendWidthRight;
	}

	public boolean isDrawYAxisRight() {
		return drawYAxisRight;
	}

	public void setDrawYAxisRight(boolean drawYAxisRight) {
		this.drawYAxisRight = drawYAxisRight;
	}

	public double getMinWidthToDrawYAxisRight() {
		return minWidthToDrawYAxisRight;
	}

	public void setMinWidthToDrawYAxisRight(double minWidthToDrawYAxisRight) {
		this.minWidthToDrawYAxisRight = minWidthToDrawYAxisRight;
	}

}
