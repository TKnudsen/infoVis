package com.github.TKnudsen.infoVis.view.painters.scatterplot.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ButterflyHullsPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ConvexHullPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;

/**
 * Standalone interactive demo comparing {@link ConvexHullPainter} (straight
 * edges) against {@link ButterflyHullsPainter} (curved, concavity-recovering
 * edges) on the same concave, crescent-shaped point cloud -- the difference
 * is most visible along the crescent's inner (concave) side, where the plain
 * convex hull cuts straight across the empty region while the butterfly hull
 * curves inward to hug the actual data.
 *
 * @version 1.0
 * @since 2026
 */
public class HullPaintersTester {

	public static void main(String[] args) {
		Random random = new Random(3);

		List<Double[]> data = new ArrayList<>();
		List<Color> colors = new ArrayList<>();
		for (int i = 0; i < 150; i++) {
			double angle = random.nextDouble() * Math.PI * 1.4 + 0.2;
			double r = 0.25 + random.nextDouble() * 0.15;
			double x = 0.5 + r * Math.cos(angle);
			double y = 0.5 + r * Math.sin(angle);
			data.add(new Double[] { x, y });
			colors.add(Color.BLUE);
		}

		ScatterPlotPainter<Double[]> scatterPlotPainter = new ScatterPlotPainter<>(data,
				new ColorEncodingFunction<>(data, colors), p -> p[0], p -> p[1]);

		ConvexHullPainter<Double[], Color> convexHullPainter = new ConvexHullPainter<>(data, d -> Color.RED,
				p -> p[0], p -> p[1], color -> color);
		convexHullPainter.setFillAlpha(null);

		ButterflyHullsPainter<Double[], Color> butterflyHullsPainter = new ButterflyHullsPainter<>(data,
				d -> Color.GREEN.darker(), p -> p[0], p -> p[1], color -> color);
		butterflyHullsPainter.setRecovery(0.02);
		butterflyHullsPainter.setDepth(5);

		InfoVisChartPanel panel = new InfoVisChartPanel();
		panel.addChartPainter(butterflyHullsPainter);
		panel.addChartPainter(convexHullPainter);
		panel.addChartPainter(scatterPlotPainter);

		SwingUtilities.invokeLater(() -> SVGFrameTools.dropSVGFrame(panel, "ConvexHullPainter vs ButterflyHullsPainter", 500, 500));
	}

}
