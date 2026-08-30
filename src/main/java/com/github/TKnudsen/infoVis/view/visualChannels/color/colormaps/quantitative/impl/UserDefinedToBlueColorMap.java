package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;
import java.util.Objects;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @version 1.01
 * @since 2025
 */
public class UserDefinedToBlueColorMap extends AbstractColorMap1D {
	protected static AbstractColorMap instance;

	protected UserDefinedToBlueColorMap(Color color) {
		// super();

		Objects.requireNonNull(color);
		if (colors.length < 1)
			throw new IllegalArgumentException("UserDefinedColorMap requires one color");

		this.colors = new Color[] { color, new Color(27, 74, 158) };
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UserDefined;
	}

	@Override
	protected void setColors() {
		// nothing to do here.
	}

	public String toString() {
		return "UserDefinedToBlueColorMap";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance(Color color) {
		if (instance == null) {
			instance = new UserDefinedToBlueColorMap(color);
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
