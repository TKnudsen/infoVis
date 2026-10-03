package com.github.TKnudsen.infoVis.view.painters.radial.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.radial.RadarChartBundlePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Manual visual test for {@link RadarChartBundlePainter}.
 *
 * @version 1.0
 * @since 2026
 */
public class RadarChartBundlePainterTester {
	public static void main(String[] args) {
		int dimensions = 12;
		int bundleSize = 25;

		List<Double> values = new ArrayList<>();
		for (int i = 0; i < dimensions; i++)
			values.add(Math.random() * 0.5 + Math.random() * 0.5 + Math.random() * 0.5);

		List<double[]> bundle = new ArrayList<>();
		for (int b = 0; b < bundleSize; b++) {
			double[] member = new double[dimensions];
			for (int i = 0; i < dimensions; i++)
				member[i] = values.get(i) + Math.random() - 0.5;
			bundle.add(member);
		}

		RadarChartBundlePainter painter = new RadarChartBundlePainter(values, bundle);
		painter.setPaint(Color.ORANGE);
		painter.setFill(true);
		painter.setDrawCoordinateMarks(true);

		InfoVisChartPanel panel = new InfoVisChartPanel(painter);

		SVGFrameTools.dropSVGFrame(panel, "RadarChartBundlePainterTester", 800, 300);
	}
}
