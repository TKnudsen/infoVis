package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative;

import java.awt.Color;

/**
 * <p>
 * Qualitative color map using a fixed 11-color "flat design" style palette.
 * </p>
 *
 * @version 1.0
 */
public class FlatColorsQualitativeColormap extends AbstractQualitativeColorMap {

	@Override
	public Color[] getColors(int count) {
		Color[] result = new Color[count];
		for (int i = 0; i < count; i++)
			result[i] = colors[i % colors.length];
		return result;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(166, 206, 227), new Color(192, 57, 45), new Color(211, 84, 0),
				new Color(241, 196, 015), new Color(210, 82, 127), new Color(155, 89, 182), new Color(22, 160, 133),
				new Color(46, 204, 113), new Color(41, 128, 217), new Color(243, 156, 18), new Color(52, 152, 219) };
	}
}
