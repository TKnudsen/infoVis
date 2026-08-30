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
public class GrayPurpleColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected GrayPurpleColorMap() {
		super();
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_GrayPurple;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(220, 220, 220), new Color(181, 75, 160) };
	}

	public String toString() {
		return "Gray Purple";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new GrayPurpleColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
