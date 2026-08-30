package com.github.TKnudsen.infoVis.view.interaction.event;

import java.util.EventListener;

/**
 * @version 1.01
 * @since 2016
 */
public interface NumberIntervalChangeListener extends EventListener {

	/**
	 * Invoked when the number interval has changed
	 * 
	 * @param event the event
	 */
	public void numberIntervalChanged(NumberIntervalChangedEvent event);
}
