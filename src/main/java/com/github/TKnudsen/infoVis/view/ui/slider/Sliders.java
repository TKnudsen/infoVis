package com.github.TKnudsen.infoVis.view.ui.slider;

import java.awt.Shape;
import java.awt.geom.Path2D;

public class Sliders {

	/**
	 * Returns a Shape representing a thumb.
	 */
	public static Shape createThumbShapeHorizontal(int width, int height, boolean flipThumb) {
		Path2D path = new Path2D.Double();

		if (flipThumb) {
			path.moveTo(0.5 * width, height);
			path.lineTo(0, 0);
			path.lineTo(width, 0);

		} else {
			path.moveTo(0.5 * width, 0);
			path.lineTo(0, height);
			path.lineTo(width, height);
		}

		path.closePath();
		return path;
	}

	/**
	 * Returns a Shape representing a thumb.
	 */
	public static Shape createThumbShapeVertical(int width, int height, boolean flipThumb) {
		Path2D path = new Path2D.Double();

		if (flipThumb) {
			path.moveTo(width, 0.5 * height);
			path.lineTo(0, 0);
			path.lineTo(0, height);
		} else {
			path.moveTo(0, 0.5 * height);
			path.lineTo(width, 0);
			path.lineTo(width, height);
		}

		path.closePath();
		return path;
	}
}
