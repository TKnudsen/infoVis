package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * <p>
 * This is my idea of an abstract ColorMap. While others used funcion pointers
 * inspired by C/C++ code, I decided to go the JavaOO way. Abstract, dynamic and
 * generic. Useful in creating Colormap concepts in new GUIs.
 * </p>
 *
 * @since 2012
 */
public abstract class AbstractColorMap implements IColorMap {

	protected boolean interpolateMissingColorValues = true;

	// public abstract void drawColormap(Rectangle2D rect, Graphics2D g, float
	// alpha, boolean reverse);

	public abstract void drawColormap(Rectangle2D rect, Graphics2D g, float alpha, boolean reverse, String label1,
			String label2);

	@Override
	public void drawColormap(Rectangle2D rect, Graphics2D g, boolean reverse) {
		drawColormap(rect, g, 1f, reverse, "", "");
	}

	// public void drawColormap(Rectangle2D rect, Graphics2D g, String label1,
	// String label2) {
	// drawColormap(rect, g, 1f, label1, label2);
	// }

	public void setInterpolateMissingColorValues(boolean interpolateMissingColorValues) {
		this.interpolateMissingColorValues = interpolateMissingColorValues;
	}

	public boolean isInterpolateMissingColorValues() {
		return interpolateMissingColorValues;
	}

	public Color applyAlpha(Color color, float alpha) {
		if (color == null)
			return null;
		if (alpha < 0.0 || alpha > 1.0) {
			throw new IllegalArgumentException("invalid alpha value: " + alpha);
		}
		return new Color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, alpha);
	}
}
