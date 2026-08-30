package com.github.TKnudsen.infoVis.view.interaction.event;

import java.util.EventObject;

import com.github.TKnudsen.ComplexDataObject.data.interval.NumberInterval;

/**
 * @version 1.02
 * @since 2016
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
