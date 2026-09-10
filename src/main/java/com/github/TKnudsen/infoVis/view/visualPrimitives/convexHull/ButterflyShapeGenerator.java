package com.github.TKnudsen.infoVis.view.visualPrimitives.convexHull;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Refines a point cloud's convex hull into a smooth curved outline that
 * "recovers" concavities of the underlying point distribution -- the
 * butterfly plot technique for visualizing large point clouds:
 * </p>
 *
 * <p>
 * Schreck, T., Schuessler, M., Zeilfelder, F., Worm, K.: Butterfly plots for
 * visual analysis of large point cloud data. Proceedings of the 16th
 * International Conference in Central Europe on Computer Graphics,
 * Visualization and Computer Vision (WSCG'08), pp. 33-40, 2008.
 * </p>
 *
 * <p>
 * For each convex hull edge, the algorithm recursively looks for the point
 * (if any) lying furthest inside the hull relative to that edge and bends the
 * outline toward it with a quadratic Bezier curve, as long as doing so
 * "recovers" at least a {@code recovery} fraction of the segment's triangle
 * area (relative to the hull's total area) and the recursion depth budget
 * allows it. The result is a closed curve that hugs the actual point density
 * more tightly than the plain straight-edged convex hull.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ButterflyShapeGenerator {

	/**
	 * @param points   the point cloud to enclose
	 * @param depth    maximum recursion depth per hull edge -- higher values
	 *                 allow more detailed (but more expensive) refinement
	 * @param recovery fraction (0..1) of a segment triangle's area that a
	 *                 candidate refinement must "recover" (i.e. cut off) to be
	 *                 accepted; higher values yield a shape closer to the plain
	 *                 convex hull, lower values hug the point cloud more tightly
	 * @return the refined (curved) hull shape, or null if fewer than 3 distinct
	 *         points are given
	 */
	public static Path2D generateButterflyHullShape(List<? extends Point2D> points, int depth, double recovery) {
		if (points == null)
			return null;

		List<Point2D> pointList = new ArrayList<>(points);
		List<Point2D> hull = ConvexHullTools.computeConvexHull(pointList);
		if (hull.size() < 3)
			return null;

		Point2D center = centroid(pointList);
		double areaThreshold = recovery * polygonArea(hull);

		List<Point2D[]> curveSegments = new ArrayList<>();

		int n = hull.size();
		for (int i = 0; i < n; i++) {
			Point2D p1 = hull.get(i);
			Point2D p2 = hull.get((i + 1) % n);

			Path2D.Double segment = new Path2D.Double();
			segment.moveTo(center.getX(), center.getY());
			segment.lineTo(p1.getX(), p1.getY());
			segment.lineTo(p2.getX(), p2.getY());
			segment.closePath();

			Line2D.Double segmentLine = new Line2D.Double(p1, p2);

			refineSegment(segment, segmentLine, center, pointList, 0, depth, areaThreshold, curveSegments);
		}

		return toPath(curveSegments);
	}

	private static Path2D toPath(List<Point2D[]> curveSegments) {
		if (curveSegments.isEmpty())
			return null;

		GeneralPath path = new GeneralPath();
		Point2D start = curveSegments.get(0)[0];
		path.moveTo(start.getX(), start.getY());

		for (Point2D[] segment : curveSegments)
			path.quadTo(segment[1].getX(), segment[1].getY(), segment[2].getX(), segment[2].getY());

		path.closePath();
		return path;
	}

	/**
	 * Recursively refines one hull-edge triangle (apex = center, base =
	 * segmentLine) into one or more quadratic-curve segments, appending each
	 * accepted {@code [from, controlPoint, to]} triple to {@code out}.
	 */
	private static void refineSegment(Path2D.Double segmentTriangle, Line2D.Double segmentLine, Point2D center,
			List<Point2D> pointSet, int currentDepth, int maxDepth, double areaThreshold, List<Point2D[]> out) {

		List<Point2D> containedPoints = pointsInside(segmentTriangle, pointSet);
		Point2D controlPoint = closestPointToLine(containedPoints, segmentLine);
		if (controlPoint == null)
			controlPoint = center;

		if (controlPoint.equals(center) || currentDepth >= maxDepth) {
			out.add(new Point2D[] { new Point2D.Double(segmentLine.x1, segmentLine.y1), controlPoint,
					new Point2D.Double(segmentLine.x2, segmentLine.y2) });
			return;
		}

		Path2D.Double leftTriangle = new Path2D.Double();
		leftTriangle.moveTo(center.getX(), center.getY());
		leftTriangle.lineTo(segmentLine.x1, segmentLine.y1);
		leftTriangle.lineTo(controlPoint.getX(), controlPoint.getY());
		leftTriangle.closePath();
		Line2D.Double leftLine = new Line2D.Double(segmentLine.x1, segmentLine.y1, controlPoint.getX(),
				controlPoint.getY());
		double leftAreaReduction = curveAreaReduction(leftLine, closestOrCenter(pointsInside(leftTriangle, pointSet),
				leftLine, center));

		Path2D.Double rightTriangle = new Path2D.Double();
		rightTriangle.moveTo(center.getX(), center.getY());
		rightTriangle.lineTo(controlPoint.getX(), controlPoint.getY());
		rightTriangle.lineTo(segmentLine.x2, segmentLine.y2);
		rightTriangle.closePath();
		Line2D.Double rightLine = new Line2D.Double(controlPoint.getX(), controlPoint.getY(), segmentLine.x2,
				segmentLine.y2);
		double rightAreaReduction = curveAreaReduction(rightLine,
				closestOrCenter(pointsInside(rightTriangle, pointSet), rightLine, center));

		if (leftAreaReduction + rightAreaReduction >= areaThreshold) {
			refineSegment(leftTriangle, leftLine, center, containedPoints, currentDepth + 1, maxDepth, areaThreshold,
					out);
			refineSegment(rightTriangle, rightLine, center, containedPoints, currentDepth + 1, maxDepth,
					areaThreshold, out);
		} else
			out.add(new Point2D[] { new Point2D.Double(segmentLine.x1, segmentLine.y1), controlPoint,
					new Point2D.Double(segmentLine.x2, segmentLine.y2) });
	}

	private static Point2D closestOrCenter(List<Point2D> points, Line2D.Double line, Point2D center) {
		Point2D p = closestPointToLine(points, line);
		return p != null ? p : center;
	}

	/** points strictly inside the given triangle, excluding points ON its boundary */
	private static List<Point2D> pointsInside(Path2D.Double triangle, List<Point2D> pointSet) {
		List<Point2D> result = new ArrayList<>();
		if (pointSet == null)
			return result;
		for (Point2D p : pointSet)
			if (triangle.contains(p))
				result.add(p);
		return result;
	}

	/**
	 * @return the point in {@code points} closest to {@code line} (by
	 *         perpendicular distance), excluding points that lie on the line or
	 *         coincide with one of its endpoints; null if none qualify
	 */
	private static Point2D closestPointToLine(List<Point2D> points, Line2D.Double line) {
		if (points == null)
			return null;

		Point2D best = null;
		double bestDist = Double.MAX_VALUE;

		for (Point2D p : points) {
			if (line.contains(p) || line.getP1().equals(p) || line.getP2().equals(p))
				continue;

			double dist = line.ptLineDist(p);
			if (dist < bestDist) {
				bestDist = dist;
				best = p;
			}
		}

		return best;
	}

	private static Point2D centroid(List<Point2D> points) {
		double x = 0;
		double y = 0;
		for (Point2D p : points) {
			x += p.getX();
			y += p.getY();
		}
		return new Point2D.Double(x / points.size(), y / points.size());
	}

	private static double polygonArea(List<Point2D> polygon) {
		double area = 0;
		int n = polygon.size();
		for (int i = 0; i < n; i++) {
			Point2D p1 = polygon.get(i);
			Point2D p2 = polygon.get((i + 1) % n);
			area += p1.getX() * p2.getY() - p2.getX() * p1.getY();
		}
		return Math.abs(area) / 2.0;
	}

	/** area "cut off" by bending the line toward the control point, via a quadratic-curve/line loop */
	private static double curveAreaReduction(Line2D.Double line, Point2D controlPoint) {
		GeneralPath path = new GeneralPath();
		path.moveTo(line.getP1().getX(), line.getP1().getY());
		path.quadTo(controlPoint.getX(), controlPoint.getY(), line.getP2().getX(), line.getP2().getY());
		path.lineTo(line.getP1().getX(), line.getP1().getY());
		path.closePath();
		return rasterArea(path);
	}

	/** rasterized area of a shape, matching the legacy pixel-counting approach (robust for self-touching curves) */
	private static double rasterArea(GeneralPath path) {
		Rectangle2D bounds = path.getBounds2D();
		int width = (int) Math.ceil(bounds.getWidth()) + 1;
		int height = (int) Math.ceil(bounds.getHeight()) + 1;
		if (width <= 0 || height <= 0)
			return 0;

		int area = 0;
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				if (path.contains(bounds.getX() + x, bounds.getY() + y))
					area++;

		return area;
	}

}
