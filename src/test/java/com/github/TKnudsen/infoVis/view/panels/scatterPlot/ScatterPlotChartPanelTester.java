package com.github.TKnudsen.infoVis.view.panels.scatterPlot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.interaction.handlers.LassoSelectionHandler;
import com.github.TKnudsen.infoVis.view.interaction.handlers.MouseButton;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlot;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlots;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;

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
 * Scatter plot chart tester
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class ScatterPlotChartPanelTester {

	public static void main(String[] args) {

		// create data
		List<Double[]> points = new ArrayList<>();
		List<Color> colors = new ArrayList<>();
		List<String> labels = new ArrayList<>();

		Random random = new Random();

		int count = 500;
		for (int i = 0; i < count; i++) {
			points.add(new Double[] { random.nextDouble() * 1000, random.nextDouble() * 1000 });
			colors.add(ColorTools.randomColor());
			Character c = new Character((char) (random.nextInt(26) + 65));
			labels.add(c.toString());
		}

		for (int i = 0; i < count; i++) {
			points.add(new Double[] { random.nextDouble() * 100, random.nextDouble() * 1000 });
			colors.add(ColorTools.randomColor());
			Character c = new Character((char) (random.nextInt(26) + 65));
			labels.add(c.toString());
		}

		for (int i = 0; i < count; i++) {
			points.add(new Double[] { random.nextDouble() * 1000, random.nextDouble() * 100 });
			colors.add(ColorTools.randomColor());
			Character c = new Character((char) (random.nextInt(26) + 65));
			labels.add(c.toString());
		}

		for (int i = 0; i < count * 0.2; i++) {
			points.add(new Double[] { 555 + random.nextDouble() * 100, 444 + random.nextDouble() * 100 });
			colors.add(ColorTools.randomColor());
			Character c = new Character((char) (random.nextInt(26) + 65));
			labels.add(c.toString());
		}

		ScatterPlot<Double[]> panel = ScatterPlots.createForDoubles(points, colors);
//		ScatterPlotChartPanel panel = new ScatterPlotChartPanel(points, colors);
		panel.setXAxisOverlay(true);
		panel.setYAxisOverlay(false);

		// SELECTION MODEL
		SelectionModel<Double[]> selectionModel = SelectionModels.create();

		// SELECTION HANDLER
		SelectionHandler<Double[]> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(panel);
		selectionHandler.setClickSelection(panel);
		selectionHandler.setRectangleSelection(panel);

		panel.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		panel.setSelectedFunction(new Function<Double[], Boolean>() {
			@Override
			public Boolean apply(Double[] t) {
				return selectionHandler.getSelectionModel().isSelected(t);
			}
		});

		// LASSO SELECTION (RIGHT MOUSE BUTTON)
		LassoSelectionHandler<Double[]> lassoSelectionHandler = new LassoSelectionHandler<>(selectionModel,
				MouseButton.RIGHT);
		lassoSelectionHandler.attachTo(panel);
		lassoSelectionHandler.setShapeSelection(panel);

		panel.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				lassoSelectionHandler.draw(g2);
			}
		});

		selectionModel.addSelectionListener(new LoggingSelectionListener<>());

		SVGFrameTools.dropSVGFrame(panel, "My first Scatterplot Panel", 800, 800);
	}

}
