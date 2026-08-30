package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * <p>
 * Variant simulating the ColorBrewer schemes Yellow-Green-Blue.
 * 
 * Light yellow, light green, teal, navy Colormap.
 * 
 * Bezier interpolated but without correct lightness gradient (['#ffffe0',
 * '#d8f5c5', '#b2e2af', '#8ec79f', '#70a894', '#57848c', '#425f86', '#2c3682',
 * '#000080']).
 * 
 * See https://vis4.net/blog/posts/mastering-multi-hued-color-scales
 * </p>
 *
 * @since 2014
 */
public class LightYellowLightGreenTealNavy extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected LightYellowLightGreenTealNavy() {
		super();
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_LightYellowLightGreenTealNavy;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(255, 255, 224), new Color(216, 245, 197), new Color(178, 226, 175),
				new Color(142, 199, 159), new Color(112, 168, 148), new Color(87, 132, 140), new Color(66, 95, 134),
				new Color(44, 54, 130), new Color(0, 0, 128) };
	}

	public String toString() {
		return "Light yellow, light green, teal, navy";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new LightYellowLightGreenTealNavy();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
