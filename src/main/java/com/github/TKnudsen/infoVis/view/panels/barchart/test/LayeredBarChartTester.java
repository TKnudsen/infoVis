package com.github.TKnudsen.infoVis.view.panels.barchart.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartVerticalPainter;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChartValueBased;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChartsValueBased;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;

/**
 * Standalone, select-capable demo of {@link BarChartsValueBased#createLayeredBarChart}:
 * 3 series over the same 6 category slots, drawn semi-transparent and
 * overlaid so they share one Y-axis scale (the true global max across all
 * layers), each with a {@link BarChartsValueBased#addPresenceMarker} baseline
 * tick, with click + rectangle selection wired via
 * {@link BarChartsValueBased#addInteraction}.
 *
 * @version 1.0
 * @since 2026
 */
public class LayeredBarChartTester {

	public static void main(String[] args) {
		Random random = new Random(5);

		int barCount = 6;
		Color[] seriesColors = { Color.RED, Color.BLUE, Color.GREEN.darker() };

		List<List<? extends Number>> data = new ArrayList<>();
		List<Color> opaqueColors = new ArrayList<>();

		for (Color seriesColor : seriesColors) {
			List<Double> values = new ArrayList<>();
			for (int i = 0; i < barCount; i++)
				// occasionally zero, to demonstrate the presence marker's value
				values.add(random.nextDouble() < 0.15 ? 0.0 : 2 + random.nextDouble() * 8);

			data.add(values);
			opaqueColors.add(seriesColor);
		}

		List<Color> transparentColors = new ArrayList<>();
		for (Color c : opaqueColors)
			transparentColors.add(ColorTools.setAlpha(c, 0.45f));

		BarChartValueBased barChart = BarChartsValueBased.createLayeredBarChart(data, transparentColors);
		barChart.setBackground(null);

		BarChartsValueBased.addPresenceMarker(barChart, barChart.getBarChartPainter(), seriesColors[0]);
		for (int i = 1; i < barChart.getChartPainters().size(); i++)
			if (barChart.getChartPainters().get(i) instanceof BarChartVerticalPainter)
				BarChartsValueBased.addPresenceMarker(barChart,
						(BarChartVerticalPainter) barChart.getChartPainters().get(i), seriesColors[i]);

		BarChartsValueBased.addInteraction(barChart);

		SwingUtilities.invokeLater(() -> SVGFrameTools.dropSVGFrame(barChart, "Layered BarChart Demo", 700, 500));
	}

}
