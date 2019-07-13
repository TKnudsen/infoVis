package com.github.TKnudsen.infoVis.view.interaction;

import java.awt.Point;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Provides zooming functionality to some component/painter.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public interface IZooming {
	public void zoom(Point location, int zoomCount, boolean zoomX, boolean zoomY);

	public void resetZoom();
}
