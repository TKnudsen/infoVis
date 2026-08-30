package com.github.TKnudsen.infoVis.view.visualChannels.position;

/**
 * <p>
 * Indicates that the class provides an axis position encoding. The orientation
 * (x/y) is abstracted. Can be externalized and be used outside the class.
 * </p>
 *
 * @version 1.06
 * @since 2016
 */
public interface IPositionEncoder {

	public IPositionEncodingFunction getPositionEncodingFunction();
}
