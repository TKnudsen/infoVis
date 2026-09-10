package com.github.TKnudsen.infoVis.view.painters.glyph.test;

import java.awt.GridLayout;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.glyph.HashCodePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive demo: a grid of {@link HashCodePainter} glyphs, one
 * per random hash code, showing the variety of shapes the technique produces.
 *
 * @version 1.0
 * @since 2026
 */
public class HashCodePainterTester {

	public static void main(String[] args) {
		Random random = new Random(42);

		JFrame frame = new JFrame();
		frame.setTitle("HashCodePainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		int numbers = 15;
		frame.setLayout(new GridLayout(numbers, numbers));

		for (int i = 0; i < numbers * numbers; i++) {
			HashCodePainter painter = new HashCodePainter(random.nextLong(), 7);
			frame.add(new InfoVisChartPanel(painter));
		}

		frame.setSize(800, 800);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

}
