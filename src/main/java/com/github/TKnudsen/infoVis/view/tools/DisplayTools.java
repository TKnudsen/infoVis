package com.github.TKnudsen.infoVis.view.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

/**
 * @version 2.03
 * @since 2016
 */
public class DisplayTools {

	private static Font font = new Font("Tahoma", java.awt.Font.PLAIN, 12);

	private static float[] dashPattern = { 3, 3 };
	static float[] lightDashPattern = { 2, 4 };

	public static Font getFont() {
		return font;
	}

	public static void setFont(Font font) {
		DisplayTools.font = Objects.requireNonNull(font, "font must not be null");
	}

	/**
	 * @return a defensive copy of the dash pattern; safe to mutate without
	 *         affecting shared state
	 */
	public static float[] getDashPattern() {
		return dashPattern.clone();
	}

	public static void setDashPattern(float[] dashPattern) {
		Objects.requireNonNull(dashPattern, "dashPattern must not be null");
		DisplayTools.dashPattern = dashPattern.clone();
	}

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

	public final static BasicStroke mediumStroke = BasicStrokeTools.get(2);
	public final static BasicStroke thickStroke = BasicStrokeTools.get(3);
	public final static BasicStroke veryThickStroke = BasicStrokeTools.get(5);
	public final static BasicStroke ultraThickStroke = BasicStrokeTools.get(7);
	public final static BasicStroke megaThickStroke = BasicStrokeTools.get(9);

	/**
	 * Fastest version for integer-based drawing. Uses the current Graphics2D paint
	 * and avoids any allocations.
	 *
	 * @param g2   the Graphics2D context (non-null)
	 * @param x    center x-coordinate
	 * @param y    center y-coordinate
	 * @param r    diameter (not radius)
	 * @param fill true to fill, false to draw outline
	 */
	public static void drawPoint(Graphics2D g2, int x, int y, int r, boolean fill) {
		if (g2 == null)
			return;
		if (r <= 0)
			r = 1;

		final int radius = r >> 1; // divide by 2 efficiently
		final int topLeftX = x - radius;
		final int topLeftY = y - radius;

		if (fill)
			g2.fillOval(topLeftX, topLeftY, r, r);
		else
			g2.drawOval(topLeftX, topLeftY, r, r);
	}

	/**
	 * Convenience wrapper that temporarily applies the given paint. Use this for
	 * occasional colored points; for high-volume drawing, manage paint externally
	 * and call the version without paint.
	 * 
	 * @deprecated try to use the variant that does not handle color here. That can
	 *             be faster and typically color is better handled outside anyways.
	 *
	 * @param g2    the Graphics2D context (non-null)
	 * @param x     center x-coordinate
	 * @param y     center y-coordinate
	 * @param r     diameter (not radius)
	 * @param paint paint to use for this point (may be null)
	 * @param fill  true to fill, false to draw outline
	 */
	public static void drawPoint(Graphics2D g2, int x, int y, int r, Paint paint, boolean fill) {
		if (g2 == null)
			return;
		if (r <= 0)
			r = 1;

		final Paint old = g2.getPaint();
		if (paint != null && paint != old)
			g2.setPaint(paint);

		drawPoint(g2, x, y, r, fill);

		if (paint != null && paint != old)
			g2.setPaint(old);
	}

	/**
	 * Default fast routine for drawing a point (circle) using the current
	 * Graphics2D paint and stroke. Designed for bulk drawing where paint is managed
	 * externally for performance.
	 *
	 * @param g2      the Graphics2D context (non-null)
	 * @param centerX the x-coordinate of the point center
	 * @param centerY the y-coordinate of the point center
	 * @param radius  the radius in pixels (values <= 0 default to 1)
	 * @param fill    true to fill the circle, false to draw only the outline
	 */
	public static void drawPoint(Graphics2D g2, double centerX, double centerY, double radius, boolean fill) {
		if (g2 == null)
			return;
		if (radius <= 0)
			radius = 1.0;

		final int diameter = (int) Math.round(radius * 2.0);
		final int topLeftX = (int) Math.round(centerX - radius);
		final int topLeftY = (int) Math.round(centerY - radius);

		if (fill)
			g2.fillOval(topLeftX, topLeftY, diameter, diameter);
		else
			g2.drawOval(topLeftX, topLeftY, diameter, diameter);
	}

	/**
	 * Convenience wrapper around
	 * {@link #drawPoint(Graphics2D, double, double, double, boolean)}. Temporarily
	 * sets the paint for this call and restores it afterwards. Use this for ad-hoc
	 * drawing; for high-volume rendering, manage the paint outside the loop and
	 * call the paint-less variant instead.
	 * 
	 * @deprecated try to use the variant that does not handle color here. That can
	 *             be faster and typically color is better handled outside anyways.
	 *
	 * @param g2      the Graphics2D context (non-null)
	 * @param centerX the x-coordinate of the point center
	 * @param centerY the y-coordinate of the point center
	 * @param radius  the radius in pixels (values <= 0 default to 1)
	 * @param paint   the paint to use for this point
	 * @param fill    true to fill the circle, false to draw only the outline
	 */
	public static void drawPoint(Graphics2D g2, double centerX, double centerY, double radius, Paint paint,
			boolean fill) {
		if (g2 == null)
			return;
		if (radius <= 0)
			radius = 1.0;

		final Paint oldPaint = g2.getPaint();
		if (paint != null && paint != oldPaint)
			g2.setPaint(paint);

		drawPoint(g2, centerX, centerY, radius, fill);

		if (paint != null && paint != oldPaint)
			g2.setPaint(oldPaint);
	}

	/**
	 * Fastest version for drawing rectangle outlines.
	 * 
	 * <p>
	 * Uses the primitive {@link Graphics2D#drawRect(int, int, int, int)} call
	 * instead of {@code g2.draw(Rectangle2D)}, which avoids creating a
	 * {@link java.awt.geom.PathIterator} and allows the rendering pipeline to use
	 * hardware-accelerated rectangle drawing directly.
	 * </p>
	 * 
	 * <p>
	 * Assumes the current paint is already set on the {@code Graphics2D} context.
	 * Use {@link #drawRectangle(Graphics2D, Rectangle2D, Paint)} if you want
	 * automatic paint handling.
	 * </p>
	 * 
	 * @param g2   the Graphics2D context (non-null)
	 * @param rect the rectangle to draw (non-null)
	 */
	public static void drawRectangle(Graphics2D g2, Rectangle2D rect) {
		if (g2 == null || rect == null)
			return;

		g2.drawRect((int) Math.round(rect.getX()), (int) Math.round(rect.getY()), (int) Math.round(rect.getWidth()),
				(int) Math.round(rect.getHeight()));
	}

	/**
	 * Convenience wrapper for {@link #drawRectangle(Graphics2D, Rectangle2D)}.
	 * 
	 * <p>
	 * Temporarily applies the specified paint, draws the rectangle outline, and
	 * restores the previous paint afterward. Paint changes are skipped if the
	 * requested paint is the same as the current one, minimizing pipeline stalls.
	 * </p>
	 * 
	 * @deprecated try to use the variant that does not handle color here. That can
	 *             be faster and typically color is better handled outside anyways.
	 * 
	 * @param g2    the Graphics2D context (non-null)
	 * @param rect  the rectangle to draw (non-null)
	 * @param paint the paint to apply temporarily (may be {@code null})
	 */
	public static void drawRectangle(Graphics2D g2, Rectangle2D rect, Paint paint) {
		if (g2 == null || rect == null)
			return;

		final Paint old = g2.getPaint();
		if (paint != null && paint != old)
			g2.setPaint(paint);

		drawRectangle(g2, rect);

		if (paint != null && paint != old)
			g2.setPaint(old);
	}

	/**
	 * Fastest version for filling rectangles.
	 * 
	 * <p>
	 * Uses the primitive {@link Graphics2D#fillRect(int, int, int, int)} call
	 * instead of {@code g2.fill(Rectangle2D)} to avoid shape creation and to
	 * leverage GPU-accelerated fill operations.
	 * </p>
	 * 
	 * <p>
	 * Assumes the paint is already configured externally on the {@code Graphics2D}
	 * context. Use {@link #fillRectangle(Graphics2D, Rectangle2D, Paint)} for the
	 * version with automatic paint handling.
	 * </p>
	 * 
	 * @param g2   the Graphics2D context (non-null)
	 * @param rect the rectangle to fill (non-null)
	 */
	public static void fillRectangle(Graphics2D g2, Rectangle2D rect) {
		if (g2 == null || rect == null)
			return;

		g2.fillRect((int) Math.round(rect.getX()), (int) Math.round(rect.getY()), (int) Math.round(rect.getWidth()),
				(int) Math.round(rect.getHeight()));
	}

	/**
	 * Convenience wrapper for {@link #fillRectangle(Graphics2D, Rectangle2D)}.
	 * 
	 * <p>
	 * Temporarily sets the provided paint, fills the rectangle, and restores the
	 * original paint afterward. Paint updates are conditional to avoid redundant
	 * state changes, which improves performance in tight render loops.
	 * </p>
	 * 
	 * @deprecated try to use the variant that does not handle color here. That can
	 *             be faster and typically color is better handled outside anyways.
	 * 
	 * @param g2    the Graphics2D context (non-null)
	 * @param rect  the rectangle to fill (non-null)
	 * @param paint the paint to apply temporarily (may be {@code null})
	 */
	public static void fillRectangle(Graphics2D g2, Rectangle2D rect, Paint paint) {
		if (g2 == null || rect == null)
			return;

		final Paint old = g2.getPaint();
		if (paint != null && paint != old)
			g2.setPaint(paint);

		fillRectangle(g2, rect);

		if (paint != null && paint != old)
			g2.setPaint(old);
	}

	/**
	 * Fastest general-purpose variant for drawing or filling arbitrary shapes.
	 * 
	 * <p>
	 * Performs exactly one rendering operation (fill or draw) depending on the
	 * {@code fill} flag. Does <b>not</b> modify paint or stroke.
	 * </p>
	 *
	 * <p>
	 * Use when the Graphics2D context already has the desired color/stroke
	 * configured (e.g., inside a tight rendering loop).
	 * </p>
	 *
	 * @param g2    the Graphics2D context (non-null)
	 * @param shape the shape to render (non-null)
	 * @param fill  if {@code true}, additionally fills the shape; the outline is
	 *              always stroked either way
	 */
	public static void drawShape(Graphics2D g2, Shape shape, boolean fill) {
		if (g2 == null || shape == null)
			return;

		if (fill)
			g2.fill(shape);
		g2.draw(shape);
	}

	/**
	 * Convenience wrapper that temporarily applies stroke and paint.
	 * 
	 * <p>
	 * Applies a stroke width and paint only for the duration of a single draw or
	 * fill operation, restoring the previous state afterward.
	 * </p>
	 *
	 * <p>
	 * State changes are conditional - if the stroke or paint are already set, they
	 * will not be re-applied to avoid costly pipeline flushes.
	 * </p>
	 *
	 * @param g2          the Graphics2D context (non-null)
	 * @param shape       the shape to draw or fill (non-null)
	 * @param fill        if {@code true}, fills the shape; otherwise, draws it
	 * @param strokeWidth stroke width to use for outlines (ignored for fills)
	 * @param color       paint color to use (may be {@code null})
	 */
	public static void drawShape(Graphics2D g2, Shape shape, boolean fill, float strokeWidth, Paint color) {
		if (g2 == null || shape == null)
			return;

		final Stroke oldStroke = g2.getStroke();
		final Paint oldPaint = g2.getPaint();

		boolean strokeChanged = false;
		boolean paintChanged = false;

		// Apply stroke if width differs
		Stroke newStroke = BasicStrokeTools.get(strokeWidth);
		if (!oldStroke.equals(newStroke)) {
			g2.setStroke(newStroke);
			strokeChanged = true;
		}

		// Apply paint only if different
		if (color != null && color != oldPaint) {
			g2.setPaint(color);
			paintChanged = true;
		}

		if (fill)
			g2.fill(shape);
		else
			g2.draw(shape);

		// Restore paint and stroke if modified
		if (paintChanged)
			g2.setPaint(oldPaint);
		if (strokeChanged)
			g2.setStroke(oldStroke);
	}

	/**
	 * 
	 * @param g2   g2
	 * @param midX X
	 * @param midY Y
	 * @param size size
	 */
	public static void drawCross(Graphics2D g2, float midX, float midY, float size) {
		Objects.requireNonNull(g2);

		DisplayTools.drawLine(g2, midX - size, midY - size, midX + size, midY + size);
		DisplayTools.drawLine(g2, midX - size, midY + size, midX + size, midY - size);
	}

	/**
	 * @deprecated use float.
	 * @param g2   g2
	 * @param midX X
	 * @param midY Y
	 * @param size size
	 */
	public static void drawCross(Graphics2D g2, double midX, double midY, double size) {
		Objects.requireNonNull(g2);

		DisplayTools.drawLine(g2, midX - size, midY - size, midX + size, midY + size);
		DisplayTools.drawLine(g2, midX - size, midY + size, midX + size, midY - size);
	}

	/**
	 * Fastest variant for drawing a line between two float coordinates.
	 *
	 * <p>
	 * Uses the primitive {@link Graphics2D#drawLine(int, int, int, int)} instead of
	 * allocating a {@link java.awt.geom.Line2D} object. This avoids garbage
	 * creation and gives direct access to the hardware-accelerated pipeline.
	 * </p>
	 *
	 * <p>
	 * Assumes the paint and stroke are already set on the {@code Graphics2D}
	 * context. Use {@link #drawLine(Graphics2D, float, float, float, float, Paint)}
	 * for the version with temporary paint handling.
	 * </p>
	 *
	 * @param g2 the Graphics2D context (non-null)
	 * @param x1 start x-coordinate
	 * @param y1 start y-coordinate
	 * @param x2 end x-coordinate
	 * @param y2 end y-coordinate
	 */
	public static void drawLine(Graphics2D g2, float x1, float y1, float x2, float y2) {
		if (g2 == null)
			return;

		// Objects.requireNonNull(g2);
		// g2.draw(new Line2D.Float(x1, y1, x2, y2));

		// Convert to ints for native rasterization - rounding avoids truncation bias
		g2.drawLine(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y2));
	}

	/**
	 * Convenience wrapper for
	 * {@link #drawLine(Graphics2D, float, float, float, float)}.
	 *
	 * <p>
	 * Temporarily applies the given paint, draws the line, and restores the
	 * previous paint afterward. Paint changes are conditional to prevent
	 * unnecessary rendering pipeline flushes.
	 * </p>
	 *
	 * @param g2    the Graphics2D context (non-null)
	 * @param x1    start x-coordinate
	 * @param y1    start y-coordinate
	 * @param x2    end x-coordinate
	 * @param y2    end y-coordinate
	 * @param paint the paint to use temporarily (may be {@code null})
	 */
	public static void drawLine(Graphics2D g2, float x1, float y1, float x2, float y2, Paint paint) {
		if (g2 == null)
			return;

		final Paint old = g2.getPaint();
		if (paint != null && paint != old)
			g2.setPaint(paint);

		drawLine(g2, x1, y1, x2, y2);

		if (paint != null && paint != old)
			g2.setPaint(old);
	}

	/**
	 * Convenience wrapper for drawing a line with a temporary stroke and paint.
	 *
	 * <p>
	 * Temporarily applies the specified {@link Stroke} and {@link Paint} to the
	 * {@link Graphics2D} context, draws the line, and restores the previous state.
	 * Conditional updates are used to prevent unnecessary rendering pipeline
	 * flushes.
	 * </p>
	 *
	 * <p>
	 * Internally delegates to the fast primitive-based version
	 * {@link #drawLine(Graphics2D, float, float, float, float)} for maximum
	 * performance.
	 * </p>
	 *
	 * @param g2     the Graphics2D context (non-null)
	 * @param x1     start x-coordinate
	 * @param y1     start y-coordinate
	 * @param x2     end x-coordinate
	 * @param y2     end y-coordinate
	 * @param stroke the stroke to use temporarily (may be {@code null})
	 * @param paint  the paint to use temporarily (may be {@code null})
	 */
	public static void drawLine(Graphics2D g2, float x1, float y1, float x2, float y2, Stroke stroke, Paint paint) {
		if (g2 == null)
			return;

		final Stroke oldStroke = g2.getStroke();
		final Paint oldPaint = g2.getPaint();

		boolean strokeChanged = false;
		boolean paintChanged = false;

		// Apply stroke if different
		if (stroke != null && !stroke.equals(oldStroke)) {
			g2.setStroke(stroke);
			strokeChanged = true;
		}

		// Apply paint if different
		if (paint != null && paint != oldPaint) {
			g2.setPaint(paint);
			paintChanged = true;
		}

		drawLine(g2, x1, y1, x2, y2);

		// Restore only if changed
		if (paintChanged)
			g2.setPaint(oldPaint);
		if (strokeChanged)
			g2.setStroke(oldStroke);
	}

	// Convenience wrapper for ad-hoc cases (rare)
	@Deprecated
	public static void drawLine(Graphics2D g2, Number x1, Number y1, Number x2, Number y2) {
		if (g2 == null)
			return;

		drawLine(g2, x1.floatValue(), y1.floatValue(), x2.floatValue(), y2.floatValue());
	}

	/**
	 * Convenience wrapper for Number inputs.
	 *
	 * <p>
	 * Delegates to the float-based version. Use only for infrequent or non-critical
	 * rendering; prefer the float version for performance.
	 * </p>
	 */
	@Deprecated
	public static void drawLine(Graphics2D g2, Number x1, Number y1, Number x2, Number y2, float strokeWidth,
			Paint paint) {
		if (g2 == null)
			return;
		drawLine(g2, x1.floatValue(), y1.floatValue(), x2.floatValue(), y2.floatValue(), strokeWidth, paint);
	}

	/**
	 * Convenience wrapper for Number parameters.
	 *
	 * <p>
	 * Converts coordinates to primitives and delegates to the optimized float-based
	 * version. Intended for non-critical or external API calls.
	 * </p>
	 */
	@Deprecated
	public static void drawLine(Graphics2D g2, Number x1, Number y1, Number x2, Number y2, Stroke stroke, Paint paint) {
		if (g2 == null)
			return;

		drawLine(g2, x1.floatValue(), y1.floatValue(), x2.floatValue(), y2.floatValue(), stroke, paint);
	}

//	/**
//	 * 
//	 * @param g2      g2
//	 * @param xPoints x
//	 * @param yPoints y
//	 * @param closed  if closed
//	 * @param fill    if fill
//	 */
//	public static void drawPath(Graphics2D g2, double[] xPoints, double[] yPoints, boolean closed, boolean fill) {
//		Objects.requireNonNull(g2);
//
//		GeneralPath filledPolygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xPoints.length);
//
//		filledPolygon.moveTo(xPoints[0], yPoints[0]);
//		for (int index = 1; index < xPoints.length; index++) {
//			filledPolygon.lineTo(xPoints[index], yPoints[index]);
//		}
//		if (closed)
//			filledPolygon.closePath();
//
//		if (fill)
//			g2.fill(filledPolygon);
//
//		g2.draw(filledPolygon);
//	}

	/**
	 * Fastest version for drawing or filling a polygonal path.
	 *
	 * <p>
	 * Avoids allocating {@link GeneralPath} objects by using direct primitive
	 * arrays. This is typically an order of magnitude faster for simple
	 * polyline/polygon drawing and is hardware-accelerated in most Java2D
	 * pipelines.
	 * </p>
	 *
	 * @param g2      the Graphics2D context (non-null)
	 * @param xPoints array of x coordinates (non-null, same length as yPoints)
	 * @param yPoints array of y coordinates (non-null, same length as xPoints)
	 * @param closed  if true, closes the polygon automatically
	 * @param fill    if true, fills instead of drawing the outline
	 */
	public static void drawPath(Graphics2D g2, double[] xPoints, double[] yPoints, boolean closed, boolean fill) {
		if (g2 == null || xPoints == null || yPoints == null)
			return;

		final int n = Math.min(xPoints.length, yPoints.length);
		if (n < 2)
			return;

		// Convert to int arrays once - rounded for better pixel alignment
		final int[] xi = new int[n];
		final int[] yi = new int[n];
		for (int i = 0; i < n; i++) {
			xi[i] = (int) Math.round(xPoints[i]);
			yi[i] = (int) Math.round(yPoints[i]);
		}

		if (fill) {
			if (closed)
				g2.fillPolygon(xi, yi, n);
			else
				g2.fillPolygon(xi, yi, n); // no polyline fill variant; fine for closed=false
		} else {
			if (closed)
				g2.drawPolygon(xi, yi, n);
			else
				g2.drawPolyline(xi, yi, n);
		}
	}

//	/**
//	 * draws a Rectangle with rounded edges. a Cube. The background of this tricky
//	 * object consists of 7 single shapes, so you better don`t use semi-transparent
//	 * colors to omit pixel artifacts.
//	 * 
//	 * @param g2             g2
//	 * @param rect           rectangle
//	 * @param xArc           x
//	 * @param yArc           y
//	 * @param background     background color
//	 * @param surroundStroke stroke
//	 * @param surroundColor  color
//	 */
//	public static void drawCube(Graphics2D g2, Rectangle2D rect, double xArc, double yArc, Paint background,
//			BasicStroke surroundStroke, Paint surroundColor) {
//		Objects.requireNonNull(g2);
//
//		Paint c = g2.getPaint();
//		Stroke s = g2.getStroke();
//
//		// Ellipse2D...
//		RoundRectangle2D rr = new RoundRectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(),
//				xArc, yArc);
//
//		if (background != null) {
//			g2.setPaint(background);
//			g2.fill(rr);
//		}
//
//		if (surroundColor != null)
//			g2.setPaint(surroundColor);
//		if (surroundStroke != null)
//			g2.setStroke(surroundStroke);
//		g2.draw(rr);
//
//		// write back
//		g2.setStroke(s);
//		g2.setPaint(c);
//	}

	/**
	 * Fastest variant for drawing a rounded rectangle ("cube") with optional
	 * background and stroke.
	 *
	 * <p>
	 * Reuses a thread-local {@link RoundRectangle2D.Float} to avoid heap allocation
	 * and conditionally applies paint/stroke only when needed. Designed for
	 * high-frequency rendering loops.
	 * </p>
	 *
	 * @param g2             Graphics2D context (non-null)
	 * @param rect           rectangle bounds (non-null)
	 * @param xArc           horizontal arc radius
	 * @param yArc           vertical arc radius
	 * @param background     fill paint (may be {@code null})
	 * @param surroundStroke outline stroke (may be {@code null})
	 * @param surroundColor  outline paint (may be {@code null})
	 */
	public static void drawCube(Graphics2D g2, Rectangle2D rect, double xArc, double yArc, Paint background,
			Stroke surroundStroke, Paint surroundColor) {
		if (g2 == null || rect == null)
			return;

		// Reuse thread-local round rectangle for zero allocation
		RoundRectangle2D.Float rr = THREAD_LOCAL_ROUND_RECT.get();
		rr.setRoundRect((float) rect.getX(), (float) rect.getY(), (float) rect.getWidth(), (float) rect.getHeight(),
				(float) xArc, (float) yArc);

		final Paint oldPaint = g2.getPaint();
		final Stroke oldStroke = g2.getStroke();

		boolean paintChanged = false;
		boolean strokeChanged = false;

		// Fill background if requested
		if (background != null) {
			if (background != oldPaint) {
				g2.setPaint(background);
				paintChanged = true;
			}
			g2.fill(rr);
		}

		// The border is always drawn -- surroundStroke/surroundColor being null
		// means "use whatever stroke/paint g2 already has", not "skip the border"
		// (matches the pre-refactor behavior kept above in the commented-out
		// old version).
		if (surroundStroke != null && !oldStroke.equals(surroundStroke)) {
			g2.setStroke(surroundStroke);
			strokeChanged = true;
		}

		if (surroundColor != null && surroundColor != oldPaint) {
			g2.setPaint(surroundColor);
			paintChanged = true;
		}

		g2.draw(rr);

		// Restore only if changed
		if (paintChanged)
			g2.setPaint(oldPaint);
		if (strokeChanged)
			g2.setStroke(oldStroke);
	}

	// Thread-local reusable shape to avoid per-call allocation
	private static final ThreadLocal<RoundRectangle2D.Float> THREAD_LOCAL_ROUND_RECT = ThreadLocal
			.withInitial(RoundRectangle2D.Float::new);

//	/**
//	 * 
//	 * @param g2        g2
//	 * @param x         x
//	 * @param y         y
//	 * @param width     width
//	 * @param height    height
//	 * @param arcWidth  arc width
//	 * @param arcHeight arc height
//	 * @param color     color
//	 */
//	public static void drawRoundRect(Graphics2D g2, int x, int y, int width, int height, int arcWidth, int arcHeight,
//			Paint color) {
//		Objects.requireNonNull(g2);
//
//		Paint c = g2.getPaint();
//		Stroke s = g2.getStroke();
//
//		g2.setPaint(color);
//		g2.setStroke(standardStroke);
//		g2.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
//		g2.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
//
//		// write back
//		g2.setStroke(s);
//		g2.setPaint(c);
//	}

	/**
	 * Fastest variant for drawing and filling a rounded rectangle.
	 *
	 * <p>
	 * Avoids per-call allocations and conditional state changes to minimize Java2D
	 * pipeline stalls. Designed for use inside high-frequency render loops.
	 * </p>
	 *
	 * @param g2        the Graphics2D context (non-null)
	 * @param x         x-coordinate of the top-left corner
	 * @param y         y-coordinate of the top-left corner
	 * @param width     rectangle width
	 * @param height    rectangle height
	 * @param arcWidth  horizontal corner radius
	 * @param arcHeight vertical corner radius
	 * @param paint     paint to use for fill and stroke (may be {@code null})
	 * @param fill      whether to fill the shape before drawing its outline
	 * @param stroke    stroke for the outline (may be {@code null})
	 */
	public static void drawRoundRect(Graphics2D g2, int x, int y, int width, int height, int arcWidth, int arcHeight,
			Paint paint, boolean fill, Stroke stroke) {
		if (g2 == null)
			return;

		final Paint oldPaint = g2.getPaint();
		final Stroke oldStroke = g2.getStroke();

		boolean paintChanged = false;
		boolean strokeChanged = false;

		if (paint != null && paint != oldPaint) {
			g2.setPaint(paint);
			paintChanged = true;
		}

		if (stroke != null && !oldStroke.equals(stroke)) {
			g2.setStroke(stroke);
			strokeChanged = true;
		}

		if (fill)
			g2.fillRoundRect(x, y, width, height, arcWidth, arcHeight);

		g2.drawRoundRect(x, y, width, height, arcWidth, arcHeight);

		if (paintChanged)
			g2.setPaint(oldPaint);
		if (strokeChanged)
			g2.setStroke(oldStroke);
	}

//	/**
//	 * paints a button-like primitive.
//	 * 
//	 * @param g2      g2
//	 * @param centerX x
//	 * @param centerY y
//	 * @param radius  r
//	 */
//	public static void drawButton(Graphics2D g2, int centerX, int centerY, int radius) {
//		Objects.requireNonNull(g2);
//
//		Stroke s = g2.getStroke();
//		Paint c = g2.getPaint();
//
//		drawPoint(g2, centerX, centerY, radius, Color.WHITE, true);
//		drawPoint(g2, centerX, centerY, radius + 1, Color.LIGHT_GRAY, false);
//		drawLine(g2, centerX - radius / 3.0, centerY, centerX + radius / 3.0, centerY);
//
//		g2.setStroke(BasicStrokeTools.get(2));
//		g2.setColor(Color.LIGHT_GRAY);
//
//		g2.setPaint(c);
//		g2.setStroke(s);
//	}

	/**
	 * Fastest version of a simple button-like primitive.
	 *
	 * <p>
	 * Draws a filled circle with a subtle light-gray border and a horizontal line
	 * (as a visual accent). Designed for high-frequency rendering or lightweight UI
	 * primitives.
	 * </p>
	 *
	 * <p>
	 * Avoids per-call allocations and redundant paint/stroke changes. Uses integer
	 * rasterization for speed and predictable pixel alignment.
	 * </p>
	 *
	 * @param g2      the Graphics2D context (non-null)
	 * @param centerX x-coordinate of the button center
	 * @param centerY y-coordinate of the button center
	 * @param radius  button radius (in pixels)
	 */
	public static void drawButton(Graphics2D g2, int centerX, int centerY, int radius) {
		if (g2 == null)
			return;

		// Cache old state
		final Paint oldPaint = g2.getPaint();
		final Stroke oldStroke = g2.getStroke();

		// Local references (avoid repeated lookup)
		final int diameter = radius * 2;
		final int topLeftX = centerX - radius;
		final int topLeftY = centerY - radius;

		boolean paintChanged = false;
		boolean strokeChanged = false;

		// --- Fill background (white) ---
		if (oldPaint != Color.WHITE) {
			g2.setPaint(Color.WHITE);
			paintChanged = true;
		}
		g2.fillOval(topLeftX, topLeftY, diameter, diameter);

		// --- Draw light-gray outline ---
		if (oldPaint != Color.LIGHT_GRAY) {
			g2.setPaint(Color.LIGHT_GRAY);
			paintChanged = true;
		}

		// Use a thin stroke for outline
		Stroke outlineStroke = BasicStrokeTools.get(1f);
		if (!oldStroke.equals(outlineStroke)) {
			g2.setStroke(outlineStroke);
			strokeChanged = true;
		}

		g2.drawOval(topLeftX, topLeftY, diameter, diameter);

		// --- Draw inner horizontal line accent ---
		int lineHalf = radius / 3;
		g2.drawLine(centerX - lineHalf, centerY, centerX + lineHalf, centerY);

		// --- Restore only if changed ---
		if (paintChanged)
			g2.setPaint(oldPaint);
		if (strokeChanged)
			g2.setStroke(oldStroke);
	}

//	/**
//	 * 
//	 * @param g2    g2
//	 * @param text  text
//	 * @param x     x
//	 * @param y     y
//	 * @param angle angle
//	 */
//	public static void drawRotatedString(Graphics2D g2, String text, float x, float y, double angle) {
//		Objects.requireNonNull(g2);
//
//		AffineTransform old = g2.getTransform();
//		AffineTransform rotate = AffineTransform.getRotateInstance(angle, x, y);
//		g2.transform(rotate);
//		g2.drawString(text, x, y);
//		g2.setTransform(old);
//	}

	/**
	 * Fastest version for drawing a rotated string.
	 *
	 * <p>
	 * Avoids allocation of {@link AffineTransform} objects and minimizes graphics
	 * pipeline state changes. Designed for high-frequency text rendering (axis
	 * labels, tick marks, etc.).
	 * </p>
	 *
	 * @param g2    the Graphics2D context (non-null)
	 * @param text  the string to draw (may be empty)
	 * @param x     the x-coordinate of the rotation anchor
	 * @param y     the y-coordinate of the rotation anchor
	 * @param angle the rotation angle in radians
	 */
	public static void drawRotatedString(Graphics2D g2, String text, float x, float y, double angle) {
		if (g2 == null || text == null || text.isEmpty())
			return;

		// Trivial fast path: no rotation
		if (angle == 0.0) {
			g2.drawString(text, x, y);
			return;
		}

		// Save current transform
		final AffineTransform oldTransform = g2.getTransform();

		// Apply rotation around anchor directly (no allocation)
		g2.rotate(angle, x, y);
		g2.drawString(text, x, y);

		// Restore original transform
		g2.setTransform(oldTransform);
	}

//	/**
//	 * creates a diamond shape
//	 * 
//	 * @param centerX x
//	 * @param centerY y
//	 * @param width   width
//	 * @param height  height
//	 * @return the path 2d
//	 */
//	public static Path2D.Double createDiamond(double centerX, double centerY, double width, double height) {
//		Path2D.Double diamond = new Path2D.Double();
//
//		diamond.moveTo(centerX - width * 0.5, centerY - height * 0.5 + height / 2);
//		diamond.lineTo(centerX - width * 0.5 + width / 2, centerY - height * 0.5);
//		diamond.lineTo(centerX - width * 0.5 + width, centerY - height * 0.5 + height / 2);
//		diamond.lineTo(centerX - width * 0.5 + width / 2, centerY - height * 0.5 + height);
//		diamond.closePath();
//
//		return diamond;
//	}

	/**
	 * Creates a diamond-shaped {@link Path2D.Float} centered at the given position.
	 *
	 * <p>
	 * This variant minimizes arithmetic and object allocation cost. It uses
	 * single-precision math for better performance during bulk rendering and
	 * constructs the shape directly in path coordinates without intermediate
	 * expressions.
	 * </p>
	 *
	 * <p>
	 * Designed for scenarios where many small glyphs (e.g. scatter plot points)
	 * need to be generated or drawn efficiently.
	 * </p>
	 *
	 * @param centerX x-coordinate of the diamond's center
	 * @param centerY y-coordinate of the diamond's center
	 * @param width   total width of the diamond
	 * @param height  total height of the diamond
	 * @return a {@link Path2D.Float} representing the diamond
	 */
	public static Path2D.Float createDiamond(float centerX, float centerY, float width, float height) {
		final float halfW = width * 0.5f;
		final float halfH = height * 0.5f;

		final float leftX = centerX - halfW;
		final float rightX = centerX + halfW;
		final float topY = centerY - halfH;
		final float bottomY = centerY + halfH;

		Path2D.Float diamond = new Path2D.Float(Path2D.WIND_EVEN_ODD, 4);
		diamond.moveTo(centerX, topY); // Top
		diamond.lineTo(rightX, centerY); // Right
		diamond.lineTo(centerX, bottomY); // Bottom
		diamond.lineTo(leftX, centerY); // Left
		diamond.closePath();

		return diamond;
	}

}
