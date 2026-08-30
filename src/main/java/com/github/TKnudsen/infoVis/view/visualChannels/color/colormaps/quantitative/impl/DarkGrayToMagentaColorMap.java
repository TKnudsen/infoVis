package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * <p>
 * DarkGrayToRedColorMap
 * </p>
 *
 * @version 1.02
 * @since 2017
 */
public class DarkGrayToMagentaColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected DarkGrayToMagentaColorMap() {
		super();
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_GrayRed;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { Color.GRAY, new Color(200, 40, 110) };
	}

	public String toString() {
		return "Dark Gray Purple";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new DarkGrayToMagentaColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
