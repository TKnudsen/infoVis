package com.github.TKnudsen.infoVis.view.visualChannels.size;

import java.util.function.Function;

/**
 * <p>
 * Provides the ability to encode the size in the implementing context
 * </p>
 *
 * @version 1.02
 * @since 2016
 */
public interface ISizeEncoding<T> {

	public void setSizeEncodingFunction(Function<? super T, Double> sizeEncodingFunction);
}
