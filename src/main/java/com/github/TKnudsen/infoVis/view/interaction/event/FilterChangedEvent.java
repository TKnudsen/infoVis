package com.github.TKnudsen.infoVis.view.interaction.event;

import java.util.EventObject;
import java.util.function.Predicate;

public class FilterChangedEvent<T> extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Predicate<T> filterStatus;

	public FilterChangedEvent(Object source, Predicate<T> filterStatus) {
		super(source);

		this.filterStatus = filterStatus;
	}

	public Predicate<T> getFilterStatus() {
		return filterStatus;
	}

}
