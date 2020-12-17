package com.github.TKnudsen.infoVis.view.panels.histogram;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2020 Juergen Bernard,
 * https://github.com/TKnudsen/InfoVis<br>
 * </p>
 * 
 * <p>
 * Test for histograms, both vertical and horizontal
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class HistogramTester {

	private static int BIN_COUNT = 5;

	public static void main(String[] args) {

		// create data
		List<Double> values = new ArrayList<>();
		int count = 1000;

		for (int i = 0; i < count; i++) {
			values.add(Math.random() * 2);
			values.add(0.3 + Math.random() * 0.2);
			values.add(1.0);
		}

		JPanel grid = new JPanel(new GridLayout(1, 0));

		grid.add(Histograms.create(values, BIN_COUNT, true));

		grid.add(Histograms.create(values, BIN_COUNT * 3, true));

		grid.add(Histograms.create(values, BIN_COUNT, false));

		Histogram<Number> h3 = Histograms.create(values, BIN_COUNT, false);
		Histograms.setShowValueDomainAxis(h3, false);
		grid.add(h3);

		SVGFrameTools.dropSVGFrame(grid, "Histograms, vertical and horizontal variants", 1200, 300);

		JFrame frame = new JFrame();
		frame.setTitle("Distribution1DVerticalPanel Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
