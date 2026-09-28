package com.github.TKnudsen.infoVis.view.painters.radial.test;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.github.TKnudsen.infoVis.view.painters.radial.PieRadarChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Manual visual test for {@link PieRadarChartPainter}.
 *
 * @version 1.0
 * @since 2026
 */
public class PieRadarChartPainterTester {

	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setTitle("PieRadarChartPainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		int numbers = 15;
		frame.setLayout(new GridLayout(numbers, numbers));
		for (int i = 0; i < numbers * numbers; i++) {
			List<Double> fractions = new ArrayList<>();
			List<Color> colors = new ArrayList<>();
			List<String> labels = new ArrayList<>();
			for (int f = 0; f < 6; f++) {
				if (f == 1)
					fractions.add(1 - fractions.get(0));
				else
					fractions.add(Math.random());
				colors.add(new Color((int) (Math.random() * 255), (int) (Math.random() * 255),
						(int) (Math.random() * 255)));
				labels.add("");
			}
			PieRadarChartPainter painter = new PieRadarChartPainter(fractions, colors, labels);
			InfoVisChartPanel p = new InfoVisChartPanel(painter);
			frame.add(p);
		}
		frame.setSize(800, 300);
		frame.setLocation(50, 50);
		frame.setVisible(true);
	}
}
