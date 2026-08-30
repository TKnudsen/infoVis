package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;

/**
 * <p>
 * "Bremm et al. (regular)": a CIELab-space 2D colormap, re-implemented here
 * as an exact pixel-for-pixel port of the reference lookup image from
 * Color2D (https://github.com/dominikjaeckle/Color2D, Apache License 2.0,
 * Copyright 2017 Dominik Jaeckle), which itself re-implements the colormap
 * described in Steiger, M. et al., "Explorative Analysis of 2D Color Maps",
 * WSCG (2015). Cataloged as "Bremm et al. (regular)" in Bernard et al., "A
 * Survey and Task-Based Quality Assessment of Static 2D Colormaps" (SPIE
 * VDA, 2015), citing the original design: Bremm, S., von Landesberger, T.,
 * Bernard, J., and Schreck, T., "Assisted descriptor selection based on
 * visual comparative data analysis," EuroVis (2011). See
 * {@code NOTICE.txt} next to the bundled lookup image for full attribution.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class BremmColorMap2D extends LookupImageColorMap2D {

	public BremmColorMap2D() {
		super("bremm.png", ColorSpace.CIELAB);
	}

	@Override
	public String getName() {
		return "Bremm et al. (regular)";
	}

	@Override
	public String getDescription() {
		return "CIELab-based 2D colormap (Bremm et al. 2011, re-implemented per Steiger et al. 2015 / Color2D)";
	}

}
