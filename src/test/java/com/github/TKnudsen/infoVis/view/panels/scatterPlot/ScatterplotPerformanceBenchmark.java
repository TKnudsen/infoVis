package com.github.TKnudsen.infoVis.view.panels.scatterPlot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.gpu.RenderMode;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotSpriteGPUPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotIndexedGPUPainter;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;

/**
 * <p>
 * Performance benchmark comparing the three scatterplot rendering
 * implementations -- {@link ScatterPlotPainter} (CPU/Java2D),
 * {@link ScatterPlotIndexedGPUPainter} and
 * {@link ScatterPlotSpriteGPUPainter} (both GPU/JOGL) -- across a range of
 * dataset sizes.
 * </p>
 *
 * <p>
 * Drives the three <b>painter</b> classes directly rather than their Swing
 * panels: for the two GPU painters, via an offscreen JOGL Pbuffer
 * ({@link GLOffscreenAutoDrawable}), and for the CPU painter, via a plain
 * {@link BufferedImage}'s {@link Graphics2D}. This isolates actual
 * rendering/query cost from Swing/AWT event-loop overhead, which is a
 * separate concern (already covered by the interactive demo testers in this
 * package) and would otherwise add noise unrelated to what this benchmark is
 * measuring.
 * </p>
 *
 * <p>
 * Metrics captured per (implementation, dataset size) cell:
 * <ul>
 * <li><b>construction</b> -- time to build the painter (data structuring,
 * position-encoding setup), excluding any GL context initialization.</li>
 * <li><b>firstFrame</b> -- time for the very first render call: for the GPU
 * painters this includes shader compilation and GL resource allocation; for
 * the CPU painter it includes the first (JIT-cold) draw pass.</li>
 * <li><b>steadyState (mean/median)</b> -- per-frame render time averaged
 * over several frames after a warmup period, the core throughput metric.</li>
 * <li><b>clickQuery</b> -- mean time per {@code getElementsAtPoint} call
 * across many random query points ("select event handling speed").</li>
 * <li><b>rectQuery</b> -- mean time per {@code getElementsInRectangle} call
 * across many random query rectangles, each covering roughly 5% of the
 * world area.</li>
 * <li><b>selectionChangeRender</b> -- time of the single render call
 * immediately following a {@code setSelectedFunction} change that marks
 * ~5% of points selected -- captures any selection-triggered re-batching
 * cost, comparable against the steady-state baseline.</li>
 * <li><b>colorChangeRender</b> -- time of the single render call
 * immediately following a {@code setColorEncodingFunction} change.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Both GPU painters already call {@code glFinish()} plus a full pixel
 * readback inside their {@code displayGL} methods (needed for their
 * Swing-blit workaround -- see the GPU scatterplot rehabilitation work this
 * benchmark follows), so timing {@code displayGL} directly captures true,
 * synchronized per-frame cost, not just command-queue submission time.
 * </p>
 *
 * <p>
 * Frame/query counts scale down as dataset size grows, to keep total
 * runtime bounded even at 1,000,000 points -- see {@link #warmupFrames},
 * {@link #measuredFrames}, {@link #queryCount}. Adjust those, the
 * {@link #SIZES} array, or add new metrics directly; this class is meant to
 * be a reusable, extensible test bench, not a one-off script.
 * </p>
 *
 * @version 1.00
 * @since 2026
 */
public class ScatterplotPerformanceBenchmark {

	private static final int[] SIZES = { 5_000, 25_000, 100_000, 250_000, 1_000_000 };
	private static final int VIEWPORT_W = 1000;
	private static final int VIEWPORT_H = 800;
	private static final long SEED = 42L;

	public static void main(String[] args) throws Exception {
		List<BenchResult> results = new ArrayList<>();

		for (int size : SIZES) {
			System.out.println("\n================ dataset size: " + size + " ================");

			List<Double[]> data = generateData(size, SEED);
			List<Color> colors = generateColors(size, SEED);

			results.add(benchmark("ScatterPlot (CPU)", size, data, colors, ScatterplotPerformanceBenchmark::cpuHandle));
			results.add(benchmark("ScatterPlotIndexedGPU", size, data, colors,
					ScatterplotPerformanceBenchmark::workingHandle));
			results.add(benchmark("ScatterPlotSpriteGPU", size, data, colors,
					ScatterplotPerformanceBenchmark::gljpanelHandle));
		}

		printTable(results);
		writeCsv(results, "scatterplot_performance_benchmark.csv");
	}

	// ==================== SCALING KNOBS ====================

	private static int warmupFrames(int size) {
		if (size <= 25_000)
			return 5;
		if (size <= 100_000)
			return 3;
		return 2;
	}

	private static int measuredFrames(int size) {
		if (size <= 25_000)
			return 20;
		if (size <= 100_000)
			return 10;
		if (size <= 250_000)
			return 6;
		return 3;
	}

	private static int queryCount(int size) {
		if (size <= 25_000)
			return 500;
		if (size <= 100_000)
			return 200;
		if (size <= 250_000)
			return 100;
		return 50;
	}

	// ==================== DATA GENERATION ====================

	/**
	 * Clustered layout (a handful of Gaussian clusters plus a uniform-random
	 * fill) rather than pure uniform random, so overplotting/density patterns
	 * are somewhat representative of real scatterplot data. Same seed for every
	 * implementation at a given size, so all three see identical data.
	 */
	private static List<Double[]> generateData(int count, long seed) {
		Random random = new Random(seed);
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

	private static List<Color> generateColors(int count, long seed) {
		Random random = new Random(seed + 1);
		List<Color> colors = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
			colors.add(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
		return colors;
	}

	// ==================== UNIFIED PAINTER HANDLE ====================

	/**
	 * Common surface the shared {@link #benchmark} loop drives, implemented once
	 * per painter family below so the actual measurement code is written only
	 * once instead of duplicated per implementation.
	 */
	private interface PainterHandle {
		/** One frame; must be synchronous (block until fully rendered). */
		void render();

		List<Double[]> getElementsAtPoint(Point p);

		List<Double[]> getElementsInRectangle(Rectangle2D r);

		void setSelectedFunction(Function<Double[], Boolean> f);

		void setColorEncodingFunction(Function<Double[], Color> f);

		/** Releases any GL/offscreen resources; no-op for the CPU handle. */
		void dispose();
	}

	private static PainterHandle cpuHandle(List<Double[]> data, Function<Double[], Color> colorMapping,
			Function<Double[], Double> mapX, Function<Double[], Double> mapY) {
		ScatterPlotPainter<Double[]> painter = new ScatterPlotPainter<>(data, colorMapping, mapX, mapY);
		painter.setRectangle(new Rectangle2D.Double(0, 0, VIEWPORT_W, VIEWPORT_H));

		BufferedImage image = new BufferedImage(VIEWPORT_W, VIEWPORT_H, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();

		return new PainterHandle() {
			@Override
			public void render() {
				painter.draw(g2);
			}

			@Override
			public List<Double[]> getElementsAtPoint(Point p) {
				return painter.getElementsAtPoint(p);
			}

			@Override
			public List<Double[]> getElementsInRectangle(Rectangle2D r) {
				return painter.getElementsInRectangle(r);
			}

			@Override
			public void setSelectedFunction(Function<Double[], Boolean> f) {
				painter.setSelectedFunction(f);
			}

			@Override
			public void setColorEncodingFunction(Function<Double[], Color> f) {
				painter.setColorEncodingFunction(f);
			}

			@Override
			public void dispose() {
				g2.dispose();
			}
		};
	}

	/** Shared offscreen Pbuffer setup for both GPU painter families. */
	private static final class OffscreenGL {
		final GLOffscreenAutoDrawable drawable;
		final GLContext context;

		OffscreenGL() {
			GLProfile.initSingleton();
			GLProfile profile = GLProfile.getMaxProgrammableCore(true);
			GLCapabilities caps = new GLCapabilities(profile);
			caps.setOnscreen(false);
			caps.setPBuffer(true);
			caps.setDoubleBuffered(false);

			GLDrawableFactory factory = GLDrawableFactory.getFactory(profile);
			drawable = factory.createOffscreenAutoDrawable(null, caps, null, VIEWPORT_W, VIEWPORT_H);
			drawable.display();
			context = drawable.getContext();
			context.makeCurrent();
		}

		void destroy() {
			GL3 gl = context.getGL().getGL3();
			gl.glFinish();
			context.release();
			drawable.destroy();
		}
	}

	private static PainterHandle workingHandle(List<Double[]> data, Function<Double[], Color> colorMapping,
			Function<Double[], Double> mapX, Function<Double[], Double> mapY) {
		OffscreenGL gl = new OffscreenGL();
		ScatterPlotIndexedGPUPainter<Double[]> painter = new ScatterPlotIndexedGPUPainter<>(data, colorMapping, mapX,
				mapY);
		painter.setRenderMode(RenderMode.GPU);

		Rectangle2D cr = new Rectangle2D.Double(0, 0, VIEWPORT_W, VIEWPORT_H);
		painter.setRectangle(cr);
		painter.initGL(gl.drawable);
		painter.reshapeGL(VIEWPORT_W, VIEWPORT_H);

		return new PainterHandle() {
			@Override
			public void render() {
				painter.displayGL(gl.drawable, cr);
			}

			@Override
			public List<Double[]> getElementsAtPoint(Point p) {
				return painter.getElementsAtPoint(p);
			}

			@Override
			public List<Double[]> getElementsInRectangle(Rectangle2D r) {
				return painter.getElementsInRectangle(r);
			}

			@Override
			public void setSelectedFunction(Function<Double[], Boolean> f) {
				painter.setSelectedFunction(f);
			}

			@Override
			public void setColorEncodingFunction(Function<Double[], Color> f) {
				painter.setColorEncodingFunction(f);
			}

			@Override
			public void dispose() {
				gl.destroy();
			}
		};
	}

	private static PainterHandle gljpanelHandle(List<Double[]> data, Function<Double[], Color> colorMapping,
			Function<Double[], Double> mapX, Function<Double[], Double> mapY) {
		OffscreenGL gl = new OffscreenGL();
		ScatterPlotSpriteGPUPainter<Double[]> painter = new ScatterPlotSpriteGPUPainter<>(data, colorMapping,
				mapX, mapY);
		painter.setRenderMode(RenderMode.GPU);

		Rectangle2D cr = new Rectangle2D.Double(0, 0, VIEWPORT_W, VIEWPORT_H);
		painter.setRectangle(cr);
		painter.initGL(gl.drawable);
		painter.reshapeGL(gl.drawable, 0, 0, VIEWPORT_W, VIEWPORT_H);

		return new PainterHandle() {
			@Override
			public void render() {
				painter.displayGL(gl.drawable);
			}

			@Override
			public List<Double[]> getElementsAtPoint(Point p) {
				return painter.getElementsAtPoint(p);
			}

			@Override
			public List<Double[]> getElementsInRectangle(Rectangle2D r) {
				return painter.getElementsInRectangle(r);
			}

			@Override
			public void setSelectedFunction(Function<Double[], Boolean> f) {
				painter.setSelectedFunction(f);
			}

			@Override
			public void setColorEncodingFunction(Function<Double[], Color> f) {
				painter.setColorEncodingFunction(f);
			}

			@Override
			public void dispose() {
				gl.destroy();
			}
		};
	}

	// ==================== SHARED BENCHMARK LOOP ====================

	private interface HandleFactory {
		PainterHandle create(List<Double[]> data, Function<Double[], Color> colorMapping,
				Function<Double[], Double> mapX, Function<Double[], Double> mapY);
	}

	private static BenchResult benchmark(String impl, int size, List<Double[]> data, List<Color> colors,
			HandleFactory factory) {
		System.out.println("--- " + impl + " ---");

		Map<Double[], Color> colorByPoint = new IdentityHashMap<>(size);
		for (int i = 0; i < size; i++)
			colorByPoint.put(data.get(i), colors.get(i));
		Function<Double[], Color> colorMapping = colorByPoint::get;
		Function<Double[], Double> mapX = p -> p[0];
		Function<Double[], Double> mapY = p -> p[1];

		long t0 = System.nanoTime();
		PainterHandle handle = factory.create(data, colorMapping, mapX, mapY);
		double constructionMs = elapsedMs(t0);

		try {
			long tFirst = System.nanoTime();
			handle.render();
			double firstFrameMs = elapsedMs(tFirst);

			int warmup = warmupFrames(size);
			int measured = measuredFrames(size);
			for (int i = 0; i < warmup; i++)
				handle.render();

			double[] frameTimes = new double[measured];
			for (int i = 0; i < measured; i++) {
				long t = System.nanoTime();
				handle.render();
				frameTimes[i] = elapsedMs(t);
			}

			int queries = queryCount(size);
			Random qr = new Random(SEED + 2);

			double clickTotalUs = 0;
			for (int i = 0; i < queries; i++) {
				Point p = new Point(qr.nextInt(VIEWPORT_W), qr.nextInt(VIEWPORT_H));
				long t = System.nanoTime();
				handle.getElementsAtPoint(p);
				clickTotalUs += elapsedUs(t);
			}

			double rectTotalUs = 0;
			for (int i = 0; i < queries; i++) {
				double rw = VIEWPORT_W * 0.22, rh = VIEWPORT_H * 0.22; // ~5% of area
				double rx = qr.nextDouble() * (VIEWPORT_W - rw);
				double ry = qr.nextDouble() * (VIEWPORT_H - rh);
				Rectangle2D.Double rect = new Rectangle2D.Double(rx, ry, rw, rh);
				long t = System.nanoTime();
				handle.getElementsInRectangle(rect);
				rectTotalUs += elapsedUs(t);
			}

			Set<Double[]> selected = new HashSet<>();
			for (int i = 0; i < data.size(); i += 20)
				selected.add(data.get(i));
			long tSel = System.nanoTime();
			handle.setSelectedFunction(selected::contains);
			handle.render();
			double selectionChangeMs = elapsedMs(tSel);

			long tColor = System.nanoTime();
			handle.setColorEncodingFunction(p -> Color.MAGENTA);
			handle.render();
			double colorChangeMs = elapsedMs(tColor);

			BenchResult result = new BenchResult(impl, size, constructionMs, firstFrameMs, mean(frameTimes),
					median(frameTimes), clickTotalUs / queries, rectTotalUs / queries, selectionChangeMs,
					colorChangeMs);
			System.out.println(result);
			return result;
		} finally {
			handle.dispose();
		}
	}

	// ==================== TIMING HELPERS ====================

	private static double elapsedMs(long startNanos) {
		return (System.nanoTime() - startNanos) / 1_000_000.0;
	}

	private static double elapsedUs(long startNanos) {
		return (System.nanoTime() - startNanos) / 1_000.0;
	}

	private static double mean(double[] values) {
		double sum = 0;
		for (double v : values)
			sum += v;
		return sum / values.length;
	}

	private static double median(double[] values) {
		double[] sorted = values.clone();
		java.util.Arrays.sort(sorted);
		int n = sorted.length;
		return (n % 2 == 0) ? (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0 : sorted[n / 2];
	}

	// ==================== OUTPUT ====================

	private record BenchResult(String impl, int size, double constructionMs, double firstFrameMs,
			double steadyStateMeanMs, double steadyStateMedianMs, double clickQueryUs, double rectQueryUs,
			double selectionChangeMs, double colorChangeMs) {

		@Override
		public String toString() {
			return String.format(Locale.ROOT,
					"  construction=%.2fms  firstFrame=%.2fms  steadyState(mean/median)=%.2f/%.2fms  "
							+ "clickQuery=%.1fus  rectQuery=%.1fus  selectionChangeRender=%.2fms  colorChangeRender=%.2fms",
					constructionMs, firstFrameMs, steadyStateMeanMs, steadyStateMedianMs, clickQueryUs, rectQueryUs,
					selectionChangeMs, colorChangeMs);
		}
	}

	private static void printTable(List<BenchResult> results) {
		System.out.println("\n\n================ SUMMARY ================");
		System.out.printf(Locale.ROOT, "%-24s %10s %12s %12s %14s %16s %12s %12s %14s %12s%n", "implementation",
				"size", "constr(ms)", "1stFrame(ms)", "steady mean(ms)", "steady median(ms)", "click(us)",
				"rect(us)", "selChg(ms)", "colChg(ms)");
		for (BenchResult r : results) {
			System.out.printf(Locale.ROOT, "%-24s %10d %12.2f %12.2f %14.2f %16.2f %12.1f %12.1f %14.2f %12.2f%n",
					r.impl(), r.size(), r.constructionMs(), r.firstFrameMs(), r.steadyStateMeanMs(),
					r.steadyStateMedianMs(), r.clickQueryUs(), r.rectQueryUs(), r.selectionChangeMs(),
					r.colorChangeMs());
		}
	}

	private static void writeCsv(List<BenchResult> results, String path) {
		try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
			out.println(
					"implementation,size,construction_ms,firstFrame_ms,steadyState_mean_ms,steadyState_median_ms,clickQuery_us,rectQuery_us,selectionChangeRender_ms,colorChangeRender_ms");
			for (BenchResult r : results) {
				out.printf(Locale.ROOT, "%s,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f%n", r.impl(), r.size(),
						r.constructionMs(), r.firstFrameMs(), r.steadyStateMeanMs(), r.steadyStateMedianMs(),
						r.clickQueryUs(), r.rectQueryUs(), r.selectionChangeMs(), r.colorChangeMs());
			}
			System.out.println("\nWrote " + new java.io.File(path).getAbsolutePath());
		} catch (IOException e) {
			System.err.println("Failed to write CSV: " + e.getMessage());
		}
	}
}
