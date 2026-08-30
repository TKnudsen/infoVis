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
public class GrayBlueColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected GrayBlueColorMap() {
		super();
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_GrayBlue;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(200, 200, 200), new Color(0, 60, 180) };
	}

	public String toString() {
		return "Gray Blue";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new GrayBlueColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
