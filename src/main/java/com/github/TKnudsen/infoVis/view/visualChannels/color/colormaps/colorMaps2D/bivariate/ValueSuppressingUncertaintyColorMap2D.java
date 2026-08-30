package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.bivariate;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;

/**
 * <p>
 * A colormap implementing the technique introduced by Correll, Moritz, and
 * Heer, "Value-Suppressing Uncertainty Palettes," CHI (2018) -- a "VSUP" in
 * the paper's own shorthand. Unlike {@link TraditionalBivariateColorMap2D}'s
 * uniform grid, VSUPs use a quantization tree: values above a given
 * uncertainty threshold collapse into a single, maximally-suppressed "root"
 * bin, and as uncertainty decreases the tree branches -- revealing
 * {@code branchingFactor} times as many distinguishable value bins at each
 * successive layer. With {@code branchingFactor=2} and {@code layers=4}
 * (the paper's own Figure 1 example), this yields {@code 1+2+4+8=15}
 * distinct output colors. A thin preset of {@link GenericBivariateColorMap2D}
 * with {@link BivariateQuantization#tree(int, int)} and the paper's default
 * modulation.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ValueSuppressingUncertaintyColorMap2D extends GenericBivariateColorMap2D {

	/**
	 * @param valueColorMap   the base colormap for the value axis
	 * @param branchingFactor number of children per tree node, e.g. 2 for a
	 *                        binary tree
	 * @param layers          tree depth (number of uncertainty bins)
	 */
	public ValueSuppressingUncertaintyColorMap2D(AbstractColorMap1D valueColorMap, int branchingFactor,
			int layers) {
		this(valueColorMap, branchingFactor, layers, UncertaintyModulation.LIGHTNESS_AND_SATURATION_LAB);
	}

	public ValueSuppressingUncertaintyColorMap2D(AbstractColorMap1D valueColorMap, int branchingFactor,
			int layers, UncertaintyModulation modulation) {
		super(valueColorMap, BivariateQuantization.tree(branchingFactor, layers), modulation);
	}

	@Override
	public String getName() {
		return "VSUP (" + getValueColorMap().getName() + ")";
	}

	@Override
	public String getDescription() {
		return "Value-Suppressing Uncertainty Palette (Correll, Moritz & Heer 2018)";
	}

}
