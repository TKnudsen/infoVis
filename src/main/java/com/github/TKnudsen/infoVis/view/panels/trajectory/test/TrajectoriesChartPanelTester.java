package com.github.TKnudsen.infoVis.view.panels.trajectory.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlots;
import com.github.TKnudsen.infoVis.view.panels.trajectory.TrajectoriesChartPanel;
import com.github.TKnudsen.infoVis.view.panels.trajectory.TrajectoriesChartPanels;

/**
 * Standalone demo of {@link TrajectoriesChartPanel}: 4 synthetic random-walk
 * trajectories sharing one pair of numerical axes, each individually
 * click/rectangle/lasso-selectable.
 *
 * @version 1.0
 * @since 2026
 */
public class TrajectoriesChartPanelTester {

	public static void main(String[] args) {
		Random random = new Random(4);

		int trajectoryCount = 4;
		int pointsPerTrajectory = 25;

		Color[] trajectoryColors = { Color.RED, Color.BLUE, Color.GREEN.darker(), Color.ORANGE.darker() };

		List<List<Double[]>> points = new ArrayList<>();
		List<List<Color>> colors = new ArrayList<>();

		for (int t = 0; t < trajectoryCount; t++) {
			List<Double[]> trajectory = new ArrayList<>();
			List<Color> trajectoryColorList = new ArrayList<>();

			double x = random.nextDouble();
			double y = random.nextDouble();
			for (int i = 0; i < pointsPerTrajectory; i++) {
				x = Math.max(0, Math.min(1, x + random.nextGaussian() * 0.05));
				y = Math.max(0, Math.min(1, y + random.nextGaussian() * 0.05));

				trajectory.add(new Double[] { x, y });
				trajectoryColorList.add(trajectoryColors[t % trajectoryColors.length]);
			}

			points.add(trajectory);
			colors.add(trajectoryColorList);
		}

		TrajectoriesChartPanel<Double[]> panel = TrajectoriesChartPanels.create(points, colors);

		ScatterPlots.addInteraction(panel, true, true, true);

		SwingUtilities.invokeLater(() -> SVGFrameTools.dropSVGFrame(panel, "TrajectoriesChartPanel Demo", 700, 700));
	}

}
