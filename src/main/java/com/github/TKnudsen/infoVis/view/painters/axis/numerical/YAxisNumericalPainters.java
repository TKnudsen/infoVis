package com.github.TKnudsen.infoVis.view.painters.axis.numerical;

import java.util.function.Function;

public class YAxisNumericalPainters {

	public static <T> YAxisNumericalPainter<Double> create(Iterable<T> data,
			Function<? super T, Double> worldPositionMappingY) {

		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		if (data != null) {
			for (T t : data) {

				double y = worldPositionMappingY.apply(t);

				if (Double.isNaN(y))
					throw new IllegalArgumentException(
							"YAxisNumericalPainters.create: world position mapping was NaN for" + t);

				minY = Math.min(minY, y);
				maxY = Math.max(maxY, y);
			}
		}

		YAxisNumericalPainter<Double> yAxisNumericalPainter = new YAxisNumericalPainter<Double>(minY, maxY);
		yAxisNumericalPainter.setPhysicalUnit(worldPositionMappingY.toString());

		return yAxisNumericalPainter;
	}
}
