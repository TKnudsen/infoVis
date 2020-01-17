package com.github.TKnudsen.infoVis.view.panels.parallelCoordinates;

import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;

/**
 * 
 * <p>
 * InfoVis
 * </p>
 * 
 * Creates parallel coordinates panels
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 *
 */
public class ParallelCoordinatesFactory {

	public static ParallelCoordinates<Double[]> createForDoubles(List<Double[]> points, List<? extends Paint> colors) {
		ColorEncodingFunction<Double[]> colorMapping = new ColorEncodingFunction<Double[]>(points, colors);

		int dimensionality = Integer.MAX_VALUE;
		for (Double[] o : points)
			dimensionality = Math.min(dimensionality, o.length);

		if (dimensionality < 1)
			throw new IllegalArgumentException("ParallelCoordinatesFactory: too few dimensions: " + dimensionality);

		List<Function<? super Double[], Double>> worldPositionMappingYs = new ArrayList<>();
		for (int i = 0; i < dimensionality; i++) {
			final int index = i;
			worldPositionMappingYs.add(p -> p[index]);
		}

		return new ParallelCoordinates<Double[]>(points, colorMapping, worldPositionMappingYs);
	}

}
