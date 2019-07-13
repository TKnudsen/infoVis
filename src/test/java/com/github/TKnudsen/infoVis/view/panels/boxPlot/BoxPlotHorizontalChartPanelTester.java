package com.github.TKnudsen.infoVis.view.panels.boxPlot;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotHorizontalCartPanel;

import de.javagl.selection.LoggingSelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

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
 * @version 1.01
 */

public class BoxPlotHorizontalChartPanelTester {
	public static void main(String[] args) {
		// create data
		List<Double> points = new ArrayList<>();
		int count = 150;

		for (int i = 0; i < count; i++) {
			points.add(Math.random() * 2);
			points.add(0.3 + Math.random() * 0.2);
		}

		StatisticsSupport dataStatistics = new StatisticsSupport(points);

		BoxPlotHorizontalCartPanel panel = new BoxPlotHorizontalCartPanel(dataStatistics);

		// SELECTION MODEL
		SelectionModel<Double> selectionModel = SelectionModels.create();

		// SELECTION HANDLER
		SelectionHandler<Double> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(panel);
		selectionHandler.setRectangleSelection(panel);

		panel.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		selectionModel.addSelectionListener(new LoggingSelectionListener<>());

		SVGFrameTools.dropSVGFrame(panel, "Boxplots", 800, 400);
	}

}
