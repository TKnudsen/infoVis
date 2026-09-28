package com.github.TKnudsen.infoVis.view.painters.radial.test;

import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.radial.RadarChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Manual visual test for {@link RadarChartPainter}.
 *
 * @version 1.0
 * @since 2026
 */
public class RadarChartPainterTester {
	public static void main(String[] args) {
		List<Double> values = new ArrayList<>();
		for (int i = 0; i < 15; i++)
			values.add(Math.random());

		RadarChartPainter painter = new RadarChartPainter(values);

		InfoVisChartPanel panel = new InfoVisChartPanel(painter);

		SVGFrameTools.dropSVGFrame(panel, "RadarChartPainterTester", 800, 300);
	}
}
