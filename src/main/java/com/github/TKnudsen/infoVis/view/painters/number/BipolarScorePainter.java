package com.github.TKnudsen.infoVis.view.painters.number;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * the neutral value is always at the center of the score axis, even if extreme
 * values are different
 *
 */
public class BipolarScorePainter extends ChartPainter {

	private final double score;
	private final double minValue;
	private final double neutralValue;
	private final double maxValue;

	private boolean drawNeutralMark = true;
	private Color neutralMarkColor = Color.BLACK;

	private NormalizationFunction normalization;

	private boolean horizontalOrientation = true;

	/**
	 * relative value between zero and one to express the uncertainty of the score.
	 * Lower means more certain.
	 */
	private double uncertainty;

	public BipolarScorePainter(double score, double minValue, double maxValue) {
		this(score, minValue, 0.0, maxValue);
	}

	public BipolarScorePainter(double score, double minValue, double neutralValue, double maxValue) {
		this(score, minValue, neutralValue, maxValue, 0.0);
	}

	public BipolarScorePainter(double score, double minValue, double neutralValue, double maxValue,
			double uncertainty) {
		this.score = score;

		if (minValue >= neutralValue)
			throw new IllegalArgumentException("BipolarScorePainter: minValue (" + minValue
					+ ") must be smaller than neutralValue (" + neutralValue + ")");
		if (maxValue <= neutralValue)
			throw new IllegalArgumentException("BipolarScorePainter: maxValue (" + maxValue
					+ ") must be greater than neutralValue (" + neutralValue + ")");

		this.minValue = minValue;
		this.neutralValue = neutralValue;
		this.maxValue = maxValue;

		this.uncertainty = uncertainty;

		normalization = (score < neutralValue) ? new LinearNormalizationFunction(minValue, neutralValue)
				: new LinearNormalizationFunction(neutralValue, maxValue);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		double relativeViewPosition = normalization.apply(score).doubleValue();

		double x;
		double y = rectangle.getMinY();
		double w;
		double h = rectangle.getHeight();

		double certainty = 1.0 - (Double.isNaN(uncertainty) ? 0 : uncertainty);

		if (horizontalOrientation) {
			y = rectangle.getMinY();
			h = rectangle.getHeight();

			y += (h * (1 - certainty)) * 0.5;
			h *= certainty;

			if (score < neutralValue) {
				x = rectangle.getMinX() + relativeViewPosition * rectangle.getWidth() * 0.5;
				w = (1 - relativeViewPosition) * rectangle.getWidth() * 0.5;
			} else {
				x = rectangle.getCenterX();
				w = relativeViewPosition * rectangle.getWidth() * 0.5;
			}
		} else {
			x = rectangle.getMinX();
			w = rectangle.getWidth();

			x += (w * (1 - certainty)) * 0.5;
			w *= certainty;

			if (score < neutralValue) {
				y = rectangle.getCenterY();
				h = (1 - relativeViewPosition) * rectangle.getHeight() * 0.5;
			} else {
				y = rectangle.getCenterY() - relativeViewPosition * rectangle.getHeight() * 0.5;
				h = relativeViewPosition * rectangle.getHeight() * 0.5;
			}
		}

		this.chartRectangle = new Rectangle2D.Double(x, y, w, h);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (rectangle == null)
			return;

		Color c = g2.getColor();

		g2.setPaint(getPaint());
		g2.draw(chartRectangle);
		g2.fill(chartRectangle);

		// draw neutral mark
		if (horizontalOrientation)
			DisplayTools.drawLine(g2, rectangle.getCenterX(), rectangle.getMinY(), rectangle.getCenterX(),
					rectangle.getMaxY(), DisplayTools.thickStroke, neutralMarkColor);
		else
			DisplayTools.drawLine(g2, rectangle.getMinX(), rectangle.getCenterY(), rectangle.getMaxX(),
					rectangle.getCenterY(), DisplayTools.thickStroke, neutralMarkColor);

		if (isDrawOutline()) {
			g2.setPaint(getBorderPaint());
			g2.draw(chartRectangle);
		}

		g2.setColor(c);
	}

	public double getScore() {
		return score;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getNeutralValue() {
		return neutralValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public boolean isDrawNeutralMark() {
		return drawNeutralMark;
	}

	public void setDrawNeutralMark(boolean drawNeutralMark) {
		this.drawNeutralMark = drawNeutralMark;
	}

	public Color getNeutralMarkColor() {
		return neutralMarkColor;
	}

	public void setNeutralMarkColor(Color neutralMarkColor) {
		this.neutralMarkColor = neutralMarkColor;
	}

	public boolean isHorizontalOrientation() {
		return horizontalOrientation;
	}

	public void setHorizontalOrientation(boolean horizontalOrientation) {
		this.horizontalOrientation = horizontalOrientation;
	}

	public double getUncertainty() {
		return uncertainty;
	}

	public void setUncertainty(double uncertainty) {
		this.uncertainty = uncertainty;
	}
}
