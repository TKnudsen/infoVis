package com.github.TKnudsen.infoVis.view.painters.axis.numerical;

import java.util.function.Function;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class XAxisNumericalPainters {

	/**
	 * 
	 * @param <T>                   t
	 * @param data                  the data collection
	 * @param worldPositionMappingX mapping
	 * @return the painter
	 */
	public static <T> XAxisNumericalPainter<Double> create(Iterable<T> data,
			Function<? super T, Double> worldPositionMappingX) {

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;

		if (data != null) {
			for (T t : data) {

				double x = worldPositionMappingX.apply(t);

				if (Double.isNaN(x))
					System.err.println("XAxisNumericalPainters.create: world position mapping was NaN for" + t);
				else {
					minX = Math.min(minX, x);
					maxX = Math.max(maxX, x);
				}
			}
		}

		XAxisNumericalPainter<Double> xAxisNumericalPainter = new XAxisNumericalPainter<Double>(minX, maxX);
		xAxisNumericalPainter.setPhysicalUnit(worldPositionMappingX.toString());

		return xAxisNumericalPainter;
	}
}
