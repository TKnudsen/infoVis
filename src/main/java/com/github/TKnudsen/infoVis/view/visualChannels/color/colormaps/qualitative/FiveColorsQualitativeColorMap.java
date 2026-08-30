package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;

/**
 * <p>
 * Qualitative color map with a fixed palette of five distinct colors.
 * </p>
 *
 * @version 1.0
 */
public class FiveColorsQualitativeColorMap extends AbstractQualitativeColorMap {

	@Override
	public Color[] getColors(int count) {
		// create colors
		if (count < colors.length)
			return Arrays.copyOfRange(this.colors, 0, count);
		else {
			List<Color> colors = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				colors.add(this.colors[i % this.colors.length]);
			}

			return DataConversion.listToArray(colors, Color.class);
		}
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(52, 152, 219), new Color(241, 196, 13), new Color(46, 204, 113),
				new Color(155, 89, 182), new Color(211, 84, 0) };
	}
}
