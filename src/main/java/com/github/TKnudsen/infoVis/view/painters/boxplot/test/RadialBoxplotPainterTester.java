package com.github.TKnudsen.infoVis.view.painters.boxplot.test;

import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.boxplot.RadialBoxplotPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.panels.QuadraticPanel;

/**
 * Manual visual test for {@link RadialBoxplotPainter}: builds a synthetic
 * dataset and renders the painter inside a {@link QuadraticPanel} so the
 * concentric rings stay circular.
 *
 * @version 1.0
 * @since 2026
 */
public class RadialBoxplotPainterTester {
	public static void main(String[] args) {
		// create data
		List<Double> points = new ArrayList<>();
		int count = 150;

		for (int i = 0; i < count; i++) {
			points.add(Math.random() * 2);
			points.add(0.3 + Math.random() * 0.2);
		}

		RadialBoxplotPainter painter = new RadialBoxplotPainter(DataConversion.toPrimitives(points));

		QuadraticPanel panel = new QuadraticPanel(new InfoVisChartPanel(painter));

		SVGFrameTools.dropSVGFrame(panel, "RadialBoxplotPainterTester", 800, 400);
	}
}
