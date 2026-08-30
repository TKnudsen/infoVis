package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;

/**
 * <p>
 * Qualitative color map using ColorBrewer's 11-color "Paired" palette.
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
public class ColorBrewerPairedColorMap extends AbstractQualitativeColorMap {

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(166, 206, 227), new Color(31, 120, 180), new Color(178, 223, 138), new Color(51, 160, 44), new Color(251, 154, 153), new Color(227, 26, 28), new Color(253, 191, 111), new Color(255, 127, 0),
				new Color(202, 178, 214), new Color(106, 61, 154), new Color(255, 255, 153) };
	}

	@Override
	public Color[] getColors(int count) {
		if (colors.length > count) {
			double step = 1.0 * colors.length / count;
			Color[] res = new Color[count];
			for (int i = 0; i < count; i++) {
				res[i] = colors[(int) Math.round(i * step)];
			}
			return res;
		} else if (colors.length == count) {
			return colors;
		} else {
			Color[] res = new Color[count];
			for (int i = 0; i < count; i++) {
				res[i] = colors[i%colors.length];
			}
			return res;
		}
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new ColorBrewerPairedColorMap();
		}
		return instance;
	}

}
