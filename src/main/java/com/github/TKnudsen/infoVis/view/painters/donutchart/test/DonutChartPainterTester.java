package com.github.TKnudsen.infoVis.view.painters.donutchart.test;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.github.TKnudsen.infoVis.view.painters.donutchart.DonutChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Manual visual test for {@link DonutChartPainter}.
 *
 * @version 1.0
 * @since 2016
 */
public class DonutChartPainterTester {

	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setTitle("DonutChartPainterTester Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		int numbers = 10;
		frame.setLayout(new GridLayout(numbers, numbers));
		for (int i = 0; i < numbers * numbers; i++) {
			List<Double> fractions = new ArrayList<>();
			List<Color> colors = new ArrayList<>();
			for (int f = 0; f < 3; f++) {
				fractions.add(Math.random());
				colors.add(new Color((int) (Math.random() * 255), (int) (Math.random() * 255),
						(int) (Math.random() * 255)));
			}
			DonutChartPainter painter = new DonutChartPainter(fractions, colors);
			painter.setDrawOutline(false);
			InfoVisChartPanel p = new InfoVisChartPanel(painter);
			frame.add(p);
		}
		frame.setSize(800, 300);
		frame.setLocation(50, 50);
		frame.setVisible(true);
	}
}
