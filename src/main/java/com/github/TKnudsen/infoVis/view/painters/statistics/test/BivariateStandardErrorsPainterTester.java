package com.github.TKnudsen.infoVis.view.painters.statistics.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import com.github.TKnudsen.infoVis.view.painters.statistics.BivariateStandardErrorsPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Demo of {@link BivariateStandardErrorsPainter}: ten randomly colored groups
 * of 1000 samples each, overlaid on one shared axis -- mirrors the original
 * "MultipleStandardErrorBivariatePainter Test Frame" screenshot that
 * motivated this painter's rescue from {@code com.JB.painterLib}.
 *
 * @since 2026
 */
public class BivariateStandardErrorsPainterTester {

	private static final int GROUP_COUNT = 10;
	private static final int SAMPLES_PER_GROUP = 1000;

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("BivariateStandardErrorsPainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Random random = new Random(0);

		Collection<double[][]> data = new ArrayList<>();
		Collection<String> labels = new ArrayList<>(GROUP_COUNT);
		List<Color> colors = new ArrayList<>(GROUP_COUNT);

		for (int i = 0; i < GROUP_COUNT; i++) {
			// group[0] is this group's X samples, group[1] its Y samples
			double[][] group = new double[2][SAMPLES_PER_GROUP];

			double centerX = random.nextDouble() * 2.2;
			double centerY = random.nextDouble() * 4.0;

			for (int j = 0; j < SAMPLES_PER_GROUP; j++) {
				group[0][j] = centerX + (random.nextDouble() - 0.5) * 0.6;
				group[1][j] = centerY + (random.nextDouble() - 0.5) * 0.6;
			}

			data.add(group);
			colors.add(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
			labels.add("" + i);
		}

		BivariateStandardErrorsPainter painter = new BivariateStandardErrorsPainter(data, labels, colors);

		InfoVisChartPanel panel = new InfoVisChartPanel(painter);
		frame.add(panel);
		frame.setSize(800, 300);
		frame.setLocation(50, 50);
		frame.setVisible(true);
	}
}
