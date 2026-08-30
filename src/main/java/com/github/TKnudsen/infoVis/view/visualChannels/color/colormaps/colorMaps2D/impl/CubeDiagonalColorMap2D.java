package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;

/**
 * <p>
 * "Cube Diagonal": an sRGB cube diagonal-cut colormap, re-implemented here
 * as an exact pixel-for-pixel port of the reference lookup image from
 * Color2D (https://github.com/dominikjaeckle/Color2D, Apache License 2.0,
 * Copyright 2017 Dominik Jaeckle), which credits it to Steiger, M. et al.,
 * "Explorative Analysis of 2D Color Maps," WSCG (2015). Unlike
 * {@link CubeDiagonalCutBCYRColorMap2D} and
 * {@link CubeDiagonalCutBMYGColorMap2D} in this package -- which
 * approximate the "Cube Diagonal Cut" family cataloged in Bernard et al.'s
 * companion survey by bilinearly interpolating the named corner colors --
 * this one is exact reference pixel data, though it is not confirmed which
 * (if either) of the survey's two named variants (B-C-Y-R / B-M-Y-G) it
 * corresponds to. See {@code NOTICE.txt} next to the bundled lookup image
 * for full attribution.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class CubeDiagonalColorMap2D extends LookupImageColorMap2D {

	public CubeDiagonalColorMap2D() {
		super("cubediagonal.png", ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "Cube Diagonal (Steiger et al.)";
	}

	@Override
	public String getDescription() {
		return "RGB cube diagonal-cut colormap (re-implemented per Steiger et al. 2015 / Color2D)";
	}

}
