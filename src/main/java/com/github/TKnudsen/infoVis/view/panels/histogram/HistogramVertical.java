package com.github.TKnudsen.infoVis.view.panels.histogram;

import java.awt.Color;
import java.util.Collection;
import java.util.function.Function;

public class HistogramVertical<T> extends Histogram<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HistogramVertical(Collection<? extends T> data, Function<? super T, Number> worldToNumberMapping,
			Function<Number, Integer> aggregationFunction, Number maxGlobal, Color defaultColor, Color filterColor) {
		super(data, worldToNumberMapping, aggregationFunction, maxGlobal, true, defaultColor, filterColor);
	}

}
