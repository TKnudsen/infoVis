package com.github.TKnudsen.infoVis.view.painters.glyph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Paints a "not available" glyph: a circle with a diagonal slash through it,
 * like the common no-signal/no-entry icon.
 *
 * @version 1.0
 * @since 2026
 */
public class NotAvailablePainter extends ChartPainter {

	private double strokeWidthRate = 0.075;

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (getRectangle() == null)
			return;

		double w = getRectangle().getWidth();
		double h = getRectangle().getHeight();
		double x = getRectangle().getX();
		double y = getRectangle().getY();

		Color resetColor = g2.getColor();
		Stroke resetStroke = g2.getStroke();

		g2.setPaint(getPaint());

		double diameter = Math.min(w, h);
		double strokeWidth = Math.max(1.0, strokeWidthRate * diameter);
		double diameterMinusStroke = Math.max(0.0, diameter - strokeWidth);
		Stroke glyphStroke = new BasicStroke((float) strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

		g2.setStroke(glyphStroke);

		Ellipse2D.Double circle = new Ellipse2D.Double(x + 0.5 * strokeWidth, y + 0.5 * strokeWidth,
				diameterMinusStroke, diameterMinusStroke);

		double lineOffset = (0.5 * diameter * (Math.sqrt(2) - 1) + 0.5 * strokeWidth) / Math.sqrt(2);

		Line2D.Double line = new Line2D.Double(x + lineOffset, y + diameter - lineOffset, x + diameter - lineOffset,
				y + lineOffset);

		g2.draw(line);
		g2.draw(circle);

		g2.setColor(resetColor);
		g2.setStroke(resetStroke);
	}

	public double getStrokeWidthRate() {
		return strokeWidthRate;
	}

	/**
	 * @param strokeWidthRate the glyph's stroke width, as a fraction of the
	 *                        rectangle's smaller dimension. Must be in
	 *                        {@code (0, 0.5]}.
	 */
	public void setStrokeWidthRate(double strokeWidthRate) {
		if (strokeWidthRate <= 0 || strokeWidthRate > 0.5)
			throw new IllegalArgumentException("NotAvailablePainter: strokeWidthRate must be in (0, 0.5]");

		this.strokeWidthRate = strokeWidthRate;
	}

}
