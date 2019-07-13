package com.github.TKnudsen.infoVis.view.chartLayouts;

import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.tools.Rectangle2DTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Creates a grid based on the master chartRectangle
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class GridChartRectangleLayout extends ChartRectangleLayout {

	private int xCount;
	private int yCount;
	private double spacing;

	private Rectangle2D[][] rectangleMatrix;

	public GridChartRectangleLayout(int xCount, int yCount, double spacing) {
		this.xCount = xCount;
		this.yCount = yCount;
		this.spacing = spacing;

		// no outer border
		setBorder(0);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		refreshRectangleMatrix();
	}

	private final void refreshRectangleMatrix() {
		rectangleMatrix = Rectangle2DTools.createRectangleMatrix(chartRectangle, xCount, yCount, spacing);
	}

	public int getxCount() {
		return xCount;
	}

	public void setxCount(int xCount) {
		this.xCount = xCount;

		refreshRectangleMatrix();
	}

	public int getyCount() {
		return yCount;
	}

	public void setyCount(int yCount) {
		this.yCount = yCount;

		refreshRectangleMatrix();
	}

	public double getSpacing() {
		return spacing;
	}

	public void setSpacing(double spacing) {
		this.spacing = spacing;

		refreshRectangleMatrix();
	}

	public Rectangle2D[][] getRectangleMatrix() {
		return rectangleMatrix;
	}

}
