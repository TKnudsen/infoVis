package com.github.TKnudsen.infoVis.view.painters.glyph.primitives;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Renders a short arrow whose angle encodes a trend value: 0 draws a
 * horizontal arrow (no change), 1 draws a 45-degree arrow (maximal
 * increase). Negative values point downward.
 *
 * @version 1.0
 * @since 2014
 */
public class TrendPainter extends ChartPainter {

	/** the arrow's tip is inset from the rectangle's edge by this fraction of its own length */
	private static final double ARROWHEAD_INSET_FACTOR = 0.5;

	private static final Polygon ARROW_HEAD = new Polygon(new int[] { 0, -5, 5 }, new int[] { 5, -5, -5 }, 3);

	private double trend;

	private double angle;
	private double startX = Double.NaN;
	private double startY = Double.NaN;
	private double endX = Double.NaN;
	private double endY = Double.NaN;

	/** @param trend the arrow angle, expressed as {@code tan(angle)}: 0 is horizontal, 1 is a 45-degree upward angle */
	public TrendPainter(double trend) {
		this.trend = trend;
		updateArrow();
	}

	private void updateArrow() {
		angle = Math.atan(trend);

		if (rectangle == null) {
			startX = Double.NaN;
			startY = Double.NaN;
			endX = Double.NaN;
			endY = Double.NaN;
			return;
		}

		double delta = Math.min(rectangle.getWidth() * 0.5, rectangle.getHeight() * 0.5);

		startX = rectangle.getCenterX() - delta * ARROWHEAD_INSET_FACTOR;
		endX = startX + Math.cos(angle) * delta;

		startY = rectangle.getCenterY();
		endY = startY - Math.sin(angle) * delta;
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);
		updateArrow();
	}

	@Override
	public void draw(Graphics2D g2) {
		if (rectangle == null)
			return;

		Paint oldPaint = g2.getPaint();
		g2.setPaint(getPaint());

		g2.draw(new Line2D.Double(startX, startY, endX, endY));

		Graphics2D g2d = (Graphics2D) g2.create();
		try {
			g2d.translate(endX, endY);
			// the polygon points toward +y (tip at (0,5)); rotate so it points along
			// the arrow's own direction instead -- note the sign flip on angle, since
			// screen Y increases downward while angle was computed in math convention
			g2d.rotate(-angle - Math.PI / 2d);
			g2d.draw(ARROW_HEAD);
			g2d.fill(ARROW_HEAD);
		} finally {
			g2d.dispose();
		}

		g2.setPaint(oldPaint);
	}

	public double getTrend() {
		return trend;
	}

	public void setTrend(double trend) {
		this.trend = trend;
		updateArrow();
	}

}
