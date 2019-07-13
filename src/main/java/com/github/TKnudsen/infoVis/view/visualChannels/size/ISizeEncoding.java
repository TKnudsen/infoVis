package com.github.TKnudsen.infoVis.view.visualChannels.size;

import java.util.function.Function;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Provides the ability to encode the size in the implementing context
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public interface ISizeEncoding<T> {

	public void setSizeEncodingFunction(Function<? super T, Double> sizeEncodingFunction);
}
