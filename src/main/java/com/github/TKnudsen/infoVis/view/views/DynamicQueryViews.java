package com.github.TKnudsen.infoVis.view.views;

import java.util.List;
import java.util.function.Function;

import de.javagl.selection.SelectionModel;

public class DynamicQueryViews {

	public static <T> DynamicQueryView<T> createDynamicQuery(List<T> data, Function<T, Number> toNumberFunction,
			SelectionModel<T> selectionModel, String title) {

		DynamicQueryView<T> dynamicQueryView = new DynamicQueryView<T>(data, toNumberFunction, selectionModel);

		if (title != null)
			dynamicQueryView.setTitle(title);

		return dynamicQueryView;
	}

	private DynamicQueryViews() {

	}
}
