package com.github.TKnudsen.infoVis.view.painters.color.test;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.color.ColorLegendPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Demo of {@link ColorLegendPainter}: a color swatch per label.
 *
 * @since 2026
 */
public class ColorLegendPainterTester {

	public static void main(String[] args) {
		ColorLegendPainter painter = new ColorLegendPainter(new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE },
				new String[] { "Apple", "Banana", "Cherry", "Date" });

		SVGFrameTools.dropSVGFrame(new InfoVisChartPanel(painter), "ColorLegendPainter", 250, 150);
	}
}
