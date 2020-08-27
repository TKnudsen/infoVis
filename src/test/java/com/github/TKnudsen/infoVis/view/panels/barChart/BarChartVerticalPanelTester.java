package com.github.TKnudsen.infoVis.view.panels.barChart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.csv.CSVParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IntegerParser;
import com.github.TKnudsen.infoVis.data.barChart.BarChartDataForTesting;
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
 * @version 1.03
 */
public class BarChartVerticalPanelTester {

	private static IntegerParser integerParser = new IntegerParser();
	private static DoubleParser doubleParser = new DoubleParser();

	public static void main(String[] args) {
		// DATA
		List<Double> points = new ArrayList<>();
		List<Color> colors = new ArrayList<>();
		List<String> labels = new ArrayList<>();

		// try data import
		CSVParser parser = new CSVParser("");
		try {
			List<ComplexDataObject> parsed = parser.parse("barchartData.csv");
			if (parsed != null) {
				for (ComplexDataObject cdo : parsed) {
					labels.add(cdo.getAttribute("Device").toString());
					points.add(doubleParser.apply(cdo.getAttribute("Count")));
					colors.add(new Color(integerParser.apply(cdo.getAttribute("R")),
							integerParser.apply(cdo.getAttribute("G")), integerParser.apply(cdo.getAttribute("B"))));
				}
			}
		} catch (IOException e) {
			// e.printStackTrace();
		}
		if (points.isEmpty()) { // otherwise use default data
			labels = BarChartDataForTesting.labels();
			points = BarChartDataForTesting.values();
			colors = BarChartDataForTesting.colors();
		}

		// BARCHART
		BarChart barChart = BarCharts.createBarChart(points, colors);
		barChart.setBackground(null);
		BarCharts.addLegend(barChart, labels);

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
