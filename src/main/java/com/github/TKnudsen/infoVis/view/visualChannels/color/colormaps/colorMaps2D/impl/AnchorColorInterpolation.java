package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

/**
 * <p>
 * Bilinear interpolation between four RGB anchor colors placed at the
 * corners of the {@code [0,1]x[0,1]} domain, shared by the anchor-color-based
 * 2D colormaps in this package.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
final class AnchorColorInterpolation {

	private AnchorColorInterpolation() {
	}

	static Color bilinear(double x, double y, Color c00, Color c10, Color c01, Color c11) {
		double w00 = (1 - x) * (1 - y);
		double w10 = x * (1 - y);
		double w01 = (1 - x) * y;
		double w11 = x * y;

		int r = clamp(w00 * c00.getRed() + w10 * c10.getRed() + w01 * c01.getRed() + w11 * c11.getRed());
		int g = clamp(w00 * c00.getGreen() + w10 * c10.getGreen() + w01 * c01.getGreen() + w11 * c11.getGreen());
		int b = clamp(w00 * c00.getBlue() + w10 * c10.getBlue() + w01 * c01.getBlue() + w11 * c11.getBlue());

		return new Color(r, g, b);
	}

	private static int clamp(double v) {
		return (int) Math.round(Math.max(0, Math.min(255, v)));
	}
}
