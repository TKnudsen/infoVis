package com.github.TKnudsen.infoVis.view.painters.scatterplot;

import java.awt.Paint;
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
 * Creates scatter plot painters.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class ScatterPlotPainters {

	public static ScatterPlotPainter<Double[]> createForDoubles(List<Double[]> points, List<? extends Paint> colors) {

		ColorEncodingFunction<Double[]> colorMapping = new ColorEncodingFunction<Double[]>(points, colors);

		return createForDoubles(points, colorMapping);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ScatterPlotPainter<Double[]> createForDoubles(List<Double[]> points,
			Function<? super Double[], ? extends Paint> colorMapping) {
		Function<Double[], Double> worldPositionMappingX = p -> p[0];
		Function<Double[], Double> worldPositionMappingY = p -> p[1];
		return new ScatterPlotPainter(points, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

	public static ScatterPlotPainter<Point2D> createForPoints(List<Point2D> points, List<? extends Paint> colors) {
		ColorEncodingFunction<Point2D> colorMapping = new ColorEncodingFunction<Point2D>(points, colors);
		Function<Point2D, Double> worldPositionMappingX = p -> p.getX();
		Function<Point2D, Double> worldPositionMappingY = p -> p.getY();
		return new ScatterPlotPainter<>(points, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

}
