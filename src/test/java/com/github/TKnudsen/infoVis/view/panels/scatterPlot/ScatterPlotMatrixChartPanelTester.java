package com.github.TKnudsen.infoVis.view.panels.scatterPlot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlotMatrixChartPanel;
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
 * Scatter plot matrix chart test
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class ScatterPlotMatrixChartPanelTester {

	public static void main(String[] args) {

		// create data
		List<Double[]> vectors = new ArrayList<>();
		List<Color> colors = new ArrayList<>();
		List<String> attributeNames = new ArrayList<>();
		int dim = 3;
		int count = 100;

		for (int i = 0; i < count; i++) {
			Double[] vector = new Double[dim];
			for (int d = 0; d < dim; d++)
				vector[d] = Math.random() * 2 + Math.random() * count * d % 3 - d % 2;
			vectors.add(vector);

			colors.add(ColorTools.randomColor());
		}

		for (int d = 0; d < dim; d++)
			attributeNames.add(d + 1 + "");

		ScatterPlotMatrixChartPanel panel = new ScatterPlotMatrixChartPanel(vectors, colors, attributeNames);

		// SELECTION MODEL, shared by every cell of the matrix
		SelectionModel<Double[]> selectionModel = SelectionModels.create();

		// CLICK + RECTANGLE + LASSO (RIGHT MOUSE BUTTON) SELECTION, wired per cell
		// since the cells -- not this panel -- actually receive mouse events
		panel.addInteraction(selectionModel, true, true, true);

		selectionModel.addSelectionListener(new LoggingSelectionListener<>());

		SVGFrameTools.dropSVGFrame(panel, "ScatterplotMatrixPainter Test Frame", 500, 500);
	}
}
