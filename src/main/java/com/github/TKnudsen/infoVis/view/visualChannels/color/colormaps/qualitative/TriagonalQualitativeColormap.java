package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative;

import java.awt.Color;

/**
 * <p>
 * Qualitative color map with a fixed 3-color palette spaced roughly 120
 * degrees apart on the hue circle.
 * </p>
 *
 * @version 1.0
 */
public class TriagonalQualitativeColormap extends AbstractQualitativeColorMap {

	@Override
	public Color[] getColors(int count) {
		if (count == colors.length)
			return colors;

		Color[] retColors = new Color[count];
		for (int i = 0; i < count; i++)
			retColors[i] = colors[i % colors.length];
		return retColors;
	}

	@Override
	protected void setColors() {
		// triadic (hue 3x120 degrees)
		// colors = new Color[] { new Color(0, 17, 173), new Color(255, 102, 0),
		// new Color(140, 200, 0) };

		// good brightness contrast, without red
		colors = new Color[] { new Color(5, 173, 0), new Color(255, 198, 0), new Color(0, 66, 200) };
	}

}
