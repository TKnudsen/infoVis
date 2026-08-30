package com.github.TKnudsen.infoVis.view.panels.scatterPlot;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * <p>
 * Renders a large, clustered synthetic dataset through all three scatterplot
 * implementations side by side -- {@code ScatterPlot} (CPU),
 * {@code ScatterPlotIndexedGPU} and {@code ScatterPlotSpriteGPU} (both
 * GPU) -- with linked click/rectangle/lasso (right mouse button) selection
 * across all three, via {@link ScatterPlotSideBySideTesters}.
 * </p>
 *
 * <p>
 * Unlike {@link ScatterplotPerformanceBenchmark}, which measures exact
 * timings headlessly, this is a hands-on complement: interact with all
 * three panels (pan by resizing the window, click/rectangle/lasso-select)
 * and feel the difference directly -- at the default size the CPU panel
 * visibly lags on every selection while both GPU panels stay responsive.
 * </p>
 *
 * <p>
 * Dataset size defaults to 250,000 (one of the sizes exercised by the
 * dedicated benchmark, so the two are directly comparable) and can be
 * overridden via the first command-line argument, e.g.
 * {@code ScatterPlotSideBySideScalabilityTester 1000000}.
 * </p>
 *
 * @version 1.00
 * @since 2026
 */
public class ScatterPlotSideBySideScalabilityTester {

	private static final int DEFAULT_SIZE = 250_000;

	public static void main(String[] args) {
		int size = DEFAULT_SIZE;
		if (args.length > 0) {
			try {
				size = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Ignoring invalid size argument '" + args[0] + "', using default " + DEFAULT_SIZE);
			}
		}

		System.out.println("Generating " + size + " points...");
		List<Double[]> points = generateData(size);
		Map<Double[], Color> colorByPoint = generateColors(points);

		Function<Double[], Paint> colorMapping = colorByPoint::get;
		Function<Double[], Double> mapX = p -> p[0];
		Function<Double[], Double> mapY = p -> p[1];

		ScatterPlotSideBySideTesters.Panels<Double[]> panels = ScatterPlotSideBySideTesters.showAndReturnPanels(
				"Scalability (" + size + " points) -- CPU vs. GPU (Indexed) vs. GPU (Sprite)", points, colorMapping,
				mapX, mapY, 480, 480);

		panels.indexedGpuPanel.enablePerformanceLogging();
		panels.spriteGpuPanel.enablePerformanceLogging();

		System.out.println("Try clicking, rectangle-selecting, or right-mouse-button lasso-selecting in any panel -- "
				+ "the same selection appears in all three. Watch how each panel responds while you interact.");

		// Print performance stats a few seconds in, off the EDT so it doesn't block
		// rendering/interaction.
		new Thread(() -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			System.out.println("\n=== ScatterPlotIndexedGPU ===");
			panels.indexedGpuPanel.printConfiguration();
			System.out.println("\n=== ScatterPlotSpriteGPU ===");
			panels.spriteGpuPanel.printConfiguration();
		}, "ScatterPlotSideBySideScalabilityTester-config-report").start();
	}

	/**
	 * Clustered layout (a handful of Gaussian clusters plus a uniform-random
	 * fill) rather than pure uniform random, so overplotting/density patterns
	 * are somewhat representative of real scatterplot data -- the same recipe
	 * {@link ScatterplotPerformanceBenchmark} uses, for visual continuity with
	 * those numbers.
	 */
	private static List<Double[]> generateData(int count) {
		Random random = new Random();
		List<Double[]> data = new ArrayList<>(count);

		int clusterCount = 5;
		double[] clusterX = new double[clusterCount];
		double[] clusterY = new double[clusterCount];
		for (int c = 0; c < clusterCount; c++) {
			clusterX[c] = random.nextDouble() * 1000;
			clusterY[c] = random.nextDouble() * 1000;
		}

		for (int i = 0; i < count; i++) {
			double x, y;
			if (i % 3 == 0) {
				x = random.nextDouble() * 1000;
				y = random.nextDouble() * 1000;
			} else {
				int c = random.nextInt(clusterCount);
				x = clusterX[c] + random.nextGaussian() * 40;
				y = clusterY[c] + random.nextGaussian() * 40;
			}
			data.add(new Double[] { x, y });
		}
		return data;
	}

	private static Map<Double[], Color> generateColors(List<Double[]> points) {
		Random random = new Random();
		Map<Double[], Color> colorByPoint = new IdentityHashMap<>(points.size());
		for (Double[] p : points)
			colorByPoint.put(p, new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
		return colorByPoint;
	}
}
