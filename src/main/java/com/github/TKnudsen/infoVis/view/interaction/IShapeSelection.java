package com.github.TKnudsen.infoVis.view.interaction;

import java.awt.Shape;
import java.util.List;

/**
 * <p>
 * Returns the list of elements that are selected by a given shape
 * </p>
 *
 * @version 1.03
 * @since 2016
 */
public interface IShapeSelection<T> {

	/**
	 * functionality that provides elements within a rectangle
	 * 
	 * @param shape shape
	 * @return list
	 */
	public List<T> getElementsInShape(Shape shape);
}
