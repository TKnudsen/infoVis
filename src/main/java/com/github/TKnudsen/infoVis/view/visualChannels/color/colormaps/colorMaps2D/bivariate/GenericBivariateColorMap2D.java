package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.bivariate;

import java.awt.Color;
import java.util.Objects;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;

/**
 * <p>
 * Builds any value/uncertainty bivariate colormap in the style of Correll,
 * Moritz, and Heer, "Value-Suppressing Uncertainty Palettes," CHI (2018)
 * from three independent, freely composable choices: a base {@code value}
 * colormap, an optional {@link BivariateQuantization} strategy (snapping
 * continuous inputs to discrete legend bins -- omit it entirely, via the
 * two-argument constructor, for a continuous, unquantized bivariate map,
 * matching the paper's "Continuous Bivariate" comparison condition), and an
 * {@link UncertaintyModulation} describing how uncertainty suppresses the
 * value color. Both dedicated
 * classes in this package are thin presets of this one:
 * {@link TraditionalBivariateColorMap2D} fixes the quantization to a
 * uniform grid, {@link ValueSuppressingUncertaintyColorMap2D} fixes
 * it to the paper's quantization tree -- both still expose the same
 * underlying choices.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class GenericBivariateColorMap2D extends AbstractColorMap2D implements IBivariateColorMap2D {

	private final AbstractColorMap1D valueColorMap;
	private final BivariateQuantization quantization;
	private final UncertaintyModulation modulation;

	/**
	 * The constructor to reach for by default: a continuous, unquantized
	 * bivariate map -- value varies smoothly, and uncertainty smoothly
	 * suppresses it. Equivalent to the three-argument constructor with
	 * {@code quantization = null}, without callers needing to know that
	 * {@code null} is a meaningful choice here rather than an oversight.
	 *
	 * @param valueColorMap the base colormap for the value axis (x)
	 * @param modulation    how uncertainty (y) suppresses the value color
	 */
	public GenericBivariateColorMap2D(AbstractColorMap1D valueColorMap, UncertaintyModulation modulation) {
		this(valueColorMap, null, modulation);
	}

	/**
	 * Escape hatch for when a continuous map is not what you want: snaps
	 * {@code (value, uncertainty)} to discrete legend bins before coloring,
	 * e.g. {@link BivariateQuantization#uniformGrid(int)} or
	 * {@link BivariateQuantization#tree(int, int)} for a VSUP-style map. If
	 * you just want "a beautiful continuous 2D colormap", prefer the
	 * two-argument constructor instead -- it is exactly this constructor
	 * with {@code quantization = null}, spelled out so you do not have to
	 * pass {@code null} for a parameter you do not care about.
	 *
	 * @param valueColorMap the base colormap for the value axis (x)
	 * @param quantization  bin-snapping strategy; {@code null} also works
	 *                      here and behaves identically to the two-argument
	 *                      constructor, but prefer that constructor when
	 *                      that is the intent
	 * @param modulation    how uncertainty (y) suppresses the value color
	 */
	public GenericBivariateColorMap2D(AbstractColorMap1D valueColorMap, BivariateQuantization quantization,
			UncertaintyModulation modulation) {
		super((x, y) -> colorFor(x, y, valueColorMap, quantization, modulation), valueColorMap.getColorSpace());

		this.valueColorMap = Objects.requireNonNull(valueColorMap, "valueColorMap must not be null");
		this.quantization = quantization;
		this.modulation = Objects.requireNonNull(modulation, "modulation must not be null");
	}

	private static Color colorFor(Double value, Double uncertainty, AbstractColorMap1D valueColorMap,
			BivariateQuantization quantization, UncertaintyModulation modulation) {

		double v = value;
		double u = uncertainty;

		if (quantization != null) {
			BivariateQuantization.Quantized q = quantization.quantize(v, u);
			v = q.value;
			u = q.uncertainty;
		}

		Color base = valueColorMap.getColor((float) v);
		return modulation.apply(base, u);
	}

	public AbstractColorMap1D getValueColorMap() {
		return valueColorMap;
	}

	public BivariateQuantization getQuantization() {
		return quantization;
	}

	public UncertaintyModulation getModulation() {
		return modulation;
	}

	@Override
	public String getName() {
		return "Bivariate (" + valueColorMap.getName() + ", " + modulation + ")";
	}

	@Override
	public String getDescription() {
		return "Value/uncertainty bivariate colormap over " + valueColorMap.getName() + ", suppressing value with "
				+ modulation + " as uncertainty increases"
				+ (quantization == null ? " (continuous)" : " (quantized)");
	}

}
