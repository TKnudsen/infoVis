package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;

/**
 * <p>
 * "TeulingFig2": an sRGB bivariate colormap, re-implemented here as an exact
 * pixel-for-pixel port of the reference lookup image from Color2D
 * (https://github.com/dominikjaeckle/Color2D, Apache License 2.0, Copyright
 * 2017 Dominik Jaeckle), which itself re-implements the colormap described
 * in Steiger, M. et al., "Explorative Analysis of 2D Color Maps", WSCG
 * (2015). Cataloged as "TeulingFig2" in Bernard et al., "A Survey and
 * Task-Based Quality Assessment of Static 2D Colormaps" (SPIE VDA, 2015),
 * citing the original design: Teuling, A. J., Stoeckli, R., and Seneviratne,
 * S. I., "Bivariate colour maps for visualizing climate data," International
 * Journal of Climatology 31(9) (2011). See {@code NOTICE.txt} next to the
 * bundled lookup image for full attribution.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class TeulingFig2ColorMap2D extends LookupImageColorMap2D {

	public TeulingFig2ColorMap2D() {
		super("teulingfig2.png", ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "TeulingFig2";
	}

	@Override
	public String getDescription() {
		return "Bivariate colour map (Teuling et al. 2011, re-implemented per Steiger et al. 2015 / Color2D)";
	}

}
