package com.github.TKnudsen.infoVis.view.painters.color;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.ComplexDataObject.data.color.ColorHistogram;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.visualPrimitives.ColoredPolygon;

/**
 * Radial chart of a hue histogram: one colored spike per hue bin, radiating
 * from a masked-out inner circle.
 *
 * @version 1.0
 * @since 2022
 */
public class ColorHuePainter extends ChartPainter {

	private final double[] hues;
	private final double hueMax;

	private final double radiusInnerPercentage;
	private double radiusInnerX = 0;
	private double radiusInnerY = 0;

	public ColorHuePainter(ColorHistogram histogram) {
		this(histogram.getHues());
	}

	public ColorHuePainter(double[] hues) {
		this(hues, 0.25);
	}

	public ColorHuePainter(double[] hues, double radiusInnerPercentage) {
		this.hues = hues;
		this.hueMax = new StatisticsSupport(hues).getMax();
		this.radiusInnerPercentage = radiusInnerPercentage;
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		Color c = g2.getColor();
		Stroke s = g2.getStroke();

		drawHue(g2);

		Ellipse2D circle = new Ellipse2D.Double();
		circle.setFrameFromCenter(rectangle.getCenterX(), rectangle.getCenterY(), rectangle.getCenterX() + radiusInnerX,
				rectangle.getCenterY() + radiusInnerY);
		g2.setColor(Color.WHITE);
		g2.fill(circle);
		g2.setColor(Color.BLACK);
		g2.draw(circle);

		g2.setStroke(s);
		g2.setColor(c);
	}

	private void drawHue(Graphics2D g2) {
		g2.setStroke(DisplayTools.thickStroke);

		ColoredPolygon poly = new ColoredPolygon();
		for (int i = 0; i < hues.length; i++) {
			double alpha = i / (double) hues.length * 360;
			Point2D.Double p = getInnerRadius(alpha);

			Point2D.Double po1 = getOuterRadius(alpha, MathFunctions.linearScale(0, hueMax, hues[i], true));

			Color col = new Color(Color.HSBtoRGB((float) alpha / 360.0f, 0.7f, 0.75f));
			g2.setColor(col);

			DisplayTools.drawLine(g2, p.getX(), p.getY(), po1.getX(), po1.getY());
			DisplayTools.drawPoint(g2, po1.getX(), po1.getY(), 0, col, true);

			poly.addPoint((int) po1.getX(), (int) po1.getY(), col);
		}
		poly.addPoint(poly.xpoints[0], poly.ypoints[0], poly.getColor(0));

		ColorPolygonPainter.draw(g2, poly);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		radiusInnerX = 0.5 * rectangle.getWidth() * radiusInnerPercentage;
		radiusInnerY = 0.5 * rectangle.getHeight() * radiusInnerPercentage;
	}

	/**
	 * calculates the inner point of the inner radius for a given angle in degrees
	 * (0-360 degrees)
	 *
	 * @param degreeGrad
	 * @return
	 */
	private Point2D.Double getInnerRadius(double degreeGrad) {
		return new Point2D.Double(rectangle.getCenterX() + Math.sin(degreeGrad / 360 * 2 * Math.PI) * radiusInnerX,
				rectangle.getCenterY() - Math.cos(degreeGrad / 360 * 2 * Math.PI) * radiusInnerY);
	}

	/**
	 * calculates the outer point for a given angle in degrees (0-360 degrees) and
	 * a ratio (0-1) of the available radial distance
	 *
	 * @param degreeGrad
	 * @param ratio
	 * @return
	 */
	private Point2D.Double getOuterRadius(double degreeGrad, double ratio) {
		Point2D.Double p = getInnerRadius(degreeGrad);
		double deltaX = rectangle.getWidth() * 0.5 - radiusInnerX;
		double deltaY = rectangle.getHeight() * 0.5 - radiusInnerY;
		return new Point2D.Double(p.getX() + Math.sin(degreeGrad / 360 * 2 * Math.PI) * deltaX * ratio,
				p.getY() - Math.cos(degreeGrad / 360 * 2 * Math.PI) * deltaY * ratio);
	}
}
