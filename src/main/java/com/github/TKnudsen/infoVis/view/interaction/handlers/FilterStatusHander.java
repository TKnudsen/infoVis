package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.github.TKnudsen.infoVis.view.interaction.event.FilterChangedEvent;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterStatusListener;

/**
 * @version 1.04
 * @since 2018
 */
public class FilterStatusHander<T> implements FilterStatusListener<T>, Predicate<T> {

	private final List<Predicate<T>> filters = new ArrayList<>();

	private final List<FilterStatusListener<T>> filterStatusListeners = new ArrayList<FilterStatusListener<T>>();

	/**
	 * predicates are filter
	 * 
	 * @param predicate predicate
	 */
	public void addFilter(Predicate<T> predicate) {
		this.filters.remove(predicate);

		this.filters.add(predicate);

		handleFilterStatusChange();
	}

	public void removeFilter(Predicate<T> predicate) {
		this.filters.remove(predicate);

		handleFilterStatusChange();
	}

	public void addFilterStatusListener(FilterStatusListener<T> listener) {
		this.filterStatusListeners.remove(listener);

		this.filterStatusListeners.add(listener);
	}

	public void removeFilterStatusListener(FilterStatusListener<T> listener) {
		this.filterStatusListeners.remove(listener);
	}

	@Override
	public void filterStatusChanged(FilterChangedEvent<T> filterChangedEvent) {
		if (filterChangedEvent == null)
			return;

		Predicate<T> predicate = filterChangedEvent.getFilterStatus();
		if (filters.contains(predicate))
			handleFilterStatusChange();
	}

	/**
	 * Forces a re-broadcast of the current combined filter status, without
	 * changing which predicates are registered -- useful when a registered
	 * predicate's own evaluation criteria changed in place (e.g. a slider
	 * moved) without going through {@link #addFilter(Predicate)}.
	 */
	public void notifyFilterStatusChanged() {
		handleFilterStatusChange();
	}

	private void handleFilterStatusChange() {
		FilterChangedEvent<T> filterChangedEvent = new FilterChangedEvent<>(this, this);

		for (FilterStatusListener<T> filterStatusListener : filterStatusListeners)
			filterStatusListener.filterStatusChanged(filterChangedEvent);
	}

	@Override
	public boolean test(T t) {
		for (Predicate<T> p : filters)
			if (!p.test(t))
				return false;
		return true;
	}

}
