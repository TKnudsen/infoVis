package com.github.TKnudsen.infoVis.view.chartLayouts;

import java.awt.geom.Rectangle2D;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class ChartRectangleLayout {

	protected Rectangle2D rectangle;

	/**
	 * space at all four borders within the layout which shall be left unused.
	 * called border earlier
	 */
	protected double margin = 3.0;

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

		double margin = this.margin;
		if (width < 2 * margin || height < 2 * margin)
			margin = 0;

		double chartMinX = minX + margin;
		double chartMinY = minY + margin;
		double chartWidth = width - 2 * margin;
		double chartHeight = height - 2 * margin;

		chartRectangle = new Rectangle2D.Double(chartMinX, chartMinY, chartWidth, chartHeight);
	}

	public Rectangle2D getChartRectangle() {
		return chartRectangle;
	}

	/**
	 * @deprecated use getMargin
	 * @return
	 */
	public double getBorder() {
		return getMargin();
	}

	/**
	 * @deprecated use setMargin
	 * @param margin
	 */
	public void setBorder(double margin) {
		this.setMargin(margin);
	}

	public double getMargin() {
		return margin;
	}

	public void setMargin(double margin) {
		this.margin = margin;
	}
}
