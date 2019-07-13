package com.github.TKnudsen.infoVis.view.interaction.event;

import java.util.EventObject;

import com.github.TKnudsen.ComplexDataObject.data.interval.NumberInterval;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Event is used to notify interested parties that the number interval of some
 * particular context has changed in the event source.
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class NumberIntervalChangedEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1449601603473381014L;

	private final NumberInterval newNumberInterval;

	private final NumberInterval oldNumberInterval;

	public NumberIntervalChangedEvent(Object source, NumberInterval oldNumberInterval,
			NumberInterval newNumberInterval) {
		super(source);
		this.newNumberInterval = newNumberInterval;
		this.oldNumberInterval = oldNumberInterval;
	}

	public NumberInterval getOldNumberInterval() {
		return oldNumberInterval;
	}

	public NumberInterval getNewNumberInterval() {
		return newNumberInterval;
	}

}
