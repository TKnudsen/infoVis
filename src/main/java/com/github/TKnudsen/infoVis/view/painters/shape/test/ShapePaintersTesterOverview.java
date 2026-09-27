package com.github.TKnudsen.infoVis.view.painters.shape.test;

import java.awt.GridLayout;

import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.shape.RegularPolygonPainter;
import com.github.TKnudsen.infoVis.view.painters.shape.RegularShapePainter;
import com.github.TKnudsen.infoVis.view.painters.shape.RoundPolygonPainter;
import com.github.TKnudsen.infoVis.view.painters.shape.StarPolygonPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * <p>
 * Gallery demo: one row each of {@link RegularPolygonPainter},
 * {@link RegularShapePainter}, {@link StarPolygonPainter} and
 * {@link RoundPolygonPainter}, growing from a triangle (3 edges) up to a
 * 17-gon -- shows how all four shape families vary with edge count, side by
 * side.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ShapePaintersTesterOverview {

	public static void main(String[] args) {

		int painterCount = 15;

		JPanel panel = new JPanel(new GridLayout(4, painterCount));

		// RegularPolygonPainter
		for (int i = 0; i < painterCount; i++)
			panel.add(new InfoVisChartPanel(new RegularPolygonPainter(i + 3)));

		// RegularShapePainter
		for (int i = 0; i < painterCount; i++)
			panel.add(new InfoVisChartPanel(new RegularShapePainter(i + 3)));

		// StarPolygonPainter
		for (int i = 0; i < painterCount; i++)
			panel.add(new InfoVisChartPanel(new StarPolygonPainter(i + 3)));

		// RoundPolygonPainter
		for (int i = 0; i < painterCount; i++)
			panel.add(new InfoVisChartPanel(new RoundPolygonPainter(i + 3)));

		SVGFrameTools.dropSVGFrame(panel, "Shape Painters Overview", 1200, 320);
	}
}
