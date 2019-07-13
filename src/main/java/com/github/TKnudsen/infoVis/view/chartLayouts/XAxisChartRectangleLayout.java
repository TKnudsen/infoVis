package com.github.TKnudsen.infoVis.view.chartLayouts;

import java.awt.geom.Rectangle2D;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Rectangle layout with an x axis (default: bottom) and a chartRectangle.
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class XAxisChartRectangleLayout extends ChartRectangleLayout {

	private Rectangle2D xAxisRectangle;
	private double xAxisLegendHeight = 30;
	private boolean drawXAxis = true;
	private double minHeightToDrawXAxis = 100.0;

	private boolean overlayOfXAxis = false;

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		chartRectangle = null;
		xAxisRectangle = null;

		if (rectangle == null)
			return;

		double minX = rectangle.getMinX();
		double minY = rectangle.getMinY();
		double width = rectangle.getWidth();
		double height = rectangle.getHeight();

		double border = this.border;
		if (width < 2 * border || height < 2 * border)
			border = 0;

		// define chart sizes
		double chartMinX = minX + border;
		double chartWidth = width - 2 * border;

		double xAxisLegendHeight = this.xAxisLegendHeight;
		if (!drawXAxis || height < minHeightToDrawXAxis)
			xAxisLegendHeight = 0;
		if (height < xAxisLegendHeight * 4.0)
			xAxisLegendHeight = height / 4.0;

		if (xAxisLegendHeight > 0) {
			xAxisRectangle = new Rectangle2D.Double(chartMinX, rectangle.getMaxY() - border - xAxisLegendHeight,
					chartWidth, xAxisLegendHeight);
		} else
			xAxisRectangle = new Rectangle2D.Double(chartMinX, rectangle.getMaxY() - border - xAxisLegendHeight,
					chartWidth, 0);

		if (!overlayOfXAxis)
			chartRectangle = new Rectangle2D.Double(chartMinX, minY + border, chartWidth,
					height - xAxisLegendHeight - 2 * border);
		else
			chartRectangle = new Rectangle2D.Double(chartMinX, minY + border, chartWidth, height - 2 * border);
	}

	public Rectangle2D getXAxisRectangle() {
		return xAxisRectangle;
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

	public double getMinHeightToDrawXAxis() {
		return minHeightToDrawXAxis;
	}

	public void setMinHeightToDrawXAxis(double minHeightToDrawXAxis) {
		this.minHeightToDrawXAxis = minHeightToDrawXAxis;
	}

	public boolean isOverlayOfXAxis() {
		return overlayOfXAxis;
	}

	public void setOverlayOfXAxis(boolean overlayOfXAxis) {
		this.overlayOfXAxis = overlayOfXAxis;
	}

}
