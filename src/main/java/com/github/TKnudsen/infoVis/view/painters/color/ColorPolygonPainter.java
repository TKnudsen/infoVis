package com.github.TKnudsen.infoVis.view.painters.color;

import java.awt.Color;
import java.awt.Graphics2D;

import com.github.TKnudsen.infoVis.view.visualPrimitives.ColoredPolygon;

/**
 * Draws a {@link ColoredPolygon} as a sequence of line segments, each in its
 * own vertex color.
 *
 * @version 1.0
 * @since 2022
 */
public class ColorPolygonPainter {

	public static void draw(Graphics2D g2, ColoredPolygon p) {
		if (p == null)
			return;

		Color c = g2.getColor();

		for (int i = 0; i < p.npoints - 1; i++) {
			g2.setColor(p.getColor(i));
			g2.drawLine(p.xpoints[i], p.ypoints[i], p.xpoints[i + 1], p.ypoints[i + 1]);
		}

		g2.setColor(c);
	}
}
