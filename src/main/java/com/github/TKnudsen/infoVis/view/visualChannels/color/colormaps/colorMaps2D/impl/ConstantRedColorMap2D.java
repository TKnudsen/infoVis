package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;

/**
 * <p>
 * RGB colormap with a constant red channel; green and blue each span one
 * axis. Cataloged as "Constant Red" in Bernard et al., "A Survey and
 * Task-Based Quality Assessment of Static 2D Colormaps" (SPIE VDA, 2015).
 * The exact red level is not specified there; a mid-range constant is used
 * here so green/blue variation stays visible across the whole domain.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ConstantRedColorMap2D extends AbstractColorMap2D {

	private static final float RED = 128 / 255f;

	public ConstantRedColorMap2D() {
		super((x, y) -> new Color(RED, (float) (double) x, (float) (double) y), ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "Constant Red";
	}

	@Override
	public String getDescription() {
		return "RGB colormap with constant red; green and blue span one axis each (Bernard et al. 2015)";
	}

}
