package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;

/**
 * <p>
 * RGB cube diagonal-cut colormap with anchors Blue, Cyan, Yellow, and Red,
 * bilinearly interpolated. Cataloged as "Cube Diagonal Cut B-C-Y-R" in
 * Bernard et al., "A Survey and Task-Based Quality Assessment of Static 2D
 * Colormaps" (SPIE VDA, 2015) -- described there as "X-axis: Green, y-axis:
 * Blue vs. Red" -- which the survey calls "easy to re-implement" and among
 * the best-performing colormaps overall. The SOM-visualization literature it
 * cites (Himberg 2000, Vesanto &amp; Himberg 1998) motivates the technique
 * (a 2D slice through the RGB cube orthogonal to its gray diagonal) without
 * giving an exact formula; this places (0,0)=Blue, (1,0)=Cyan (x adds
 * green), (0,1)=Red, (1,1)=Yellow (y moves from the blue/cyan side to the
 * red/yellow side), matching the stated axis semantics.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class CubeDiagonalCutBCYRColorMap2D extends AbstractColorMap2D {

	public CubeDiagonalCutBCYRColorMap2D() {
		super((x, y) -> AnchorColorInterpolation.bilinear(x, y, Color.BLUE, Color.CYAN, Color.RED, Color.YELLOW),
				ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "Cube Diagonal Cut B-C-Y-R";
	}

	@Override
	public String getDescription() {
		return "RGB cube diagonal cut with anchors Blue, Cyan, Yellow, Red (Bernard et al. 2015)";
	}

}
