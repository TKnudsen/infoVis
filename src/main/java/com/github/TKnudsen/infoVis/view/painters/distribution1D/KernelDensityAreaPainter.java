package com.github.TKnudsen.infoVis.view.painters.distribution1D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;

/**
 * Draws a density estimate (evaluation points plus their density value, e.g.
 * from a kernel density estimator) as a filled area curve -- x is the sample
 * domain, y is the estimated density. Purely numerical: no time, no
 * categories, no dependency on how the density was computed, just two lists
 * of {@code Double} of equal size.
 *
 * @since 2026
 */
public class KernelDensityAreaPainter extends ChartPainter {

	private final List<Double> evaluationPoints;
	private final List<Double> density;

	private final PositionEncodingFunction xPositionEncodingFunction;
	private final PositionEncodingFunction yPositionEncodingFunction;

	private Paint fillColor;

	public KernelDensityAreaPainter(List<Double> evaluationPoints, List<Double> density, Paint fillColor) {
		if (evaluationPoints.size() != density.size())
			throw new IllegalArgumentException("KernelDensityAreaPainter: evaluationPoints and density sizes differ");

		this.evaluationPoints = evaluationPoints;
		this.density = density;
		this.fillColor = fillColor;

		double minX = MathFunctions.getMin(evaluationPoints);
		double maxX = MathFunctions.getMax(evaluationPoints);
		double maxY = MathFunctions.getMax(density);

		this.xPositionEncodingFunction = new PositionEncodingFunction(minX, maxX, 0d, 1d);
		this.yPositionEncodingFunction = new PositionEncodingFunction(0.0, maxY, 0d, 1d, true);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		xPositionEncodingFunction.setMinPixel(rectangle.getMinX());
		xPositionEncodingFunction.setMaxPixel(rectangle.getMaxX());
		yPositionEncodingFunction.setMinPixel(rectangle.getMinY());
		yPositionEncodingFunction.setMaxPixel(rectangle.getMaxY());
	}

	@Override
	public void draw(Graphics2D g2) {
		if (rectangle == null || evaluationPoints.isEmpty())
			return;

		Color c = g2.getColor();
		Stroke s = g2.getStroke();

		int n = evaluationPoints.size();
		double[] curveX = new double[n];
		double[] curveY = new double[n];
		for (int i = 0; i < n; i++) {
			curveX[i] = xPositionEncodingFunction.apply(evaluationPoints.get(i));
			curveY[i] = yPositionEncodingFunction.apply(density.get(i));
		}

		double baselineY = yPositionEncodingFunction.apply(0.0);

		// close the curve down to the baseline (density = 0) at both ends, rather
		// than letting fillPolygon cut a diagonal straight between the first and
		// last curve points
		double[] fillX = new double[n + 2];
		double[] fillY = new double[n + 2];
		System.arraycopy(curveX, 0, fillX, 0, n);
		System.arraycopy(curveY, 0, fillY, 0, n);
		fillX[n] = curveX[n - 1];
		fillY[n] = baselineY;
		fillX[n + 1] = curveX[0];
		fillY[n + 1] = baselineY;

		g2.setPaint(fillColor);
		DisplayTools.drawPath(g2, fillX, fillY, true, true);

		g2.setStroke(getStroke());
		g2.setPaint(fillColor);
		DisplayTools.drawPath(g2, curveX, curveY, false, false);

		g2.setStroke(s);
		g2.setColor(c);
	}

	public Paint getFillColor() {
		return fillColor;
	}

	public void setFillColor(Paint fillColor) {
		this.fillColor = fillColor;
	}

}
