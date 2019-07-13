package com.github.TKnudsen.infoVis.view.visualChannels.color;

import java.awt.Paint;
import java.util.function.Function;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Provides the ability to encode colors in the implementing context
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public interface IColorEncoding<T> {

	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction);
}
