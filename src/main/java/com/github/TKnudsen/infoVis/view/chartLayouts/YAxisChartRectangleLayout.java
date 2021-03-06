package com.github.TKnudsen.infoVis.view.chartLayouts;

import java.awt.geom.Rectangle2D;

/**
 * Rectangle layout with a y axis (default: left) and a chartRectangle.
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public class YAxisChartRectangleLayout extends ChartRectangleLayout {

	private Rectangle2D yAxisRectangle;
	private double yAxisLegendWidth = 40;
	private boolean drawYAxis = true;

	private boolean yAxisOverlay = false;

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		if (rectangle == null) {
			chartRectangle = null;
			yAxisRectangle = null;
			return;
		}

		double minX = rectangle.getX();
		double minY = rectangle.getY();
		double width = rectangle.getWidth();
		double height = rectangle.getHeight();

		double yAxisLegendWidth = this.yAxisLegendWidth;

		double chartWidth = width - yAxisLegendWidth - margin;
		if (yAxisOverlay)
			chartWidth = width - margin;

		if (drawYAxis)
			yAxisRectangle = new Rectangle2D.Double(minX + margin, minY + margin, yAxisLegendWidth - margin,
					height - 2 * margin);
		else {
			yAxisRectangle = new Rectangle2D.Double(minX + margin, minY + margin, 0, height - 2 * margin);
			chartWidth = width - margin;
		}

		chartRectangle = new Rectangle2D.Double(yAxisRectangle.getMaxX(), this.rectangle.getY() + margin, chartWidth,
				height - 2 * margin);
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

	public boolean isDrawYAxis() {
		return drawYAxis;
	}

	public void setDrawYAxis(boolean drawYAxis) {
		this.drawYAxis = drawYAxis;
	}

	public boolean isYAxisOverlay() {
		return yAxisOverlay;
	}

	public void setYAxisOverlay(boolean yAxisOverlay) {
		this.yAxisOverlay = yAxisOverlay;
	}
}
