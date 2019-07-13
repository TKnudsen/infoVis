package com.github.TKnudsen.infoVis.view.tools;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Utilities for the creation of tool-tips
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class ToolTipTools {
	public static Rectangle2D createToolTipRectangle(Rectangle2D context, Point p, double width, double height) {
		if (context == null || p == null)
			return null;

		double xStart = p.getX() - width;
		if (xStart < 0 && context.getWidth() > width)
			xStart = Math.min(0, context.getWidth() - width);

		double yStart = p.getY() - height;
		if (yStart < 0 && context.getHeight() > height)
			yStart = Math.min(0, context.getHeight() - height);

		Rectangle2D rect = new Rectangle2D.Double(xStart, yStart, width, height);

		return rect;
	}

	/**
	 * creates a tool-tip for a numerical value defined by its screen coordinate and
	 * its world value. (inverse).
	 * 
	 * @param p
	 * @param positionMapping
	 * @param chartRectangle
	 * @return
	 */
	public static ChartPainter getTooltipForPositionMapping1D(Point p, Double yOrYPointComponent, Number inverseMapping,
			Rectangle2D chartRectangle) {
		if (chartRectangle == null)
			return null;

		if (Double.isNaN(yOrYPointComponent))
			return null;

		StringPainter stringPainter = new StringPainter("V: " + MathFunctions.round(inverseMapping.doubleValue(), 2)
				+ ", Pix: " + MathFunctions.round(yOrYPointComponent, 2));

		Rectangle2D rect = ToolTipTools.createToolTipRectangle(chartRectangle, p, 140, 32);
		stringPainter.setRectangle(rect);

		stringPainter.setBackgroundPaint(ColorTools.setAlpha(Color.DARK_GRAY, 0.5f));
		stringPainter.setFontColor(Color.WHITE);
		stringPainter.setFontSize(15);

		return stringPainter;
	}
}