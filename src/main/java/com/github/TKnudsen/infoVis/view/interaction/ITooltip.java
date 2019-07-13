package com.github.TKnudsen.infoVis.view.interaction;

import java.awt.Point;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Guarantees that an implementing object is able to provide tool-tip
 * information at a given point.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public interface ITooltip {
	public ChartPainter getTooltip(Point p);

	public boolean isToolTipping();

	public void setToolTipping(boolean toolTipping);
}
