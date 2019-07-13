package com.github.TKnudsen.infoVis.view.interaction;

import java.awt.Point;
import java.util.List;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Provides click selection behavior.
 * 
 * clicking an index (Integer)
 * 
 * clicking an element (T)
 * 
 * clicking several (overplotted) elements (T), thus using a List as return type
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.03
 */
public interface IClickSelection<T> {

	public List<T> getElementsAtPoint(Point p);
}
