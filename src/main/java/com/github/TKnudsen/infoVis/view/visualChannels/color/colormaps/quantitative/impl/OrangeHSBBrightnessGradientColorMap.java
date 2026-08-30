package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @version 1.0
 * @since 2012
 */
public class OrangeHSBBrightnessGradientColorMap extends AbstractColorMap1D {
	protected static AbstractColorMap instance;

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(143, 85, 17), new Color(179, 107, 21), new Color(215, 128, 25),
				new Color(232, 148, 48), new Color(236, 166, 84), new Color(240, 185, 120), new Color(244, 204, 156),
				new Color(248, 222, 192), new Color(252, 239, 224), new Color(255, 255, 255) };
	}

	public String toString() {
		return "Orange-White Colormap with Brightness Gradient";
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UniColor;
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new OrangeHSBBrightnessGradientColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}

}
