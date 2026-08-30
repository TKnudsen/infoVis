package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;

/**
 * <p>
 * Base contract shared by all color maps: drawing a palette swatch and
 * reporting the underlying {@link ColorSpace}.
 * </p>
 *
 * @version 1.0
 */
public interface IColorMap extends ISelfDescription {

	public void drawColormap(Rectangle2D rect, Graphics2D g, boolean reverse);

	public ColorSpace getColorSpace();
}
