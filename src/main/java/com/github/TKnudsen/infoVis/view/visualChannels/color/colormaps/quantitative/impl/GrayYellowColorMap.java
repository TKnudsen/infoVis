package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * <p>
 * Sequential color map from gray to yellow.
 * </p>
 *
 * @version 1.0
 */
public class GrayYellowColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected GrayYellowColorMap() {
		super();
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_GrayYellow;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(220, 220, 220), new Color(232, 192, 8) };
	}

	public String toString() {
		return "Gray Yellow";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new GrayYellowColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}

}
