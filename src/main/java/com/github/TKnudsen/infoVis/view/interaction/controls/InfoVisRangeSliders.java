package com.github.TKnudsen.infoVis.view.interaction.controls;

import java.util.function.Predicate;

/**
 * *
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * InfoVisRangeSliders is a helper class to ease the use of range sliders
 * wrapped into the InfoVisRangeSlider class
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2022 Juergen Bernard,
 * https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
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
