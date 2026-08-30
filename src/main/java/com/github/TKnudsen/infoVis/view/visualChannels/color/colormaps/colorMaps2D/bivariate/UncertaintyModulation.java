package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.bivariate;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces.ColorCIELab;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces.ColorConversions;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces.ColorHSL;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces.ColorXYZ;

/**
 * <p>
 * The three ways Correll, Moritz, and Heer, "Value-Suppressing Uncertainty
 * Palettes," CHI (2018) suggest turning a base "value" color into an
 * uncertainty-aware one, ported from the reference implementation's
 * {@code simpleScale} modes (https://github.com/uwdata/vsup,
 * {@code src/scale.js}: {@code "usl"}, {@code "us"}, {@code "ul"}). In all
 * three, {@code uncertainty = 0} leaves the base color unchanged and
 * {@code uncertainty = 1} reaches the mode's fully-suppressed extreme.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public enum UncertaintyModulation {

	/**
	 * The paper's default ("usl"): interpolate the base color toward white in
	 * CIELAB space. Reduces saturation and increases lightness simultaneously,
	 * which is why the paper describes its recommended channels as "increasing
	 * luminance and decreasing saturation" together rather than separately.
	 */
	LIGHTNESS_AND_SATURATION_LAB {
		@Override
		Color apply(Color base, double uncertainty) {
			ColorXYZ baseXyz = ColorConversions.convertRGBtoXYZ(base.getRGB());
			ColorCIELab baseLab = ColorConversions.convertXYZtoCIELab(baseXyz);

			double l = baseLab.L + (100.0 - baseLab.L) * uncertainty;
			double a = baseLab.a * (1.0 - uncertainty);
			double b = baseLab.b * (1.0 - uncertainty);

			ColorXYZ xyz = ColorConversions.convertCIELabtoXYZ(l, a, b);
			return new Color(ColorConversions.convertXYZtoRGB(xyz));
		}
	},

	/** ("us") Reduce HSL saturation toward 0; hue and lightness unchanged. */
	SATURATION_ONLY {
		@Override
		Color apply(Color base, double uncertainty) {
			ColorHSL hsl = ColorConversions.convertRGBtoHSL(base.getRGB());
			double s = hsl.S * (1.0 - uncertainty);
			return new Color(ColorConversions.convertHSLtoRGB(hsl.H, s, hsl.L));
		}
	},

	/** ("ul") Raise HSL lightness toward 1 (white); hue and saturation unchanged. */
	LIGHTNESS_ONLY {
		@Override
		Color apply(Color base, double uncertainty) {
			ColorHSL hsl = ColorConversions.convertRGBtoHSL(base.getRGB());
			double l = hsl.L + (1.0 - hsl.L) * uncertainty;
			return new Color(ColorConversions.convertHSLtoRGB(hsl.H, hsl.S, l));
		}
	};

	/**
	 * @param base        the color for the (unsuppressed) data value
	 * @param uncertainty in [0,1]; 0 = base color unchanged, 1 = fully
	 *                    suppressed
	 */
	abstract Color apply(Color base, double uncertainty);

}
