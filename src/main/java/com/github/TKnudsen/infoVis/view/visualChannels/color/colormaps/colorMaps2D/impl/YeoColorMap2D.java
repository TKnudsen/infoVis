package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;

/**
 * <p>
 * RGB colormap with four color anchors -- Green, Dark Green, Pink, and
 * Purple -- bilinearly interpolated. Cataloged in Bernard et al., "A Survey
 * and Task-Based Quality Assessment of Static 2D Colormaps" (SPIE VDA,
 * 2015), citing Yeo, N. et al., "Colour image segmentation using the
 * self-organizing map and adaptive resonance theory", Image and Vision
 * Computing 23(12) (2005). The survey describes the original as a
 * "topological map in geometric RGB color space", which may involve a
 * non-bilinear geometry not fully specified there; this approximates it as
 * a bilinear interpolation between the same four named anchors, in the
 * absence of a more precise formula. The two greens and the two
 * pink/purple tones are placed on opposite diagonals (rather than adjacent
 * corners) so all four hues remain visually distinct across the swatch.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class YeoColorMap2D extends AbstractColorMap2D {

	public YeoColorMap2D() {
		super((x, y) -> AnchorColorInterpolation.bilinear(x, y, Color.GREEN, new Color(128, 0, 128),
				Color.GREEN.darker(), new Color(255, 105, 180)), ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "Yeo et al.";
	}

	@Override
	public String getDescription() {
		return "RGB colormap with anchors Green, Dark Green, Pink, Purple, approximated as bilinear "
				+ "(Yeo et al. 2005, re-implemented per Bernard et al. 2015)";
	}

}
