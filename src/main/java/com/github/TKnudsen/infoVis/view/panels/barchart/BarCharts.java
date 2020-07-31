package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Color;
import java.util.List;

import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DPainterPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.VerticalStringAlignment;

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
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class BarCharts {

	public static BarChart createBarChart(List<Double> bars, List<Color> colors) {

		return new BarChart(bars, colors);
	}

	public static BarChartHorizontal createBarChartHorizontal(List<Double> bars, List<Color> colors) {

		return new BarChartHorizontal(bars, colors);

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

	public static void addLegend(BarChart barChart, List<String> labels) {

		StringPainter[][] painters = new StringPainter[labels.size()][1];

		for (int i = 0; i < labels.size(); i++) {
			StringPainter stringPainter = new StringPainter(labels.get(i));
			stringPainter.setBackgroundPaint(null);

			stringPainter.setVerticalOrientation(true);
			stringPainter.setVerticalStringAlignment(VerticalStringAlignment.UP);
			painters[i][0] = stringPainter;
		}

		Grid2DPainterPainter<StringPainter> gridPainter = new Grid2DPainterPainter<>(painters);
		gridPainter.setBackgroundPaint(null);

		barChart.addChartPainter(gridPainter);
	}

	public static void addLegend(BarChartHorizontal barChart, List<String> labels) {

		StringPainter[][] painters = new StringPainter[1][labels.size()];

		for (int i = 0; i < labels.size(); i++) {
			StringPainter stringPainter = new StringPainter(labels.get(i));
			stringPainter.setBackgroundPaint(null);

			stringPainter.setHorizontalStringAlignment(HorizontalStringAlignment.RIGHT);
			painters[0][i] = stringPainter;
		}

		Grid2DPainterPainter<StringPainter> gridPainter = new Grid2DPainterPainter<>(painters);
		gridPainter.setBackgroundPaint(null);

		barChart.addChartPainter(gridPainter);
	}

	/**
	 * avoid instantiation
	 */
	private BarCharts() {
	};
}
