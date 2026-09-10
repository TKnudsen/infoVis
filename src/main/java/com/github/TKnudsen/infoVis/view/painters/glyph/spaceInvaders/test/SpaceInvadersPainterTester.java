package com.github.TKnudsen.infoVis.view.painters.glyph.spaceInvaders.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.glyph.spaceInvaders.SpaceInvadersPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive demo of {@link SpaceInvadersPainter} at a few
 * different sizes, since its stroke thickness and headline layout both
 * change across internal size tiers.
 *
 * @version 1.0
 * @since 2026
 */
public class SpaceInvadersPainterTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("SpaceInvadersPainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel grid = new JPanel(new GridLayout(1, 3, 10, 10));

		String[] titles = { "Small", "Medium", "Large" };
		int[] sizes = { 150, 350, 700 };
		Color[] colors = { Color.PINK, Color.YELLOW, Color.LIGHT_GRAY };

		for (int i = 0; i < titles.length; i++) {
			SpaceInvadersPainter painter = new SpaceInvadersPainter();
			painter.setTitle(titles[i]);
			painter.setBackgroundPaint(colors[i]);

			InfoVisChartPanel panel = new InfoVisChartPanel(painter);
			panel.setPreferredSize(new Dimension(sizes[i], sizes[i]));
			grid.add(panel);
		}

		frame.add(grid);
		frame.setSize(1300, 750);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

}
