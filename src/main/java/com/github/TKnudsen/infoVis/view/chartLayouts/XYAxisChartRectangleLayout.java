package com.github.TKnudsen.infoVis.view.chartLayouts;

import java.awt.geom.Rectangle2D;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Rectangle layout with an x axis (default: bottom), an y axis (default: left)
 * and a chartRectangle.
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class XYAxisChartRectangleLayout extends ChartRectangleLayout {

	protected Rectangle2D xAxisRectangle;
	protected double xAxisLegendHeight = 30;
	protected boolean drawXAxis = true;
	private double minHeightToDrawXAxis = 100.0;

	private boolean xAxisOverlay = false;

	protected Rectangle2D yAxisRectangle;
	protected double yAxisLegendWidth = 40;
	protected boolean drawYAxis = true;
	private double minWidthToDrawYAxis = 100.0;

	private boolean yAxisOverlay = false;

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		chartRectangle = null;
		xAxisRectangle = null;
		yAxisRectangle = null;

		if (rectangle == null)
			return;

		double minX = rectangle.getMinX();
		double minY = rectangle.getMinY();
		double width = rectangle.getWidth();
		double height = rectangle.getHeight();

		double border = this.margin;
		if (width < 2 * border || height < 2 * border)
			border = 0;

		// adjust xAxisLegendHeight
		double xAxisLegendHeight = this.xAxisLegendHeight;
		if (!drawXAxis)
			xAxisLegendHeight = 0;
		if (height < xAxisLegendHeight * 4.0)
			xAxisLegendHeight = (!xAxisOverlay) ? height / 4.0 : height / 2.0;

		// adjust yAxisLegendWidth
		double yAxisLegendWidth = this.yAxisLegendWidth;
		if (!drawYAxis)// || width < minWidthToDrawYAxis)
			yAxisLegendWidth = 0;
		if (width < yAxisLegendWidth * 4.0)
			yAxisLegendWidth = width / 4.0;

		// define chart sizes
		double chartMinX = minX + yAxisLegendWidth;
		if (yAxisOverlay)
			chartMinX = minX;
		if (!drawYAxis)
			chartMinX += border;

		double chartMinY = minY + border;

		double chartWidth = width - yAxisLegendWidth - border;
		if (yAxisOverlay)
			chartWidth = width - border;

		if (!drawYAxis)
			// chartWidth -= border; // +=?!
			chartWidth = width - border;

		double chartHeight = height - xAxisLegendHeight - 2 * border;
		if (xAxisOverlay)
			chartHeight = height - 2 * border; // use more height

		// X-AXIS
		xAxisRectangle = new Rectangle2D.Double(chartMinX, rectangle.getMaxY() - border - xAxisLegendHeight, chartWidth,
				xAxisLegendHeight);

		// Y-AXIS
		yAxisRectangle = new Rectangle2D.Double(minX + border, minY + border, yAxisLegendWidth - border, chartHeight);

		// CHART RECTANGLE

		chartRectangle = new Rectangle2D.Double(chartMinX, chartMinY, chartWidth, chartHeight);
	}

	public Rectangle2D getXAxisRectangle() {
		return xAxisRectangle;
	}

	public Rectangle2D getYAxisRectangle() {
		return yAxisRectangle;
	}

	public double getYAxisLegendWidth() {
		return yAxisLegendWidth;
	}

	public void setYAxisLegendWidth(double yAxisLegendWidth) {
		this.yAxisLegendWidth = yAxisLegendWidth;
	}

	public double getXAxisLegendHeight() {
		return xAxisLegendHeight;
	}

	public void setXAxisLegendHeight(double xAxisLegendHeight) {
		this.xAxisLegendHeight = xAxisLegendHeight;
	}

	public boolean isDrawXAxis() {
		return drawXAxis;
	}

	public void setDrawXAxis(boolean drawXAxis) {
		this.drawXAxis = drawXAxis;
	}

	public boolean isDrawYAxis() {
		return drawYAxis;
	}

	public void setDrawYAxis(boolean drawYAxis) {
		this.drawYAxis = drawYAxis;
	}

	public double getMinHeightToDrawXAxis() {
		return minHeightToDrawXAxis;
	}

	public void setMinHeightToDrawXAxis(double minHeightToDrawXAxis) {
		this.minHeightToDrawXAxis = minHeightToDrawXAxis;
	}

	public double getMinWidthToDrawYAxis() {
		return minWidthToDrawYAxis;
	}

	public void setMinWidthToDrawYAxis(double minWidthToDrawYAxis) {
		this.minWidthToDrawYAxis = minWidthToDrawYAxis;
	}

	public boolean isXAxisOverlay() {
		return xAxisOverlay;
	}

	public void setXAxisOverlay(boolean xAxisOverlay) {
		this.xAxisOverlay = xAxisOverlay;
	}

	public boolean isYAxisOverlay() {
		return yAxisOverlay;
	}

	public void setYAxisOverlay(boolean yAxisOverlay) {
		this.yAxisOverlay = yAxisOverlay;
	}
}
