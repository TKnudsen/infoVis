package com.github.TKnudsen.infoVis.view.visualChannels.color;

import java.awt.Paint;
import java.util.function.Function;

/**
 * <p>
 * Provides the ability to encode colors in the implementing context
 * </p>
 *
 * @version 1.03
 * @since 2016
 */
public interface IColorEncoding<T> {

	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction);
}
