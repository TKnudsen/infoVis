package com.github.TKnudsen.infoVis.view.painters.radial.test;

import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.radial.RadarChartButterflyPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Manual visual test for {@link RadarChartButterflyPainter}.
 *
 * @version 1.0
 * @since 2026
 */
public class RadarChartButterflyPainterTester {
	public static void main(String[] args) {
		List<Double> values = new ArrayList<>();
		for (int i = 0; i < 15; i++)
			values.add(Math.random());

		RadarChartButterflyPainter painter = new RadarChartButterflyPainter(values);

		InfoVisChartPanel panel = new InfoVisChartPanel(painter);

		SVGFrameTools.dropSVGFrame(panel, "RadarChartButterflyPainterTester", 800, 300);
	}
}
