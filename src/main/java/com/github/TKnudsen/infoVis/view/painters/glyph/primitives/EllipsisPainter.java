package com.github.TKnudsen.infoVis.view.painters.glyph.primitives;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Renders a filled, bordered ellipse (inset from the painter's rectangle by a
 * fixed border width) with a centered text label.
 *
 * @version 1.0
 * @since 2014
 */
public class EllipsisPainter extends ChartPainter {

	private static final int BORDER = 5;

	private final String label;

	public EllipsisPainter(String label) {
		this.label = label;

		setBackgroundPaint(Color.WHITE);
		setBorderPaint(Color.WHITE);
		setPaint(Color.BLACK);
	}

	@Override
	public void draw(Graphics2D g2) {
		if (getRectangle() == null)
			return;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(getBackgroundPaint());
		g2.fill(getRectangle());

		Ellipse2D.Double ellipse = new Ellipse2D.Double(getRectangle().getX() + BORDER, getRectangle().getY() + BORDER,
				getRectangle().getWidth() - 2 * BORDER, getRectangle().getHeight() - 2 * BORDER);

		g2.setPaint(getPaint());
		g2.fill(ellipse);
		g2.setPaint(getBorderPaint());
		g2.draw(ellipse);

		FontMetrics metrics = g2.getFontMetrics();
		float labelX = ((float) getRectangle().getWidth() - metrics.stringWidth(label)) * 0.5f + (float) getRectangle().getX();
		float labelY = (float) (getRectangle().getHeight() + metrics.getFont().getSize2D()) * 0.5f
				+ (float) getRectangle().getY();
		g2.drawString(label, labelX, labelY);
	}

}
