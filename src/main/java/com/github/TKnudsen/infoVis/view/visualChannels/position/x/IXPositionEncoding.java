package com.github.TKnudsen.infoVis.view.visualChannels.position.x;

import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;

/**
 * <p>
 * Indicates that the object includes a x-axis position encoding. Allows to set
 * the position encoding function from an external source.
 * </p>
 *
 * @version 1.04
 * @since 2016
 */
public interface IXPositionEncoding {

	public void setXPositionEncodingFunction(IPositionEncodingFunction xPositionEncodingFunction);
}
