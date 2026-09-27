package com.github.TKnudsen.infoVis.view.visualPrimitives;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;

/**
 * A cubic B-spline rendered from a sequence of control points, with optional
 * per-segment coloring.
 *
 * @version 1.0
 * @since 2026
 */
public class Bspline extends ControlCurve {

	final int STEPS = 12;

	private Color[] colors = null;

	public double distanceThreshold = 100;

	float b(int i, float t) {
		switch (i) {
		case -2:
			return (((-t + 3) * t - 3) * t + 1) / 6;
		case -1:
			return (((3 * t - 6) * t) * t + 4) / 6;
		case 0:
			return (((-3 * t + 3) * t + 3) * t + 1) / 6;
		case 1:
			return (t * t * t) / 6;
		}
		return 0;
	}

	Point p(int i, float t) {
		float px = 0;
		float py = 0;
		for (int j = -2; j <= 1; j++) {
			px += b(j, t) * pts.xpoints[i + j];
			py += b(j, t) * pts.ypoints[i + j];
		}
		return new Point((int) Math.round(px), (int) Math.round(py));
	}

	public void setColors(Color[] colors) {
		this.colors = colors;
	}

	public void paint(Graphics g) {
		super.paint(g);

		boolean useColors = (this.colors != null) && (this.colors.length == pts.npoints);
		Graphics2D g2 = (Graphics2D) g;
		Stroke stroke = g2.getStroke();

		for (int i = 2; i < pts.npoints - 1; i++) {
			Polygon pol = new Polygon();
			Point q = p(i, 0);
			pol.addPoint(q.x, q.y);
			for (int j = 1; j <= STEPS; j++) {
				q = p(i, j / (float) STEPS);
				pol.addPoint(q.x, q.y);
			}
			if (useColors)
				g.setColor(colors[i]);

			g.drawPolyline(pol.xpoints, pol.ypoints, pol.npoints);
			g2.setStroke(stroke);
		}
	}

	public Bspline createScaledBspline(int width, int height) {
		double scaleFactorX = (double) width / (double) getMax(pts.xpoints);
		double scaleFactorY = (double) height / (double) getMax(pts.ypoints);
		double scaleFactor = Math.min(scaleFactorX, scaleFactorY);

		Bspline scaledSpline = new Bspline();

		for (int i = 0; i < pts.npoints; i++) {
			scaledSpline.addPoint((int) (pts.xpoints[i] * scaleFactor), (int) (pts.ypoints[i] * scaleFactor));
		}

		return scaledSpline;
	}

	public double[] getVector() {
		double[] vector = new double[pts.npoints * 2];
		for (int i = 0; i < pts.npoints; i++) {
			vector[i * 2] = pts.xpoints[i];
			vector[i * 2 + 1] = pts.ypoints[i];
		}
		return vector;
	}

	static int getMax(int[] input) {
		return input[getMaxIndex(input)];
	}

	static int getMaxIndex(int vals[]) {
		if (vals == null || vals.length == 0)
			return -1;
		int maxIndex = 0;
		double max = vals[maxIndex];
		for (int i = 1; i < vals.length; i++) {
			if (vals[i] > max) {
				max = vals[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
}
