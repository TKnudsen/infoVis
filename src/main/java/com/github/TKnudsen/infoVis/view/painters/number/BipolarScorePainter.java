package com.github.TKnudsen.infoVis.view.painters.number;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * Bipolar score painter with optimized Graphics2D usage and DisplayTools calls.
 * 
 * Neutral value is at the axis center. Supports horizontal/vertical orientation
 * and an uncertainty factor (shrinks the filled bar toward its center)
 * </p>
 *
 * @version 2.03
 * @since 2016
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

	/** [0..1], lower means more certain. */
	private double uncertainty;

	/** Reuse the rectangle object instead of allocating each layout pass . */
	private final Rectangle2D.Double reusedRect = new Rectangle2D.Double();

	public BipolarScorePainter(double score, double minValue, double maxValue) {
		this(score, minValue, 0.0, maxValue);
	}

	public BipolarScorePainter(double score, double minValue, double neutralValue, double maxValue) {
		this(score, minValue, neutralValue, maxValue, 0.0);
	}

	public BipolarScorePainter(double score, double minValue, double neutralValue, double maxValue,
			double uncertainty) {
		if (minValue >= neutralValue)
			throw new IllegalArgumentException("BipolarScorePainter: minValue (" + minValue
					+ ") must be smaller than neutralValue (" + neutralValue + ")");
		if (maxValue <= neutralValue)
			throw new IllegalArgumentException("BipolarScorePainter: maxValue (" + maxValue
					+ ") must be greater than neutralValue (" + neutralValue + ")");

		this.score = score;
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

		// Normalize and clamp to [0,1]
		double rel = normalization.apply(score).doubleValue();
		if (rel < 0)
			rel = 0;
		else if (rel > 1)
			rel = 1;

		// certainty in [0,1]
		double cert = 1.0 - (Double.isNaN(uncertainty) ? 0.0 : uncertainty);
		if (cert < 0)
			cert = 0;
		else if (cert > 1)
			cert = 1;

		final double rx = rectangle.getX();
		final double ry = rectangle.getY();
		final double rw = rectangle.getWidth();
		final double rh = rectangle.getHeight();

		double x, y, w, h;

		if (horizontalOrientation) {
			// shrink about vertical center based on certainty
			y = ry + (rh * (1 - cert)) * 0.5;
			h = rh * cert;

			if (score < neutralValue) {
				// left half: [min..neutral] maps to [left..center]
				x = rx + rel * rw * 0.5;
				w = (1 - rel) * rw * 0.5;
			} else {
				// right half: [neutral..max] maps to [center..right]
				x = rx + rw * 0.5;
				w = rel * rw * 0.5;
			}
		} else {
			// shrink about horizontal center based on certainty
			x = rx + (rw * (1 - cert)) * 0.5;
			w = rw * cert;

			if (score < neutralValue) {
				// bottom half
				y = ry + rh * 0.5;
				h = (1 - rel) * rh * 0.5;
			} else {
				// top half
				y = ry + rh * 0.5 - rel * rh * 0.5;
				h = rel * rh * 0.5;
			}
		}

		reusedRect.setRect(x, y, w, h);
		this.chartRectangle = reusedRect; // keep parent contract

//		double relativeViewPosition = normalization.apply(score).doubleValue();
//
//		double x;
//		double y = rectangle.getMinY();
//		double w;
//		double h = rectangle.getHeight();
//
//		double certainty = 1.0 - (Double.isNaN(uncertainty) ? 0 : uncertainty);
//
//		if (horizontalOrientation) {
//			y = rectangle.getMinY();
//			h = rectangle.getHeight();
//
//			y += (h * (1 - certainty)) * 0.5;
//			h *= certainty;
//
//			if (score < neutralValue) {
//				x = rectangle.getMinX() + relativeViewPosition * rectangle.getWidth() * 0.5;
//				w = (1 - relativeViewPosition) * rectangle.getWidth() * 0.5;
//			} else {
//				x = rectangle.getCenterX();
//				w = relativeViewPosition * rectangle.getWidth() * 0.5;
//			}
//		} else {
//			x = rectangle.getMinX();
//			w = rectangle.getWidth();
//
//			x += (w * (1 - certainty)) * 0.5;
//			w *= certainty;
//
//			if (score < neutralValue) {
//				y = rectangle.getCenterY();
//				h = (1 - relativeViewPosition) * rectangle.getHeight() * 0.5;
//			} else {
//				y = rectangle.getCenterY() - relativeViewPosition * rectangle.getHeight() * 0.5;
//				h = relativeViewPosition * rectangle.getHeight() * 0.5;
//			}
//		}
//
//		this.chartRectangle = new Rectangle2D.Double(x, y, w, h);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (rectangle == null || chartRectangle == null)
			return;

		final Paint oldPaint = g2.getPaint();

		// fill then draw the bar using fast DisplayTools variants
		g2.setPaint(getPaint());
		DisplayTools.fillRectangle(g2, chartRectangle);
		DisplayTools.drawRectangle(g2, chartRectangle);

		g2.setPaint(getPaint());
		g2.draw(chartRectangle);
		g2.fill(chartRectangle);

//		// draw neutral mark
//		if (horizontalOrientation)
//			DisplayTools.drawLine(g2, rectangle.getCenterX(), rectangle.getMinY(), rectangle.getCenterX(),
//					rectangle.getMaxY(), DisplayTools.thickStroke, neutralMarkColor);
//		else
//			DisplayTools.drawLine(g2, rectangle.getMinX(), rectangle.getCenterY(), rectangle.getMaxX(),
//					rectangle.getCenterY(), DisplayTools.thickStroke, neutralMarkColor);

		// neutral mark (optional)
		if (drawNeutralMark) {
			final float cx = (float) rectangle.getCenterX();
			final float cy = (float) rectangle.getCenterY();

			if (horizontalOrientation) {
				// vertical neutral line at center X
				DisplayTools.drawLine(g2, cx, (float) rectangle.getMinY(), cx, (float) rectangle.getMaxY(),
						DisplayTools.thickStroke, neutralMarkColor);
			} else {
				// horizontal neutral line at center Y
				DisplayTools.drawLine(g2, (float) rectangle.getMinX(), cy, (float) rectangle.getMaxX(), cy,
						DisplayTools.thickStroke, neutralMarkColor);
			}
		}

		// optional outer border of the bar
		if (isDrawOutline()) {
			g2.setPaint(getBorderPaint());
			DisplayTools.drawRectangle(g2, chartRectangle);
		}

		g2.setPaint(oldPaint);
	}

	// --- getters/setters ---

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
