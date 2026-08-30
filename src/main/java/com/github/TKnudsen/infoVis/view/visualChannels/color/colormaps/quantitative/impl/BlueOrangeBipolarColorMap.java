package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @since 2011
 */
public class BlueOrangeBipolarColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected BlueOrangeBipolarColorMap() {
		super();
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(55, 0, 81), new Color(36, 30, 149), new Color(16, 62, 220),
				new Color(30, 87, 230), new Color(79, 107, 178), new Color(127, 127, 127), new Color(178, 138, 117),
				new Color(230, 148, 106), new Color(254, 173, 89), new Color(254, 213, 65), new Color(253, 252, 41) };
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.BIPOLAR_BlueOrange;
	}

	public String toString() {
		return "Blue Orange Bipolar";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new BlueOrangeBipolarColorMap();
		}
		return instance;
	}
	
	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
