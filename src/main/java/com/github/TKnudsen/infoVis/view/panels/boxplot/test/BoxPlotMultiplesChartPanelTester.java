package com.github.TKnudsen.infoVis.view.panels.boxplot.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotHorizontalMultiplesChartPanels;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotVerticalMultiplesChartPanels;

/**
 * Demo of {@link BoxPlotVerticalMultiplesChartPanels} and
 * {@link BoxPlotHorizontalMultiplesChartPanels}: 5 synthetic distributions,
 * each labeled -- verifies category labels are actually drawn (the vertical
 * panel used to silently drop its labels argument), and that both
 * orientations render.
 *
 * @version 1.0
 * @since 2026
 */
public class BoxPlotMultiplesChartPanelTester {

	public static void main(String[] args) {
		Random random = new Random(9);

		int count = 5;
		List<StatisticsSupport> dataStatisticsPerDimension = new ArrayList<>();
		List<Color> colors = new ArrayList<>();
		List<String> labels = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			List<Double> samples = new ArrayList<>();
			double mean = 3 + random.nextDouble() * 7;
			for (int s = 0; s < 60; s++)
				samples.add(mean + random.nextGaussian() * (0.5 + i * 0.4));

			dataStatisticsPerDimension.add(new StatisticsSupport(samples));
			colors.add(Color.getHSBColor(i / (float) count, 0.6f, 0.85f));
			labels.add("Category " + i);
		}

		BoxPlotVerticalMultiplesChartPanels.dropATestFrame(dataStatisticsPerDimension, colors, labels);
		BoxPlotHorizontalMultiplesChartPanels.dropATestFrame(dataStatisticsPerDimension, colors, labels);
	}

}
