package com.github.TKnudsen.infoVis.view.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.02
 */
public class DisplayTools {

	public static Font font = new Font("Tahoma", java.awt.Font.PLAIN, 12);

	public static float[] dashPattern = { 3, 3 };
	static float[] lightDashPattern = { 2, 4 };

	public final static BasicStroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
			10.0f, dashPattern, 0);
	public final static BasicStroke thickDashedStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0);
	public final static BasicStroke mediumDashedStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0);
	public final static BasicStroke standardStroke = new BasicStroke(1);
	public final static BasicStroke standardDashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0);
	public final static BasicStroke standardLightDashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10.0f, lightDashPattern, 0);

	public final static BasicStroke mediumStroke = new BasicStroke(2);
	public final static BasicStroke thickStroke = new BasicStroke(3);
	public final static BasicStroke veryThickStroke = new BasicStroke(5);
	public final static BasicStroke ultraThickStroke = new BasicStroke(7);
	public final static BasicStroke megaThickStroke = new BasicStroke(9);

	/**
	 * 
	 * @param g2
	 * @param x
	 * @param y
	 * @param r
	 * @param paint
	 * @param fill
	 */
	public static void drawPoint(Graphics2D g2, int x, int y, int r, Paint paint, boolean fill) {
		Objects.requireNonNull(g2);

		Paint p = g2.getPaint();
		g2.setPaint(paint);

		if (fill)
			g2.fillOval(x - r / 2, y - r / 2, r, r);
		else
			g2.drawOval(x - r / 2, y - r / 2, r, r);

		g2.setPaint(p);
	}

	/**
	 * 
	 * @param g2
	 * @param centerX
	 * @param centerY
	 * @param radius
	 * @param fill
	 */
	public static void drawPoint(Graphics2D g2, double centerX, double centerY, double radius, boolean fill) {
		Objects.requireNonNull(g2);

		Stroke s = g2.getStroke();

		if (fill) {
			// faster variant using lines with strokes
			Stroke thisStroke = new BasicStroke((float) (2 * radius), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
			g2.setStroke(thisStroke);

			if (radius < 0) {
				System.err.println("DisplayTools.drawPoint: radius < 0, radius used: 1.0");
				radius = 1;
			}

			drawLine(g2, centerX, centerY, centerX, centerY);

		} else {
			g2.setStroke(standardStroke);

			Ellipse2D circle = new Ellipse2D.Double();
			circle.setFrameFromCenter(centerX, centerY, centerX + radius, centerY + radius);
			g2.draw(circle);
		}

		g2.setStroke(s);
	}

	/**
	 * 
	 * @param g2
	 * @param centerX
	 * @param centerY
	 * @param radius
	 * @param paint
	 * @param fill
	 */
	public static void drawPoint(Graphics2D g2, double centerX, double centerY, double radius, Paint paint,
			boolean fill) {
		Objects.requireNonNull(g2);

		Paint c = g2.getPaint();
		g2.setPaint(paint);

		drawPoint(g2, centerX, centerY, radius, fill);

		g2.setPaint(c);
	}

	/**
	 * 
	 * @param g2
	 * @param rect
	 * @param paint
	 */
	public static void drawRectangle(Graphics2D g2, Rectangle2D rect, Paint paint) {
		Objects.requireNonNull(g2);

		Paint c = g2.getPaint();
		g2.setPaint(paint);

		if (rect != null)
			g2.draw(rect);

		g2.setPaint(c);
	}

	/**
	 * 
	 * @param g2
	 * @param rect
	 * @param paint
	 */
	public static void fillRectangle(Graphics2D g2, Rectangle2D rect, Paint paint) {
		Objects.requireNonNull(g2);

		Paint c = g2.getPaint();
		g2.setPaint(paint);

		g2.fill(rect);

		g2.setPaint(c);
	}

	public static void drawShape(Graphics2D g2, Shape shape, boolean fill) {
		Objects.requireNonNull(g2);

		if (fill)
			g2.fill(shape);

		g2.draw(shape);
	}

	public static void drawShape(Graphics2D g2, Shape shape, boolean fill, float strokeWidth, Paint color) {
		Objects.requireNonNull(g2);

		Stroke s = g2.getStroke();
		Paint c = g2.getPaint();

		g2.setStroke(new BasicStroke(strokeWidth));
		g2.setPaint(color);

		if (fill)
			g2.fill(shape);

		g2.draw(shape);

		g2.setPaint(c);
		g2.setStroke(s);
	}

	/**
	 * 
	 * @param g2
	 * @param midX
	 * @param midY
	 * @param size
	 */
	public static void drawCross(Graphics2D g2, double midX, double midY, double size) {
		Objects.requireNonNull(g2);

		DisplayTools.drawLine(g2, midX - size, midY - size, midX + size, midY + size);
		DisplayTools.drawLine(g2, midX - size, midY + size, midX + size, midY - size);
	}

	/**
	 * 
	 * @param g2
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public static void drawLine(Graphics2D g2, Number x1, Number y1, Number x2, Number y2) {
		Objects.requireNonNull(g2);

		Objects.requireNonNull(x1);
		Objects.requireNonNull(y1);
		Objects.requireNonNull(x2);
		Objects.requireNonNull(y2);

		g2.draw(new Line2D.Float(x1.floatValue(), y1.floatValue(), x2.floatValue(), y2.floatValue()));
	}

	/**
	 * 
	 * @param g2
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param strokeWidth
	 * @param color
	 */
	public static void drawLine(Graphics2D g2, Number x1, Number y1, Number x2, Number y2, float strokeWidth,
			Paint color) {
		Objects.requireNonNull(g2);

		Stroke s = g2.getStroke();
		Paint c = g2.getPaint();

		g2.setStroke(new BasicStroke(strokeWidth));
		g2.setPaint(color);

		drawLine(g2, x1, y1, x2, y2);

		g2.setPaint(c);
		g2.setStroke(s);
	}

	/**
	 * 
	 * @param g2
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param stroke
	 * @param color
	 */
	public static void drawLine(Graphics2D g2, Number x1, Number y1, Number x2, Number y2, Stroke stroke, Paint color) {
		Objects.requireNonNull(g2);

		Stroke s = g2.getStroke();
		Paint c = g2.getPaint();

		g2.setStroke(stroke);
		g2.setPaint(color);

		drawLine(g2, x1, y1, x2, y2);

		g2.setPaint(c);
		g2.setStroke(s);
	}

	/**
	 * 
	 * @param g2
	 * @param xPoints
	 * @param yPoints
	 * @param closed
	 * @param fill
	 */
	public static void drawPath(Graphics2D g2, double[] xPoints, double[] yPoints, boolean closed, boolean fill) {
		Objects.requireNonNull(g2);

		GeneralPath filledPolygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xPoints.length);

		filledPolygon.moveTo(xPoints[0], yPoints[0]);
		for (int index = 1; index < xPoints.length; index++) {
			filledPolygon.lineTo(xPoints[index], yPoints[index]);
		}
		if (closed)
			filledPolygon.closePath();

		if (fill)
			g2.fill(filledPolygon);

		g2.draw(filledPolygon);
	}

	/**
	 * draws a Rectangle with rounded edges. a Cube. The background of this tricky
	 * object consists of 7 single shapes, so you better don`t use semi-transparent
	 * colors to omit pixel artifacts.
	 * 
	 * @param g2
	 * @param rect
	 * @param xArc
	 * @param yArc
	 * @param background
	 * @param surroundStroke
	 * @param surroundColor
	 */
	public static void drawCube(Graphics2D g2, Rectangle2D rect, double xArc, double yArc, Paint background,
			BasicStroke surroundStroke, Paint surroundColor) {
		Objects.requireNonNull(g2);

		Paint c = g2.getPaint();
		Stroke s = g2.getStroke();

		// Ellipse2D...
		RoundRectangle2D rr = new RoundRectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(),
				xArc, yArc);

		if (background != null) {
			g2.setPaint(background);
			g2.fill(rr);
		}

		if (surroundColor != null)
			g2.setPaint(surroundColor);
		if (surroundStroke != null)
			g2.setStroke(surroundStroke);
		g2.draw(rr);

		// write back
		g2.setStroke(s);
		g2.setPaint(c);
	}

	/**
	 * 
	 * @param g2
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param arcWidth
	 * @param arcHeight
	 * @param color
	 */
	public static void drawRoundRect(Graphics2D g2, int x, int y, int width, int height, int arcWidth, int arcHeight,
			Paint color) {
		Objects.requireNonNull(g2);

		Paint c = g2.getPaint();
		Stroke s = g2.getStroke();

		g2.setPaint(color);
		g2.setStroke(standardStroke);
		g2.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
		g2.drawRoundRect(x, y, width, height, arcWidth, arcHeight);

		// write back
		g2.setStroke(s);
		g2.setPaint(c);
	}

	/**
	 * paints a button-like primitive.
	 * 
	 * @param g2
	 * @param centerX
	 * @param centerY
	 * @param radius
	 */
	public static void drawButton(Graphics2D g2, int centerX, int centerY, int radius) {
		Objects.requireNonNull(g2);

		Stroke s = g2.getStroke();
		Paint c = g2.getPaint();

		drawPoint(g2, centerX, centerY, radius, Color.WHITE, true);
		drawPoint(g2, centerX, centerY, radius + 1, Color.LIGHT_GRAY, false);
		drawLine(g2, centerX - radius / 3.0, centerY, centerX + radius / 3.0, centerY);

		g2.setStroke(new BasicStroke(2));
		g2.setColor(Color.LIGHT_GRAY);

		g2.setPaint(c);
		g2.setStroke(s);
	}

	/**
	 * 
	 * @param g2
	 * @param text
	 * @param x
	 * @param y
	 * @param angle
	 */
	public static void drawRotatedString(Graphics2D g2, String text, float x, float y, double angle) {
		Objects.requireNonNull(g2);

		AffineTransform old = g2.getTransform();
		AffineTransform rotate = AffineTransform.getRotateInstance(angle, x, y);
		g2.transform(rotate);
		g2.drawString(text, x, y);
		g2.setTransform(old);
	}

}
