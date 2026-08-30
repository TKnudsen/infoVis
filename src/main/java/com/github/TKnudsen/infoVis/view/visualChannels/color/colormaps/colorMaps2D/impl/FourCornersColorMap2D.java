package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;

/**
 * <p>
 * RGB colormap with four color anchors at the corners -- Red, Blue, Green,
 * and Yellow -- bilinearly interpolated. Cataloged as "Four Corners R-B-G-Y"
 * in Bernard et al., "A Survey and Task-Based Quality Assessment of Static
 * 2D Colormaps" (SPIE VDA, 2015), citing Ziegler et al. (2007) for financial
 * time series exploration. The survey names the four corner colors but not
 * their exact placement; this places them (0,0)=Red, (1,0)=Blue,
 * (0,1)=Green, (1,1)=Yellow -- a guess, since no exact reference was
 * available at the time. {@link ZieglerColorMap2D}, added later from exact
 * pixel-for-pixel reference data, confirms the same four anchor colors but
 * places them (0,0)=Yellow, (1,0)=Green, (0,1)=Red, (1,1)=Blue; prefer that
 * class where exact fidelity to Ziegler et al.'s original matters.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class FourCornersColorMap2D extends AbstractColorMap2D {

	public FourCornersColorMap2D() {
		super((x, y) -> AnchorColorInterpolation.bilinear(x, y, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW),
				ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "Four Corners R-B-G-Y";
	}

	@Override
	public String getDescription() {
		return "RGB colormap with four color anchors: Red, Blue, Green, Yellow (Bernard et al. 2015)";
	}

}
