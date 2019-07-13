package com.github.TKnudsen.infoVis.view.chartLayouts;

import java.awt.geom.Rectangle2D;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class ChartRectangleLayout {

	protected Rectangle2D rectangle;

	protected double border = 3.0;

	protected Rectangle2D chartRectangle;

	public Rectangle2D getRectangle() {
		return rectangle;
	}

	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		chartRectangle = null;

		if (rectangle == null)
			return;

		double minX = rectangle.getMinX();
		double minY = rectangle.getMinY();
		double width = rectangle.getWidth();
		double height = rectangle.getHeight();

		double border = this.border;
		if (width < 2 * border || height < 2 * border)
			border = 0;

		double chartMinX = minX + border;
		double chartMinY = minY + border;
		double chartWidth = width - 2 * border;
		double chartHeight = height - 2 * border;

		chartRectangle = new Rectangle2D.Double(chartMinX, chartMinY, chartWidth, chartHeight);
	}

	public Rectangle2D getChartRectangle() {
		return chartRectangle;
	}

	public double getBorder() {
		return border;
	}

	public void setBorder(double border) {
		this.border = border;
	}
}
