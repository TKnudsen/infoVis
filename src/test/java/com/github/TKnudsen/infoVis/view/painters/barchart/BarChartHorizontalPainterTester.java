package com.github.TKnudsen.infoVis.view.painters.barchart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard,
 * https://github.com/TKnudsen/InfoVis<br>
 * </p>
 * 
 * <p>
 * Horizontal bar chart tester
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class BarChartHorizontalPainterTester {
	public static void main(String[] args) {
		// create data
		List<Double> points = new ArrayList<>();
		List<Color> colors = new ArrayList<>();
		List<String> labels = new ArrayList<>();

		labels.add("Flug");
		points.add(340.42 * 4);
		colors.add(new Color(120, 180, 87));

		labels.add("Mietauto");
		points.add(536.0);
		colors.add(new Color(106, 161, 215));

		labels.add("Essen");
		points.add(461.4);
		colors.add(new Color(236, 137, 69));

		labels.add("Unterkunft");
		points.add(420.0);
		colors.add(new Color(172, 172, 172));

		labels.add("Einkaufen");
		points.add(239.16);
		colors.add(new Color(254, 196, 27));

		labels.add("Tanken");
		points.add(75.0);
		colors.add(new Color(82, 120, 193));

		BarChartHorizontalPainter painter = new BarChartHorizontalPainter(points, colors);

		SVGFrameTools.dropSVGFrame(painter, "Bar Chart Painter Test", 800, 400);

		System.err
				.println("For an interaction demo you need a panel (see, e.g., InfoVisBarChartHorizontalPanelTester)");
	}

}
