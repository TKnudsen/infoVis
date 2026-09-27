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
	 * Insets each axis rectangle's top by its own axisLegendOffset, so the axis
	 * name/physical-unit label (drawn above the axis's value range, see
	 * {@link com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter#drawPhysU})
	 * has dedicated header space instead of overlapping the chart's topmost
	 * data values. Previously axisLegendOffset was stored but never applied by
	 * this layout, so the "space reserved for axis labels" it documents never
	 * actually existed.
	 */
	@Override
	protected void assignRectanglesToAxes(Rectangle2D[][] rectangleMatrix) {
		for (int i = 0; i < rectangleMatrix.length && i < axisSizeCharacteristics.size(); i++) {
			Rectangle2D r = rectangleMatrix[i][0];
			double legendOffset = axisSizeCharacteristics.get(i).getAxisLegendOffset();

			axisSizeCharacteristics.get(i).setAxisRectangle(new Rectangle2D.Double(r.getX(), r.getY() + legendOffset,
					r.getWidth(), Math.max(0, r.getHeight() - legendOffset)));
		}
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
