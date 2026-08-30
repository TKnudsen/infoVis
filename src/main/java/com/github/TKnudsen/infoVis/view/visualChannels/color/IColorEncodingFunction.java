package com.github.TKnudsen.infoVis.view.visualChannels.color;

import java.awt.Paint;
import java.util.function.Function;

/**
 * Marker interface for functions that encode a data item of type {@code T}
 * as its visual {@link Paint} (typically a {@link java.awt.Color}) - the
 * color-channel counterpart to the position and size encoding functions.
 *
 * @version 1.03
 * @since 2016
 */
public interface IColorEncodingFunction<T> extends Function<T, Paint> {

}