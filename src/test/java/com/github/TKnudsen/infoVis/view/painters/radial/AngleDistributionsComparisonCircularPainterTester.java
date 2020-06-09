package com.github.TKnudsen.infoVis.view.painters.radial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;

import de.javagl.common.ui.JSpinners;

public class AngleDistributionsComparisonCircularPainterTester {
	public static void main(String[] args) {

		List<AngleDistributionCircularPainter> painters = new ArrayList<>();

		int grid = 4;
		int valueCount = 5;
		int distributionsCount = 5;

		// grid
		JPanel canvas = new JPanel(new BorderLayout());
		JPanel gridPanel = new JPanel(new GridLayout(grid, grid));
		for (int i = 0; i < grid * grid; i++) {
			List<StatisticsSupport> distributions = new ArrayList<>();

			for (int d = 0; d < distributionsCount; d++) {
				List<Double> createDistribution = AngleDistributionCircularPainterTester.createDistribution(valueCount);
				distributions.add(new StatisticsSupport(createDistribution));
			}

			AngleDistributionsComparisonCircularPainter painter = new AngleDistributionsComparisonCircularPainter(
					distributions);
			painter.setBorderPaint(ColorTools.setAlpha(Color.BLACK, 0.33f));

			for (StatisticsSupport distribution : distributions) {
				painter.setPaint(distribution, ColorTools.randomColor());
			}

			InfoVisChartPanel p = new InfoVisChartPanel(painter);
			gridPanel.add(p);

			painters.addAll(painter.getAngleDistributionCircularPainters());
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

		SVGFrameTools.dropSVGFrame(canvas, "AngleDistributionsComparisonCircularPainter Test Frame", 700, 900, 250, 50);
	}
}
