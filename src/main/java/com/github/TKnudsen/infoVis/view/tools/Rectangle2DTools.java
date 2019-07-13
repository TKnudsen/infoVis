package com.github.TKnudsen.infoVis.view.tools;

import java.awt.geom.Rectangle2D;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Tools for the creation of a matrix (2D-grid) of Rectangle2D.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public class Rectangle2DTools {

	public static Rectangle2D[][] createRectangleMatrix(Rectangle2D rectangle, int xCount, int yCount, double spacing) {
		if (rectangle == null || xCount <= 0 || yCount <= 0 || spacing < 0
				|| rectangle.getWidth() < xCount + spacing * (xCount - 1)
				|| rectangle.getHeight() < yCount + spacing * (yCount - 1))
			return null;

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

	public static double calculateSpacingValue(double width, double height, int xCount, int yCount) {
		return Math.floor(Math.min(width / (double) xCount, height / (double) yCount) * 0.05);
	}
}
