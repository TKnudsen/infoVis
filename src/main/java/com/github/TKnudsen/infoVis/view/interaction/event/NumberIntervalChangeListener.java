package com.github.TKnudsen.infoVis.view.interaction.event;

import java.util.EventListener;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Invoked when the number interval has changed.
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public interface NumberIntervalChangeListener extends EventListener {

	/**
	 * Invoked when the number interval has changed
	 * 
	 * @param event
	 */
	public void numberIntervalChanged(NumberIntervalChangedEvent event);
}
