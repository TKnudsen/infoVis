package com.github.TKnudsen.infoVis.view.painters.piechart.test;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.github.TKnudsen.infoVis.view.painters.piechart.PieChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Manual visual test for {@link PieChartPainter}.
 *
 * @version 1.0
 * @since 2016
 */
public class PieChartPainterTester {
	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setTitle("PieChartPainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		int numbers = 5;
		frame.setLayout(new GridLayout(numbers, numbers));
		for (int i = 0; i < numbers * numbers; i++) {
			List<Double> fractions = new ArrayList<>();
			List<Color> colors = new ArrayList<>();
			for (int f = 0; f < 2; f++) {
				if (f == 1)
					fractions.add(1 - fractions.get(0));
				else
					fractions.add(Math.random());
				colors.add(new Color((int) (Math.random() * 255), (int) (Math.random() * 255),
						(int) (Math.random() * 255)));
			}

			PieChartPainter painter = new PieChartPainter(fractions, colors);
			InfoVisChartPanel p = new InfoVisChartPanel(painter);
			frame.add(p);
		}
		frame.setSize(800, 300);
		frame.setLocation(50, 50);
		frame.setVisible(true);
	}
}
