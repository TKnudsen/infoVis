package com.github.TKnudsen.infoVis.view.painters.scatterplot.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ConvexHullPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;

/**
 * Standalone interactive demo of {@link ConvexHullPainter} layered behind a
 * {@link ScatterPlotPainter}: three synthetic clusters, each colored and
 * outlined by its own convex hull, over a handful of unclustered noise
 * points (which get no hull, since a hull's group must have at least 3
 * members).
 *
 * @version 1.0
 * @since 2026
 */
public class ConvexHullPainterTester {

	public static void main(String[] args) {
		Random random = new Random(42);

		List<Double[]> data = new ArrayList<>();
		List<Color> colors = new ArrayList<>();

		addCluster(data, colors, random, 40, 0.2, 0.2, 0.08, Color.RED);
		addCluster(data, colors, random, 40, 0.8, 0.2, 0.08, Color.BLUE);
		addCluster(data, colors, random, 40, 0.5, 0.8, 0.08, Color.GREEN.darker());

		// a couple of noise points in their own singleton "groups" -- must not
		// produce hulls (fewer than 3 points per group)
		data.add(new Double[] { 0.5, 0.5 });
		colors.add(Color.GRAY);

		ScatterPlotPainter<Double[]> scatterPlotPainter = new ScatterPlotPainter<>(data,
				new ColorEncodingFunction<>(data, colors), p -> p[0], p -> p[1]);

		// the color itself doubles as the group key here since data/colors are
		// built in lockstep above; a real consumer would map each element to its
		// own class label instead.
		ConvexHullPainter<Double[], Color> convexHullPainter = new ConvexHullPainter<>(data,
				d -> colors.get(data.indexOf(d)), p -> p[0], p -> p[1], color -> color);

		InfoVisChartPanel panel = new InfoVisChartPanel();
		panel.addChartPainter(convexHullPainter);
		panel.addChartPainter(scatterPlotPainter);

		SwingUtilities.invokeLater(() -> SVGFrameTools.dropSVGFrame(panel, "ConvexHullPainter", 500, 500));
	}

	private static void addCluster(List<Double[]> data, List<Color> colors, Random random, int count, double centerX,
			double centerY, double spread, Color color) {
		for (int i = 0; i < count; i++) {
			double x = centerX + (random.nextDouble() - 0.5) * 2 * spread;
			double y = centerY + (random.nextDouble() - 0.5) * 2 * spread;
			data.add(new Double[] { x, y });
			colors.add(color);
		}
	}

}
