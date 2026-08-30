package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;

/**
 * <p>
 * "Ziegler": an sRGB 2D colormap, re-implemented here as an exact
 * pixel-for-pixel port of the reference lookup image from Color2D
 * (https://github.com/dominikjaeckle/Color2D, Apache License 2.0, Copyright
 * 2017 Dominik Jaeckle), which itself re-implements the colormap described
 * in Steiger, M. et al., "Explorative Analysis of 2D Color Maps", WSCG
 * (2015), citing the original design: Ziegler, H., Nietzschmann, T., and
 * Keim, D. A., "Visual exploration and discovery of atypical behavior in
 * financial time series data using two-dimensional colormaps," Information
 * Visualization (2007) -- also cited (for the corner colors, not this exact
 * pixel data) as "Four Corners R-B-G-Y" in Bernard et al., "A Survey and
 * Task-Based Quality Assessment of Static 2D Colormaps" (SPIE VDA, 2015).
 * See {@code NOTICE.txt} next to the bundled lookup image for full
 * attribution.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ZieglerColorMap2D extends LookupImageColorMap2D {

	public ZieglerColorMap2D() {
		super("ziegler.png", ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "Ziegler et al.";
	}

	@Override
	public String getDescription() {
		return "2D colormap (Ziegler et al. 2007, re-implemented per Steiger et al. 2015 / Color2D)";
	}

}
