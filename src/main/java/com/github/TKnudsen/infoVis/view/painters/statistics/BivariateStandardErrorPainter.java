package com.github.TKnudsen.infoVis.view.painters.statistics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * Draws a single bivariate group as a mean-centered error-bar cross: a
 * horizontal bar spanning {@code [mean - error, mean + error]} on X, and a
 * vertical bar spanning the same on Y, crossing at the group's (meanX, meanY)
 * -- a compact way to show central tendency and spread on two variables at
 * once, several of which {@link BivariateStandardErrorsPainter} overlays on
 * one shared axis.
 * </p>
 *
 * <p>
 * {@code error} is not literally the standard error of the mean; it is
 * whatever absolute distance the caller passes in (see
 * {@link BivariateStandardErrorsPainter}'s {@code spreadMultiplier}, which is
 * typically a multiple of the standard deviation). This class only knows how
 * to draw a cross at a given mean +/- a given distance, not how that distance
 * was derived.
 * </p>
 *
 * @version 1.0
 * @since 2018
 */
public class BivariateStandardErrorPainter extends ChartPainter {

	protected double[] dataX;
	protected double[] dataY;

	private final double minX;
	private final double maxX;
	private final double minY;
	private final double maxY;

	private double outerLineWidth = 30;
	private double meanLineWidth = 10;
	private Color lineColor = Color.BLACK;
	private Color meanColor = Color.GRAY;
	private String label;
	private boolean drawLabel = true;

	// normalized (0..1) within [minX,maxX]/[minY,maxY] -- not pixel coordinates
	private final double meanXRatio;
	private final double upperXRatio;
	private final double lowerXRatio;
	private final double meanYRatio;
	private final double upperYRatio;
	private final double lowerYRatio;

	private final StringPainter stringPainter;

	private Stroke meansStroke = DisplayTools.thickStroke;

	/** Uses {@code dataX}/{@code dataY}'s own min/max as the normalization domain. */
	public BivariateStandardErrorPainter(double[] dataX, double[] dataY, double errorX, double errorY,
			String label) {
		this(dataX, dataY, errorX, errorY, label, MathFunctions.getMin(dataX), MathFunctions.getMax(dataX),
				MathFunctions.getMin(dataY), MathFunctions.getMax(dataY));
	}

	/**
	 * @param dataX  the group's X samples (only {@link MathFunctions#getMean} of
	 *               these is used; the array itself is not retained beyond
	 *               construction)
	 * @param dataY  the group's Y samples
	 * @param errorX half-width of the X error bar around the mean (already in
	 *               data units, e.g. a multiple of the standard deviation)
	 * @param errorY half-width of the Y error bar around the mean
	 * @param label  this group's legend label
	 * @param minX   normalization domain minimum on X -- typically shared across
	 *               every group in a {@link BivariateStandardErrorsPainter}, not
	 *               this group's own local min
	 * @param maxX   normalization domain maximum on X
	 * @param minY   normalization domain minimum on Y
	 * @param maxY   normalization domain maximum on Y
	 */
	public BivariateStandardErrorPainter(double[] dataX, double[] dataY, double errorX, double errorY, String label,
			double minX, double maxX, double minY, double maxY) {
		this.dataX = dataX;
		this.dataY = dataY;
		this.label = label;
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;

		setBackgroundPaint(null);

		stringPainter = new StringPainter(label);
		stringPainter.setHorizontalStringAlignment(HorizontalStringAlignment.LEFT);
		stringPainter.setOffset(6);
		stringPainter.setBackgroundPaint(null);
		stringPainter.setBorderPaint(null);

		double meanX = MathFunctions.getMean(dataX);
		double meanY = MathFunctions.getMean(dataY);

		meanXRatio = MathFunctions.linearScale(minX, maxX, meanX, true);
		upperXRatio = MathFunctions.linearScale(minX, maxX, meanX + errorX, true);
		lowerXRatio = MathFunctions.linearScale(minX, maxX, meanX - errorX, true);
		meanYRatio = MathFunctions.linearScale(minY, maxY, meanY, true);
		upperYRatio = MathFunctions.linearScale(minY, maxY, meanY + errorY, true);
		lowerYRatio = MathFunctions.linearScale(minY, maxY, meanY - errorY, true);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);
		Color resetColor = g2.getColor();
		Stroke resetStroke = g2.getStroke();

		g2.setColor(lineColor);
		g2.setStroke(stroke);

		// vertical error bar: baseline + end caps
		drawVerticalLine(g2, meanXRatio, meanYRatio, (upperYRatio - lowerYRatio) * getRectangle().getHeight());
		drawHorizontalLine(g2, meanXRatio, upperYRatio, outerLineWidth);
		drawHorizontalLine(g2, meanXRatio, lowerYRatio, outerLineWidth);

		// horizontal error bar: baseline + end caps
		drawHorizontalLine(g2, meanXRatio, meanYRatio, (upperXRatio - lowerXRatio) * getRectangle().getWidth());
		drawVerticalLine(g2, upperXRatio, meanYRatio, outerLineWidth);
		drawVerticalLine(g2, lowerXRatio, meanYRatio, outerLineWidth);

		// mean cross, drawn last/thicker so it reads above the error bars
		g2.setStroke(meansStroke);
		g2.setColor(meanColor);
		drawVerticalLine(g2, meanXRatio, meanYRatio, meanLineWidth);
		drawHorizontalLine(g2, meanXRatio, meanYRatio, meanLineWidth);

		if (drawLabel)
			stringPainter.draw(g2);

		if (isDrawOutline()) {
			g2.setStroke(DisplayTools.standardStroke);
			DisplayTools.drawRectangle(g2, getChartRectangle(), Color.BLACK);
		}

		g2.setColor(resetColor);
		g2.setStroke(resetStroke);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		double xMin = rectangle.getMinX();
		double yMax = rectangle.getMaxY();
		double w = rectangle.getWidth();
		double h = rectangle.getHeight();

		double stringRectHeight = 24;
		double stringRectWidth = 150;

		double x = xMin + meanXRatio * w;
		double y = yMax - meanYRatio * h - stringRectHeight;

		stringPainter.setRectangle(new Rectangle2D.Double(x, y, stringRectWidth, stringRectHeight));

		if (Math.min(w, h) < 500) {
			stroke = DisplayTools.standardStroke;
			meansStroke = new BasicStroke(3.0f);
		} else {
			stroke = new BasicStroke(2.0f);
			meansStroke = new BasicStroke(5.0f);
		}
	}

	protected void drawHorizontalLine(Graphics2D g2, double xRatio, double yRatio, double lineWidth) {
		double xMin = getRectangle().getMinX();
		double yMax = getRectangle().getMaxY();
		double w = getRectangle().getWidth();
		double h = getRectangle().getHeight();

		double x1 = xMin + xRatio * w - 0.5 * lineWidth;
		double x2 = x1 + lineWidth;
		double y = yMax - yRatio * h;

		DisplayTools.drawLine(g2, x1, y, x2, y);
	}

	protected void drawVerticalLine(Graphics2D g2, double xRatio, double yRatio, double lineWidth) {
		double xMin = getRectangle().getMinX();
		double yMax = getRectangle().getMaxY();
		double w = getRectangle().getWidth();
		double h = getRectangle().getHeight();

		double y1 = yMax - yRatio * h - 0.5 * lineWidth;
		double y2 = y1 + lineWidth;
		double x = xMin + xRatio * w;

		DisplayTools.drawLine(g2, x, y1, x, y2);
	}

	public double getOuterLineWidth() {
		return outerLineWidth;
	}

	public void setOuterLineWidth(double outerLineWidth) {
		this.outerLineWidth = outerLineWidth;
	}

	public double getMeanLineWidth() {
		return meanLineWidth;
	}

	public void setMeanLineWidth(double meanLineWidth) {
		this.meanLineWidth = meanLineWidth;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public Color getMeanColor() {
		return meanColor;
	}

	public void setMeanColor(Color meanColor) {
		this.meanColor = meanColor;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isDrawLabel() {
		return drawLabel;
	}

	public void setDrawLabel(boolean drawLabel) {
		this.drawLabel = drawLabel;
	}
}
