package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @version 1.0
 * @since 2016
 */
public class GrayGrassGreenColormap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected GrayGrassGreenColormap() {
		super();
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_GrayGrassGreen;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(206, 206, 206), new Color(16, 119, 52) };
	}

	public String toString() {
		return "Gray Green"; // . Hue is constant while lightness and saturation
								// are linar ascending. Colormap has contrast to
								// White and Black";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new GrayGrassGreenColormap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
