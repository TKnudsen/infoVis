package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @version 1.02 BlueYellowEvenBrightnessBipolarColorMap() Constructor added
 * @since 2011-11-03
 */
public class BlueYellowEvenBrightnessBipolarColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected BlueYellowEvenBrightnessBipolarColorMap() {
		super();
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(19, 70, 237), new Color(26, 84, 230), new Color(78, 106, 178),
				new Color(128, 128, 128), new Color(164, 117, 92), new Color(219, 97, 37), new Color(254, 127, 2) };
		// colors = new Color[] { new Color(51,43,213), new Color(18,70,228), new
		// Color(26, 84,230), new Color(78, 106, 178), new Color(128, 128, 128), new
		// Color(164,117,92), new Color(219, 97, 37), new Color(254, 127,2), new
		// Color(254, 198, 2), new Color(253, 250, 3) };
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.BIPOLAR_BlueYellow_EvenBrightness;
	}

	public String toString() {
		return "Blue Yellow Bipolar";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new BlueYellowEvenBrightnessBipolarColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
