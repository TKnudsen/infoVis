package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * <p>
 * WhiteRedColorMap
 * </p>
 *
 * @version 1.0
 * @since 2012
 */
public class WhiteDarkGrayColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected WhiteDarkGrayColorMap() {

		super();
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_WhiteDarkGray;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(255, 255, 255), Color.DARK_GRAY };
	}

	public String toString() {
		return "White - Dark Gray";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new WhiteDarkGrayColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
