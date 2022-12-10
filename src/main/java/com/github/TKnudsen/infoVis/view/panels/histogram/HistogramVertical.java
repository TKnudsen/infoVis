package com.github.TKnudsen.infoVis.view.panels.histogram;

import java.awt.Color;
import java.util.Collection;
import java.util.function.Function;

public class HistogramVertical<T> extends Histogram<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * No global minimum and maximum any more. Interacts with aggregationFunction
	 * where this is already modeled.
	 * 
	 * @param data
	 * @param worldToNumberMapping
	 * @param minGlobal
	 * @param maxGlobal
	 * @param binCount             null if internal default value shall be taken.
	 *                             Necessary because minimum and maximum in
	 *                             aggregation function may not be reached with the
	 *                             global value domain. As a result it is impossible
	 *                             to anticipate the number of bins required.
	 * @param defaultColor
	 * @param filterColor
	 */
	public HistogramVertical(Collection<? extends T> data, Function<? super T, Number> worldToNumberMapping,
			Number minGlobal, Number maxGlobal, Integer binCount, Color defaultColor, Color filterColor) {
		super(data, worldToNumberMapping, minGlobal, maxGlobal, binCount, true, defaultColor, filterColor);
	}

}
