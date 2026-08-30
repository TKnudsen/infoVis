package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.IColorMap;

/**
 * <p>
 * Bivariate color map: maps a pair of normalized values to a single color.
 * </p>
 *
 * @version 1.0
 */
public interface IColorMap2D extends IColorMap {

	public Color getColor(double arg0, double arg1);

	public Color getColor(float arg0, float arg1);

}
