package com.github.TKnudsen.infoVis.view.painters.boxPlot;

import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.boxplot.BoxPlotHorizontalPainter;

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
 * Horizontal boxplot tester
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class BoxPlotHorizontalPainterTester {
	public static void main(String[] args) {
		// create data
		List<Double> points = new ArrayList<>();
		int count = 150;

		for (int i = 0; i < count; i++) {
			points.add(Math.random() * 2);
			points.add(0.3 + Math.random() * 0.2);
		}

		StatisticsSupport dataStatistics = new StatisticsSupport(points);

		BoxPlotHorizontalPainter painter = new BoxPlotHorizontalPainter(dataStatistics);
		painter.getPositionEncodingFunction().setMinWorldValue(-1.0);
		painter.getPositionEncodingFunction().setMaxWorldValue(5.0);

		SVGFrameTools.dropSVGFrame(painter, "Boxplot Painter Test", 800, 400);
	}

}
