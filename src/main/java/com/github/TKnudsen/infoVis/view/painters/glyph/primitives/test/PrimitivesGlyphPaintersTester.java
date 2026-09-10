package com.github.TKnudsen.infoVis.view.painters.glyph.primitives.test;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.glyph.primitives.ArrowPainter;
import com.github.TKnudsen.infoVis.view.painters.glyph.primitives.EllipsisPainter;
import com.github.TKnudsen.infoVis.view.painters.glyph.primitives.RectangularPainter;
import com.github.TKnudsen.infoVis.view.painters.glyph.primitives.TrendBundlePainter;
import com.github.TKnudsen.infoVis.view.painters.glyph.primitives.TrendPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive demo of every painter in the
 * {@code com.github.TKnudsen.infoVis.view.painters.glyph.primitives}
 * package: {@link ArrowPainter}, {@link EllipsisPainter}, {@link TrendPainter},
 * {@link TrendBundlePainter}, and a minimal concrete subclass of
 * {@link RectangularPainter} (itself abstract, with no production subclass
 * yet) exercising its icon/bar/description drawing helpers.
 *
 * @version 1.0
 * @since 2026
 */
public class PrimitivesGlyphPaintersTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("glyph.primitives Painters Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel grid = new JPanel(new GridLayout(2, 3, 10, 10));

		grid.add(labeled("ArrowPainter", new InfoVisChartPanel(new ArrowPainter())));
		grid.add(labeled("EllipsisPainter", new InfoVisChartPanel(new EllipsisPainter("Ellipsis"))));
		grid.add(labeled("TrendPainter (trend=1)", new InfoVisChartPanel(new TrendPainter(1))));
		grid.add(labeled("TrendPainter (trend=-0.5)", new InfoVisChartPanel(new TrendPainter(-0.5))));
		grid.add(labeled("TrendBundlePainter", new InfoVisChartPanel(new TrendBundlePainter(Arrays.asList(1.0, 0.0, -1.0)))));
		grid.add(labeled("RectangularPainter (demo subclass)", new InfoVisChartPanel(new DemoRectangularPainter())));

		frame.add(grid);
		frame.setSize(700, 500);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

	private static JPanel labeled(String title, InfoVisChartPanel panel) {
		panel.setPreferredSize(new Dimension(180, 180));
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new javax.swing.BoxLayout(wrapper, javax.swing.BoxLayout.Y_AXIS));
		javax.swing.JLabel label = new javax.swing.JLabel(title);
		label.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		panel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		wrapper.add(label);
		wrapper.add(panel);
		return wrapper;
	}

	/**
	 * Minimal concrete {@link RectangularPainter} for demo purposes: draws a
	 * generated icon, a title bar, and a wrapped description paragraph stacked
	 * vertically -- {@link RectangularPainter} itself has no production
	 * subclass to demo instead.
	 */
	private static class DemoRectangularPainter extends RectangularPainter {

		private final BufferedImage icon = generateIcon();

		private static BufferedImage generateIcon() {
			BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			g2.setColor(java.awt.Color.ORANGE);
			g2.fillOval(0, 0, ICON_SIZE, ICON_SIZE);
			g2.dispose();
			return image;
		}

		@Override
		public void draw(Graphics2D g2) {
			super.draw(g2);

			if (rectangle == null)
				return;

			double x = rectangle.getX() + OFFSET;
			double y = rectangle.getY() + OFFSET;

			drawIcon(g2, x, y, ICON_SIZE, icon);
			drawBar(g2, x + ICON_SIZE + OFFSET, y, rectangle.getWidth() - ICON_SIZE - 3 * OFFSET, "Title", g2.getFont());
			drawDescription(g2, x, y + BAR_HEIGHT + OFFSET, rectangle.getWidth() - 2 * OFFSET,
					rectangle.getHeight() - BAR_HEIGHT - 2 * OFFSET,
					"A longer description that wraps across several lines to demonstrate drawDescription().");
		}

	}

}
