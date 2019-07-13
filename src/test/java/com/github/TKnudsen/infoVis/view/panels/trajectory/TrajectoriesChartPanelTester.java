package com.github.TKnudsen.infoVis.view.panels.trajectory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.SizeEncodingWithVariationFunction;

import de.javagl.selection.SelectionModels;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class TrajectoriesChartPanelTester {

	public static void main(String[] args) {

		// create data
		List<List<Point2D>> points = new ArrayList<>();
		List<List<Color>> colors = new ArrayList<>();

		Random random = new Random();

		int trajectories = 5;
		int count = 40;

		Point2D lastPoint = null;
		for (int t = 0; t < trajectories; t++) {
			List<Point2D> ps = new ArrayList<>();
			List<Color> cs = new ArrayList<>();
			Color c = ColorTools.randomColor();

			for (int i = 0; i < count; i++) {
				Point2D point = null;
				if (lastPoint == null)
					point = new Point2D.Double(500.0, 500.0);
				else
					point = new Point2D.Double(lastPoint.getX() + random.nextDouble() * 100 - 45,
							lastPoint.getY() + random.nextDouble() * 100 - 45);
				ps.add(point);
				lastPoint = point;

				cs.add(c);
			}

			points.add(ps);
			colors.add(cs);

			lastPoint = null;
		}

		TrajectoriesChartPanel<Point2D> panel = TrajectoriesChartPanels.createForPoints(points, colors);

		// size encoding
//		panel.setSizeEncodingFunction(new SizeEncodingFunction<>(panel, 0.5));

		// size encoding: increase size towards the end
		Map<Point2D, Double> scales = new HashMap<>();
		for (int t = 0; t < trajectories; t++)
			for (int i = 0; i < count; i++)
				scales.put(points.get(t).get(i), 1.0 + (i / (double) count) * 2);
		panel.setSizeEncodingFunction(new SizeEncodingWithVariationFunction<>(panel, 0.5, scales));

		SelectionHandler<Point2D> selectionHandler = new SelectionHandler<>(SelectionModels.create());
		selectionHandler.attachTo(panel);
		selectionHandler.setClickSelection(panel);
		selectionHandler.setRectangleSelection(panel);

		panel.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		panel.setSelectedFunction(new Function<Point2D, Boolean>() {
			@Override
			public Boolean apply(Point2D t) {
				return selectionHandler.getSelectionModel().isSelected(t);
			}
		});

		System.out.println(selectionHandler.getSelectionModel());

		SVGFrameTools.dropSVGFrame(panel, "My first trajectory panel", 800, 800);
	}

}
