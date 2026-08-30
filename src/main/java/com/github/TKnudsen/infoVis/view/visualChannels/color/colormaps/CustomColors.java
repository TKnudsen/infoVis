package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps;

import java.awt.Color;
import java.util.Arrays;

/**
 * <p>
 * The cb_blueish/cb_greenish/cb_earthy palettes below include color
 * specifications and designs developed by Cynthia Brewer
 * (http://colorbrewer.org/). Copyright (c) 2002 Cynthia Brewer, Mark
 * Harrower, and The Pennsylvania State University, licensed under the Apache
 * License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 * </p>
 *
 * @author Tobias Schreck
 * @version 1.0
 * @since 2005
 */
public class CustomColors {

	//  yellow / blue as used in SIGKDD Explorations Paper
	private static final Color[] blue = { new Color(0f, 0f, 1f, 0.80f), new Color(0f, 0f, 1f, 0.50f),
			new Color(0f, 0f, 1f, 0.20f) };
	private static final Color[] yellow = { new Color(1f, 1f, 0f, 0.80f), new Color(1f, 1f, 0f, 0.50f),
			new Color(1f, 1f, 0f, 0.20f) };

	// some colors obtained by colorbrewer
	private static final Color[] cb_blueish = { new Color(4, 90, 141), new Color(43, 140, 190),
			new Color(116, 169, 207) };
	private static final Color[] cb_greenish = { new Color(0, 104, 55), new Color(49, 163, 84),
			new Color(120, 198, 121) };
	private static final Color[] cb_earthy = { new Color(153, 52, 4), new Color(217, 95, 14),
			new Color(254, 153, 41) };

	public static Color[] getBlue() {
		return Arrays.copyOf(blue, blue.length);
	}

	public static Color[] getYellow() {
		return Arrays.copyOf(yellow, yellow.length);
	}

	public static Color[] getCbBlueish() {
		return Arrays.copyOf(cb_blueish, cb_blueish.length);
	}

	public static Color[] getCbGreenish() {
		return Arrays.copyOf(cb_greenish, cb_greenish.length);
	}

	public static Color[] getCbEarthy() {
		return Arrays.copyOf(cb_earthy, cb_earthy.length);
	}
}
