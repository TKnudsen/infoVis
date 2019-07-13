package com.github.TKnudsen.infoVis.view.panels.trajectory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.SizeEncodingFunction;

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
public class TrajectoryChartPanelTester {

	public static void main(String[] args) {

		// create data
		List<Point2D> points = new ArrayList<>();
		List<Color> colors = new ArrayList<>();
		List<String> labels = new ArrayList<>();

		Random random = new Random();

		int count = 50;
		Point2D lastPoint = null;
		for (int i = 0; i < count; i++) {
			Point2D point = null;
			if (lastPoint == null)
				point = new Point2D.Double(random.nextDouble() * 1000, random.nextDouble() * 1000);
			else
				point = new Point2D.Double(lastPoint.getX() + random.nextDouble() * 100 - 45,
						lastPoint.getY() + random.nextDouble() * 100 - 45);
			points.add(point);
			lastPoint = point;

			colors.add(ColorTools.randomColor());
			Character c = new Character((char) (random.nextInt(26) + 65));
			labels.add(c.toString());
		}

		TrajectoryChartPanel<Point2D> panel = TrajectoryChartPanels.createForPoints(points, colors);
		panel.setSizeEncodingFunction(new SizeEncodingFunction<>(panel, 0.25));

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
