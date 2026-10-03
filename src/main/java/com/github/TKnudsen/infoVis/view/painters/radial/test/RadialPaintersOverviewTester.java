package com.github.TKnudsen.infoVis.view.painters.radial.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.radial.IntervalArcGaugeComparisonPainter;
import com.github.TKnudsen.infoVis.view.painters.radial.IntervalArcGaugePainter;
import com.github.TKnudsen.infoVis.view.painters.radial.PieRadarChartPainter;
import com.github.TKnudsen.infoVis.view.painters.radial.RadarChartBundlePainter;
import com.github.TKnudsen.infoVis.view.painters.radial.RadarChartButterflyBundlePainter;
import com.github.TKnudsen.infoVis.view.painters.radial.RadarChartButterflyPainter;
import com.github.TKnudsen.infoVis.view.painters.radial.RadarChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Manual visual overview of every painter in {@code
 * com.github.TKnudsen.infoVis.view.painters.radial}, one labeled cell each.
 *
 * @version 1.0
 * @since 2026
 */
public class RadialPaintersOverviewTester {

	private static final Random random = new Random();

	public static void main(String[] args) {
		JPanel grid = new JPanel(new GridLayout(2, 4, 10, 10));

		grid.add(namedCell("IntervalArcGaugePainter", intervalArcGaugePainter()));
		grid.add(namedCell("IntervalArcGaugeComparisonPainter", intervalArcGaugeComparisonPainter()));
		grid.add(namedCell("RadarChartPainter", radarChartPainter()));
		grid.add(namedCell("RadarChartButterflyPainter", radarChartButterflyPainter()));
		grid.add(namedCell("RadarChartBundlePainter", radarChartBundlePainter()));
		grid.add(namedCell("RadarChartButterflyBundlePainter", radarChartButterflyBundlePainter()));
		grid.add(namedCell("PieRadarChartPainter", pieRadarChartPainter()));

		SVGFrameTools.dropSVGFrame(grid, "radial package overview", 1600, 800);
	}

	private static JPanel namedCell(String title, InfoVisChartPanel chartPanel) {
		JPanel cell = new JPanel(new BorderLayout());
		cell.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH);
		cell.add(chartPanel, BorderLayout.CENTER);
		return cell;
	}

	private static InfoVisChartPanel intervalArcGaugePainter() {
		IntervalArcGaugePainter gauge = new IntervalArcGaugePainter(new StatisticsSupport(createAngles(200)));
		gauge.setPaint(new Color(79, 129, 188));
		return new InfoVisChartPanel(gauge);
	}

	private static InfoVisChartPanel intervalArcGaugeComparisonPainter() {
		List<StatisticsSupport> series = new ArrayList<>();
		for (int i = 0; i < 3; i++)
			series.add(new StatisticsSupport(createAngles(150)));

		IntervalArcGaugeComparisonPainter comparison = new IntervalArcGaugeComparisonPainter(series);
		return new InfoVisChartPanel(comparison);
	}

	private static InfoVisChartPanel radarChartPainter() {
		RadarChartPainter painter = new RadarChartPainter(createValues(8));
		painter.setPaint(new Color(79, 129, 188));
		return new InfoVisChartPanel(painter);
	}

	private static InfoVisChartPanel radarChartButterflyPainter() {
		RadarChartButterflyPainter painter = new RadarChartButterflyPainter(createValues(8));
		painter.setPaint(new Color(192, 80, 77));
		return new InfoVisChartPanel(painter);
	}

	private static InfoVisChartPanel radarChartBundlePainter() {
		List<Double> values = createValues(10);
		RadarChartBundlePainter painter = new RadarChartBundlePainter(values, createBundle(values, 15));
		painter.setPaint(Color.ORANGE);
		return new InfoVisChartPanel(painter);
	}

	private static InfoVisChartPanel radarChartButterflyBundlePainter() {
		List<Double> values = createValues(10);
		RadarChartButterflyBundlePainter painter = new RadarChartButterflyBundlePainter(values,
				createBundle(values, 15));
		painter.setPaint(new Color(155, 187, 89));
		return new InfoVisChartPanel(painter);
	}

	private static InfoVisChartPanel pieRadarChartPainter() {
		List<Double> fractions = new ArrayList<>();
		List<Color> colors = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			fractions.add(random.nextDouble());
			colors.add(randomColor());
			labels.add("");
		}

		PieRadarChartPainter painter = new PieRadarChartPainter(fractions, colors, labels);
		return new InfoVisChartPanel(painter);
	}

	private static List<Double> createValues(int dimensions) {
		List<Double> values = new ArrayList<>();
		for (int i = 0; i < dimensions; i++)
			values.add(random.nextDouble());
		return values;
	}

	private static List<double[]> createBundle(List<Double> center, int bundleSize) {
		List<double[]> bundle = new ArrayList<>();
		for (int b = 0; b < bundleSize; b++) {
			double[] member = new double[center.size()];
			for (int i = 0; i < center.size(); i++)
				member[i] = center.get(i) + (random.nextDouble() - 0.5) * 0.4;
			bundle.add(member);
		}
		return bundle;
	}

	private static double[] createAngles(int count) {
		double[] angles = new double[count];
		for (int i = 0; i < count; i++)
			angles[i] = (random.nextDouble() - random.nextDouble() * 0.5) * 100;
		return angles;
	}

	private static Color randomColor() {
		return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}
}
