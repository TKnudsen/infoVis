package com.github.TKnudsen.infoVis.view.tools;

import java.awt.geom.Rectangle2D;

/**
 * <p>
 * Tools for the creation of a matrix (2D-grid) of Rectangle2D.
 * </p>
 *
 * @version 1.05
 * @since 2016
 */
public class Rectangle2DTools {

	public static Rectangle2D[][] createRectangleMatrix(Rectangle2D rectangle, int xCount, int yCount, double spacing) {
		if (rectangle == null || xCount <= 0 || yCount <= 0 || spacing < 0
				|| rectangle.getWidth() < xCount + spacing * (xCount - 1)
				|| rectangle.getHeight() < yCount + spacing * (yCount - 1))
			return null;

		if (Double.isNaN(spacing))
			spacing = 0;

		Rectangle2D[][] rectangleArray = new Rectangle2D[xCount][yCount];

		double ySpace = rectangle.getHeight() - ((yCount - 1) * spacing);
		double xSpace = rectangle.getWidth() - ((xCount - 1) * spacing);

		double height = ySpace / yCount;
		double width = xSpace / xCount;

		for (int x = 0; x < xCount; x++) {
			for (int y = 0; y < yCount; y++) {
				double xPosition = rectangle.getX() + x * width + x * spacing;
				double yPosition = rectangle.getY() + y * height + y * spacing;

				rectangleArray[x][y] = new Rectangle2D.Double(xPosition, yPosition, width, height);
			}
		}

		return rectangleArray;
	}

	/**
	 * 
	 * @param width
	 * @param height
	 * @param xCount
	 * @param yCount
	 * @return
	 */
	public static double calculateSpacingValue(double width, double height, int xCount, int yCount) {
		return calculateSpacingValue(width, height, xCount, yCount, 0.05);
	}

	/**
	 * 
	 * @param width
	 * @param height
	 * @param xCount
	 * @param yCount
	 * @param ratio  default: 0.05
	 * @return
	 */
	public static double calculateSpacingValue(double width, double height, int xCount, int yCount, double ratio) {
		return Math.floor(Math.min(width / (double) xCount, height / (double) yCount))
				* Math.max(0.01, Math.min(0.5, ratio));
	}
}
