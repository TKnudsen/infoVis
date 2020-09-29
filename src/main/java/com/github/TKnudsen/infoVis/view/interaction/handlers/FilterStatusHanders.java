package com.github.TKnudsen.infoVis.view.interaction.handlers;

public class FilterStatusHanders {

	public static <T> FilterStatusHander<T> create() {
		return new FilterStatusHander<T>();
	}

	private FilterStatusHanders() {

	}
}
