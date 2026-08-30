package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * <p>
 * Hue-Lightness colormap that between (dark) blue and (light)
 * yellow. preserves contrast to black and white colors.
 * 
 * Lookup colormap creation at:
 * http://tristen.ca/hcl-picker/#/hlc/9/1/2E4052/F9E261
 * </p>
 *
 * @since 2014
 */
public class DarkBlueGreenLightYellowHueLightnessColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected DarkBlueGreenLightYellowHueLightnessColorMap() {
		super();
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_DarkBlueGreenLightYellowHueLightness;
	}

	@Override
	protected void setColors() {
		// colors = new Color[] { new Color(29, 45, 57), new Color(29, 68, 78),
		// new Color(27, 92, 94), new Color(35, 118, 104), new Color(59, 143,
		// 108), new Color(95, 167, 106), new Color(139, 189, 100), new
		// Color(191, 209, 96), new Color(249, 226, 97) };
		colors = new Color[] { new Color(46, 64, 82), new Color(42, 86, 101), new Color(35, 110, 114),
				new Color(39, 133, 120), new Color(63, 155, 120), new Color(99, 177, 114), new Color(143, 196, 105),
				new Color(193, 213, 98), new Color(249, 226, 97) };
	}

	public String toString() {
		return "Dark blue, green, light yellow. Hue-lightness";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new DarkBlueGreenLightYellowHueLightnessColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
