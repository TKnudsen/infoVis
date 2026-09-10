package com.github.TKnudsen.infoVis.view.painters.glyph.test;

import java.awt.GridLayout;
import java.util.Random;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.glyph.HashCodePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive demo: a grid of {@link HashCodePainter} glyphs, one
 * per random object (here, a random {@link UUID}), showing the variety of
 * shapes the technique produces -- the same object always yields the same
 * glyph, since the glyph is derived from {@link Object#hashCode()}.
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
			UUID object = new UUID(random.nextLong(), random.nextLong());
			HashCodePainter painter = new HashCodePainter(object, 7);
			frame.add(new InfoVisChartPanel(painter));
		}

		frame.setSize(800, 800);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

}
