package com.github.TKnudsen.infoVis.view.panels.barChart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
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
 * Copyright: (c) 2018-2019 Juergen Bernard,
 * https://github.com/TKnudsen/InfoVis<br>
 * </p>
 * 
 * <p>
 * Vertical bar chart test with interaction design
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class BarChartVerticalPanelTester {
	public static void main(String[] args) {
		// create data
		List<Double> points = new ArrayList<>();
		List<Color> colors = new ArrayList<>();
		List<String> labels = new ArrayList<>();

		labels.add("Flug");
		points.add(340.42 * 4);
		colors.add(new Color(120, 180, 87));

		labels.add("Mietauto");
		points.add(536.0);
		colors.add(new Color(106, 161, 215));

		labels.add("Essen");
		points.add(461.4);
		colors.add(new Color(236, 137, 69));

		labels.add("Unterkunft");
		points.add(420.0);
		colors.add(new Color(172, 172, 172));

		labels.add("Einkaufen");
		points.add(239.16);
		colors.add(new Color(254, 196, 27));

		labels.add("Tanken");
		points.add(75.0);
		colors.add(new Color(82, 120, 193));

//		BarChart panel = new BarChart(points, colors);
		BarChart panel = BarCharts.createBarChart(points, colors);

		// SELECTION MODEL
		SelectionModel<Integer> selectionModel = SelectionModels.create();

		// SELECTION HANDLER
		SelectionHandler<Integer> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(panel);
		selectionHandler.setClickSelection(panel);
		selectionHandler.setRectangleSelection(panel);

		panel.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		panel.setSelectedFunction(new Function<Integer, Boolean>() {

			@Override
			public Boolean apply(Integer t) {
				return selectionHandler.getSelectionModel().isSelected(t);
			}
		});

		selectionModel.addSelectionListener(new LoggingSelectionListener<>());

		SVGFrameTools.dropSVGFrame(panel, "Boxplots", 800, 400);
	}

}
