package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;

/**
 * <p>
 * "TeulingFig3": the diverging bivariate colour legend from Teuling, A. J.,
 * Stoeckli, R., and Seneviratne, S. I., "Bivariate colour maps for visualizing
 * climate data," International Journal of Climatology 31(9), 1408-1412
 * (2011) -- the paper's own Figure 3 (temperature/precipitation trends),
 * built from the two-step construction in the paper's Figure 4(c): three
 * corners of each RGB component's linear surface reach maximum intensity and
 * the opposite corner reaches zero, then a "whitening kernel" is added that
 * turns the medium-grey origin white while leaving the corners unchanged.
 * Cataloged as "TeulingFig3" in Bernard et al., "A Survey and Task-Based
 * Quality Assessment of Static 2D Colormaps" (SPIE VDA, 2015), and identified
 * there as the single best-performing colormap in their evaluation.
 * </p>
 *
 * <p>
 * Unlike the other Teuling variant in this package ({@link
 * TeulingFig2ColorMap2D}, ported pixel-for-pixel from Color2D), no reference
 * implementation of Figure 3 was available. Instead, the bundled lookup image
 * was built directly from the primary source: the paper states the exact
 * parameters used to plot its own Figure 3 ("n = 5 and a = 0.37"), and
 * Figure 3's own 5x5 legend swatch is printed as a flat, axis-aligned,
 * undistorted image -- so its 25 cells were sampled pixel-by-pixel from the
 * published PDF and bilinearly upsampled to give a smooth approximation
 * between those 25 real, published sample points. This is therefore an exact
 * reproduction of the 25 sampled colours and an interpolated approximation
 * everywhere else, not a re-derivation of the underlying per-channel
 * formulas.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class TeulingFig3ColorMap2D extends LookupImageColorMap2D {

	public TeulingFig3ColorMap2D() {
		super("teulingfig3.png", ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "TeulingFig3";
	}

	@Override
	public String getDescription() {
		return "Diverging bivariate colour map (Teuling et al. 2011, Figure 3; n=5, a=0.37)";
	}

}
