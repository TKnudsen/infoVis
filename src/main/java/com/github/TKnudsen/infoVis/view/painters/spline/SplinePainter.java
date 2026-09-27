package com.github.TKnudsen.infoVis.view.painters.spline;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.color.ColorPolygonPainter;
import com.github.TKnudsen.infoVis.view.visualPrimitives.ColoredPolygon;

/**
 * Renders a colored, smoothed (B-spline) polyline through a sequence of
 * relative (x,y) coordinates.
 *
 * @version 1.1 -- data is now immutable, supplied through the constructor
 *          (matching the other painters in this package) instead of via
 *          setData/setColors mutators.
 * @since 2019
 */
public class SplinePainter extends ChartPainter {

	private static final int PRUNING_DISTANCE = 2;

	protected final double[] xValuesRelative;
	protected final double[] yValuesRelative;
	protected final Color[] colors;

	protected double[] xValuesAbsolute;
	protected double[] yValuesAbsolute;

	protected ColoredPolygon myColoredPolygon;

	public SplinePainter(double[] xValuesRelative, double[] yValuesRelative, Color[] colors) {
		if (xValuesRelative == null || yValuesRelative == null)
			throw new IllegalArgumentException("xValuesRelative and yValuesRelative must not be null");
		if (xValuesRelative.length != yValuesRelative.length)
			throw new IllegalArgumentException(
					"xValuesRelative and yValuesRelative must have the same length, got " + xValuesRelative.length
							+ " and " + yValuesRelative.length);

		this.xValuesRelative = xValuesRelative;
		this.yValuesRelative = yValuesRelative;
		this.colors = colors;
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);
		if (xValuesAbsolute == null || yValuesAbsolute == null)
			return;

		Stroke s = g2.getStroke();
		g2.setStroke(stroke);
		ColorPolygonPainter.draw(g2, myColoredPolygon);
		g2.setStroke(s);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		if (rectangle == null) {
			xValuesAbsolute = null;
			yValuesAbsolute = null;
			myColoredPolygon = null;
			return;
		}

		xValuesAbsolute = new double[xValuesRelative.length];
		yValuesAbsolute = new double[yValuesRelative.length];

		for (int i = 0; i < xValuesRelative.length; i++) {
			xValuesAbsolute[i] = (int) Math.round(rectangle.getX() + xValuesRelative[i] * rectangle.getWidth());
			yValuesAbsolute[i] = (int) Math.round(rectangle.getY() + yValuesRelative[i] * rectangle.getHeight());
		}

		myColoredPolygon = BSplineTools.getSplinePolygon(xValuesAbsolute, yValuesAbsolute, PRUNING_DISTANCE, colors);
	}

	public double[] getxValuesRelative() {
		return xValuesRelative;
	}

	public double[] getyValuesRelative() {
		return yValuesRelative;
	}

	public Color[] getColors() {
		return colors;
	}

	public ColoredPolygon getData() {
		return myColoredPolygon;
	}
}
