package com.github.TKnudsen.infoVis.view.panels.piechart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

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
 * PieChartPanelTester
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class PieChartPanelTester {
	public static void main(String[] args) {
		// create data
		List<Double> data = new ArrayList<>();
		List<Color> colors = new ArrayList<>();

		data.add(5.0);
		colors.add(new Color(120, 180, 87));

		data.add(2.0);
		colors.add(new Color(106, 161, 215));

		PieChartPanel panel = new PieChartPanel(data, colors);

		// SELECTION MODEL
		SelectionModel<Integer> selectionModel = SelectionModels.create();

		// SELECTION HANDLER
		SelectionHandler<Integer> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(panel);
		selectionHandler.setClickSelection(panel);

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

		SVGFrameTools.dropSVGFrame(panel, "PieChartPanelTester", 800, 400);
	}

}
