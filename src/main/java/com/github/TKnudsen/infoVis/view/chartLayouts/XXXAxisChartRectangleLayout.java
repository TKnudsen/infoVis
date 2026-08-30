package com.github.TKnudsen.infoVis.view.chartLayouts;

import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.TKnudsen.infoVis.view.panels.axis.sizeCharacteristics.AxisSizeCharacteristics;
import com.github.TKnudsen.infoVis.view.tools.Rectangle2DTools;

/**
 * <p>
 * Layouts a pre-given number of X-axes across the available Y-space,
 * distributing them vertically with configurable spacing.
 * 
 * Example usage: Stacked time series plots where each series is represented by
 * a horizontal axis arranged vertically.
 * </p>
 *
 * @version 1.0
 * @since 2016
 */
public class XXXAxisChartRectangleLayout extends MultiAxisChartRectangleLayout {

	public XXXAxisChartRectangleLayout() {
		super();
	}

	public XXXAxisChartRectangleLayout(int axes) {
		super(axes);
	}

	public XXXAxisChartRectangleLayout(List<AxisSizeCharacteristics> axisSizeCharacteristics) {
		super(axisSizeCharacteristics);
	}

	@Override
	protected double getAxisCenterPosition(Rectangle2D axisRectangle) {
		return axisRectangle.getCenterY();
	}

	@Override
	protected double getSpacingDimension(Rectangle2D rectangle) {
		return rectangle.getHeight();
	}

	@Override
	protected Rectangle2D[][] createAxisRectangleMatrix(Rectangle2D bounds, int axisCount, int spacingInPixels) {
		return Rectangle2DTools.createRectangleMatrix(bounds, 1, axisCount, spacingInPixels);
	}

	@Override
	protected String getAxisTypeName() {
		return "X-axis";
	}

	@Override
	protected void assignRectanglesToAxes(Rectangle2D[][] rectangleMatrix) {
		// For vertical layout, axes are in columns (rectangleMatrix[0][i])
		for (int i = 0; i < rectangleMatrix[0].length && i < axisSizeCharacteristics.size(); i++)
			axisSizeCharacteristics.get(i).setAxisRectangle(rectangleMatrix[0][i]);
	}

	/**
	 * Gets the rectangle bounds for the X-axis at the specified index.
	 * 
	 * @param axisIndex the zero-based index of the axis
	 * @return the rectangle defining the axis bounds
	 * @throws IndexOutOfBoundsException if index is invalid
	 */
	public Rectangle2D getXAxisRectangle(int axisIndex) {
		return getAxisRectangle(axisIndex);
	}

}