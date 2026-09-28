package com.github.TKnudsen.infoVis.view.painters.radial;

import java.awt.geom.GeneralPath;
import java.util.List;

/**
 * <p>
 * Radar chart variant that bows each edge of the data polygon in toward the
 * center with a quadratic curve, instead of connecting adjacent vertices
 * with a straight line. Low values on neighboring axes pinch the curve in
 * sharply, giving the chart's outline a spiky, flower- or butterfly-like
 * silhouette rather than a flat-faced polygon.
 * </p>
 *
 * @version 1.0
 * @since 2018
 */
public class RadarChartButterflyPainter extends RadarChartPainter {

	public RadarChartButterflyPainter(double[] data) {
		super(data);
	}

	public RadarChartButterflyPainter(List<Double> data) {
		super(data);
	}

	@Override
	protected GeneralPath buildChartPath(int[][] vertices, int xMean, int yMean) {
		GeneralPath path = new GeneralPath();

		path.moveTo(vertices[0][0], vertices[0][1]);
		for (int i = 1; i < vertices.length; i++)
			path.quadTo(xMean, yMean, vertices[i][0], vertices[i][1]);
		path.quadTo(xMean, yMean, vertices[0][0], vertices[0][1]);

		return path;
	}

	@Override
	public String toString() {
		return "RadarChartButterflyPainter";
	}
}
