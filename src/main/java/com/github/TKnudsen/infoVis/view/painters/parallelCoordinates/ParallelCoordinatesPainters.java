package com.github.TKnudsen.infoVis.view.painters.parallelCoordinates;

import java.util.List;

/**
 * <p>
 * Static helper methods for {@link ParallelCoordinatesPainter}.
 * </p>
 *
 * @version 1.0
 */
public class ParallelCoordinatesPainters {

	public static <T> List<T> getData(ParallelCoordinatesPainter<T> parallelCoordinatesPainter) {
		return parallelCoordinatesPainter.data;
	}
}
