package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;

/**
 * <p>
 * "Schumann": a 2D colormap re-implemented here as an exact pixel-for-pixel
 * port of the reference lookup image from Color2D
 * (https://github.com/dominikjaeckle/Color2D, Apache License 2.0, Copyright
 * 2017 Dominik Jaeckle), which credits it to Steiger, M. et al.,
 * "Explorative Analysis of 2D Color Maps," WSCG (2015), attributed there to
 * Schuhmann and Urban. Not one of the 22 colormaps cataloged in Bernard et
 * al.'s companion survey, "A Survey and Task-Based Quality Assessment of
 * Static 2D Colormaps" (SPIE VDA, 2015), but from the same author group's
 * sibling publication. See {@code NOTICE.txt} next to the bundled lookup
 * image for full attribution.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class SchumannColorMap2D extends LookupImageColorMap2D {

	public SchumannColorMap2D() {
		super("schumann.png", ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "Schumann and Urban";
	}

	@Override
	public String getDescription() {
		return "2D colormap (Schumann & Urban, re-implemented per Steiger et al. 2015 / Color2D)";
	}

}
