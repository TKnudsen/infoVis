package com.github.TKnudsen.infoVis.view.panels.scatterPlot;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.tools.ColorTools;

/**
 * <p>
 * Renders the same synthetic dataset used by {@link ScatterPlotTester} --
 * three differently-shaped uniform-random regions plus a small dense
 * overplotted cluster -- through all three scatterplot implementations side
 * by side, with linked click/rectangle/lasso (right mouse button) selection
 * across all three, via {@link ScatterPlotSideBySideTesters}.
 * </p>
 *
 * <p>
 * {@link ScatterPlotTester} itself is unchanged and still useful as the
 * minimal single-panel (CPU-only) reference demo; this class reuses its
 * exact data recipe for a 3-way comparison instead.
 * </p>
 *
 * @version 1.00
 * @since 2026
 */
public class ScatterPlotSideBySideSyntheticTester {

	public static void main(String[] args) {
		List<Double[]> points = new ArrayList<>();
		Map<Double[], Color> colorByPoint = new IdentityHashMap<>();

		Random random = new Random();
		int count = 500;

		for (int i = 0; i < count; i++) {
			Double[] p = new Double[] { random.nextDouble() * 1000, random.nextDouble() * 1000 };
			points.add(p);
			colorByPoint.put(p, ColorTools.randomColor());
		}

		for (int i = 0; i < count; i++) {
			Double[] p = new Double[] { random.nextDouble() * 100, random.nextDouble() * 1000 };
			points.add(p);
			colorByPoint.put(p, ColorTools.randomColor());
		}

		for (int i = 0; i < count; i++) {
			Double[] p = new Double[] { random.nextDouble() * 1000, random.nextDouble() * 100 };
			points.add(p);
			colorByPoint.put(p, ColorTools.randomColor());
		}

		for (int i = 0; i < count * 0.2; i++) {
			Double[] p = new Double[] { 555 + random.nextDouble() * 100, 444 + random.nextDouble() * 100 };
			points.add(p);
			colorByPoint.put(p, ColorTools.randomColor());
		}

		Function<Double[], Paint> colorMapping = colorByPoint::get;
		Function<Double[], Double> mapX = p -> p[0];
		Function<Double[], Double> mapY = p -> p[1];

		ScatterPlotSideBySideTesters.show("Synthetic dataset -- CPU vs. GPU (Indexed) vs. GPU (Sprite)", points,
				colorMapping, mapX, mapY, 480, 480);
	}
}
