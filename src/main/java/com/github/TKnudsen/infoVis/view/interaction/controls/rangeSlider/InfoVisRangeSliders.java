package com.github.TKnudsen.infoVis.view.interaction.controls.rangeSlider;

import java.util.function.Predicate;

/**
 * <p>
 * InfoVisRangeSliders is a helper class to ease the use of range sliders
 * wrapped into the InfoVisRangeSlider class
 * </p>
 *
 * @version 1.03
 * @since 2016
 */
public class InfoVisRangeSliders {

	public static Predicate<Number> predicate(InfoVisRangeSlider infoVisRangeSlider) {
		return t -> infoVisRangeSlider.inRange(t.doubleValue());
	}

	public static Number getMinRangeBound(InfoVisRangeSlider infoVisRangeSlider) {
		return infoVisRangeSlider.getModel().getValue();
	}

	public static Number getMaxRangeBound(InfoVisRangeSlider infoVisRangeSlider) {
		// lower value (default) plus extent (delta) == upper slider value
		return infoVisRangeSlider.getModel().getValue() + infoVisRangeSlider.getModel().getExtent();
	}

}
