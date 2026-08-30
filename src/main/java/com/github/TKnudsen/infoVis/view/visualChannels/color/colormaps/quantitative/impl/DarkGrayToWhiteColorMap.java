package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * <p>
 * Sequential color map from dark gray to white.
 * </p>
 *
 * @version 1.0
 */
public class DarkGrayToWhiteColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected DarkGrayToWhiteColorMap() {
		super();
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(45, 45, 45), Color.WHITE };
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_GrayWhite;
	}

	public String toString() {
		return "Gray White";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new DarkGrayToWhiteColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}

}
