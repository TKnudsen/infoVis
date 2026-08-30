package com.github.TKnudsen.infoVis.view.interaction;

import java.awt.Point;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * <p>
 * Guarantees that an implementing object is able to provide tool-tip
 * information at a given point.
 * </p>
 *
 * @version 1.05
 * @since 2016
 */
public interface ITooltip {
	public ChartPainter getTooltip(Point p);

	public boolean isToolTipping();

	public void setToolTipping(boolean toolTipping);

	default public Object getMaster() {
		return this;
	}
}
