package com.github.TKnudsen.infoVis.view.panels.boxPlot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotVerticalMultiplesChartPanels;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.01
 */

public class BoxPlotVerticalMultiplesChartPanelTester {

	public static void main(String[] args) {
		// create data
		int dim = 10;
		int count = 150;

		List<StatisticsSupport> dataStatisticsPerDimension = new ArrayList<>();
		List<Color> colors = new ArrayList<>(dim);
		List<String> labels = new ArrayList<>(dim);

		for (int i = 0; i < dim; i++) {
			List<Double> values = new ArrayList<>();
			for (int j = 0; j < count; j++)
				values.add(Math.random() * 10);
			dataStatisticsPerDimension.add(new StatisticsSupport(values));
			colors.add(
					new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
			labels.add("" + i);
		}

		dataStatisticsPerDimension.get(1).clear();
		dataStatisticsPerDimension.get(1).addValue(0.1 * 10);
		dataStatisticsPerDimension.get(1).addValue(0.1 * 10);
		dataStatisticsPerDimension.get(1).addValue(0.1 * 10);
		dataStatisticsPerDimension.get(1).addValue(0.08 * 10);
		dataStatisticsPerDimension.get(1).addValue(0.12 * 10);
		dataStatisticsPerDimension.get(1).addValue(1.22 * 10);

		BoxPlotVerticalMultiplesChartPanels.dropATestFrame(dataStatisticsPerDimension, colors, labels);
	}

}
