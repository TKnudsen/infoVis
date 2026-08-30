package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @author Tobias Schreck
 * @version 1.01
 * @since 2005
 */
public class RainbowColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected RainbowColorMap() {
		super();
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_Rainbow;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(255, 0, 0), new Color(255, 15, 0), new Color(255, 31, 0),
				new Color(255, 46, 0), new Color(255, 61, 0), new Color(255, 77, 0), new Color(255, 92, 0),
				new Color(255, 107, 0), new Color(255, 122, 0), new Color(255, 138, 0), new Color(255, 153, 0),
				new Color(255, 168, 0), new Color(255, 184, 0), new Color(255, 199, 0), new Color(255, 214, 0),
				new Color(255, 229, 0), new Color(255, 245, 0), new Color(250, 255, 0), new Color(235, 255, 0),
				new Color(219, 255, 0), new Color(204, 255, 0), new Color(189, 255, 0), new Color(173, 255, 0),
				new Color(158, 255, 0), new Color(143, 255, 0), new Color(128, 255, 0), new Color(112, 255, 0),
				new Color(97, 255, 0), new Color(82, 255, 0), new Color(66, 255, 0), new Color(51, 255, 0),
				new Color(36, 255, 0), new Color(20, 255, 0), new Color(5, 255, 0), new Color(0, 255, 10),
				new Color(0, 255, 26), new Color(0, 255, 41), new Color(0, 255, 56), new Color(0, 255, 71),
				new Color(0, 255, 87), new Color(0, 255, 102), new Color(0, 255, 117), new Color(0, 255, 133),
				new Color(0, 255, 148), new Color(0, 255, 163), new Color(0, 255, 179), new Color(0, 255, 194),
				new Color(0, 255, 209), new Color(0, 255, 224), new Color(0, 255, 240), new Color(0, 255, 255),
				new Color(0, 240, 255), new Color(0, 224, 255), new Color(0, 209, 255), new Color(0, 194, 255),
				new Color(0, 178, 255), new Color(0, 163, 255), new Color(0, 148, 255), new Color(0, 133, 255),
				new Color(0, 117, 255), new Color(0, 102, 255), new Color(0, 87, 255), new Color(0, 71, 255),
				new Color(0, 56, 255), new Color(0, 41, 255), new Color(0, 25, 255), new Color(0, 10, 255),
				new Color(5, 0, 255), new Color(20, 0, 255), new Color(36, 0, 255), new Color(51, 0, 255),
				new Color(66, 0, 255), new Color(82, 0, 255), new Color(97, 0, 255), new Color(112, 0, 255),
				new Color(128, 0, 255), new Color(143, 0, 255), new Color(158, 0, 255), new Color(173, 0, 255),
				new Color(189, 0, 255), new Color(204, 0, 255), new Color(219, 0, 255), new Color(235, 0, 255),
				new Color(250, 0, 255), new Color(255, 0, 245), new Color(255, 0, 230), new Color(255, 0, 214),
				new Color(255, 0, 199), new Color(255, 0, 184), new Color(255, 0, 168), new Color(255, 0, 153),
				new Color(255, 0, 138), new Color(255, 0, 122), new Color(255, 0, 107), new Color(255, 0, 92),
				new Color(255, 0, 76), new Color(255, 0, 61), new Color(255, 0, 46), new Color(255, 0, 31),
				new Color(255, 0, 15) };
	}

	public String toString() {
		return "Rainbow Colormap";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new RainbowColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}