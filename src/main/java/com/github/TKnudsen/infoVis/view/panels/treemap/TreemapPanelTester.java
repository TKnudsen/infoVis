package com.github.TKnudsen.infoVis.view.panels.treemap;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.treemap.TreemapMetric;
import com.github.TKnudsen.infoVis.view.painters.treemap.TreemapNode;
import com.github.TKnudsen.infoVis.view.painters.treemap.TreemapValues;

import de.javagl.selection.LoggingSelectionListener;

/**
 * <p>
 * TreemapPanelTester
 *
 * Builds a small synthetic folder/file-like hierarchy (no real filesystem
 * access -- {@code T} is just {@code String} here) and drops a {@link
 * TreemapPanel} showing it, to manually exercise layout, selection
 * (click/rectangle), and double-click drill-down.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class TreemapPanelTester {

	// ============================================================
	// PARAMETERS (play around here)
	// ============================================================

	/** how many top-level folders to synthesize */
	private static final int FOLDER_COUNT = 5;

	/** min/max files per folder */
	private static final int MIN_FILES_PER_FOLDER = 2;
	private static final int MAX_FILES_PER_FOLDER = 12;

	/** value range per synthetic file (stands in for e.g. a file size) */
	private static final double MIN_VALUE = 1;
	private static final double MAX_VALUE = 1000;

	private static final long RANDOM_SEED = 42;

	// ============================================================
	// MAIN
	// ============================================================

	public static void main(String[] args) {
		Random random = new Random(RANDOM_SEED);

		TreemapNode<String> root = buildSyntheticHierarchy(random);

		// leaves carry their own value directly (parsed from the label, see
		// buildSyntheticHierarchy); an internal node's value is the sum of its
		// children's -- ordinary disk-usage-style aggregation
		TreemapMetric<String> metric = new TreemapMetric<String>() {
			@Override
			public double value(String leaf) {
				return Double.parseDouble(leaf.substring(leaf.lastIndexOf(' ') + 1));
			}

			@Override
			public double aggregate(List<Double> childValues) {
				double sum = 0;
				for (double v : childValues)
					sum += v;
				return sum;
			}
		};
		TreemapValues.computeValues(root, metric);

		TreemapPanel<String> panel = new TreemapPanel<>(root);
		panel.setFillColorFunction(node -> node.isLeaf() ? new Color(100, 100, 100) : Color.LIGHT_GRAY);
		panel.getSelectionModel().addSelectionListener(new LoggingSelectionListener<>());
		panel.setOnDrillDown(node -> System.out.println("TreemapPanelTester: drilled into " + node.getLabel()));

		SVGFrameTools.dropSVGFrame(panel, "TreemapPanelTester -- synthetic hierarchy", 900, 600);
	}

	// ============================================================
	// BUILDING BLOCKS
	// ============================================================

	private static TreemapNode<String> buildSyntheticHierarchy(Random random) {
		TreemapNode<String> root = new TreemapNode<>(null, "root");

		for (int f = 0; f < FOLDER_COUNT; f++) {
			TreemapNode<String> folder = new TreemapNode<>(null, "folder-" + f);
			root.addChild(folder);

			int fileCount = MIN_FILES_PER_FOLDER + random.nextInt(MAX_FILES_PER_FOLDER - MIN_FILES_PER_FOLDER + 1);
			for (int i = 0; i < fileCount; i++) {
				double value = MIN_VALUE + random.nextDouble() * (MAX_VALUE - MIN_VALUE);
				// value is embedded in the label itself (" <value>" suffix) purely so
				// this synthetic String-typed demo has somewhere to read it back from
				// in the TreemapMetric above -- a real T (e.g. File) wouldn't need this
				String label = "file-" + f + "-" + i + " " + value;
				folder.addChild(new TreemapNode<>(label, label));
			}
		}

		return root;
	}
}
