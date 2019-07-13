package com.github.TKnudsen.infoVis.view.visualChannels.position.x;

import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Indicates that the object provides an x-axis position encoding. Can be
 * externalized and be used outside the class.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2018 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public interface IXPositionEncoder {

	public IPositionEncodingFunction getXPositionEncodingFunction();
}
