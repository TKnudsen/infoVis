package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @since 2023
 */
public class PurpleGreenBipolarColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected PurpleGreenBipolarColorMap() {
		super();
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(181, 75, 160), new Color(128, 128, 128), new Color(59, 197, 85) };
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.BIPOLAR_PurpleGreen;
	}

	public String toString() {
		return "Purple Green Bipolar";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new PurpleGreenBipolarColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
