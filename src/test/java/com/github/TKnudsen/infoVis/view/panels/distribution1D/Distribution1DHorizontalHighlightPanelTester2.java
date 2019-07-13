package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
 * Test for horizontal distribution charts
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class Distribution1DHorizontalHighlightPanelTester2 {
	public static void main(String[] args) {

		List<Double> data = new ArrayList<>();
		List<Double> samples = new ArrayList<>();
		List<Color> colors = new ArrayList<>();
		Random rnd = new Random();

		for (int i = 0; i < 150; i++) {
			data.add(rnd.nextDouble());
		}

		for (int i = 0; i < 10; i++) {
			samples.add(rnd.nextDouble());
			colors.add(new Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
		}

		Distribution1DHorizontalPanel panel = new Distribution1DHorizontalPanel(data);

		for (int i = 0; i < samples.size(); i++)
			panel.addSpecialValue(samples.get(i), new ShapeAttributes(colors.get(i), DisplayTools.standardStroke));

		SVGFrameTools.dropSVGFrame(panel, "InfoVisDistribution1DHorizontalPanel", 800, 200);

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
