package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;

/**
 * <p>
 * RGB colormap with a constant green channel; red and blue each span one
 * axis. Cataloged as "Constant Green" in Bernard et al., "A Survey and
 * Task-Based Quality Assessment of Static 2D Colormaps" (SPIE VDA, 2015).
 * The exact green level is not specified there; a mid-range constant is used
 * here so red/blue variation stays visible across the whole domain.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ConstantGreenColorMap2D extends AbstractColorMap2D {

	private static final float GREEN = 128 / 255f;

	public ConstantGreenColorMap2D() {
		super((x, y) -> new Color((float) (double) x, GREEN, (float) (double) y), ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "Constant Green";
	}

	@Override
	public String getDescription() {
		return "RGB colormap with constant green; red and blue span one axis each (Bernard et al. 2015)";
	}

}
