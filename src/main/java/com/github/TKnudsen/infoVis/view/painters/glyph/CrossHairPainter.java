package com.github.TKnudsen.infoVis.view.painters.glyph;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Renders a crosshair (a horizontal line, a vertical line, or both) through a
 * given pixel position, spanning the painter's rectangle.
 *
 * @version 1.02
 * @since 2012
 */
public class CrossHairPainter extends ChartPainter {

	private final boolean renderX;
	private final boolean renderY;

	private int x;
	private int y;

	/**
	 * @param renderX whether to draw the horizontal line (at y, spanning the
	 *                rectangle's width)
	 * @param renderY whether to draw the vertical line (at x, spanning the
	 *                rectangle's height)
	 */
	public CrossHairPainter(boolean renderX, boolean renderY) {
		this.renderX = renderX;
		this.renderY = renderY;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (rectangle == null)
			return;

		Paint oldPaint = g2.getPaint();
		g2.setPaint(getPaint());

		if (renderX)
			g2.drawLine((int) rectangle.getMinX(), y, (int) rectangle.getMaxX(), y);
		if (renderY)
			g2.drawLine(x, (int) rectangle.getMinY(), x, (int) rectangle.getMaxY());

		g2.setPaint(oldPaint);
	}

	/**
	 * @param x pixel x-coordinate the vertical line is drawn at
	 * @param y pixel y-coordinate the horizontal line is drawn at
	 */
	public void setCoordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Point2D getPosition() {
		return new Point2D.Double(x, y);
	}

}
