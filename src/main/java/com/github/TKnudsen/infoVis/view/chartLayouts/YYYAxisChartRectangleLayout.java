package com.github.TKnudsen.infoVis.view.chartLayouts;

import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.TKnudsen.infoVis.view.panels.axis.sizeCharacteristics.AxisSizeCharacteristics;
import com.github.TKnudsen.infoVis.view.tools.Rectangle2DTools;

/**
 * <p>
 * Layouts a pre-given number of Y-axes across the available X-space,
 * distributing them horizontally with configurable spacing.
 * 
 * Example usage: Parallel Coordinates plots where each dimension is represented
 * by a vertical axis arranged horizontally.
 * </p>
 *
 * @version 1.1
 * @since 2016
 */
public class YYYAxisChartRectangleLayout extends MultiAxisChartRectangleLayout {

	public YYYAxisChartRectangleLayout() {
		super();
	}

	public YYYAxisChartRectangleLayout(int axes) {
		super(axes);
	}

	public YYYAxisChartRectangleLayout(List<AxisSizeCharacteristics> axisSizeCharacteristics) {
		super(axisSizeCharacteristics);
	}

	@Override
	protected double getAxisCenterPosition(Rectangle2D axisRectangle) {
		return axisRectangle.getCenterX();
	}

	@Override
	protected double getSpacingDimension(Rectangle2D rectangle) {
		return rectangle.getWidth();
	}

	@Override
	protected Rectangle2D[][] createAxisRectangleMatrix(Rectangle2D bounds, int axisCount, int spacingInPixels) {
		return Rectangle2DTools.createRectangleMatrix(bounds, axisCount, 1, spacingInPixels);
	}

	@Override
	protected String getAxisTypeName() {
		return "Y-axis";
	}

	/**
	 * Gets the rectangle bounds for the Y-axis at the specified index.
	 * 
	 * @param axisIndex the zero-based index of the axis
	 * @return the rectangle defining the axis bounds
	 * @throws IndexOutOfBoundsException if index is invalid
	 */
	public Rectangle2D getYAxisRectangle(int axisIndex) {
		return getAxisRectangle(axisIndex);
	}

}
