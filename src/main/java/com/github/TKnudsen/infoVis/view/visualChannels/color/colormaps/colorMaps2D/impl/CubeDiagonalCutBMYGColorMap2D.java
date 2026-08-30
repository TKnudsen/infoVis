package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;

/**
 * <p>
 * RGB cube diagonal-cut colormap with anchors Blue, Magenta, Yellow, and
 * Green, bilinearly interpolated. Cataloged as "Cube Diagonal Cut B-M-Y-G"
 * in Bernard et al., "A Survey and Task-Based Quality Assessment of Static
 * 2D Colormaps" (SPIE VDA, 2015) -- described there as "X-axis: Red, y-axis:
 * Blue vs. Green". As with {@link CubeDiagonalCutBCYRColorMap2D}, the
 * SOM-visualization literature it cites motivates the technique without an
 * exact formula; this places (0,0)=Blue, (1,0)=Magenta (x adds red),
 * (0,1)=Green, (1,1)=Yellow (y moves from the blue/magenta side to the
 * green/yellow side), matching the stated axis semantics.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class CubeDiagonalCutBMYGColorMap2D extends AbstractColorMap2D {

	public CubeDiagonalCutBMYGColorMap2D() {
		super((x, y) -> AnchorColorInterpolation.bilinear(x, y, Color.BLUE, Color.MAGENTA, Color.GREEN,
				Color.YELLOW), ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "Cube Diagonal Cut B-M-Y-G";
	}

	@Override
	public String getDescription() {
		return "RGB cube diagonal cut with anchors Blue, Magenta, Yellow, Green (Bernard et al. 2015)";
	}

}
