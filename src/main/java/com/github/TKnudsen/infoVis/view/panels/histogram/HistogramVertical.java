package com.github.TKnudsen.infoVis.view.panels.histogram;

import java.awt.Color;
import java.util.Collection;
import java.util.function.Function;

/**
 * <p>
 * Vertical histogram variant.
 * 
 * Uses vertical bar chart painters for all three layers (global, filter,
 * selection).
 * </p>
 *
 * @version 2.02 (revised)
 */
public class HistogramVertical<T> extends Histogram<T> {

	private static final long serialVersionUID = 1L;

	public HistogramVertical(Collection<? extends T> data, Function<? super T, Number> worldToNumberMapping,
			Number minGlobal, Number maxGlobal, Integer binCount, Color defaultColor, Color filterColor) {

		super(data, worldToNumberMapping, minGlobal, maxGlobal, binCount, true, defaultColor, filterColor);
	}
}