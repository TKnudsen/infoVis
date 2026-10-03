package com.github.TKnudsen.infoVis.view.painters.glyph.primitives;

import java.awt.Graphics2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * Paints a diagonal cross ("X") centered in the painter's rectangle.
 *
 * @version 1.0
 * @since 2026
 */
public class CrossPainter extends ChartPainter {

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (getRectangle() == null)
			return;

		double delta = Math.min(getRectangle().getWidth() / 2 * 0.7, getRectangle().getHeight() / 2 * 0.7);
		double centerX = getRectangle().getCenterX();
		double centerY = getRectangle().getCenterY();

		DisplayTools.drawLine(g2, (float) (centerX - delta), (float) (centerY - delta), (float) (centerX + delta),
				(float) (centerY + delta), DisplayTools.mediumStroke, getPaint());

		DisplayTools.drawLine(g2, (float) (centerX - delta), (float) (centerY + delta), (float) (centerX + delta),
				(float) (centerY - delta), DisplayTools.mediumStroke, getPaint());
	}
}
