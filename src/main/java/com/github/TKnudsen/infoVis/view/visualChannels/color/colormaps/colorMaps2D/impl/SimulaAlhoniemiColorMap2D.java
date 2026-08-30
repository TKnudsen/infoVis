package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;

/**
 * <p>
 * HSV colormap with a black center and constant saturation: hue is the angle
 * around the domain's center, value (brightness) grows with the radius, so
 * the center is black and the edges are fully bright and saturated.
 * Cataloged in Bernard et al., "A Survey and Task-Based Quality Assessment
 * of Static 2D Colormaps" (SPIE VDA, 2015), citing Simula, O. and Alhoniemi,
 * E., "SOM based analysis of pulping process data" (1999).
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class SimulaAlhoniemiColorMap2D extends AbstractColorMap2D {

	private static final float SATURATION = 1.0f;

	public SimulaAlhoniemiColorMap2D() {
		super((x, y) -> polar(x, y), ColorSpace.HSB);
	}

	private static Color polar(double x, double y) {
		double dx = x - 0.5;
		double dy = y - 0.5;
		float radius = (float) Math.min(1.0, Math.sqrt(dx * dx + dy * dy) / Math.sqrt(0.5));
		float hue = (float) ((Math.atan2(dy, dx) + Math.PI) / (2 * Math.PI));
		return Color.getHSBColor(hue, SATURATION, radius);
	}

	@Override
	public String getName() {
		return "Simula and Alhoniemi";
	}

	@Override
	public String getDescription() {
		return "HSV colormap with black center and constant saturation; hue by angle, value by radius "
				+ "(Simula & Alhoniemi 1999, re-implemented per Bernard et al. 2015)";
	}

}
