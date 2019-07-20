package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Color;
import java.util.List;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Factory for BarCharts.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class BarCharts {

	public static BarChart createBarChart(List<Double> bars, List<Color> colors) {

		return new BarChart(bars, colors);
	}

	public static BarChartHorizontalPanel createBarChartHorizontal(List<Double> bars, List<Color> colors) {

		return new BarChartHorizontalPanel(bars, colors);

//		// SELECTION MODEL
//		SelectionModel<Integer> selectionModel = SelectionModels.create();
//
//		// SELECTION HANDLER
//		SelectionHandler<Integer> selectionHandler = new SelectionHandler<>(selectionModel);
//		selectionHandler.attachTo(panel);
//		selectionHandler.setClickSelection(panel);
//		selectionHandler.setRectangleSelection(panel);
//
//		panel.addChartPainter(new ChartPainter() {
//			@Override
//			public void draw(Graphics2D g2) {
//				selectionHandler.draw(g2);
//			}
//		});
//
//		panel.setSelectedFunction(new Function<Integer, Boolean>() {
//
//			@Override
//			public Boolean apply(Integer t) {
//				return selectionHandler.getSelectionModel().isSelected(t);
//			}
//		});
//
//		selectionModel.addSelectionListener(new LoggingSelectionListener<>());
	}

}
