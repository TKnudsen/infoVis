package com.github.TKnudsen.infoVis.view.interaction;

import java.awt.Point;

/**
 * <p>
 * Provides zooming functionality to some component/painter.
 * </p>
 *
 * @version 1.03
 * @since 2016
 */
public interface IZooming {
	public void zoom(Point location, int zoomCount, boolean zoomX, boolean zoomY);

	public void resetZoom();
}
