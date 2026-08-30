package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * <p>
 * GreenYellowRedLowSaturatedColorMap
 * </p>
 *
 * @version 1.00
 * @since 2016
 */
public class GreenYellowRedLowSaturationColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(144, 238, 144), new Color(255, 255, 153), new Color(205, 92, 92) };
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.GreenYellowRedLowSaturationColorMap;
	}

	public String toString() {
		return "Green Yellow Red Low Saturation";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new GreenYellowRedLowSaturationColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
