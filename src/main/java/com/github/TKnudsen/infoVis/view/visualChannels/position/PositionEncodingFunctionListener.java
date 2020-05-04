package com.github.TKnudsen.infoVis.view.visualChannels.position;

import java.util.EventListener;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 *
 */
public interface PositionEncodingFunctionListener extends EventListener {

	/**
	 * Will be called when when the functionality (the internal state) of the
	 * {@link IPositionEncodingFunction} was changed.
	 */
	void encodingFunctionChanged();
}
