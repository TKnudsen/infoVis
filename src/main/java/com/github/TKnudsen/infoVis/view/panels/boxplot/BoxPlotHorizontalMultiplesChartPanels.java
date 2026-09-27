package com.github.TKnudsen.infoVis.view.panels.boxplot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

import de.javagl.selection.LoggingSelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * @version 1.0
 * @since 2026
 */
public class BoxPlotHorizontalMultiplesChartPanels {

	public static void dropATestFrame(List<StatisticsSupport> dataStatisticsPerDimension, List<Color> colors,
			List<String> labels) {

		BoxPlotHorizontalMultiplesChartPanel panel = new BoxPlotHorizontalMultiplesChartPanel(
				dataStatisticsPerDimension, colors, labels);

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

		SVGFrameTools.dropSVGFrame(panel, "BoxPlotHorizontalMultiplesChartPanel", 500, 500);
	}

	private BoxPlotHorizontalMultiplesChartPanels() {
	}
}
