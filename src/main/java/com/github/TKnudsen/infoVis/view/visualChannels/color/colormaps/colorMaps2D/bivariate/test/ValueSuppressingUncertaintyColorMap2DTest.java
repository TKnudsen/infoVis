package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.bivariate.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.panels.QuadraticPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.Colormap2DPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.IColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.bivariate.BivariateQuantization;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.bivariate.GenericBivariateColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.bivariate.TraditionalBivariateColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.bivariate.UncertaintyModulation;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.bivariate.ValueSuppressingUncertaintyColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.RainbowColorMap;

/**
 * <p>
 * Compares three ways to build a value/uncertainty bivariate map, per Correll,
 * Moritz, and Heer, "Value-Suppressing Uncertainty Palettes," CHI (2018): a
 * genuinely <b>continuous</b> bivariate map (the paper's own "Continuous
 * Bivariate" comparison condition, Figure 6 -- value varies smoothly, no
 * binning at all), the paper's literal Figure 1 <b>discrete</b> baseline (a
 * fixed 4x4 grid -- "bivariate maps in practice are often limited to a small
 * set of output colors, say a 4x4 matrix"), and the <b>VSUP</b>. Value is
 * discretized in the first two only because the respective class was told to
 * be; nothing about {@link TraditionalBivariateColorMap2D} requires it, so this
 * demo does not treat "traditional" and "discrete" as synonyms --
 * {@link GenericBivariateColorMap2D} with {@code quantization = null} is just
 * as much a "traditional" bivariate map, and arguably the more common default
 * in practice. VSUP's discreteness, in contrast, is inherent to the technique:
 * "collapsing" values together is only meaningful if there is a finite set of
 * outputs to collapse onto. The paper recommends Viridis for the value axis
 * specifically because it avoids very light/dark colors that would interfere
 * with the lightness-based uncertainty encoding; this library does not have a
 * Viridis port, so {@link RainbowColorMap} is used here instead purely as a
 * stand-in.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ValueSuppressingUncertaintyColorMap2DTest {

	public static void main(String[] args) {

		AbstractColorMap1D palette = (AbstractColorMap1D) RainbowColorMap.getInstance();

		IColorMap2D continuous = new GenericBivariateColorMap2D(palette,
				UncertaintyModulation.LIGHTNESS_AND_SATURATION_LAB);
		IColorMap2D discrete = new TraditionalBivariateColorMap2D(palette, 4);
		IColorMap2D vsup = new ValueSuppressingUncertaintyColorMap2D(palette, 2, 4);

		JPanel wrapper = new JPanel(new GridLayout(1, 3, 20, 0));
		wrapper.add(swatch(continuous, "Continuous Bivariate Map"));
		wrapper.add(swatch(discrete, "Discrete Bivariate Map (4x4, Fig. 1 baseline)"));
		wrapper.add(swatch(vsup, "VSUP (branch=2, layers=4 = 15 colors)"));

		SVGFrameTools.dropSVGFrame(wrapper, "Value-Suppressing Uncertainty Palettes (Correll, Moritz & Heer 2018)",
				1020, 380);

		// Also demonstrate the fully generic, parametric class with a hand-picked,
		// non-default configuration.
		GenericBivariateColorMap2D custom = new GenericBivariateColorMap2D(palette, BivariateQuantization.tree(3, 3),
				UncertaintyModulation.SATURATION_ONLY);
		JPanel customWrapper = new JPanel(new BorderLayout());
		customWrapper.add(swatch(custom, "Custom: branch=3, layers=3, saturation-only suppression"),
				BorderLayout.CENTER);
		customWrapper.setPreferredSize(new Dimension(340, 320));
		SVGFrameTools.dropSVGFrame(customWrapper, "GenericBivariateColorMap2D: a hand-picked configuration", 360, 340);
	}

	private static JPanel swatch(IColorMap2D colorMap, String title) {
		JPanel panel = new JPanel(new BorderLayout());

		JLabel label = new JLabel(title, SwingConstants.CENTER);
		panel.add(label, BorderLayout.NORTH);

		// A 2D colormap's domain is [0,1]x[0,1] -- a square -- so its swatch should
		// be one too, regardless of how much width the panel happens to have.
		Colormap2DPanel gradient = new Colormap2DPanel(colorMap);
		QuadraticPanel quadraticGradient = new QuadraticPanel(gradient);
		quadraticGradient.setPreferredSize(new Dimension(280, 280));
		panel.add(quadraticGradient, BorderLayout.CENTER);

		JLabel xAxis = new JLabel("Value ->", SwingConstants.CENTER);
		panel.add(xAxis, BorderLayout.SOUTH);

		JLabel yAxis = new JLabel("<html><body style='writing-mode: vertical-rl'>Uncertainty -></body></html>");
		panel.add(yAxis, BorderLayout.WEST);

		return panel;
	}

}
