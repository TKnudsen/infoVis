package com.github.TKnudsen.infoVis.view.painters.radial.test;

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
import com.github.TKnudsen.infoVis.view.painters.radial.IntervalArcGaugeComparisonPainter;
import com.github.TKnudsen.infoVis.view.painters.radial.IntervalArcGaugePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;

import de.javagl.common.ui.JSpinners;

/**
 * Manual visual test for {@link IntervalArcGaugeComparisonPainter}.
 *
 * @version 1.0
 * @since 2026
 */
public class IntervalArcGaugeComparisonPainterTester {
	public static void main(String[] args) {

		List<IntervalArcGaugePainter> gauges = new ArrayList<>();

		int grid = 4;
		int valueCount = 5;
		int seriesCount = 5;

		JPanel canvas = new JPanel(new BorderLayout());
		JPanel gridPanel = new JPanel(new GridLayout(grid, grid));
		for (int i = 0; i < grid * grid; i++) {
			List<StatisticsSupport> series = new ArrayList<>();

			for (int s = 0; s < seriesCount; s++) {
				List<Double> sampleAngles = IntervalArcGaugePainterTester.createSampleAngles(valueCount);
				series.add(new StatisticsSupport(sampleAngles));
			}

			IntervalArcGaugeComparisonPainter comparison = new IntervalArcGaugeComparisonPainter(series);
			comparison.setBorderPaint(ColorTools.setAlpha(Color.BLACK, 0.33f));

			for (StatisticsSupport oneSeries : series)
				comparison.setSeriesPaint(oneSeries, ColorTools.randomColor());

			InfoVisChartPanel p = new InfoVisChartPanel(comparison);
			gridPanel.add(p);

			gauges.addAll(comparison.getGauges());
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

		SVGFrameTools.dropSVGFrame(canvas, "IntervalArcGaugeComparisonPainter Test Frame", 700, 900, 250, 50);
	}
}
