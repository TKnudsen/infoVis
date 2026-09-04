package com.github.TKnudsen.infoVis.view.panels.distribution1D.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.KernelDensityChart;

/**
 * Manual demo for {@link KernelDensityChart}. Builds its own tiny Gaussian
 * mixture directly (no estimator dependency, e.g. DMandML's {@code
 * GaussianKernelDensityEstimator}) since the chart only cares about the
 * resulting (evaluationPoints, density) pair, not how it was computed.
 *
 * @since 2026
 */
public class KernelDensityChartTester {

	public static void main(String[] args) {
		List<Double> evaluationPoints = new ArrayList<>();
		List<Double> density = new ArrayList<>();

		for (double x = 0.0; x <= 1.0; x += 0.002) {
			double bump1 = gauss(x, 0.3, 0.05);
			double bump2 = gauss(x, 0.65, 0.08);

			evaluationPoints.add(x);
			density.add(bump1 + bump2);
		}

		KernelDensityChart chart = new KernelDensityChart(evaluationPoints, density, Color.CYAN);

		SVGFrameTools.dropSVGFrame(chart, "KernelDensityChart Tester", 1000, 400);
	}

	private static double gauss(double x, double mean, double variance) {
		double normalization = 1 / (variance * Math.sqrt(2 * Math.PI));
		return normalization * Math.exp(-Math.pow(x - mean, 2) / (2 * Math.pow(variance, 2)));
	}
}
