package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;

/**
 * <p>
 * Qualitative color map using ColorBrewer's 11-color "Set3" palette.
 * </p>
 *
 * <p>
 * This product includes color specifications and designs developed by
 * Cynthia Brewer (http://colorbrewer.org/). Copyright (c) 2002 Cynthia
 * Brewer, Mark Harrower, and The Pennsylvania State University, licensed
 * under the Apache License, Version 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 * </p>
 *
 * @version 1.0
 */
public class ColorBrewerSet3ColorMap extends AbstractQualitativeColorMap {

	@Override
	public Color[] getColors(int count) {
		if (colors.length > count)
			return Arrays.copyOfRange(colors, 0, count);
		else if (colors.length == count)
			return colors;
		else {
			List<Color> colorList = new ArrayList<>();
			int i = 0;
			while (colorList.size() < count) {
				colorList.add(colors[i % colors.length]);
				i++;
			}

			Color[] out = new Color[colorList.size()];
			return colorList.toArray(out);
		}
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(141, 211, 199), new Color(255, 255, 179), new Color(190, 186, 218), new Color(251, 128, 114), new Color(128, 177, 211), new Color(253, 180, 98), new Color(179, 222, 105), new Color(252, 205, 229),
				new Color(217, 217, 217), new Color(188, 128, 189), new Color(205, 235, 197) };

	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new ColorBrewerSet3ColorMap();
		}
		return instance;
	}

}
