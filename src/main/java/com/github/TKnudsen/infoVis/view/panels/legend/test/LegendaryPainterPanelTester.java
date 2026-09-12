package com.github.TKnudsen.infoVis.view.panels.legend.test;

import java.awt.Color;
import java.util.Arrays;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartVerticalPainter;
import com.github.TKnudsen.infoVis.view.panels.legend.LegendaryPainterPanel;

/**
 * Demo of {@link LegendaryPainterPanel}: a bar chart with a legend overlay
 * naming each bar's color, drawn on top rather than occupying its own axis
 * space.
 *
 * @since 2026
 */
public class LegendaryPainterPanelTester {

	public static void main(String[] args) {
		double[] data = { 3, 7, 4, 9, 5 };
		Color[] colors = { Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.MAGENTA };

		BarChartVerticalPainter barChart = new BarChartVerticalPainter(data, colors);
		barChart.setDrawBarOutlines(true);

		LegendaryPainterPanel<BarChartVerticalPainter> panel = new LegendaryPainterPanel<>(barChart,
				Arrays.asList("Apple", "Banana", "Cherry", "Date", "Elderberry"), Arrays.asList(colors));

		SVGFrameTools.dropSVGFrame(panel, "LegendaryPainterPanel", 400, 300);
	}
}
