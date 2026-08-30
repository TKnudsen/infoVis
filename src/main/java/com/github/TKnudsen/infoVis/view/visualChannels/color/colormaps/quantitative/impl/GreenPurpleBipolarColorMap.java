package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @since 2014
 */
public class GreenPurpleBipolarColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected GreenPurpleBipolarColorMap() {
		super();
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(59, 197, 85), new Color(128, 128, 128), new Color(181, 75, 160) };
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.BIPOLAR_GreenPurple;
	}

	public String toString() {
		return "Green Purple Bipolar";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new GreenPurpleBipolarColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
