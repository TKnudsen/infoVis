package com.github.TKnudsen.infoVis.view.panels.scatterPlot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.gpu.PerformanceLogger;
import com.github.TKnudsen.infoVis.view.gpu.RenderMode;
import com.github.TKnudsen.infoVis.view.interaction.handlers.LassoSelectionHandler;
import com.github.TKnudsen.infoVis.view.interaction.handlers.MouseButton;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotSpriteGPUPainter;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlotIndexedGPU;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlots;

import de.javagl.selection.LoggingSelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Comprehensive scatter plot tester with CPU vs GPU performance comparison.
 * Tests various dataset sizes and rendering modes to assess performance
 * characteristics and identify optimal rendering strategies.
 * </p>
 * 
 * <p>
 * Usage:
 * <ul>
 * <li>Run main() for interactive demo with default dataset</li>
 * <li>Call testSmallDataset() for basic CPU rendering test</li>
 * <li>Call testMediumDataset() for CPU vs GPU comparison</li>
 * <li>Call testLargeDataset() for GPU performance test</li>
 * <li>Call runComprehensiveBenchmark() for full performance analysis</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2026 Juergen Bernard,
 * https://github.com/TKnudsen/InfoVis<br>
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.00
 */
public class ScatterPlotGPUTester {

	// ==================== MAIN - INTERACTIVE DEMO ====================

	public static void main(String[] args) {
		// Must be set before any AWT/Swing init: D3D pipeline conflicts with GLJPanel FBO blit
		System.setProperty("sun.java2d.d3d", "false");
		System.setProperty("sun.java2d.noddraw", "true");

		System.out.println("=== InfoVis ScatterPlot Interactive Demo ===\n");

		List<Double[]> points = generateDemoDataset();
		List<Color> colors = generateColors(points.size(), new Random(42));

		System.out.println("Generated " + points.size() + " data points");
		System.out.println("Creating scatter plot panel...\n");

		ScatterPlotIndexedGPU<Double[]> panel = ScatterPlots.createForDoublesGPU(points, colors);
		panel.setXAxisOverlay(true);
		panel.setYAxisOverlay(false);
		panel.autoConfigureForDataSize();
		panel.enablePerformanceLogging();

		System.out.println("Render mode: " + panel.getEffectiveRenderMode());
		System.out.println("Data size: " + panel.getDataSize());

		// Setup selection
		SelectionModel<Double[]> selectionModel = SelectionModels.create();

		SelectionHandler<Double[]> selectionHandler = new SelectionHandler<Double[]>(selectionModel);
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
				return Boolean.valueOf(selectionHandler.getSelectionModel().isSelected(t));
			}
		});

		LassoSelectionHandler<Double[]> lassoSelectionHandler = new LassoSelectionHandler<Double[]>(selectionModel,
				MouseButton.RIGHT);
		lassoSelectionHandler.attachTo(panel);
		lassoSelectionHandler.setShapeSelection(panel);

		panel.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				lassoSelectionHandler.draw(g2);
			}
		});

		selectionModel.addSelectionListener(new LoggingSelectionListener<Double[]>());

		SVGFrameTools.dropSVGFrame(panel, "ScatterPlot Demo - " + panel.getEffectiveRenderMode() + " Rendering", 800,
				800);

		// Print performance stats a few seconds in, off the EDT so it doesn't block
		// rendering/interaction.
		new Thread(() -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			panel.printConfiguration();
		}, "ScatterPlotGPUTester-config-report").start();
	}

	// ==================== DATA GENERATION ====================

	private static List<Double[]> generateDemoDataset() {
		List<Double[]> points = new ArrayList<Double[]>();
		Random random = new Random(42);
		int count = 500;

		for (int i = 0; i < count; i++) {
			points.add(new Double[] { Double.valueOf(random.nextDouble() * 1000),
					Double.valueOf(random.nextDouble() * 1000) });
		}

		for (int i = 0; i < count; i++) {
			points.add(new Double[] { Double.valueOf(random.nextDouble() * 100),
					Double.valueOf(random.nextDouble() * 1000) });
		}

		for (int i = 0; i < count; i++) {
			points.add(new Double[] { Double.valueOf(random.nextDouble() * 1000),
					Double.valueOf(random.nextDouble() * 100) });
		}

		for (int i = 0; i < (int) (count * 0.2); i++) {
			points.add(new Double[] { Double.valueOf(555 + random.nextDouble() * 100),
					Double.valueOf(444 + random.nextDouble() * 100) });
		}

		return points;
	}

	private static List<Double[]> generateRandomPoints(int count, Random random) {
		List<Double[]> points = new ArrayList<Double[]>();

		for (int i = 0; i < count; i++) {
			double x = random.nextDouble() * 1000;
			double y = random.nextDouble() * 1000;
			points.add(new Double[] { Double.valueOf(x), Double.valueOf(y) });
		}

		return points;
	}

	private static List<Color> generateColors(int count, Random random) {
		List<Color> colors = new ArrayList<Color>();
		for (int i = 0; i < count; i++) {
			colors.add(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
		}
		return colors;
	}

	// ==================== PERFORMANCE TESTS ====================

	public static void testSmallDataset() {
		System.out.println("\n" + repeatString("=", 60));
		System.out.println("TEST: Small Dataset (1,800 points)");
		System.out.println(repeatString("=", 60));

		List<Double[]> points = generateDemoDataset();
		List<Color> colors = generateColors(points.size(), new Random(42));

		System.out.println("\nTesting CPU rendering...");
		PerformanceLogger cpuLogger = testRenderMode(points, colors, RenderMode.CPU, 30, 60);

		System.out.println("\n--- Results ---");
		System.out.println(cpuLogger.getStatisticsReport());
		System.out.println("\nConclusion: For small datasets, CPU rendering is optimal.");
	}

	public static void testMediumDataset() {
		System.out.println("\n" + repeatString("=", 60));
		System.out.println("TEST: Medium Dataset (10,000 points)");
		System.out.println(repeatString("=", 60));

		Random random = new Random(42);
		List<Double[]> points = generateRandomPoints(10000, random);
		List<Color> colors = generateColors(points.size(), random);

		System.out.println("\nTesting CPU rendering...");
		PerformanceLogger cpuLogger = testRenderMode(points, colors, RenderMode.CPU, 30, 120);

		System.out.println("\nTesting GPU rendering (windowed)...");
		PerformanceLogger gpuLogger = testRenderModeWindowed(points, colors, RenderMode.GPU, 5000);

		System.out.println("\n--- Results ---");
		System.out.println("\n" + cpuLogger.getStatisticsReport());
		if (gpuLogger != null) {
			System.out.println("\n" + gpuLogger.getStatisticsReport());
			cpuLogger.compareWith(gpuLogger);
		}

		System.out.println("\nConclusion: At ~10K points, GPU starts showing advantages.");
	}

	public static void testLargeDataset() {
		System.out.println("\n" + repeatString("=", 60));
		System.out.println("TEST: Large Dataset (50,000 points)");
		System.out.println(repeatString("=", 60));

		Random random = new Random(42);
		List<Double[]> points = generateRandomPoints(50000, random);
		List<Color> colors = generateColors(points.size(), random);

		System.out.println("\nTesting CPU rendering...");
		PerformanceLogger cpuLogger = testRenderMode(points, colors, RenderMode.CPU, 20, 100);

		System.out.println("\nTesting GPU rendering (windowed)...");
		PerformanceLogger gpuLogger = testRenderModeWindowed(points, colors, RenderMode.GPU, 5000);

		System.out.println("\n--- Results ---");
		System.out.println("\n" + cpuLogger.getStatisticsReport());
		if (gpuLogger != null) {
			System.out.println("\n" + gpuLogger.getStatisticsReport());
			cpuLogger.compareWith(gpuLogger);
		}

		System.out.println("\nConclusion: GPU rendering significantly faster for large datasets.");
	}

	// ==================== WINDOWED GPU TEST ====================

	/**
	 * Test GPU rendering in a real window with proper OpenGL context. This is
	 * necessary because GPU rendering requires a graphics device.
	 */
	private static PerformanceLogger testRenderModeWindowed(final List<Double[]> points, final List<Color> colors,
			final RenderMode mode, final int testDurationMs) {

		System.out.println("  Opening test window...");

		final PerformanceLogger[] loggerHolder = new PerformanceLogger[1];
		final boolean[] testComplete = new boolean[] { false };

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Create scatter plot
				final ScatterPlotIndexedGPU<Double[]> panel = ScatterPlots.createForDoublesGPU(points, colors);
				panel.setRenderMode(mode);
				panel.enablePerformanceLogging();

				// Create window
				final JFrame frame = new JFrame("GPU Performance Test - " + points.size() + " points");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

				// Add info panel
				final JLabel infoLabel = new JLabel("Testing GPU performance... ", JLabel.CENTER);
				infoLabel.setFont(infoLabel.getFont().deriveFont(16f));

				JPanel mainPanel = new JPanel(new BorderLayout());
				mainPanel.add(infoLabel, BorderLayout.NORTH);
				mainPanel.add(panel, BorderLayout.CENTER);

				frame.setContentPane(mainPanel);
				frame.setSize(1920, 1080);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);

				System.out.println("  Window opened, render mode: " + panel.getEffectiveRenderMode());

				// Update info label periodically
				final Timer updateTimer = new Timer(500, null);
				updateTimer.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent e) {
						PerformanceLogger logger = panel.getPerformanceLogger();
						if (logger != null && logger.getFrameCount() > 0) {
							infoLabel.setText(String.format("GPU Test: %d frames @ %.1f FPS (target: 60 FPS)",
									Long.valueOf(logger.getFrameCount()), Double.valueOf(logger.getAverageFPS())));
						}
					}
				});
				updateTimer.start();

				// Close window after test duration
				final Timer closeTimer = new Timer(testDurationMs, null);
				closeTimer.setRepeats(false);
				closeTimer.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent e) {
						updateTimer.stop();

						PerformanceLogger logger = panel.getPerformanceLogger();
						if (logger != null) {
							System.out.println("  GPU test complete: " + logger.getFrameCount() + " frames @ "
									+ String.format("%.1f", Double.valueOf(logger.getAverageFPS())) + " FPS");
							loggerHolder[0] = logger;
						} else {
							System.err.println("  ERROR: No performance data collected!");
						}

						frame.dispose();
						panel.dispose();

						synchronized (testComplete) {
							testComplete[0] = true;
							testComplete.notify();
						}
					}
				});
				closeTimer.start();
			}
		});

		// Wait for test to complete
		synchronized (testComplete) {
			while (!testComplete[0]) {
				try {
					testComplete.wait(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		return loggerHolder[0];
	}

	// ==================== DIRECT PAINTER BENCHMARK (CPU Only) ====================

	/**
	 * Test CPU rendering by directly benchmarking the painter's draw() method. This
	 * is fast and doesn't require a window.
	 */
	private static PerformanceLogger testRenderMode(List<Double[]> points, List<Color> colors, RenderMode mode,
			int warmupFrames, int measurementFrames) {

		// Create the painter directly
		Function<Double[], Double> xMapping = new Function<Double[], Double>() {
			@Override
			public Double apply(Double[] p) {
				return p[0];
			}
		};

		Function<Double[], Double> yMapping = new Function<Double[], Double>() {
			@Override
			public Double apply(Double[] p) {
				return p[1];
			}
		};

		// Precompute a point-identity -> color lookup once, instead of an O(n)
		// points.indexOf(p) call inside the per-point, per-frame color mapping below
		// (which would make this "CPU" benchmark accidentally O(n^2) per frame).
		final java.util.Map<Double[], Color> colorByPoint = new java.util.IdentityHashMap<>(points.size());
		for (int i = 0; i < points.size() && i < colors.size(); i++)
			colorByPoint.put(points.get(i), colors.get(i));

		Function<Double[], Color> colorMapping = new Function<Double[], Color>() {
			@Override
			public Color apply(Double[] p) {
				return colorByPoint.getOrDefault(p, Color.GRAY);
			}
		};

		ScatterPlotSpriteGPUPainter<Double[]> painter = new ScatterPlotSpriteGPUPainter<Double[]>(points,
				colorMapping, xMapping, yMapping);

		painter.setRenderMode(mode);
		painter.enablePerformanceLogging();

		Rectangle2D chartRect = new Rectangle2D.Double(0, 0, 1920, 1080);
		painter.setRectangle(chartRect);

		System.out.println("  Render mode: " + painter.getEffectiveRenderMode());

		BufferedImage img = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Warmup
		System.out.print("  Warming up");
		for (int i = 0; i < warmupFrames; i++) {
			painter.draw(g2);
			if (i % 10 == 0) {
				System.out.print(".");
			}
		}
		System.out.println(" done");

		PerformanceLogger logger = painter.getPerformanceLogger();
		if (logger != null && logger.getFrameCount() > 0) {
			System.out.println("  Warmup: " + logger.getFrameCount() + " frames @ "
					+ String.format("%.1f", Double.valueOf(logger.getAverageFPS())) + " FPS");
		}

		painter.resetPerformanceStatistics();

		// Measurement
		System.out.print("  Measuring");
		for (int i = 0; i < measurementFrames; i++) {
			painter.draw(g2);
			if (i % 20 == 0) {
				System.out.print(".");
			}
		}
		System.out.println(" done");

		if (logger != null && logger.getFrameCount() > 0) {
			System.out.println("  Result: " + logger.getFrameCount() + " frames @ "
					+ String.format("%.1f", Double.valueOf(logger.getAverageFPS())) + " FPS");
		}

		g2.dispose();
		painter.dispose();

		return logger;
	}

	// ==================== COMPREHENSIVE BENCHMARK ====================

	public static void runComprehensiveBenchmark() {
		System.out.println("\n" + repeatString("=", 80));
		System.out.println("COMPREHENSIVE PERFORMANCE BENCHMARK");
		System.out.println("CPU: Direct painter benchmarking | GPU: Windowed testing");
		System.out.println(repeatString("=", 80));

		TestConfig[] configs = new TestConfig[] { new TestConfig(1000, "Tiny", true, false),
				new TestConfig(5000, "Small", true, true), new TestConfig(10000, "Medium", true, true),
				new TestConfig(25000, "Large", true, true), new TestConfig(50000, "Very Large", true, true),
				new TestConfig(100000, "Huge", false, true) };

		List<BenchmarkResult> results = new ArrayList<BenchmarkResult>();

		for (int idx = 0; idx < configs.length; idx++) {
			TestConfig config = configs[idx];
			System.out.println("\n" + repeatString("-", 80));
			System.out.println("Testing: " + config);
			System.out.println(repeatString("-", 80));

			Random random = new Random(42);
			List<Double[]> points = generateRandomPoints(config.pointCount, random);
			List<Color> colors = generateColors(points.size(), random);

			PerformanceLogger cpuLogger = null;
			PerformanceLogger gpuLogger = null;

			if (config.testCPU) {
				System.out.println("\n[CPU Test]");
				cpuLogger = testRenderMode(points, colors, RenderMode.CPU, config.warmupFrames,
						config.measurementFrames);
			}

			if (config.testGPU) {
				System.out.println("\n[GPU Test - Windowed]");
				gpuLogger = testRenderModeWindowed(points, colors, RenderMode.GPU, 5000);
			}

			results.add(new BenchmarkResult(config, cpuLogger, gpuLogger));
		}

		printBenchmarkSummary(results);
	}

	private static class BenchmarkResult {
		final TestConfig config;
		final PerformanceLogger cpuLogger;
		final PerformanceLogger gpuLogger;

		BenchmarkResult(TestConfig config, PerformanceLogger cpuLogger, PerformanceLogger gpuLogger) {
			this.config = config;
			this.cpuLogger = cpuLogger;
			this.gpuLogger = gpuLogger;
		}
	}

	private static void printBenchmarkSummary(List<BenchmarkResult> results) {
		System.out.println("\n\n" + repeatString("=", 80));
		System.out.println("BENCHMARK SUMMARY");
		System.out.println(repeatString("=", 80));
		System.out.println();
		System.out.printf("%-15s | %10s | %10s | %10s | %10s%n", "Dataset", "Points", "CPU FPS", "GPU FPS", "Speedup");
		System.out.println(repeatString("-", 80));

		for (int i = 0; i < results.size(); i++) {
			BenchmarkResult result = results.get(i);
			String cpuFps = result.cpuLogger != null
					? String.format("%.1f", Double.valueOf(result.cpuLogger.getAverageFPS()))
					: "N/A";
			String gpuFps = result.gpuLogger != null
					? String.format("%.1f", Double.valueOf(result.gpuLogger.getAverageFPS()))
					: "N/A";

			String speedup = "N/A";
			if (result.cpuLogger != null && result.gpuLogger != null) {
				double ratio = result.gpuLogger.getAverageFPS() / result.cpuLogger.getAverageFPS();
				speedup = String.format("%.2fx", Double.valueOf(ratio));
			}

			System.out.printf("%-15s | %,10d | %10s | %10s | %10s%n", result.config.description,
					Integer.valueOf(result.config.pointCount), cpuFps, gpuFps, speedup);
		}

		System.out.println(repeatString("=", 80));
		System.out.println("\nRecommendations:");
		System.out.println("  - < 5,000 points: Use CPU rendering (less overhead)");
		System.out.println("  - 5,000 - 10,000 points: Either mode works well");
		System.out.println("  - > 10,000 points: GPU rendering recommended");
		System.out.println("  - > 50,000 points: GPU rendering essential");
	}

	public static void runAllTests() {
		testSmallDataset();
		testMediumDataset();
		testLargeDataset();
	}

	private static String repeatString(String str, int count) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append(str);
		}
		return sb.toString();
	}
}