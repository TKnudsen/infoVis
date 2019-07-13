package com.github.TKnudsen.infoVis.view.painters.boxPlot;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.boxplot.BoxPlotHorizontalPainter;
import com.github.TKnudsen.infoVis.view.painters.boxplot.BoxPlotVerticalPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotVerticalChartPanel;

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
 * Paints a horizontal and a vertical boxplot.
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class BoxPlotPainterTester {
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("BoxPlotPainterTester Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create data
		List<Double> points = new ArrayList<>();
		int count = 150;

		for (int i = 0; i < count; i++) {
			points.add(Math.random() * 2);
			points.add(0.3 + Math.random() * 0.2);
		}

		StatisticsSupport dataStatistics = new StatisticsSupport(points);

		// create painter and JFrame
		BoxPlotVerticalPainter painter1 = new BoxPlotVerticalPainter(dataStatistics);
		painter1.getPositionEncodingFunction().setMinWorldValue(0.0);
		painter1.getPositionEncodingFunction().setMaxWorldValue(2.0);
//		painter1.setFlipAxis(false);
		InfoVisChartPanel p1 = new InfoVisChartPanel();
		p1.addChartPainter(painter1);

		BoxPlotHorizontalPainter painter2 = new BoxPlotHorizontalPainter(dataStatistics);
		painter2.getPositionEncodingFunction().setMinWorldValue(0.0);
		painter2.getPositionEncodingFunction().setMaxWorldValue(2.0);
//		painter2.setFlipAxis(false);
		InfoVisChartPanel p2 = new InfoVisChartPanel();
		p2.addChartPainter(painter2);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 2));
		panel.add(p1);
		panel.add(new JPanel());
		panel.add(p2);
		panel.add(new JPanel());

		SVGFrameTools.dropSVGFrame(panel, "Boxplots", 800, 400);
	}

	public static void dropATestFrame(StatisticsSupport dataStatistics) {

		BoxPlotVerticalChartPanel panel = new BoxPlotVerticalChartPanel(dataStatistics);
		panel.setYAxisMinValue(0.0);
		panel.setYAxisMaxValue(2.0);

		SVGFrameTools.dropSVGFrame(panel, "BoxPlotVerticalChartPanel Test Frame", 800, 300);
	}
}
