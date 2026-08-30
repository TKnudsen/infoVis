package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @version 1.02 GrayUnipolarColorMap() Constructor added (2012-01-10)
 * @since 2011-11-03
 */
public class GrayUnipolarColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected GrayUnipolarColorMap() {
		super();
		setInterpolateMissingColorValues(true);
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_Gray;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(0, 0, 0), new Color(255, 255, 255) };
	}

	public String toString() {
		return "Gray Unipolar";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new GrayUnipolarColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
