package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * <p>
 * Static, non-instantiable ColorBrewer-style palette of up to 12 colors,
 * with helpers for indexed color lookup and drawing a labeled legend.
 * </p>
 *
 * <p>
 * This product includes color specifications and designs developed by
 * Cynthia Brewer (http://colorbrewer.org/). Copyright (c) 2002 Cynthia
 * Brewer, Mark Harrower, and The Pennsylvania State University, licensed
 * under the Apache License, Version 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 * </p>
 *
 * @version 1.0
 */
public class StaticColorBrewerColorMap {
	private static final Color colors[] = { new Color(141, 211, 199),
			new Color(238,238,115), //new Color(255, 255, 179), 
			new Color(190, 186, 218),
			new Color(251, 128, 144), new Color(128, 177, 211),
			new Color(253, 180, 98), new Color(179, 222, 105),
			new Color(252, 205, 229), new Color(217, 217, 217),
			new Color(188, 128, 189), new Color(204, 235, 197),
			new Color(255, 237, 111) };

	// index [0, 11], alpha [0, 1]
	public static Color getColor(int index, float alpha) {
		Color c = getColor(index);
		float components[] = c.getComponents(null);
		return new Color(components[0], components[1], components[2], alpha);
	}

	// index [0, 11]
	public static Color getColor(int index) {
		if (index >= 0 && index <= 11)
			return colors[index];
		else {
			try {
				throw new Exception(
						"StaticColorBrewerColorMap: This colormap supports only maximal 12 classes. Make sure that your index is in the range [0, ..., 11]");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public static void drawColormap(Rectangle2D rect, Graphics2D g,
			int classes, boolean horizontal) {
		drawColormap(rect, g, classes, 1f, horizontal);
	}

	// draw colormap legend into given Rectangle2D
	public static void drawColormap(Rectangle2D rect, Graphics2D g,
			int classes, float alpha, boolean horizontal) {
		if (classes < 1 || classes > 12) {
			try {
				throw new Exception(
						"StaticColorBrewerColorMap: This colormap supports only maximal 12 classes. Make sure that your classes index is in the range [1, ..., 12]");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		double width = rect.getWidth();
		double height = rect.getHeight();

		if (horizontal) {
			double rectWidth = width / classes;
			for (int i = 0; i < classes; i++) {
				g.setColor(getColor(i, alpha));
				g.fill(new Rectangle2D.Double(rect.getX() + i * rectWidth, rect
						.getY(), rectWidth, height));
			}
		} else {
			double rectHeight = height / classes;
			for (int i = 0; i < classes; i++) {
				g.setColor(getColor(i, alpha));
				g.fill(new Rectangle2D.Double(rect.getX(), rect.getY() + i
						* rectHeight, width, rectHeight));
			}
		}

		g.setColor(Color.BLACK);
		g.draw(rect);
	}

	public static void drawColormap(Rectangle2D rect, Graphics2D g,
			String[] labels, boolean horizontal) {
		drawColormap(rect, g, 1f, labels, horizontal);
	}

	// draw colormap legend into given Rectangle2D
	public static void drawColormap(Rectangle2D rect, Graphics2D g,
			float alpha, String[] labels, boolean horizontal) {
		int classes = labels.length;
		drawColormap(rect, g, classes, alpha, horizontal);

		// draw legend
		double width = rect.getWidth();
		double height = rect.getHeight();

		g.setColor(Color.BLACK);

		if (horizontal) {
			double rectWidth = width / classes;
			for (int i = 0; i < classes; i++) {
				if (labels[i] != null)
					g.drawString(labels[i], (int) (rect.getMinX() + 5 + i
							* rectWidth), (int) rect.getMinY() + 12);
			}
		} else {
			double rectHeight = height / classes;
			for (int i = 0; i < classes; i++) {
				if (labels[i] != null)
					g.drawString(labels[i], (int) (rect.getMinX() + 5),
							(int) (rect.getMinY() + 12 + i * rectHeight));
			}
		}
	}
}