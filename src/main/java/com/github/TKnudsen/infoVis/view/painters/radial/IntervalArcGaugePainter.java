package com.github.TKnudsen.infoVis.view.painters.radial;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.BasicStrokeTools;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * A semicircular gauge glyph for a distribution of signed angles: a
 * needle-like pointer marks the median (the "speedometer" part), two
 * translucent wedges show the interquartile and 95% intervals, and a
 * concentric ring further out repeats those same two intervals as a
 * boxplot-style band with tick marks at each interval boundary. Meant to be
 * compact enough to serve as a small multiple, e.g. one per patient in a
 * cohort overview.
 * </p>
 *
 * <p>
 * Re-implemented from the glyph design described in:
 * </p>
 * <p>
 * Using Dashboard Networks to Visualize Multiple Patient Histories: A Design
 * Study on Post-Operative Prostate Cancer. IEEE Transactions on Visualization
 * and Computer Graphics (TVCG), pp. 1615-1628 (2018).
 * doi: 10.1109/TVCG.2018.2803829
 * </p>
 *
 * @version 1.0
 * @since 2018
 */
public class IntervalArcGaugePainter extends ChartPainter {

	/**
	 * Whether a gauge element is shown. {@code AUTOMATIC} lets the element's
	 * visibility follow the gauge's current on-screen size, hiding it once the
	 * gauge is too small to render it legibly.
	 */
	public enum ElementVisibility {
		AUTOMATIC, ALWAYS, NEVER
	}

	// the distribution and the screen-space angles its key quantiles map to
	private final StatisticsSupport angleStatistics;
	private double medianAngleDeg;
	private double lowerQuartileAngleDeg;
	private double upperQuartileAngleDeg;
	private double lowerWhiskerAngleDeg;
	private double upperWhiskerAngleDeg;

	// the half-width, in real-world degrees, that maps to a full +-90 screen
	// quadrant -- e.g. 90 means the data domain and the screen angle coincide
	private double angleScaleDegrees = 90;

	// layout, recomputed on every setRectangle
	private final Rectangle2D.Double innerSquare = new Rectangle2D.Double();
	private final Rectangle2D.Double outerSquare = new Rectangle2D.Double();
	private double ringGapRatio = 0.05;
	private double percentileRingThicknessRatio = 0.1;

	// per-element visibility, defaulting to size-based automatic decisions
	private ElementVisibility bandVisibility = ElementVisibility.AUTOMATIC;
	private ElementVisibility ringOutlineVisibility = ElementVisibility.AUTOMATIC;
	private ElementVisibility percentileRingVisibility = ElementVisibility.AUTOMATIC;
	private boolean bandsFitCurrentSize;
	private boolean ringOutlineFitsCurrentSize;
	private boolean percentileRingFitsCurrentSize;

	private boolean showMedianPointer = true;
	private boolean showZeroReferenceLine = false;
	private boolean showSupportRing = false;
	private boolean showScaleLegend = false;
	private Paint medianPointerPaint = null;

	public IntervalArcGaugePainter(StatisticsSupport angleStatistics) {
		this.angleStatistics = angleStatistics;

		recomputeQuantileAngles();

		setBackgroundPaint(null);
	}

	private void recomputeQuantileAngles() {
		if (angleStatistics == null)
			return;

		double toScreenDegrees = 90 / angleScaleDegrees;

		medianAngleDeg = clampToQuadrant(angleStatistics.getMedian() * toScreenDegrees);
		lowerQuartileAngleDeg = clampToQuadrant(angleStatistics.getPercentile(25) * toScreenDegrees);
		upperQuartileAngleDeg = clampToQuadrant(angleStatistics.getPercentile(75) * toScreenDegrees);
		lowerWhiskerAngleDeg = clampToQuadrant(angleStatistics.getPercentile(2.5) * toScreenDegrees);
		upperWhiskerAngleDeg = clampToQuadrant(angleStatistics.getPercentile(97.5) * toScreenDegrees);
	}

	private static double clampToQuadrant(double degrees) {
		return Math.max(-90, Math.min(90, degrees));
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		double diameter = rectangle.getHeight();
		double innerDiameter = diameter * (1.0 - percentileRingThicknessRatio);
		double squareInset = Math.round(ringGapRatio * innerDiameter) + (diameter - innerDiameter);

		double left = rectangle.getX() - diameter * 0.5;
		double top = rectangle.getY();

		outerSquare.setRect(left + (diameter - innerDiameter) * 0.5, top + (diameter - innerDiameter) * 0.5,
				innerDiameter, innerDiameter);
		innerSquare.setRect(left + squareInset, top + squareInset, diameter - 2 * squareInset,
				diameter - 2 * squareInset);

		ringOutlineFitsCurrentSize = diameter >= 10;
		bandsFitCurrentSize = diameter >= 30;
		percentileRingFitsCurrentSize = diameter >= 50;
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (getRectangle() == null)
			return;

		Paint previousPaint = g2.getPaint();
		Font previousFont = g2.getFont();
		g2.setFont(getFont());

		if (showScaleLegend)
			drawScaleLegend(g2);

		if (showZeroReferenceLine || showScaleLegend)
			drawZeroReferenceLine(g2);

		if (isVisible(bandVisibility, bandsFitCurrentSize))
			drawPercentileBands(g2);

		if (isVisible(ringOutlineVisibility, ringOutlineFitsCurrentSize))
			drawRingOutline(g2);

		if (showSupportRing)
			drawSupportRing(g2);

		if (isVisible(percentileRingVisibility, percentileRingFitsCurrentSize))
			drawPercentileRing(g2);

		if (showMedianPointer)
			drawMedianPointer(g2);

		g2.setFont(previousFont);
		g2.setPaint(previousPaint);
	}

	private static boolean isVisible(ElementVisibility visibility, boolean automaticDecision) {
		switch (visibility) {
		case ALWAYS:
			return true;
		case NEVER:
			return false;
		default:
			return automaticDecision;
		}
	}

	/**
	 * The angular span between two quantile angles, measured the short way
	 * around zero -- e.g. from -10 to 20 spans 30 degrees, not the 350 a naive
	 * subtraction across the wrap point would suggest.
	 */
	private static double angularSpan(double fromDeg, double toDeg) {
		boolean sameSideOfZero = (fromDeg > 0 && toDeg > 0) || (fromDeg < 0 && toDeg < 0);
		return sameSideOfZero ? Math.abs(fromDeg - toDeg) : Math.abs(fromDeg) + Math.abs(toDeg);
	}

	private void drawScaleLegend(Graphics2D g2) {
		drawScaleTick(g2, 45.0);
		drawScaleTick(g2, 0.0);
	}

	private void drawScaleTick(Graphics2D g2, double screenAngleDeg) {
		Stroke previousStroke = g2.getStroke();

		double radius = outerSquare.getWidth();
		double centerX = outerSquare.getCenterX();
		double centerY = outerSquare.getCenterY();
		double tickLength = Math.round(percentileRingThicknessRatio * 2 * radius) * 0.5;

		double directionX = Math.abs(Math.cos(Math.toRadians(screenAngleDeg)));
		double directionY = Math.sin(Math.toRadians(screenAngleDeg));

		double x0 = centerX + (radius * 0.5) * directionX;
		double y0 = centerY - (radius * 0.5) * directionY;
		double x1 = centerX + (radius * 0.5 + tickLength) * directionX;
		double y1 = centerY - (radius * 0.5 + tickLength) * directionY;

		g2.setStroke(BasicStrokeTools.get(0.5f));
		g2.setPaint(getBorderPaint());
		g2.draw(new Line2D.Double(x0, y0, x1, y1));

		double realWorldDegrees = screenAngleDeg / 90 * angleScaleDegrees;
		DisplayTools.drawRotatedString(g2, MathFunctions.round(realWorldDegrees, 1) + "", (float) (x1 + 3),
				(float) (y1 + 3), 0.0f);

		g2.setStroke(previousStroke);
	}

	private void drawZeroReferenceLine(Graphics2D g2) {
		g2.setStroke(BasicStrokeTools.get(0.5f));
		g2.setPaint(getBorderPaint());
		g2.draw(new Line2D.Double(getRectangle().getMinX(), getRectangle().getCenterY(), outerSquare.getMaxX(),
				getRectangle().getCenterY()));
	}

	private void drawSupportRing(Graphics2D g2) {
		g2.setStroke(BasicStrokeTools.get(0.5f));
		g2.setPaint(getBorderPaint());
		g2.draw(new Arc2D.Double(outerSquare, 90, -180, Arc2D.OPEN));
	}

	private void drawRingOutline(Graphics2D g2) {
		g2.setPaint(getBorderPaint());
		g2.draw(new Arc2D.Double(innerSquare, 90, -180, Arc2D.OPEN));
	}

	private void drawPercentileBands(Graphics2D g2) {
		double whiskerSpan = angularSpan(lowerWhiskerAngleDeg, upperWhiskerAngleDeg);
		double quartileSpan = angularSpan(lowerQuartileAngleDeg, upperQuartileAngleDeg);

		g2.setPaint(ColorTools.setAlpha(getPaint(), 0.25f));
		g2.fill(new Arc2D.Double(innerSquare, lowerWhiskerAngleDeg, whiskerSpan, Arc2D.PIE));
		g2.fill(new Arc2D.Double(innerSquare, lowerQuartileAngleDeg, quartileSpan, Arc2D.PIE));
	}

	private void drawPercentileRing(Graphics2D g2) {
		Stroke previousStroke = g2.getStroke();

		double whiskerSpan = angularSpan(lowerWhiskerAngleDeg, upperWhiskerAngleDeg);
		double quartileSpan = angularSpan(lowerQuartileAngleDeg, upperQuartileAngleDeg);

		g2.setPaint(getBorderPaint());
		g2.draw(new Arc2D.Double(outerSquare, lowerWhiskerAngleDeg, whiskerSpan, Arc2D.OPEN));

		float ringStrokeWidth = Math.round(percentileRingThicknessRatio * outerSquare.getWidth());
		g2.setStroke(BasicStrokeTools.get(ringStrokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setPaint(ColorTools.setAlpha(getPaint(), 0.75f));
		g2.draw(new Arc2D.Double(outerSquare, lowerQuartileAngleDeg, quartileSpan, Arc2D.OPEN));

		g2.setPaint(getBorderPaint());
		for (double boundaryAngle : new double[] { lowerQuartileAngleDeg, upperQuartileAngleDeg,
				lowerWhiskerAngleDeg, upperWhiskerAngleDeg })
			g2.draw(new Arc2D.Double(outerSquare, boundaryAngle, 1.0, Arc2D.OPEN));

		g2.setStroke(previousStroke);
	}

	private void drawMedianPointer(Graphics2D g2) {
		Stroke previousStroke = g2.getStroke();

		float ringStrokeWidth = Math.round(percentileRingThicknessRatio * outerSquare.getWidth());
		double centerX = outerSquare.getCenterX();
		double centerY = outerSquare.getCenterY();
		double pointerLength = outerSquare.getWidth() * 0.5 + ringStrokeWidth * 0.5;
		double tipX = centerX + pointerLength * Math.abs(Math.cos(Math.toRadians(medianAngleDeg)));
		double tipY = centerY - pointerLength * Math.sin(Math.toRadians(medianAngleDeg));

		g2.setStroke(BasicStrokeTools.get(Math.max(1, Math.round(0.1 * ringStrokeWidth)), BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL));
		g2.setPaint(medianPointerPaint == null ? getBorderPaint() : medianPointerPaint);
		g2.draw(new Line2D.Double(centerX, centerY, tipX, tipY));

		g2.setStroke(previousStroke);
	}

	public boolean isShowMedianPointer() {
		return showMedianPointer;
	}

	public void setShowMedianPointer(boolean showMedianPointer) {
		this.showMedianPointer = showMedianPointer;
	}

	public void setRingOutlineVisibility(ElementVisibility ringOutlineVisibility) {
		this.ringOutlineVisibility = ringOutlineVisibility;
	}

	public void setBandVisibility(ElementVisibility bandVisibility) {
		this.bandVisibility = bandVisibility;
	}

	public void setPercentileRingVisibility(ElementVisibility percentileRingVisibility) {
		this.percentileRingVisibility = percentileRingVisibility;
	}

	public double getPercentileRingThicknessRatio() {
		return percentileRingThicknessRatio;
	}

	public void setPercentileRingThicknessRatio(double percentileRingThicknessRatio) {
		this.percentileRingThicknessRatio = percentileRingThicknessRatio;

		setRectangle(getRectangle());
	}

	public double getRingGapRatio() {
		return ringGapRatio;
	}

	public void setRingGapRatio(double ringGapRatio) {
		this.ringGapRatio = ringGapRatio;

		setRectangle(getRectangle());
	}

	public boolean isShowZeroReferenceLine() {
		return showZeroReferenceLine;
	}

	public void setShowZeroReferenceLine(boolean showZeroReferenceLine) {
		this.showZeroReferenceLine = showZeroReferenceLine;
	}

	public Paint getMedianPointerPaint() {
		return medianPointerPaint;
	}

	public void setMedianPointerPaint(Paint medianPointerPaint) {
		this.medianPointerPaint = medianPointerPaint;
		this.showMedianPointer = true;
	}

	public boolean isShowSupportRing() {
		return showSupportRing;
	}

	public void setShowSupportRing(boolean showSupportRing) {
		this.showSupportRing = showSupportRing;
	}

	public boolean isShowScaleLegend() {
		return showScaleLegend;
	}

	public void setShowScaleLegend(boolean showScaleLegend) {
		this.showScaleLegend = showScaleLegend;
	}

	public double getAngleScaleDegrees() {
		return angleScaleDegrees;
	}

	public void setAngleScaleDegrees(double angleScaleDegrees) {
		this.angleScaleDegrees = angleScaleDegrees;

		recomputeQuantileAngles();
	}

	@Override
	public String toString() {
		return "IntervalArcGaugePainter";
	}
}
