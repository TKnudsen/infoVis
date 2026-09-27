package com.github.TKnudsen.infoVis.view.painters.spline;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.visualPrimitives.ColoredPoint;
import com.github.TKnudsen.infoVis.view.visualPrimitives.ColoredPolygon;

/**
 * Builds a smoothed (cubic B-spline), per-vertex-colored {@link Polygon} from
 * a sequence of control points. Sole consumer is {@link SplinePainter}.
 *
 * @version 2.0 -- trimmed down to the one code path SplinePainter actually
 *          uses: the uncolored overloads and the List/boxed-array variants
 *          carried over from the legacy PainterLib class had no callers left
 *          anywhere in the codebase, so they were dropped rather than
 *          migrated.
 * @since 2019
 */
public class BSplineTools {

	/** distance (in pixels) used to decide how many artificial start/end control points to insert */
	private static final double ENDPOINT_PADDING_UNIT = 30;

	/** distance (in pixels) used to decide how many interpolation steps a segment gets */
	private static final double SEGMENT_STEP_UNIT = 20;

	/** radius of the moving-average smoothing pass applied to the final polygon */
	private static final int SMOOTHING_RADIUS = 2;

	public static ColoredPolygon getSplinePolygon(double[] vectorXValues, double[] vectorYValues, int pruningDist, Color[] colors) {
		if (vectorXValues == null || vectorYValues == null || colors == null)
			return null;
		if (vectorXValues.length != colors.length || vectorYValues.length != colors.length)
			throw new IllegalArgumentException("vectorXValues, vectorYValues and colors must have the same length, got "
					+ vectorXValues.length + ", " + vectorYValues.length + " and " + colors.length);

		int iter = Math.min(vectorXValues.length, vectorYValues.length);
		List<Integer[]> poi = new ArrayList<>();
		List<Color> c = new ArrayList<>();
		for (int i = 0; i < iter; i++) {
			poi.add(new Integer[] { (int) vectorXValues[i], (int) vectorYValues[i] });
			c.add(colors[i]);
		}
		return buildSplinePolygon(poi, pruningDist, c);
	}

	private static ColoredPolygon buildSplinePolygon(List<Integer[]> xy, int pruningDist, List<Color> colors) {
		ColoredPolygon inputPolygon = new ColoredPolygon();

		// solve start and endpoint problem by inserting artificial stuetzstellen
		if (xy.size() >= 3) {
			Point q0 = new Point(xy.get(0)[0], xy.get(0)[1]);
			Point q1 = new Point(xy.get(1)[0], xy.get(1)[1]);
			double dist = Math.floor(q0.distance(q1) / ENDPOINT_PADDING_UNIT) + 1;
			for (int i = 0; i < dist; i++) {
				xy.add(1, new Integer[] { xy.get(0)[0], xy.get(0)[1] });
				colors.add(1, colors.get(0));
			}

			q0 = new Point(xy.get(xy.size() - 2)[0], xy.get(xy.size() - 2)[1]);
			q1 = new Point(xy.get(xy.size() - 1)[0], xy.get(xy.size() - 1)[1]);
			dist = Math.floor(q0.distance(q1) / ENDPOINT_PADDING_UNIT) + 1;
			for (int i = 0; i < dist; i++) {
				xy.add(new Integer[] { xy.get(xy.size() - 1)[0], xy.get(xy.size() - 1)[1] });
				colors.add(colors.get(colors.size() - 1));
			}
		}

		for (int i = 0; i < xy.size(); i++)
			inputPolygon.addPoint(xy.get(i)[0], xy.get(i)[1], colors.get(i));

		ColoredPolygon pol = new ColoredPolygon();

		for (int i = 2; i < inputPolygon.npoints - 1; i++) {

			ColoredPoint q = getPoint(inputPolygon, i, 0);

			// heuristics to define steps
			Point q0 = new Point(xy.get(i - 1)[0], xy.get(i - 1)[1]);
			Point q1 = new Point(xy.get(i)[0], xy.get(i)[1]);
			Point q2 = new Point(xy.get(i + 1)[0], xy.get(i + 1)[1]);
			double dist = q0.distance(q1);
			dist = Math.max(dist, q1.distance(q2));
			float steps = (float) Math.floor(Math.max(1, dist / SEGMENT_STEP_UNIT));

			pol.addPoint(q.x, q.y, q.color);
			Color c0 = inputPolygon.getColor(i);
			Color c1 = inputPolygon.getColor(i + 1);

			for (int j = 1; j <= steps; j++) {
				q = getPoint(inputPolygon, i, j / (float) steps);

				// interpolate color
				float weight = 1 - (j / ((float) Math.floor(steps) + 1));
				Color newColor = new Color((c0.getRed() * weight + c1.getRed() * (1 - weight)) / 255, (c0.getGreen() * weight + c1.getGreen() * (1 - weight)) / 255, (c0.getBlue() * weight + c1.getBlue() * (1 - weight)) / 255,
						(c0.getAlpha() * weight + c1.getAlpha() * (1 - weight)) / 255);

				// pruning
				if (pruningDist > 0 && i < inputPolygon.npoints && i != 2) {
					if (pol.getPoint(pol.npoints - 1).distance(q) > pruningDist)
						pol.addPoint(q.x, q.y, newColor);
				} else
					pol.addPoint(q.x, q.y, newColor);
			}
		}

		// moving average...
		int radius = Math.min(SMOOTHING_RADIUS, Math.max(pol.npoints / 2, 1));
		double[] xs = new double[pol.npoints];
		double[] ys = new double[pol.npoints];
		for (int m = 0; m < pol.npoints; m++) {
			xs[m] = pol.xpoints[m];
			ys[m] = pol.ypoints[m];
		}
		xs = getMovingAverage(xs, radius);
		ys = getMovingAverage(ys, radius);
		// recover start and end values
		if (pol.xpoints.length != 0 && pol.ypoints.length != 0 && pol.npoints != 0) {
			xs[0] = pol.xpoints[0];
			ys[0] = pol.ypoints[0];
		}
		for (int a = 1; a < radius; a++) {
			double dx = 0;
			double dy = 0;
			dx += pol.xpoints[a] * (1 - a / (double) radius);
			dx += xs[a] * (a / (double) radius);
			dy += pol.ypoints[a] * (1 - a / (double) radius);
			dy += ys[a] * (a / (double) radius);
			xs[a] = dx;
			ys[a] = dy;
		}
		for (int a = 1; a < radius; a++) {
			double dx = 0;
			double dy = 0;
			dx += pol.xpoints[xs.length - 1 - a] * (1 - a / (double) radius);
			dx += xs[xs.length - 1 - a] * (a / (double) radius);
			dy += pol.ypoints[xs.length - 1 - a] * (1 - a / (double) radius);
			dy += ys[xs.length - 1 - a] * (a / (double) radius);
			xs[xs.length - 1 - a] = dx;
			ys[xs.length - 1 - a] = dy;
		}
		if (pol.xpoints.length != 0 && pol.ypoints.length != 0 && pol.npoints != 0) {
			xs[xs.length - 1] = pol.xpoints[xs.length - 1];
			ys[ys.length - 1] = pol.ypoints[ys.length - 1];
		}

		Color[] colorTmp = new Color[pol.getColors().size()];
		for (int i = 0; i < pol.getColors().size(); i++)
			colorTmp[i] = pol.getColor(i);
		pol = new ColoredPolygon();
		for (int m = 0; m < xs.length; m++)
			pol.addPoint((int) MathFunctions.round(xs[m], 0), (int) MathFunctions.round(ys[m], 0), colorTmp[m]);
		return pol;
	}

	private static ColoredPoint getPoint(ColoredPolygon inputPolygon, int i, float t) {
		float px = 0;
		float py = 0;

		for (int j = -2; j <= 1; j++) {
			px += getB(j, t) * inputPolygon.xpoints[i + j];
			py += getB(j, t) * inputPolygon.ypoints[i + j];
		}

		return new ColoredPoint((int) Math.round(px), (int) Math.round(py), inputPolygon.getColor(i));
	}

	private static float getB(int i, float t) {
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

	private static double[] getMovingAverage(double[] values, int radius) {
		double[] averages = new double[values.length];

		for (int i = 0; i < values.length; i++) {
			double d = values[i];
			double w = 1.0;

			for (int j = 1; j <= radius; j++) {
				if (i - j >= 0) {
					d += values[i - j] * (1 - j / (double) radius);
					w += (1 - j / (double) radius);
				}
				if (i + j < values.length) {
					d += values[i + j] * (1 - j / (double) radius);
					w += (1 - j / (double) radius);
				}
			}

			averages[i] = d / w;
		}

		return averages;
	}
}
