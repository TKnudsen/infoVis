package com.github.TKnudsen.infoVis.view.painters.primitives;

import java.awt.Color;
import java.awt.Graphics2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Draws a filled and/or outlined rectangle matching the painter's
 * rectangle directly.
 *
 * @version 1.02
 * @since 2018
 */
public class RectanglePainter extends ChartPainter {

	public RectanglePainter() {
		setBackgroundPaint(null);
	}

	private boolean fill = true;

	public void draw(Graphics2D g2) {
		super.draw(g2);

		Color c = g2.getColor();

		if (fill && getRectangle() != null && getPaint() != null) {
			g2.setPaint(getPaint());
			g2.fill(getRectangle());
		}

		if (isDrawOutline() && getRectangle() != null) {
			g2.setPaint(getBorderPaint());
			g2.draw(getRectangle());
		}

		g2.setColor(c);
	}

	public boolean isFill() {
		return fill;
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}
}
