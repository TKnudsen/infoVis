package com.github.TKnudsen.infoVis.view.panels.trajectory;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
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
public class TrajectoriesChartPanels {

	public static TrajectoriesChartPanel<Double[]> create(List<List<Double[]>> points, List<List<Color>> colors) {
		List<Double[]> dataList = new ArrayList<>();
		for (List<Double[]> data : points)
			dataList.addAll(data);

		List<Color> colorList = new ArrayList<>();
		for (List<Color> data : colors)
			colorList.addAll(data);

		ColorEncodingFunction<Double[]> colorMapping = new ColorEncodingFunction<Double[]>(dataList, colorList);
		Function<Double[], Double> worldPositionMappingX = p -> p[0];
		Function<Double[], Double> worldPositionMappingY = p -> p[1];
		return new TrajectoriesChartPanel<>(points, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

	public static TrajectoriesChartPanel<Point2D> createForPoints(List<List<Point2D>> points,
			List<List<Color>> colors) {
		List<Point2D> dataList = new ArrayList<>();
		for (List<Point2D> data : points)
			dataList.addAll(data);

		List<Color> colorList = new ArrayList<>();
		for (List<Color> data : colors)
			colorList.addAll(data);

		ColorEncodingFunction<Point2D> colorMapping = new ColorEncodingFunction<Point2D>(dataList, colorList);
		Function<Point2D, Double> worldPositionMappingX = p -> p.getX();
		Function<Point2D, Double> worldPositionMappingY = p -> p.getY();
		return new TrajectoriesChartPanel<>(points, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

}
