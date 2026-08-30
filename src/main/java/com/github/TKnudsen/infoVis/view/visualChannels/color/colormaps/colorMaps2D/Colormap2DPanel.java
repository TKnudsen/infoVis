package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.view.ColorMapPanel;

/**
 * <p>
 * {@link ColorMapPanel} specialized for {@link IColorMap2D} bivariate color
 * maps.
 * </p>
 *
 * @version 1.0
 */
public class Colormap2DPanel extends ColorMapPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5789044142101544283L;

	public Colormap2DPanel(IColorMap2D colorMap) {
		super(colorMap);
	}
}
