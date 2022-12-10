package com.github.TKnudsen.infoVis.view.interaction;

import java.awt.Shape;
import java.util.List;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Returns the list of elements that are selected by a given shape
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
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
