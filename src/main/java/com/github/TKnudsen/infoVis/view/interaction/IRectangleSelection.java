package com.github.TKnudsen.infoVis.view.interaction;

import java.awt.geom.RectangularShape;
import java.util.List;

/**
 * <p>
 * Returns the list of elements that are selected by a given rectangular shape
 * </p>
 *
 * @version 2.04
 * @since 2017
 */
public interface IRectangleSelection<T> {

	/**
	 * functionality that provides elements within a rectangle
	 * 
	 * @param rectangle rectangular shape
	 * @return a list of elements
	 */
	public List<T> getElementsInRectangle(RectangularShape rectangle);
}
