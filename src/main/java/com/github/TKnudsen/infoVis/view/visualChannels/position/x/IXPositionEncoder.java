package com.github.TKnudsen.infoVis.view.visualChannels.position.x;

import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;

/**
 * <p>
 * Indicates that the object provides an x-axis position encoding. Can be
 * externalized and be used outside the class.
 * </p>
 *
 * @version 1.01
 * @since 2016
 */
public interface IXPositionEncoder {

	public IPositionEncodingFunction getXPositionEncodingFunction();
}
