package com.github.TKnudsen.infoVis.view.painters.string.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.string.UpperRightLabelPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive demo of {@link UpperRightLabelPainter}, layered
 * over a plain colored panel to show the badge pinned to the host
 * rectangle's upper-right corner at a few different sizes.
 *
 * @version 1.0
 * @since 2026
 */
public class UpperRightLabelPainterTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("UpperRightLabelPainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel grid = new JPanel(new GridLayout(1, 3, 10, 10));

		// the badge's font (and so its size) scales with the host rectangle, with
		// no upper cap -- keep these modest, or the badge dominates the panel
		String[] labels = { "New", "Beta", "Deprecated" };
		int[] sizes = { 60, 90, 120 };

		for (int i = 0; i < labels.length; i++) {
			JPanel host = new JPanel() {
				@Override
				protected void paintComponent(java.awt.Graphics g) {
					super.paintComponent(g);
					g.setColor(Color.LIGHT_GRAY);
					g.fillRect(0, 0, getWidth(), getHeight());
				}
			};
			host.setLayout(new java.awt.BorderLayout());

			UpperRightLabelPainter painter = new UpperRightLabelPainter(labels[i]);
			InfoVisChartPanel overlay = new InfoVisChartPanel(painter);
			overlay.setOpaque(false);
			host.add(overlay);
			host.setPreferredSize(new Dimension(sizes[i], sizes[i]));

			grid.add(host);
		}

		frame.add(grid);
		frame.setSize(650, 300);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

}
