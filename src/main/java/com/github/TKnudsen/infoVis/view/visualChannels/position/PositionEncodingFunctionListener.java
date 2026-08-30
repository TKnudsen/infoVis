package com.github.TKnudsen.infoVis.view.visualChannels.position;

import java.util.EventListener;

/**
 * @version 1.01
 * @since 2016
 */
public interface PositionEncodingFunctionListener extends EventListener {

	/**
	 * Will be called when when the functionality (the internal state) of the
	 * {@link IPositionEncodingFunction} was changed.
	 */
	void encodingFunctionChanged();
}
