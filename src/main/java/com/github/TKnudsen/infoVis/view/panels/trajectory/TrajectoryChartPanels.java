package com.github.TKnudsen.infoVis.view.panels.trajectory;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Creates trajectory chart panels.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class TrajectoryChartPanels {

	public static TrajectoryChartPanel<Double[]> create(List<Double[]> points, List<Color> colors) {
		ColorEncodingFunction<Double[]> colorMapping = new ColorEncodingFunction<Double[]>(points, colors);
		Function<Double[], Double> worldPositionMappingX = p -> p[0];
		Function<Double[], Double> worldPositionMappingY = p -> p[1];
		return new TrajectoryChartPanel<>(points, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

	public static TrajectoryChartPanel<Point2D> createForPoints(List<Point2D> points, List<Color> colors) {
		ColorEncodingFunction<Point2D> colorMapping = new ColorEncodingFunction<Point2D>(points, colors);
		Function<Point2D, Double> worldPositionMappingX = p -> p.getX();
		Function<Point2D, Double> worldPositionMappingY = p -> p.getY();
		return new TrajectoryChartPanel<>(points, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

}
