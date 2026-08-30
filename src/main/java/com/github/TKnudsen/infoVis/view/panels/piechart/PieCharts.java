package com.github.TKnudsen.infoVis.view.panels.piechart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.tools.ColorTools;

/**
 * @version 1.01
 * @since 2018
 */
public class PieCharts {

	/**
	 * 
	 * @param percentage between [0... and 1] percentage
	 * @param color      color
	 * @return pie
	 */
	public static PieChart createPieChartBipartite(double percentage, Color color) {
		if (percentage < 0 || percentage > 1.0)
			throw new IllegalArgumentException("PieCharts.createPieChartBipartite: percentage out of range");

		List<Double> percentages = new ArrayList<>();
		percentages.add(percentage);
		percentages.add(1.0 - percentage);

		List<Color> colors = new ArrayList<>();
		colors.add(color);
		colors.add(ColorTools.setAlpha(color, 0.0f));

		return new PieChart(percentages, colors);
	}

}
