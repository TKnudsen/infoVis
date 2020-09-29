package com.github.TKnudsen.infoVis.view.interaction.controls;

import java.util.function.Predicate;

public class InfoVisRangeSliders {

	public static Predicate<Number> predicate(InfoVisRangeSlider infoVisRangeSlider) {
		return t -> infoVisRangeSlider.inRange(t.doubleValue());
	}

}
