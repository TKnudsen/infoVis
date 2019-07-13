package com.github.TKnudsen.infoVis.view.tools;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 *
 */
public class AffineTransformTools {

	/**
	 * mirrors a given shape along the X axis. The object stays at the same center.
	 * 
	 * @param shape
	 * @return
	 */
	public static Shape mirrorAlongX(Shape shape) {
		if (shape == null)
			return null;

		double centerX = shape.getBounds().getCenterX();
		AffineTransform at = new AffineTransform();
		at.translate(centerX, 0);
		at.scale(-1, 1);
		at.translate(-centerX, 0);

		return at.createTransformedShape(shape);
	}

	/**
	 * mirrors a given shape along the Y axis. The object stays at the same center.
	 * 
	 * @param shape
	 * @return
	 */
	public static Shape mirrorAlongY(Shape shape) {
		if (shape == null)
			return null;

		double centerY = shape.getBounds().getCenterY();

		AffineTransform at = new AffineTransform();
		at.translate(0, centerY);
		at.scale(1, -1);
		at.translate(0, -centerY);

		return at.createTransformedShape(shape);
	}
}
