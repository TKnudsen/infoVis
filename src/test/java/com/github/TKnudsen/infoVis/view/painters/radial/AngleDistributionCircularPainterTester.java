package com.github.TKnudsen.infoVis.view.painters.radial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;

import de.javagl.common.ui.JSpinners;

public class AngleDistributionCircularPainterTester {

	private static Random random = new Random();

	public static void main(String[] args) {

		List<AngleDistributionCircularPainter> painters = new ArrayList<>();

		int grid = 4;
		int count = 5;

		// grid
		JPanel canvas = new JPanel(new BorderLayout());
		JPanel gridPanel = new JPanel(new GridLayout(grid, grid));
		for (int i = 0; i < grid * grid; i++) {

			AngleDistributionCircularPainter painter = new AngleDistributionCircularPainter(
					new StatisticsSupport(createDistribution(count)));
			painter.setPaint(new Color(79, 129, 188));
			painter.setDrawLegend(true);
			painter.setBorderPaint(ColorTools.setAlpha(Color.BLACK, 0.33f));
			InfoVisChartPanel p = new InfoVisChartPanel(painter);
			gridPanel.add(p);

			painters.add(painter);
		}
		canvas.add(gridPanel, BorderLayout.CENTER);

		// controls
		JPanel north = new JPanel(new GridLayout(1, 0));
		SpinnerNumberModel spinnerModelBoxPlot = new SpinnerNumberModel(0.1, 0.01, 1.0, 0.01);
		JSpinner spinnerBoxplot = JSpinners.createSpinner(spinnerModelBoxPlot, 2);
		JSpinners.setSpinnerDraggingEnabled(spinnerBoxplot, true);
		spinnerBoxplot.addChangeListener(e -> {
			for (AngleDistributionCircularPainter painter : painters)
				painter.setBoxplotStokeWidthRelative((double) spinnerBoxplot.getValue());

			gridPanel.repaint();
			gridPanel.revalidate();
		});
		north.add(spinnerBoxplot);

		SpinnerNumberModel spinnerModelGapSize = new SpinnerNumberModel(0.1, 0.01, 1.0, 0.01);
		JSpinner spinnerGapSize = JSpinners.createSpinner(spinnerModelGapSize, 2);
		JSpinners.setSpinnerDraggingEnabled(spinnerGapSize, true);
		spinnerGapSize.addChangeListener(e -> {
			for (AngleDistributionCircularPainter painter : painters)
				painter.setRelativeGapSizeBetweenArcAndBoxplot((double) spinnerGapSize.getValue());

			gridPanel.repaint();
			gridPanel.revalidate();
		});
		north.add(spinnerGapSize);

		canvas.add(north, BorderLayout.NORTH);

		SVGFrameTools.dropSVGFrame(canvas, "AngleDistributionCircularPainter Test Frame", 700, 900, 250, 50);
	}

	public static List<Double> createDistribution(int count) {
		List<Double> values = new ArrayList<Double>();

		for (int f = 0; f < count; f++)
			values.add((random.nextDouble() - random.nextDouble() * 0.5) * 100);

		return values;
	}
}
