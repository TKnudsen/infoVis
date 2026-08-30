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
public class UserDefinedToRedColorMap extends AbstractColorMap1D {
	protected static AbstractColorMap instance;

	protected UserDefinedToRedColorMap(Color color) {
		// super();

		Objects.requireNonNull(color);
		if (colors.length < 1)
			throw new IllegalArgumentException("UserDefinedColorMap requires one color");

		this.colors = new Color[] { color, new Color(180, 45, 45) };
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
		return "UserDefinedToRedColorMap";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance(Color color) {
		if (instance == null) {
			instance = new UserDefinedToRedColorMap(color);
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
