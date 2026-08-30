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
public class BlueHSBBrightnessGradientColorMap extends AbstractColorMap1D {
	protected static AbstractColorMap instance;

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(29, 87, 127), new Color(33, 99, 144), new Color(37, 111, 163), new Color(41, 122, 179), new Color(45, 133, 195), new Color(52, 144, 208), new Color(68, 153, 212), new Color(84, 162, 216), new Color(100, 171, 220), new Color(117, 179, 223), new Color(133, 188, 227), new Color(149, 197, 231), new Color(165, 206, 235), new Color(181, 215, 239), new Color(198, 224, 242), new Color(214, 233, 246), new Color(230, 242, 250), new Color(242, 249, 254),
				new Color(255, 255, 255) };
	}

	public String toString() {
		return "Blue-White Colormap with Brightness Gradient";
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
			instance = new BlueHSBBrightnessGradientColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
