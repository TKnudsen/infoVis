package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.visualChannels.ShapeAttributes;

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
 * Test for vertical distribution charts
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class Distribution1DVerticalHighlightPanelTester {
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("Distribution1DVerticalPanel Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create data
		List<Double> values = new ArrayList<>();
		int count = 100;

		for (int i = 0; i < count; i++) {
			values.add(Math.random() * 2);
			values.add(0.3 + Math.random() * 0.2);
			values.add(1.0);
		}
		Distribution1DVerticalPanel<Double> panel = (Distribution1DVerticalPanel<Double>) Distribution1DPanels
				.createForDoubles(values, Color.BLACK, -0.5, 3.3, true);
		// Distribution1DVerticalPanel<Double> panel =
		// Distribution1DVerticalPanels.createForDoubles(values, Color.BLACK,
		// -0.5, 3.3);

		// special values, such as clusters
		panel.addSpecialValue(1.1, new ShapeAttributes(Color.MAGENTA, DisplayTools.ultraThickStroke));
		panel.addSpecialValue(1.4, new ShapeAttributes(Color.CYAN, DisplayTools.thickStroke));
		panel.addSpecialValue(1.7, new ShapeAttributes(Color.BLUE, DisplayTools.ultraThickStroke));

		SVGFrameTools.dropSVGFrame(panel, "InfoVisDistribution1DVerticalPanel", 800, 400);

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
	}

}
