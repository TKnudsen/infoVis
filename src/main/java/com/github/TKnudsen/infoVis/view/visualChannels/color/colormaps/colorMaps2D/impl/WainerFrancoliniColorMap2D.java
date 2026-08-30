package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;

/**
 * <p>
 * RGB colormap with a 4x4 grid of discrete color anchors -- Red, Blue,
 * Green, and Yellow in the corners -- reproducing the original 1980 study's
 * discrete color patches rather than a smooth gradient. Bilinearly
 * interpolated between the same four corners as {@link FourCornersColorMap2D},
 * then snapped to a 4x4 grid. Cataloged in Bernard et al., "A Survey and
 * Task-Based Quality Assessment of Static 2D Colormaps" (SPIE VDA, 2015),
 * citing Wainer, H. and Francolini, C. M., "An empirical inquiry concerning
 * human understanding of two-variable color maps", The American Statistician
 * 34(2) (1980).
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class WainerFrancoliniColorMap2D extends AbstractColorMap2D {

	private static final int LEVELS = 4;

	public WainerFrancoliniColorMap2D() {
		super((x, y) -> AnchorColorInterpolation.bilinear(quantize(x), quantize(y), Color.RED, Color.BLUE,
				Color.GREEN, Color.YELLOW), ColorSpace.RGB);
	}

	private static double quantize(double v) {
		return Math.round(v * (LEVELS - 1)) / (double) (LEVELS - 1);
	}

	@Override
	public String getName() {
		return "Wainer and Francolini";
	}

	@Override
	public String getDescription() {
		return "RGB colormap with 4x4 discrete color anchors: Red, Blue, Green, Yellow "
				+ "(Wainer & Francolini 1980, re-implemented per Bernard et al. 2015)";
	}

}
