package com.github.TKnudsen.infoVis.view.visualChannels.color.impl;

import java.awt.Paint;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.IColorMap2D;

/**
 * <p>
 * Bridges a 2D {@link IColorMap2D} palette into the domain-typed
 * {@link IColorEncodingFunction} world: normalizes a domain object T along
 * two independent {@code [0,1]} axes, then delegates the color lookup to the
 * palette.
 * 
 * {@link IColorMap2D} is intentionally just an interface, not tied to any
 * particular implementation strategy -- a palette backed by a third-party
 * library that cannot be published (e.g. the legacy {@code ColorMapLib}'s
 * Fraunhofer-IGD-derived 2D colormaps, retrofitted to implement this
 * package's {@code IColorMap2D}) plugs into this adapter exactly the same
 * way as one built on {@link com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D}
 * -- the source of the palette never needs to live in this project for it to
 * receive the same encoding-function behavior as everything else here.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ColorMapEncodingFunction2D<T> implements IColorEncodingFunction<T>, ISelfDescription {

	private final ToDoubleFunction<T> normalizedValueFunctionX;
	private final ToDoubleFunction<T> normalizedValueFunctionY;
	private final IColorMap2D colorMap;

	/**
	 * The constructor to reach for by default: extract a raw attribute per axis,
	 * then rescale each to [0,1] with its own {@link NormalizationFunction} (e.g.
	 * {@code LinearNormalizationFunction}) -- composing the two per axis is done
	 * here, so callers do not have to write that composition by hand, twice.
	 *
	 * @param extractorX     pulls the raw x attribute out of T
	 * @param normalizationX rescales the raw x value to [0,1]; held by
	 *                       reference, so updating its bounds later is reflected
	 *                       on the next {@link #apply(Object)} without
	 *                       rebuilding this encoding function
	 * @param extractorY     pulls the raw y attribute out of T
	 * @param normalizationY rescales the raw y value to [0,1]; same
	 *                       held-by-reference behavior as normalizationX
	 * @param colorMap       the palette
	 */
	public ColorMapEncodingFunction2D(Function<? super T, ? extends Number> extractorX,
			NormalizationFunction normalizationX, Function<? super T, ? extends Number> extractorY,
			NormalizationFunction normalizationY, IColorMap2D colorMap) {
		this(compose(extractorX, normalizationX), compose(extractorY, normalizationY), colorMap);
	}

	private static <T> ToDoubleFunction<T> compose(Function<? super T, ? extends Number> extractor,
			NormalizationFunction normalization) {
		Objects.requireNonNull(extractor, "extractor must not be null");
		Objects.requireNonNull(normalization, "normalization must not be null");
		return t -> normalization.apply(extractor.apply(t)).doubleValue();
	}

	/**
	 * Escape hatch for the minority of cases the constructor above cannot
	 * express: {@link NormalizationFunction} is strictly {@code Number ->
	 * Number}, so anything that needs more than "extract one attribute per axis,
	 * then rescale it" -- an axis derived from several fields, a non-monotonic
	 * or cyclic mapping -- has to be written by hand here instead. If your case
	 * IS "extract one attribute per axis, then rescale it", prefer the other
	 * constructor; it is very easy to forget the rescale step here and pass a
	 * function that is not actually in [0,1] yet, which this constructor has no
	 * way to detect -- values outside [0,1] are silently clamped by most
	 * palettes rather than rejected.
	 *
	 * @param normalizedValueFunctionX must already return values in
	 *                                  {@code [0,1]} for every T -- no rescaling
	 *                                  happens here
	 * @param normalizedValueFunctionY same contract as
	 *                                  {@code normalizedValueFunctionX}, for the
	 *                                  y axis
	 * @param colorMap                  the palette
	 */
	public ColorMapEncodingFunction2D(ToDoubleFunction<T> normalizedValueFunctionX,
			ToDoubleFunction<T> normalizedValueFunctionY, IColorMap2D colorMap) {
		this.normalizedValueFunctionX = Objects.requireNonNull(normalizedValueFunctionX,
				"normalizedValueFunctionX must not be null");
		this.normalizedValueFunctionY = Objects.requireNonNull(normalizedValueFunctionY,
				"normalizedValueFunctionY must not be null");
		this.colorMap = Objects.requireNonNull(colorMap, "colorMap must not be null");
	}

	@Override
	public Paint apply(T t) {
		return colorMap.getColor(normalizedValueFunctionX.applyAsDouble(t), normalizedValueFunctionY.applyAsDouble(t));
	}

	public IColorMap2D getColorMap() {
		return colorMap;
	}

	@Override
	public String getName() {
		return "ColorMapEncodingFunction2D (" + colorMap.getName() + ")";
	}

	@Override
	public String getDescription() {
		return "normalizes a domain value along two axes and delegates to " + colorMap.getName();
	}

}
