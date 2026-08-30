package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative;

import java.awt.Color;

/**
 * <p>
 * Qualitative color map cycling through 6 hues at 3 brightness/saturation
 * levels, extending the effective palette to 18 distinguishable colors.
 * </p>
 *
 * @version 1.0
 */
public class SixColorsInThreeBrightnessLevelsQualitativeColorMap extends AbstractQualitativeColorMap {

	@Override
	public Color[] getColors(int count) {

		Color[] output = new Color[count];

		for (int i = 0; i < count; i++) {
			float hue = 1.0f / 6 * (i % 6);
			float lightness = 1.0f;
			float saturation = 1.0f;
			if (i > 12)
				saturation = 0.6f;
			else if (i > 6)
				lightness = 0.6f;
			output[i] = Color.getHSBColor(hue, saturation, lightness);
		}

		return output;
	}

	@Override
	protected void setColors() {
	}
}
