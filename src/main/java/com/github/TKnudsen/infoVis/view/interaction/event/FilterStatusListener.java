package com.github.TKnudsen.infoVis.view.interaction.event;

import java.util.EventListener;

/**
 * <p>
 * Listener notified when a {@link FilterChangedEvent} occurs.
 * </p>
 *
 * @version 1.0
 */
public interface FilterStatusListener<T> extends EventListener {

	/**
	 * Invoked when the filter status has changed
	 * 
	 * @param filterChangedEvent the event
	 */
	public void filterStatusChanged(FilterChangedEvent<T> filterChangedEvent);
}
