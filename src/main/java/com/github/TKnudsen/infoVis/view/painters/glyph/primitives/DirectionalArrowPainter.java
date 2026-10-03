package com.github.TKnudsen.infoVis.view.painters.glyph.primitives;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Paints a filled arrow (a triangular head, with a rectangular shaft once the
 * rectangle is tall enough), pointing at an arbitrary angle and sized to fill
 * the painter's rectangle. For a thin fixed-length horizontal arrow with a
 * small arrowhead instead, see {@link ArrowPainter}.
 *
 * @version 1.0
 * @since 2026
 */
public class DirectionalArrowPainter extends ChartPainter {

	private double angle;

	private Shape arrow;

	/**
	 * @param direction one of the four cardinal directions
	 */
	public DirectionalArrowPainter(Direction direction) {
		this(direction.angle);
	}

	/**
	 * @param angle the arrow's direction in degrees, measured counterclockwise
	 *              from the positive X axis (so {@code 0} points right,
	 *              {@code 90} points up) -- matches {@link Direction}'s angles,
	 *              but any value is accepted.
	 */
	public DirectionalArrowPainter(double angle) {
		this.angle = angle;
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null) {
			arrow = null;
			return;
		}

		arrow = buildArrow(rectangle, angle);
	}

	/**
	 * Builds the arrow shape, upright, sized to the bounding box the given
	 * rectangle would occupy once rotated by {@code angle}, then rotates it into
	 * its final orientation.
	 */
	private static Shape buildArrow(Rectangle2D rectangle, double angle) {
		double sin = Math.abs(Math.sin(Math.toRadians(angle)));
		double cos = Math.abs(Math.cos(Math.toRadians(angle)));

		double w = sin * rectangle.getWidth() + cos * rectangle.getHeight();
		double h = cos * rectangle.getWidth() + sin * rectangle.getHeight();

		double x = rectangle.getCenterX() - 0.5 * w;
		double y = rectangle.getCenterY() - 0.5 * h;
		double xMax = x + w;
		double yMax = y + h;

		boolean hasShaft = 2 * h > w;
		double triangleHeight = Math.min(h, 0.5 * w);
		double shaftWidth = 0.5 * w;

		Path2D path = new Path2D.Double();
		path.moveTo(x + 0.5 * w, y);
		path.lineTo(xMax, y + triangleHeight);
		if (hasShaft) {
			path.lineTo(xMax - 0.5 * shaftWidth, y + triangleHeight);
			path.lineTo(xMax - 0.5 * shaftWidth, yMax);
			path.lineTo(x + 0.5 * shaftWidth, yMax);
			path.lineTo(x + 0.5 * shaftWidth, y + triangleHeight);
		}
		path.lineTo(x, y + triangleHeight);
		path.closePath();

		AffineTransform rotation = AffineTransform.getRotateInstance(Math.toRadians(90 - angle),
				rectangle.getCenterX(), rectangle.getCenterY());
		return rotation.createTransformedShape(path);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (arrow == null)
			return;

		Color resetColor = g2.getColor();

		g2.setPaint(getPaint());
		g2.fill(arrow);

		if (isDrawOutline()) {
			g2.setPaint(getBorderPaint());
			g2.draw(arrow);
		}

		g2.setColor(resetColor);
	}

	public double getAngle() {
		return angle;
	}

	/**
	 * @param angle the arrow's new direction in degrees; see the constructor for
	 *              the angle convention. Rebuilds the arrow shape immediately if
	 *              a rectangle is already set.
	 */
	public void setAngle(double angle) {
		this.angle = angle;

		if (getRectangle() != null)
			setRectangle(getRectangle());
	}

	public void setDirection(Direction direction) {
		setAngle(direction.angle);
	}

	public enum Direction {
		UP(90), DOWN(-90), LEFT(-180), RIGHT(0);

		private final double angle;

		Direction(double angle) {
			this.angle = angle;
		}
	}

}
