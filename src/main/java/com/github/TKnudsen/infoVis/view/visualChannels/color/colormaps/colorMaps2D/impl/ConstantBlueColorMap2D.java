package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;

/**
 * <p>
 * RGB colormap with a constant blue channel; red and green each span one
 * axis. A generic, widely-used 2D colormap construction, e.g. in the
 * SOM-visualization literature (Kramer &amp; Gieseke, "Analysis of wind
 * energy time series with kernel methods and neural networks", 2011), and
 * cataloged as "Constant Blue" in Bernard et al., "A Survey and Task-Based
 * Quality Assessment of Static 2D Colormaps" (SPIE VDA, 2015). The exact
 * blue level is not specified there; a mid-range constant is used here so
 * red/green variation stays visible across the whole domain.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ConstantBlueColorMap2D extends AbstractColorMap2D {

	private static final float BLUE = 128 / 255f;

	public ConstantBlueColorMap2D() {
		super((x, y) -> new Color((float) (double) x, (float) (double) y, BLUE), ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "Constant Blue";
	}

	@Override
	public String getDescription() {
		return "RGB colormap with constant blue; red and green span one axis each (Bernard et al. 2015)";
	}

}
