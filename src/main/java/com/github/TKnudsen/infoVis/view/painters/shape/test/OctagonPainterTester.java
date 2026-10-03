package com.github.TKnudsen.infoVis.view.painters.shape.test;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import com.github.TKnudsen.infoVis.view.painters.shape.OctagonPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Demo of {@link OctagonPainter}: a regular octagon (default
 * {@code diagonalSizeRatio}), a more square-like one with shrunk corner
 * cuts, and a thick-banded variant via {@code strokeWidthRate}.
 *
 * @since 2026
 */
public class OctagonPainterTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("OctagonPainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(1, 3, 10, 10));

		List<Color> colors = Arrays.asList(Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN,
				Color.BLUE, Color.MAGENTA, Color.PINK);

		OctagonPainter regular = new OctagonPainter(colors);
		frame.add(new InfoVisChartPanel(regular));

		OctagonPainter squarish = new OctagonPainter(colors);
		squarish.setDiagonalSizeRatio(0.3);
		frame.add(new InfoVisChartPanel(squarish));

		OctagonPainter thickBand = new OctagonPainter(colors.subList(0, 4));
		thickBand.setStrokeWidthRate(0.08);
		frame.add(new InfoVisChartPanel(thickBand));

		frame.setSize(1000, 350);
		frame.setLocation(50, 50);
		frame.setVisible(true);
	}
}
