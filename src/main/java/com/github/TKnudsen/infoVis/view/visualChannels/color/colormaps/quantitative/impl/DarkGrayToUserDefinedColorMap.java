package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @version 1.1
 * @since 2017
 */
public class DarkGrayToUserDefinedColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	public DarkGrayToUserDefinedColorMap(Color c) {
		colors = new Color[] { Color.GRAY.darker(), c };
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_GrayUserDefined;
	}

	@Override
	protected void setColors() {
		// colors = new Color[] { Color.GRAY.darker(), new Color(255, 128, 10) };
	}

	public String toString() {
		return "Dark UserDefined";
	}

	/**
	 * @param c the target color the gradient runs to, from dark gray
	 * @return the shared singleton instance of this color map for the given
	 *         target color
	 */
	public static synchronized AbstractColorMap getInstance(Color c) {
		if (instance == null) {
			instance = new DarkGrayToUserDefinedColorMap(c);
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
