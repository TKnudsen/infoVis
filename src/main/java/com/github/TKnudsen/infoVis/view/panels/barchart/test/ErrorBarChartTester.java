package com.github.TKnudsen.infoVis.view.panels.barchart.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.barchart.ErrorBarChartPainter.ErrorMeasure;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChartValueBased;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChartsValueBased;

/**
 * Standalone, select-capable demo of
 * {@link BarChartsValueBased#createErrorBarChart}: 6 bars, each backed by a
 * synthetic Gaussian sample, bar height = mean, whisker = confidence
 * interval, with click + rectangle selection wired via
 * {@link BarChartsValueBased#addInteraction}.
 *
 * @version 1.0
 * @since 2026
 */
public class ErrorBarChartTester {

	public static void main(String[] args) {
		Random random = new Random(3);

		int barCount = 6;
		List<StatisticsSupport> valuesPerBar = new ArrayList<>();
		List<Color> colors = new ArrayList<>();

		for (int i = 0; i < barCount; i++) {
			List<Double> samples = new ArrayList<>();
			double mean = 3 + random.nextDouble() * 7;
			for (int s = 0; s < 50; s++)
				samples.add(mean + random.nextGaussian() * (0.5 + i * 0.3));

			valuesPerBar.add(new StatisticsSupport(samples));
			colors.add(Color.getHSBColor(i / (float) barCount, 0.6f, 0.85f));
		}

		BarChartValueBased barChart = BarChartsValueBased.createErrorBarChart(valuesPerBar, colors,
				ErrorMeasure.CONFIDENCE_INTERVAL);
		barChart.setBackground(null);

		BarChartsValueBased.addInteraction(barChart);

		SwingUtilities.invokeLater(() -> SVGFrameTools.dropSVGFrame(barChart, "ErrorBarChart Demo", 700, 500));
	}

}
