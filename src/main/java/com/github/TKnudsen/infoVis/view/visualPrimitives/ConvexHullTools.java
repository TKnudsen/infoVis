package com.github.TKnudsen.infoVis.view.visualPrimitives;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * <p>
 * Computes the convex hull of a 2D point set via the Graham scan algorithm (O(n
 * log n), dominated by the angular sort). Operates directly on {@link Point2D},
 * with no auxiliary point-wrapper type.
 * </p>
 *
 * @since 2017
 * @version 1.1
 * 
 */
public class ConvexHullTools {

	private ConvexHullTools() {
	}

	/**
	 * Computes the convex hull of {@code points} and stores it (in
	 * counter-clockwise order) into {@code convexHull} via
	 * {@link ConvexHull#setConvexHull(List)}, reading the input from
	 * {@link ConvexHull#getCoordinatesInPixels()}.
	 *
	 * @throws NullPointerException if convexHull or its pixel coordinates are null
	 */
	public static void compute(ConvexHull convexHull) {
		convexHull.setConvexHull(computeConvexHull(convexHull.getCoordinatesInPixels()));
	}

	/**
	 * Computes the convex hull of {@code points} via the Graham scan algorithm.
	 *
	 * @param points the point set; not modified
	 * @return the hull vertices in counter-clockwise order, starting from the
	 *         lowest (then leftmost) point. Fewer than 3 distinct, non-null, finite
	 *         input points yield those points back unchanged (a degenerate "hull"
	 *         -- a point or a segment -- rather than an exception, since callers
	 *         typically just want something to draw).
	 */
	public static List<Point2D> computeConvexHull(List<Point2D> points) {
		List<Point2D> distinct = distinctFinitePoints(points);

		if (distinct.size() < 3)
			return distinct;

		Point2D pivot = lowestPoint(distinct);

		List<Point2D> sorted = new ArrayList<>(distinct);
		sorted.remove(pivot);
		sorted.sort(byPolarAngle(pivot));

		List<Point2D> hull = new ArrayList<>();
		hull.add(pivot);

		for (Point2D p : sorted) {
			while (hull.size() >= 2 && crossProduct(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0)
				hull.remove(hull.size() - 1);
			hull.add(p);
		}

		return hull;
	}

	private static List<Point2D> distinctFinitePoints(List<Point2D> points) {
		if (points == null)
			return new ArrayList<>();

		LinkedHashSet<Point2D> result = new LinkedHashSet<>();
		for (Point2D p : points) {
			if (p == null || !Double.isFinite(p.getX()) || !Double.isFinite(p.getY()))
				continue;
			result.add(p);
		}
		return new ArrayList<>(result);
	}

	private static Point2D lowestPoint(List<Point2D> points) {
		Point2D lowest = points.get(0);
		for (Point2D p : points)
			if (p.getY() < lowest.getY() || (p.getY() == lowest.getY() && p.getX() < lowest.getX()))
				lowest = p;
		return lowest;
	}

	/**
	 * Orders points by polar angle around {@code pivot}; points at the same angle
	 * are ordered by increasing distance from the pivot, so the Graham scan loop
	 * naturally keeps only the farthest of any collinear run.
	 */
	private static Comparator<Point2D> byPolarAngle(Point2D pivot) {
		return (a, b) -> {
			double cross = crossProduct(pivot, a, b);
			if (cross != 0)
				return cross > 0 ? -1 : 1;

			double da = pivot.distanceSq(a);
			double db = pivot.distanceSq(b);
			return Double.compare(da, db);
		};
	}

	/**
	 * Cross product of (b - o) x (c - o). Positive if o-b-c turns
	 * counter-clockwise, negative if clockwise, zero if collinear.
	 */
	private static double crossProduct(Point2D o, Point2D b, Point2D c) {
		return (b.getX() - o.getX()) * (c.getY() - o.getY()) - (b.getY() - o.getY()) * (c.getX() - o.getX());
	}

}
