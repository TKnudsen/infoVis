package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.RainbowColorMap;

/**
 * <p>
 * Qualitative color map deriving distinct colors from
 * {@link RainbowColorMap}, sampled and shuffled (alternating from both ends)
 * to spread adjacent categories apart on the color wheel.
 * </p>
 *
 * @version 1.0
 */
public class RainbowBasedQualitativeColormap extends AbstractQualitativeColorMap {

	AbstractColorMap1D rcm = (AbstractColorMap1D) RainbowColorMap.getInstance();

	@Override
	public Color[] getColors(int count) {
		// create colors
		List<Color> colors = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
			colors.add(rcm.getColor((i / (float) count)));
		// usually (count - 1), but the colormap is radial

		// shuffle colors
		Color[] output = new Color[count];
		for (int i = 0; i < count; i++)
			if (i % 2 == 0)
				output[i] = colors.remove(0);
			else
				output[i] = colors.remove(colors.size() - 1);

		return output;
	}

	@Override
	protected void setColors() {
		colors = null;
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new RainbowBasedQualitativeColormap();
		}
		return instance;
	}

}
