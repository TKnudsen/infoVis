package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.bivariate;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.IColorMap2D;

/**
 * <p>
 * Sub-type of {@link IColorMap2D} for colormaps whose two axes are not
 * interchangeable, but have distinct roles: {@code x} encodes a data
 * <b>value</b>, {@code y} encodes the <b>uncertainty</b> of that value. This
 * is the "bivariate map" construction discussed in Correll, Moritz, and
 * Heer, "Value-Suppressing Uncertainty Palettes," CHI (2018) -- as opposed
 * to a generic 2D colormap (e.g. {@code AbstractColorMap2D}), where both
 * axes are typically two arbitrary, unrelated dimensions and neither reads
 * as "how confident are we in the other axis". Nothing about
 * {@link IColorMap2D}'s contract changes; this interface exists purely to
 * make the value/uncertainty semantics explicit and discoverable, and to
 * give bivariate-specific implementations ({@link TraditionalBivariateColorMap2D},
 * {@link ValueSuppressingUncertaintyColorMap2D}, {@link GenericBivariateColorMap2D})
 * a common, more specific type than "any 2D colormap".
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public interface IBivariateColorMap2D extends IColorMap2D {

	/**
	 * Alias for {@link #getColor(double, double)} with self-documenting
	 * parameter names -- purely a readability convenience at call sites, same
	 * underlying contract.
	 *
	 * @param value       in [0,1]
	 * @param uncertainty in [0,1]
	 */
	default Color getColorForValue(double value, double uncertainty) {
		return getColor(value, uncertainty);
	}

}
