package com.github.TKnudsen.infoVis.view.painters.boxplot.test;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.boxplot.ClusterDimensionsDistributionPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Manual visual test for {@link ClusterDimensionsDistributionPainter}: builds a
 * synthetic centroid and a set of feature vectors scattered around it, and
 * renders the painter with a title and per-dimension names. Two checkboxes
 * toggle the centroid label and the axis-endpoint labels independently.
 *
 * @version 1.1
 * @since 2026
 */
public class ClusterDimensionsDistributionPainterTester {
	public static void main(String[] args) {
		// create data: 6 dimensions, each with its own real-valued range --
		// the painter itself works in [0, 1] per dimension, so the real
		// values are generated first and normalized afterwards
		int dimensions = 6;
		int featureVectorCount = 40;

		double[] dimensionMin = { 0, -50, 0, 1, 100, 0 };
		double[] dimensionMax = { 500, 50, 1, 10, 10_000, 100 };

		double[] centroidReal = new double[dimensions];
		for (int i = 0; i < dimensions; i++)
			centroidReal[i] = dimensionMin[i] + Math.random() * (dimensionMax[i] - dimensionMin[i]);

		List<double[]> featureVectorsReal = new ArrayList<>();
		for (int i = 0; i < featureVectorCount; i++) {
			double[] featureVector = new double[dimensions];
			for (int j = 0; j < dimensions; j++) {
				double spread = (dimensionMax[j] - dimensionMin[j]) * 0.2;
				double value = centroidReal[j] + (Math.random() - 0.5) * 2 * spread;
				featureVector[j] = Math.max(dimensionMin[j], Math.min(dimensionMax[j], value));
			}
			featureVectorsReal.add(featureVector);
		}

		// per-dimension real-valued statistics -- required for the painter
		// to show its real-unit centroid label (red) and axis-endpoint
		// labels (dark gray); without it, both are silently suppressed
		StatisticsSupport[] globalStatisticsPerDimension = new StatisticsSupport[dimensions];
		for (int j = 0; j < dimensions; j++) {
			double[] column = new double[featureVectorsReal.size()];
			for (int i = 0; i < featureVectorsReal.size(); i++)
				column[i] = featureVectorsReal.get(i)[j];
			globalStatisticsPerDimension[j] = new StatisticsSupport(column);
		}

		double[] centroid = new double[dimensions];
		for (int j = 0; j < dimensions; j++)
			centroid[j] = (centroidReal[j] - dimensionMin[j]) / (dimensionMax[j] - dimensionMin[j]);

		List<double[]> featureVectors = new ArrayList<>();
		for (double[] real : featureVectorsReal) {
			double[] normalized = new double[dimensions];
			for (int j = 0; j < dimensions; j++)
				normalized[j] = (real[j] - dimensionMin[j]) / (dimensionMax[j] - dimensionMin[j]);
			featureVectors.add(normalized);
		}

		ClusterDimensionsDistributionPainter painter = new ClusterDimensionsDistributionPainter(centroid, featureVectors);
		painter.setTitle("Cluster Dimension Distribution");
		painter.setDimensionNames(new ArrayList<>(
				Arrays.asList("revenue", "margin", "growth", "risk", "liquidity", "leverage")));
		painter.setGlobalStatisticsPerDimension(globalStatisticsPerDimension);

		InfoVisChartPanel chartPanel = new InfoVisChartPanel();
		chartPanel.addChartPainter(painter);

		JCheckBox centroidLabelCheckBox = new JCheckBox("Centroid labels", painter.isShowCentroidLabel());
		centroidLabelCheckBox.addItemListener(e -> {
			painter.setShowCentroidLabel(centroidLabelCheckBox.isSelected());
			chartPanel.repaint();
		});

		JCheckBox axisEndpointLabelCheckBox = new JCheckBox("Axis-endpoint labels", painter.isShowAxisEndpointLabels());
		axisEndpointLabelCheckBox.addItemListener(e -> {
			painter.setShowAxisEndpointLabels(axisEndpointLabelCheckBox.isSelected());
			chartPanel.repaint();
		});

		JPanel controls = new JPanel();
		controls.add(centroidLabelCheckBox);
		controls.add(axisEndpointLabelCheckBox);

		JPanel content = new JPanel(new BorderLayout());
		content.add(controls, BorderLayout.NORTH);
		content.add(chartPanel, BorderLayout.CENTER);

		SVGFrameTools.dropSVGFrame(content, "ClusterDimensionsDistributionPainterTester", 800, 440);
	}
}
