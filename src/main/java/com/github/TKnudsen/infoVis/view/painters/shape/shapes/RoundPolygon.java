package com.github.TKnudsen.infoVis.view.painters.shape.shapes;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Wraps a {@link Polygon} with rounded corners: each straight edge is cut
 * back by {@code arcWidth} pixels near every vertex, and the gap is closed
 * with a quadratic curve through the original corner point.
 *
 * @version 1.0
 * @since 2026
 */
public class RoundPolygon implements Shape {

	private final GeneralPath path;

	public RoundPolygon(Polygon p, int arcWidth) {
		path = new GeneralPath();
		transform(p, arcWidth, path);
	}

	@Override
	public Rectangle getBounds() {
		return path.getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
		return path.getBounds2D();
	}

	@Override
	public boolean contains(double x, double y) {
		return path.contains(x, y);
	}

	@Override
	public boolean contains(Point2D p) {
		return path.contains(p);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return path.intersects(x, y, w, h);
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return path.intersects(r);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return path.contains(x, y, w, h);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return path.contains(r);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return path.getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return path.getPathIterator(at, flatness);
	}

	protected static void transform(Polygon shape, int arcWidth, GeneralPath path) {
		PathIterator pIter = shape.getPathIterator(new AffineTransform());

		Point2D.Float pointFirst = new Point2D.Float(0, 0);
		Point2D.Float pointSecond = null;

		Point2D.Float pointLast = new Point2D.Float(0, 0);
		Point2D.Float pointCorner = null;
		Point2D.Float pointNext = null;

		float[] coor = new float[6];
		while (!pIter.isDone()) {
			int type = pIter.currentSegment(coor);
			float x1 = coor[0];
			float y1 = coor[1];
			float x2 = coor[2];
			float y2 = coor[3];
			float x3 = coor[4];
			float y3 = coor[5];

			switch (type) {
			case PathIterator.SEG_CUBICTO:
				path.curveTo(x1, y1, x2, y2, x3, y3);
				break;
			case PathIterator.SEG_LINETO:
				if (pointCorner == null) {
					pointCorner = new Point2D.Float(x1, y1);
					if (pointNext == null) {
						// first move
						pointSecond = new Point2D.Float(x1, y1);
						Point2D.Float arcStartPoint = getArcPoint(pointSecond, pointFirst, arcWidth);

						path.moveTo(arcStartPoint.x, arcStartPoint.y);
					}
				} else {
					pointNext = new Point2D.Float(x1, y1);
					add(path, pointLast, pointCorner, pointNext, arcWidth);
					pointLast = pointCorner;
					pointCorner = pointNext;
				}
				break;
			case PathIterator.SEG_MOVETO:
				pointLast.x = x1;
				pointLast.y = y1;
				pointFirst.x = x1;
				pointFirst.y = y1;
				break;
			case PathIterator.SEG_QUADTO:
				path.quadTo(x1, y1, x2, y2);
				break;
			}
			pIter.next();
		}

		add(path, pointLast, pointCorner, pointFirst, arcWidth);
		add(path, pointCorner, pointFirst, pointSecond, arcWidth);
		path.closePath();
	}

	protected static void add(GeneralPath path, Point2D.Float last, Point2D.Float corner, Point2D.Float next,
			float w) {
		Point2D.Float arcStartPoint = getArcPoint(last, corner, w);
		Point2D.Float arcEndPoint = getArcPoint(next, corner, w);

		path.lineTo(arcStartPoint.x, arcStartPoint.y);
		path.quadTo(corner.x, corner.y, arcEndPoint.x, arcEndPoint.y);
	}

	protected static Point2D.Float getArcPoint(Point2D.Float p1, Point2D.Float p2, float w) {
		Point2D.Float res = new Point2D.Float();
		float d = Math.round(Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));

		if (p1.x < p2.x)
			res.x = p2.x - w * Math.abs(p1.x - p2.x) / d;
		else
			res.x = p2.x + w * Math.abs(p1.x - p2.x) / d;

		if (p1.y < p2.y)
			res.y = p2.y - w * Math.abs(p1.y - p2.y) / d;
		else
			res.y = p2.y + w * Math.abs(p1.y - p2.y) / d;

		return res;
	}
}
