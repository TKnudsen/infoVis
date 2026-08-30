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
public class BlackSaturationReduced_AlphaColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	private static final Color color = new Color(40, 40, 40);

	protected BlackSaturationReduced_AlphaColorMap() {
		super();
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_BlackSaturationReducedAlpha;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(40, 40, 40) };
	}

	@Override
	public Color getColor(float value) {
		return applyAlpha(color, value);
	}

	public String toString() {
		return "BlackSaturationReduced-Alpha ColorMap";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new BlackSaturationReduced_AlphaColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
