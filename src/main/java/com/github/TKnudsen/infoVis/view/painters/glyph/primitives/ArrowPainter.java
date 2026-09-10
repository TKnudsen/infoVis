package com.github.TKnudsen.infoVis.view.painters.glyph.primitives;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Renders a plain horizontal arrow spanning the painter's rectangle, from its
 * left edge to a small margin before its right edge, with a triangular
 * arrowhead at the tip.
 *
 * @version 1.0
 * @since 2014
 */
public class ArrowPainter extends ChartPainter {

	private static final int ARROWHEAD_MARGIN = 5;

	private final Polygon arrowHead = new Polygon(new int[] { 0, -5, 5 }, new int[] { 5, -5, -5 }, 3);

	public ArrowPainter() {
		setBackgroundPaint(Color.WHITE);
		setBorderPaint(Color.WHITE);
		setPaint(Color.RED);
	}

	/**
	 * @return the arrowhead shape, in the painter's own rotated/translated
	 *         coordinate space (see {@link #draw(Graphics2D)})
	 */
	public Polygon getArrowHead() {
		return arrowHead;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (rectangle == null)
			return;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(getBackgroundPaint());
		g2.fill(rectangle);
		g2.setPaint(getPaint());

		Graphics2D g2d = (Graphics2D) g2.create();
		try {
			double x1 = rectangle.getX();
			double y = rectangle.getCenterY();
			double x2 = x1 + rectangle.getWidth() - ARROWHEAD_MARGIN;

			g2d.draw(new Line2D.Double(x1, y, x2, y));
			g2d.translate(x2, y);
			// arrowHead's own coordinates point toward +y (tip at (0,5)); rotate a
			// quarter turn so it points along the line instead, toward +x
			g2d.rotate(-Math.PI / 2d);
			g2d.draw(arrowHead);
			g2d.fill(arrowHead);
		} finally {
			g2d.dispose();
		}
	}

}
