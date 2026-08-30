package com.github.TKnudsen.infoVis.view.interaction;

import java.awt.Point;
import java.util.List;

/**
 * @version 2.03
 * @since 2016
 */
public interface IClickSelection<T> {

	public List<T> getElementsAtPoint(Point p);
}
