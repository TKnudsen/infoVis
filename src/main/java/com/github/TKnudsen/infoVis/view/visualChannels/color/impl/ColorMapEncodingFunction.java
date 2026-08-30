package com.github.TKnudsen.infoVis.view.visualChannels.color.impl;

import java.awt.Paint;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;

/**
 * <p>
 * Bridges a quantitative 1D {@link AbstractColorMap1D} palette into the
 * domain-typed {@link IColorEncodingFunction} world: normalizes a domain object
 * T to {@code [0,1]} via the supplied function, then delegates the actual color
 * lookup to the palette. {@code AbstractColorMap1D} cannot implement
 * {@link IColorEncodingFunction} directly -- it already implements
 * {@code Function<Float, Color>}, and a class cannot also implement
 * {@code Function<T, Paint>} (what {@link IColorEncodingFunction} extends)
 * without a method-erasure clash -- so this adapter is the connective tissue
 * between the two layers: a palette (normalized number -&gt; color) versus a
 * visual encoding (arbitrary domain object -&gt; color).
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ColorMapEncodingFunction<T> implements IColorEncodingFunction<T>, ISelfDescription {

	private final ToDoubleFunction<T> normalizedValueFunction;
	private final AbstractColorMap1D colorMap;

	/**
	 * The constructor to reach for by default: extract a raw attribute from T,
	 * then rescale it to [0,1] with a {@link NormalizationFunction} (e.g.
	 * {@code LinearNormalizationFunction}) -- composing the two is done here, so
	 * callers do not have to write that composition by hand.
	 *
	 * @param extractor     pulls the raw, not-yet-normalized attribute value out
	 *                      of T
	 * @param normalization re-scales that raw attribute value to [0,1]; held by
	 *                      reference, so updating its bounds later (e.g. via
	 *                      {@code setGlobalMin}/{@code setGlobalMax}) is
	 *                      reflected on the next {@link #apply(Object)} without
	 *                      rebuilding this encoding function
	 * @param colorMap      the palette
	 */
	public ColorMapEncodingFunction(Function<? super T, ? extends Number> extractor,
			NormalizationFunction normalization, AbstractColorMap1D colorMap) {
		this(compose(extractor, normalization), colorMap);
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
	 * Number}, so anything that needs more than "extract one attribute, then
	 * rescale it" -- combining several fields into one derived value,
	 * a non-monotonic or cyclic mapping (e.g. day-of-year onto a repeating
	 * seasonal color), a lookup table -- has to be written by hand here instead.
	 * If your case IS "extract one attribute, then rescale it", prefer the other
	 * constructor; it is very easy to forget the rescale step here and pass a
	 * function that is not actually in [0,1] yet, which this constructor has no
	 * way to detect -- values outside [0,1] are silently clamped by most
	 * palettes rather than rejected.
	 *
	 * @param normalizedValueFunction must already return values in {@code [0,1]}
	 *                                for every T -- this constructor performs no
	 *                                rescaling of its own
	 * @param colorMap                the palette
	 */
	public ColorMapEncodingFunction(ToDoubleFunction<T> normalizedValueFunction, AbstractColorMap1D colorMap) {
		this.normalizedValueFunction = Objects.requireNonNull(normalizedValueFunction,
				"normalizedValueFunction must not be null");
		this.colorMap = Objects.requireNonNull(colorMap, "colorMap must not be null");
	}

	@Override
	public Paint apply(T t) {
		return colorMap.getColor((float) normalizedValueFunction.applyAsDouble(t));
	}

	public AbstractColorMap1D getColorMap() {
		return colorMap;
	}

	@Override
	public String getName() {
		return "ColorMapEncodingFunction (" + colorMap.getName() + ")";
	}

	@Override
	public String getDescription() {
		return "normalizes a domain value and delegates to " + colorMap.getName();
	}

}
