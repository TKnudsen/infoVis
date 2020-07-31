package com.github.TKnudsen.infoVis.view.panels.barChart;

import java.awt.Graphics2D;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChart;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarCharts;

import de.javagl.selection.LoggingSelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2020 Juergen Bernard,
 * https://github.com/TKnudsen/InfoVis<br>
 * </p>
 * 
 * <p>
 * Vertical bar chart test with interaction design
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class BarChartVerticalPanelTester {
	public static void main(String[] args) {
//		BarChart barChart = new BarChart(points, colors);
		BarChart barChart = BarCharts.createBarChart(BarChartDataForTesting.values(), BarChartDataForTesting.colors());
		barChart.setBackground(null);
		BarCharts.addLegend(barChart, BarChartDataForTesting.labels());

		// SELECTION MODEL
		SelectionModel<Integer> selectionModel = SelectionModels.create();

		// SELECTION HANDLER
		SelectionHandler<Integer> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(barChart);
		selectionHandler.setClickSelection(barChart);
		selectionHandler.setRectangleSelection(barChart);

		barChart.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		barChart.setSelectedFunction(new Function<Integer, Boolean>() {

			@Override
			public Boolean apply(Integer t) {
				return selectionHandler.getSelectionModel().isSelected(t);
			}
		});

		selectionModel.addSelectionListener(new LoggingSelectionListener<>());

		SVGFrameTools.dropSVGFrame(barChart, "BarChartVerticalPanelTester", 800, 400);
	}

}
