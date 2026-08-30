package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * <p>
 * Jorge Poco, Angela Mayhua, and Jeffrey Heer: Extracting and
 * Retargeting Color Mappings from Bitmap Images of Visualizations.
 * 
 * Colormap from Fig 4b, used from 0% to 80% (the remaining 20% go back to
 * yellow).
 * </p>
 *
 * @since 2017
 */
public class BlueToOrangePocoetalColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected BlueToOrangePocoetalColorMap() {
		super();
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(48, 38, 92), new Color(60, 88, 168), new Color(42, 114, 184),
				new Color(34, 142, 205), new Color(12, 167, 196), new Color(26, 184, 161), new Color(122, 194, 120),
				new Color(185, 189, 91), new Color(242, 187, 57) };
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_BlueToYellow;
	}

	public String toString() {
		return "Blue To Yellow";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new BlueToOrangePocoetalColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
