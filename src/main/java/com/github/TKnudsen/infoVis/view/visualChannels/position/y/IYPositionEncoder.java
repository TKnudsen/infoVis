package com.github.TKnudsen.infoVis.view.visualChannels.position.y;

import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;

/**
 * <p>
 * Indicates that the object provides an y-axis position encoding. Can be
 * externalized and be used outside the class.
 * </p>
 *
 * @version 1.01
 * @since 2016
 */
public interface IYPositionEncoder {

	public IPositionEncodingFunction getYPositionEncodingFunction();
}
