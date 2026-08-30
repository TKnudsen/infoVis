package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;

/**
 * <p>
 * HSV colormap with a white center and maximal saturation at the edges: hue
 * is the angle around the domain's center, saturation grows with the
 * radius at constant full brightness, so the center is white and the edges
 * are fully saturated. Cataloged in Bernard et al., "A Survey and Task-Based
 * Quality Assessment of Static 2D Colormaps" (SPIE VDA, 2015), citing
 * Ramirez, C. et al., "Self-organizing maps in seismic image segmentation"
 * (2012).
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class RamirezColorMap2D extends AbstractColorMap2D {

	public RamirezColorMap2D() {
		super((x, y) -> polar(x, y), ColorSpace.HSB);
	}

	private static Color polar(double x, double y) {
		double dx = x - 0.5;
		double dy = y - 0.5;
		float radius = (float) Math.min(1.0, Math.sqrt(dx * dx + dy * dy) / Math.sqrt(0.5));
		float hue = (float) ((Math.atan2(dy, dx) + Math.PI) / (2 * Math.PI));
		return Color.getHSBColor(hue, radius, 1.0f);
	}

	@Override
	public String getName() {
		return "Ramirez et al.";
	}

	@Override
	public String getDescription() {
		return "HSV colormap with white center, maximal saturation at the edges; hue by angle, saturation by "
				+ "radius (Ramirez et al. 2012, re-implemented per Bernard et al. 2015)";
	}

}
