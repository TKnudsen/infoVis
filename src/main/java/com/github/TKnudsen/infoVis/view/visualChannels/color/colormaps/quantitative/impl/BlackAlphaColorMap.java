package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @version 1.0
 * @since 2013
 */
public class BlackAlphaColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected BlackAlphaColorMap() {
		super();
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_BlackAlpha;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { Color.BLACK };
	}

	@Override
	public Color getColor(float value) {
		return applyAlpha(Color.BLACK, value);
	}

	public String toString() {
		return "Black-Alpha ColorMap";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new BlackAlphaColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
