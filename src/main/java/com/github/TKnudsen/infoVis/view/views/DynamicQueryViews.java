package com.github.TKnudsen.infoVis.view.views;

import java.awt.Color;
import java.util.Collection;
import java.util.function.Function;

import de.javagl.selection.SelectionModel;

public class DynamicQueryViews {

	public static <T> DynamicQueryView<T> createDynamicQuery(Collection<T> data, Function<T, Number> toNumberFunction,
			SelectionModel<T> selectionModel, String title) {

		return createDynamicQuery(data, toNumberFunction, selectionModel, title, 25);
	}

	/**
	 * 
	 * @param <T>              t
	 * @param data             data
	 * @param toNumberFunction function
	 * @param selectionModel   the selection model is NOT meant to represent the
	 *                         internal (or external) filter status of the dynamic
	 *                         query (queries). It can be used to synchronize data
	 *                         selections within the data distribution chart.
	 * @param title            title
	 * @param binCount         number of bins. default is 25.
	 * @return dynamic query view
	 */
	public static <T> DynamicQueryView<T> createDynamicQuery(Collection<T> data, Function<T, Number> toNumberFunction,
			SelectionModel<T> selectionModel, String title, int binCount) {

		DynamicQueryView<T> dynamicQueryView = new DynamicQueryView<T>(data, toNumberFunction, selectionModel,
				binCount);

		if (title != null)
			dynamicQueryView.setTitle(title);
		
		return dynamicQueryView;
	}

	public static <T> Color getAllDataColor(DynamicQueryView<T> view) {
		return view.getHistogram().getAllDataColor();
	}

	public static <T> void setAllDataColor(DynamicQueryView<T> view, Color allDataColor) {
		view.getHistogram().setAllDataColor(allDataColor);
	}

	public static <T> Color getFilterColor(DynamicQueryView<T> view) {
		return view.getHistogram().getFilterColor();
	}

	public static <T> void setFilterColor(DynamicQueryView<T> view, Color filterColor) {
		view.getHistogram().setFilterColor(filterColor);
	}

	public static <T> Color getSelectionColor(DynamicQueryView<T> view) {
		return view.getHistogram().getSelectionColor();
	}

	public static <T> void setSelectionColor(DynamicQueryView<T> view, Color selectionColor) {
		view.getHistogram().setSelectionColor(selectionColor);
	}

	/**
	 * decides whether Double.NaN values are considered in or out
	 * 
	 * @param <T>
	 * @param view
	 * @param keep
	 */
	public static <T> void setMissingValuePolicy(DynamicQueryView<T> view, boolean keep) {
		view.setMissingValuesAreIn(keep);
	}

	private DynamicQueryViews() {

	}
}
