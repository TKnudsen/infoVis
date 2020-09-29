package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartVerticalPainter;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DPainterPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.VerticalStringAlignment;

import de.javagl.selection.LoggingSelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

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

	/**
	 * creates a bar chart with all bars having the same color
	 * 
	 * @param bars
	 * @param color
	 * @return
	 */
	public static BarChart createBarChart(List<Double> bars, Color color) {
		List<Color> colors = new ArrayList<Color>();

		for (@SuppressWarnings("unused")
		Double d : bars)
			colors.add(color);

		return new BarChart(bars, colors);
	}

	public static BarChart createBarChart(List<Double> bars, List<Color> colors) {
		return new BarChart(bars, colors);
	}

	public static BarChartHorizontal createBarChartHorizontal(List<Double> bars, List<Color> colors) {
		return new BarChartHorizontal(bars, colors);
	}

	/**
	 * 
	 * @param data   list of bar chart data
	 * @param colors one color for each bar chart layer
	 * @return
	 */
	public static BarChart createLayeredBarChart(List<List<Double>> data, List<Color> barchartColors) {
		Objects.requireNonNull(data);

		if (data.isEmpty())
			return null;

		// create bar chart
		Color color = Color.GRAY;
		if (barchartColors != null && !barchartColors.isEmpty())
			color = barchartColors.get(0);
		BarChart barChart = createBarChart(data.get(0), color);

		// add additional bar chart layers
		for (int i = 1; i < data.size(); i++) {
			List<Double> bars = data.get(i);

			List<Color> colors = new ArrayList<Color>();
			Color c = barchartColors.size() > i ? barchartColors.get(i) : Color.BLACK;
			for (@SuppressWarnings("unused")
			Double d : bars)
				colors.add(c);

			BarChartVerticalPainter barChartVerticalPainter = new BarChartVerticalPainter(bars, colors);
			barChart.addChartPainter(barChartVerticalPainter, true);
		}

		return barChart;
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

	public static void addInteraction(BarChart barChart) {
		SelectionModel<Integer> selectionModel = SelectionModels.create();

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
	}

	/**
	 * avoid instantiation
	 */
	private BarCharts() {
	};
}
