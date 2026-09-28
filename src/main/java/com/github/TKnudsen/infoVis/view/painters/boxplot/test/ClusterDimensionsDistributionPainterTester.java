package com.github.TKnudsen.infoVis.view.painters.boxplot.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.boxplot.ClusterDimensionsDistributionPainter;

/**
 * Manual visual test for {@link ClusterDimensionsDistributionPainter}: builds a
 * synthetic centroid and a set of feature vectors scattered around it, and
 * renders the painter with a title and per-dimension names.
 *
 * @version 1.0
 * @since 2026
 */
public class ClusterDimensionsDistributionPainterTester {
	public static void main(String[] args) {
		// create data: 6 dimensions, values normalized to [0, 1]
		int dimensions = 6;
		int featureVectorCount = 40;

		double[] centroid = new double[dimensions];
		for (int i = 0; i < dimensions; i++)
			centroid[i] = Math.random();

		List<double[]> featureVectors = new ArrayList<>();
		for (int i = 0; i < featureVectorCount; i++) {
			double[] featureVector = new double[dimensions];
			for (int j = 0; j < dimensions; j++)
				featureVector[j] = Math.max(0, Math.min(1, centroid[j] + (Math.random() - 0.5) * 0.4));
			featureVectors.add(featureVector);
		}

		ClusterDimensionsDistributionPainter painter = new ClusterDimensionsDistributionPainter(centroid, featureVectors);
		painter.setTitle("Cluster Dimension Distribution");
		painter.setDimensionNames(new ArrayList<>(
				Arrays.asList("revenue", "margin", "growth", "risk", "liquidity", "leverage")));

		SVGFrameTools.dropSVGFrame(painter, "ClusterDimensionsDistributionPainterTester", 800, 400);
	}
}
