package com.github.TKnudsen.infoVis.view.visualChannels.position.x;

import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Indicates that the object includes a x-axis position encoding. Allows to set
 * the position encoding function from an external source.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2018 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public interface IXPositionEncoding {

	public void setXPositionEncodingFunction(IPositionEncodingFunction xPositionEncodingFunction);
}
