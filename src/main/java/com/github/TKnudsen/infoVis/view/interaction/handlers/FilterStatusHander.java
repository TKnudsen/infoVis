package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.github.TKnudsen.infoVis.view.interaction.event.FilterChangedEvent;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterStatusListener;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 *
 */
public class FilterStatusHander<T> implements FilterStatusListener<T> {

	private final List<Predicate<T>> filters = new ArrayList<>();

	private final List<FilterStatusListener<T>> filterStatusListeners = new ArrayList<FilterStatusListener<T>>();

	/**
	 * predicates are filter
	 * 
	 * @param predicate
	 */
	public void addFilter(Predicate<T> predicate) {
		this.filters.remove(predicate);

		this.filters.add(predicate);
	}

	public void addFilterStatusListener(FilterStatusListener<T> listener) {
		this.filterStatusListeners.remove(listener);

		this.filterStatusListeners.add(listener);
	}

	@Override
	public void filterStatusChanged(FilterChangedEvent<T> filterChangedEvent) {
		if (filterChangedEvent == null)
			return;

		Predicate<T> predicate = filterChangedEvent.getFilterStatus();
		if (filters.contains(predicate))
			handleFilterStatusChange();
	}

	private void handleFilterStatusChange() {
		FilterChangedEvent<T> filterChangedEvent = new FilterChangedEvent<>(this, t -> {
			for (Predicate<T> p : filters)
				if (!p.test(t))
					return false;
			return true;
		});

		for (FilterStatusListener<T> filterStatusListener : filterStatusListeners)
			filterStatusListener.filterStatusChanged(filterChangedEvent);
	}

}
