package com.github.TKnudsen.infoVis.view.tools;

import java.awt.Component;
import java.awt.geom.Rectangle2D;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Some tools for components
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.02
 */
public class ComponentTools {

	/**
	 * retrieves the coordinates of a Component within its canvas. It does NOT
	 * necessarily start with (0/0).
	 * 
	 * @param panel
	 * @return
	 */
	public static Rectangle2D getBoundsRectangle(Component panel) {
		if (panel == null)
			return null;

		Rectangle2D rectangle = new Rectangle2D.Double(panel.getBounds().getX(), panel.getBounds().getY(),
				panel.getBounds().getWidth(), panel.getBounds().getHeight());

		return rectangle;
	}

	/**
	 * retrieves the rectangle within a component starting at (0/0), its with, and
	 * its height.
	 * 
	 * @param panel
	 * @return
	 */
	public static Rectangle2D getCompontentDrawableRectangle(Component panel) {
		if (panel == null)
			return null;

		Rectangle2D rect = getBoundsRectangle(panel);

		if (rect == null)
			return null;

		Rectangle2D rectangle = new Rectangle2D.Double(0, 0, rect.getWidth(), rect.getHeight());

		return rectangle;
	}
}
