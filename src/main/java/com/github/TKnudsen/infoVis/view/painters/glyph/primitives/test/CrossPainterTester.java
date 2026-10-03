package com.github.TKnudsen.infoVis.view.painters.glyph.primitives.test;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.glyph.primitives.CrossPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive demo of {@link CrossPainter}: a diagonal cross
 * centered in the panel.
 *
 * @version 1.0
 * @since 2026
 */
public class CrossPainterTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("CrossPainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		CrossPainter crossPainter = new CrossPainter();
		crossPainter.setPaint(Color.RED);

		InfoVisChartPanel panel = new InfoVisChartPanel(crossPainter);
		panel.setPreferredSize(new Dimension(300, 150));
		frame.add(panel);

		frame.setSize(320, 190);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

}
