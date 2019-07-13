package com.github.TKnudsen.infoVis.view.painters.scatterplot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;

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
 * Tests the scatter plot painter
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class ScatterPlotPainterTester {

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

		ScatterPlotPainter<Double[]> painter = ScatterPlotPainters.createForDoubles(points, colors);

		SVGFrameTools.dropSVGFrame(painter, "Scatterplot Painter Test", 800, 800);

		System.err.println(
				"ScatterPlotPainterTester: use a panel variant when interaction is relevant (see e.g., InfoVisScatterPlotChartPanelTester.java)");
	}

}
