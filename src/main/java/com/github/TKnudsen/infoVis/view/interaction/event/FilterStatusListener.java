package com.github.TKnudsen.infoVis.view.interaction.event;

import java.util.EventListener;

public interface FilterStatusListener<T> extends EventListener {

	/**
	 * Invoked when the filter status has changed
	 * 
	 * @param event
	 */
	public void filterStatusChanged(FilterChangedEvent<T> filterChangedEvent);
}
