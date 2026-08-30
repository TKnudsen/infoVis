package com.github.TKnudsen.infoVis.view.panels.axis.sizeCharacteristics;

import java.awt.geom.Rectangle2D;

/**
 * <p>
 * Describes an axis's layout rectangle, legend offset, and the size
 * thresholds controlling whether it is drawn at all.
 * </p>
 *
 * @version 1.0
 */
public interface IAxisSizeCharacteristics {

	public Rectangle2D getAxisRectangle();

	public double getAxisLegendOffset();

	public boolean isDrawAxis();

	public double getMinSizeToDrawAxis();

}
