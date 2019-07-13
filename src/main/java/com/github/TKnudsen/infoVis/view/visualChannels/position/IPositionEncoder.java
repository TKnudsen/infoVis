package com.github.TKnudsen.infoVis.view.visualChannels.position;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Indicates that the class provides an axis position encoding. The orientation
 * (x/y) is abstracted. Can be externalized and be used outside the class.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.06
 */
public interface IPositionEncoder {

	public IPositionEncodingFunction getPositionEncodingFunction();
}
