package com.github.TKnudsen.infoVis.view.visualPrimitives.convexHull;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Java POJO for convex hulls: the raw data coordinates, their pixel projection,
 * and the hull polygon derived from them.
 *
 * @since 2017
 * @version 1.02
 */
public class ConvexHull {

	// data
	private List<Double[]> coordinates;

	// display coordinates
	private List<Point2D> coordinatesInPixels;
	private List<Point2D> convexHull;

	public ConvexHull(List<Double[]> coordinates) {
		this.coordinates = coordinates;
	}

	public List<Double[]> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<Double[]> coordinates) {
		this.coordinates = coordinates;
	}

	public List<Point2D> getCoordinatesInPixels() {
		return coordinatesInPixels;
	}

	public void setCoordinatesInPixels(List<Point2D> coordinatesInPixels) {
		this.coordinatesInPixels = coordinatesInPixels;
	}

	public List<Point2D> getConvexHull() {
		return convexHull;
	}

	public void setConvexHull(List<Point2D> convexHull) {
		this.convexHull = convexHull;
	}
}
