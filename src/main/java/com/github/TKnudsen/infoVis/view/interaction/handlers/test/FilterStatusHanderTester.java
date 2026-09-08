package com.github.TKnudsen.infoVis.view.interaction.handlers.test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterChangedEvent;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterStatusListener;
import com.github.TKnudsen.infoVis.view.interaction.handlers.FilterStatusHander;
import com.github.TKnudsen.infoVis.view.views.DynamicQueryView;
import com.github.TKnudsen.infoVis.view.views.DynamicQueryViews;

import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * <p>
 * Standalone interactive demo of {@link FilterStatusHander}: combines a
 * {@link DynamicQueryView} range filter with a second, ad-hoc predicate
 * filter (an "even IDs only" toggle), and prints the combined IN/OUT status
 * for every data element to the console whenever either filter changes.
 * Drops an SVGFrame with the DynamicQueryView's slider/histogram so the
 * range filter can be exercised interactively.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class FilterStatusHanderTester {

	public static void main(String[] args) {
		List<Long> ids = new ArrayList<>();
		for (long l = 0; l < 20; l++)
			ids.add(l);

		FilterStatusHander<Long> filterStatusHander = new FilterStatusHander<>();

		SelectionModel<Long> selectionModel = SelectionModels.create();
		DynamicQueryView<Long> dynamicQueryView = DynamicQueryViews.createDynamicQuery(ids, id -> (double) id,
				selectionModel, "Dynamic Query");

		// register the DynamicQueryView's own predicate as a filter, and route its
		// slider changes back into the hander so it re-broadcasts the combined status
		filterStatusHander.addFilter(dynamicQueryView);
		dynamicQueryView.addFilterStatusListener(filterStatusHander);

		// a second, ad-hoc filter alongside the range filter
		Predicate<Long> evenIdsOnly = id -> id % 2 == 0;
		filterStatusHander.addFilter(evenIdsOnly);

		filterStatusHander.addFilterStatusListener(new FilterStatusListener<Long>() {

			@Override
			public void filterStatusChanged(FilterChangedEvent<Long> filterChangedEvent) {
				printStatus(ids, filterStatusHander);
			}
		});

		printStatus(ids, filterStatusHander);

		SwingUtilities.invokeLater(() -> SVGFrameTools.dropSVGFrame(dynamicQueryView, "Dynamic Query", 400, 400));
	}

	private static void printStatus(List<Long> ids, FilterStatusHander<Long> filterStatusHander) {
		for (Long id : ids)
			System.out.println("ID " + id + ": " + (filterStatusHander.test(id) ? "IN" : "OUT"));
		System.out.println();
	}

}
