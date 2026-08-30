package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;

/**
 * <p>
 * RGB colormap with four color anchors -- Green, Yellow, Red, and Black --
 * bilinearly interpolated. Cataloged as "Baum et al. G-Y-R-B" in Bernard et
 * al., "A Survey and Task-Based Quality Assessment of Static 2D Colormaps"
 * (SPIE VDA, 2015), citing Baum, K. G. et al., "Investigation of PET/MRI
 * image fusion schemes for enhanced breast cancer diagnosis", Nuclear
 * Science Symposium (2007). The survey names the four corner colors but not
 * their exact placement; this places them (0,0)=Green, (1,0)=Yellow,
 * (0,1)=Red, (1,1)=Black.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class BaumColorMap2D extends AbstractColorMap2D {

	public BaumColorMap2D() {
		super((x, y) -> AnchorColorInterpolation.bilinear(x, y, Color.GREEN, Color.YELLOW, Color.RED, Color.BLACK),
				ColorSpace.RGB);
	}

	@Override
	public String getName() {
		return "Baum et al. G-Y-R-B";
	}

	@Override
	public String getDescription() {
		return "2D color lookup table with anchors Green, Yellow, Red, Black "
				+ "(Baum et al. 2006/2007, re-implemented per Bernard et al. 2015)";
	}

}
