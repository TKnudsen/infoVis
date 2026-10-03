package com.github.TKnudsen.infoVis.view.tools;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Renders a {@link ChartPainter} or a Swing panel into a new in-memory
 * {@link BufferedImage} -- e.g. for thumbnails, exports, or embedding a
 * chart's current appearance into another image.
 *
 * @version 1.0
 * @since 2026
 */
public class ImageTools {

	/**
	 * Renders the given painter into a new ARGB image, sized {@code width} x
	 * {@code height} regardless of the painter's current rectangle (which this
	 * overwrites).
	 *
	 * @param painter the painter to render
	 * @param width   the image width
	 * @param height  the image height
	 * @return the rendered image
	 */
	public static BufferedImage createImage(ChartPainter painter, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();

		try {
			painter.setRectangle(new Rectangle2D.Double(0, 0, width, height));
			painter.draw(graphics);
		} finally {
			graphics.dispose();
		}

		return image;
	}

	/**
	 * Renders the given panel into a new ARGB image using its current
	 * dimensions.
	 *
	 * @param panel the panel to render
	 * @return the rendered image
	 */
	public static BufferedImage createImage(InfoVisChartPanel panel) {
		return createImage((JPanel) panel);
	}

	/**
	 * Renders the given panel into a new ARGB image using its current dimensions.
	 *
	 * @param panel the panel to render
	 * @return the rendered image
	 * @throws IllegalArgumentException if the panel's width or height isn't
	 *                                   positive
	 */
	public static BufferedImage createImage(JPanel panel) {
		int width = panel.getWidth();
		int height = panel.getHeight();

		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException("ImageTools: panel must have a positive width and height.");

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();

		try {
			panel.printAll(graphics);
		} finally {
			graphics.dispose();
		}

		return image;
	}

}
