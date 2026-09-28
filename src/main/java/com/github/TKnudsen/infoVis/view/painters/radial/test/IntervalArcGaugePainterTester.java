package com.github.TKnudsen.infoVis.view.painters.radial.test;

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
import com.github.TKnudsen.infoVis.view.painters.radial.IntervalArcGaugePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;

import de.javagl.common.ui.JSpinners;

/**
 * Manual visual test for {@link IntervalArcGaugePainter}.
 *
 * @version 1.0
 * @since 2026
 */
public class IntervalArcGaugePainterTester {

	private static Random random = new Random();

	public static void main(String[] args) {

		List<IntervalArcGaugePainter> gauges = new ArrayList<>();

		int grid = 4;
		int count = 5;

		JPanel canvas = new JPanel(new BorderLayout());
		JPanel gridPanel = new JPanel(new GridLayout(grid, grid));
		for (int i = 0; i < grid * grid; i++) {

			IntervalArcGaugePainter gauge = new IntervalArcGaugePainter(
					new StatisticsSupport(createSampleAngles(count)));
			gauge.setPaint(new Color(79, 129, 188));
			gauge.setShowScaleLegend(true);
			gauge.setBorderPaint(ColorTools.setAlpha(Color.BLACK, 0.33f));
			InfoVisChartPanel p = new InfoVisChartPanel(gauge);
			gridPanel.add(p);

			gauges.add(gauge);
		}
		canvas.add(gridPanel, BorderLayout.CENTER);

		JPanel north = new JPanel(new GridLayout(1, 0));
		SpinnerNumberModel ringThicknessModel = new SpinnerNumberModel(0.1, 0.01, 1.0, 0.01);
		JSpinner ringThicknessSpinner = JSpinners.createSpinner(ringThicknessModel, 2);
		JSpinners.setSpinnerDraggingEnabled(ringThicknessSpinner, true);
		ringThicknessSpinner.addChangeListener(e -> {
			for (IntervalArcGaugePainter gauge : gauges)
				gauge.setPercentileRingThicknessRatio((double) ringThicknessSpinner.getValue());

			gridPanel.repaint();
			gridPanel.revalidate();
		});
		north.add(ringThicknessSpinner);

		SpinnerNumberModel ringGapModel = new SpinnerNumberModel(0.1, 0.01, 1.0, 0.01);
		JSpinner ringGapSpinner = JSpinners.createSpinner(ringGapModel, 2);
		JSpinners.setSpinnerDraggingEnabled(ringGapSpinner, true);
		ringGapSpinner.addChangeListener(e -> {
			for (IntervalArcGaugePainter gauge : gauges)
				gauge.setRingGapRatio((double) ringGapSpinner.getValue());

			gridPanel.repaint();
			gridPanel.revalidate();
		});
		north.add(ringGapSpinner);

		canvas.add(north, BorderLayout.NORTH);

		SVGFrameTools.dropSVGFrame(canvas, "IntervalArcGaugePainter Test Frame", 700, 900, 250, 50);
	}

	public static List<Double> createSampleAngles(int count) {
		List<Double> values = new ArrayList<>();

		for (int i = 0; i < count; i++)
			values.add((random.nextDouble() - random.nextDouble() * 0.5) * 100);

		return values;
	}
}
