package com.github.TKnudsen.infoVis.view.chartLayouts;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.tools.Rectangle2DTools;

/**
 * @version 1.01
 * @since 2025
 */
public class XAxisChartsJuxtaposedRectangleLayout extends XAxisChartRectangleLayout {

	private int chartsCount = 0;

	private int spacingBetweenCharts = 2;

	private Double relativeSpaceBetweenCharts = 0.01;

	private final List<Rectangle2D> chartRectangles = new ArrayList<Rectangle2D>();

	public void increaseChartCount() {
		chartsCount++;

		setRectangle(rectangle);
	}

	/**
	 * Creates the rectangle matrix for axis layout. Subclasses implement this to
	 * create Nx1 matrices for horizontal layouts or 1xN matrices for vertical
	 * layouts.
	 * 
	 * @param bounds          the bounding rectangle
	 * @param chartsCount     the number of charts
	 * @param spacingInPixels the spacing between charts
	 * @return the rectangle matrix, or null if creation failed
	 */
	protected Rectangle2D[][] createAxisRectangleMatrix(int spacingInPixels) {
		return Rectangle2DTools.createRectangleMatrix(chartRectangle, 1, chartsCount, spacingInPixels);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		int spacingBetweenCharts = this.spacingBetweenCharts;
		if (relativeSpaceBetweenCharts != null && !Double.isNaN(relativeSpaceBetweenCharts))
			spacingBetweenCharts = (int) (getSpacingDimension(rectangle) * relativeSpaceBetweenCharts);

		Rectangle2D[][] rectangleMatrix = createAxisRectangleMatrix(spacingBetweenCharts);

		chartRectangles.clear();
		if (rectangleMatrix == null)
			for (int i = 0; i < chartsCount; i++)
				chartRectangles.add(chartRectangle);
		else
			for (int i = 0; i < rectangleMatrix[0].length && i < chartsCount; i++)
				chartRectangles.add(rectangleMatrix[0][i]);
	}

	/**
	 * Calculates the spacing to use based on the rectangle dimensions. Subclasses
	 * implement this to use getWidth() for horizontal layouts or getHeight() for
	 * vertical layouts.
	 * 
	 * @param rectangle the bounding rectangle
	 * @return the dimension to use for relative spacing calculation
	 */
	protected double getSpacingDimension(Rectangle2D rectangle) {
		return rectangle.getHeight();
	}

	/**
	 * Gets the absolute spacing between charts in pixels.
	 * 
	 * @return spacing in pixels, or calculated from relative spacing if set
	 */
	public int getSpacingBetweenCharts() {
		return spacingBetweenCharts;
	}

	/**
	 * Sets the absolute spacing between charts in pixels. This is overridden if
	 * relative spacing is set.
	 * 
	 * @param spacingBetweenCharts spacing in pixels (must be non-negative)
	 * @throws IllegalArgumentException if spacing is negative
	 */
	public void setSpacingBetweenCharts(int spacingBetweenCharts) {
		if (spacingBetweenCharts < 0)
			throw new IllegalArgumentException(
					"MultiAxisChartRectangleLayout.setSpacingBetweenCharts: Spacing cannot be negative: "
							+ spacingBetweenCharts);

		this.spacingBetweenCharts = spacingBetweenCharts;

		setRectangle(rectangle);
	}

	/**
	 * Gets the relative spacing between charts as a fraction of total dimension.
	 * 
	 * @return relative spacing (0.0-1.0), or null if using absolute spacing
	 */
	public Double getRelativeSpaceBetweenCharts() {
		return relativeSpaceBetweenCharts;
	}

	/**
	 * Sets the relative spacing between charts as a fraction of total dimension.
	 * When set, this overrides absolute spacing.
	 * 
	 * @param relativeSpaceBetweenCharts spacing as fraction (0.0-1.0), or null for
	 *                                   absolute spacing
	 * @throws IllegalArgumentException if value is outside valid range
	 */
	public void setRelativeSpaceBetweenCharts(Double relativeSpaceBetweenCharts) {
		if (relativeSpaceBetweenCharts != null
				&& (relativeSpaceBetweenCharts < 0.0 || relativeSpaceBetweenCharts > 1.0))
			throw new IllegalArgumentException(
					"MultiAxisChartRectangleLayout.setRelativeSpaceBetweenCharts: Relative spacing must be between 0.0 and 1.0: "
							+ relativeSpaceBetweenCharts);

		this.relativeSpaceBetweenCharts = relativeSpaceBetweenCharts;

		setRectangle(rectangle);
	}

	public List<Rectangle2D> getChartRectangles() {
		return chartRectangles;
	}

}
