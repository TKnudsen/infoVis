package com.github.TKnudsen.infoVis.view.panels.piechart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.tools.ColorTools;

/**
 * 
 * <p>
 * InfoVis
 * </p>
 * 
 * Creates pie charts
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 *
 */
public class PieCharts {

	/**
	 * 
	 * @param percentage between [0... and 1]
	 * @param color
	 * @return
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
