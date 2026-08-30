package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.bivariate;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;

/**
 * <p>
 * The "traditional bivariate map" baseline that Correll, Moritz, and Heer,
 * "Value-Suppressing Uncertainty Palettes," CHI (2018) compare their
 * technique against throughout the paper (e.g. Figures 2 and 4): a uniform
 * {@code n x n} grid -- exactly {@code n} equally-sized value bins at every
 * one of {@code n} uncertainty levels, regardless of how uncertain that
 * level is. Unlike {@link ValueSuppressingUncertaintyColorMap2D},
 * value resolution does not degrade with uncertainty, which is precisely
 * the property the paper argues against for uncertainty-aware
 * decision-making. A thin preset of {@link GenericBivariateColorMap2D} with
 * {@link BivariateQuantization#uniformGrid(int)} and the paper's default
 * modulation.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class TraditionalBivariateColorMap2D extends GenericBivariateColorMap2D {

	/**
	 * @param valueColorMap the base colormap for the value axis
	 * @param n             number of uncertainty levels, and of value bins per
	 *                      level (a "traditional" bivariate map is square)
	 */
	public TraditionalBivariateColorMap2D(AbstractColorMap1D valueColorMap, int n) {
		this(valueColorMap, n, UncertaintyModulation.LIGHTNESS_AND_SATURATION_LAB);
	}

	public TraditionalBivariateColorMap2D(AbstractColorMap1D valueColorMap, int n, UncertaintyModulation modulation) {
		super(valueColorMap, BivariateQuantization.uniformGrid(n), modulation);
	}

	@Override
	public String getName() {
		return "Traditional Bivariate Map (" + getValueColorMap().getName() + ")";
	}

	@Override
	public String getDescription() {
		return "Uniform-grid value/uncertainty bivariate map (Correll, Moritz & Heer 2018 baseline)";
	}

}
