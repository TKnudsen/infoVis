package com.github.TKnudsen.infoVis.view.interaction;

import java.awt.geom.RectangularShape;
import java.util.List;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Returns the list of elements that are selected by a given rectangular shape
 * </p>
 * 
 * <p>
 * Copyright: (c) 2017-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.04
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
