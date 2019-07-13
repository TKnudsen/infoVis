package com.github.TKnudsen.infoVis.view.interaction;

import java.beans.PropertyChangeEvent;
import java.util.Map;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class SelectionChangedEvent extends PropertyChangeEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -973114462820227456L;

	private long hash;

	private Map<?, Boolean> selectionStatus;

	public SelectionChangedEvent(Object arg0, String propertyChangeName, long hash, Map<?, Boolean> selection) {
		super(arg0, propertyChangeName, hash, selection);

		this.hash = hash;
		this.selectionStatus = selection;
	}

	public long getHash() {
		return hash;
	}

	public Map<?, Boolean> getSelectionStatus() {
		return selectionStatus;
	}
}
