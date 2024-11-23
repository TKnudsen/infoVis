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
import com.github.TKnudsen.infoVis.view.tools.BasicStrokes;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * paints the statistical distribution of angles (between -90 and +90 by
 * default) by combining a speedometer metaphor with a boxplot.
 * 
 * dataStatistics may be null. makes handling easier from extetnal
 */
public class AngleDistributionCircularPainter extends ChartPainter {

	// statistical derivates
	private final StatisticsSupport dataStatistics;
	private double r25;
	private double r75;
	private double rmed;
	private double rlowerW;
	private double rupperW;

	// graphical primitives
	private Rectangle2D.Double innerRect;
	private Rectangle2D.Double outerRect;
	private Arc2D.Double innerArc;
	private Arc2D.Double outerArc;

	// LOD parameters
	private boolean LODdisabled = false;
	private boolean drawCircleOutlineLOD;
	private boolean drawCircleAreasLOD;
	private boolean drawOuterRadialBoxplotLOD;

	// settable parameters
	private boolean drawZeroLine = false;
	private Boolean drawMeanLine = true;
	private Boolean drawCircleOutlineSettable = null;
	private Boolean drawCircleAreasSettable = null;
	private Boolean drawOuterRadialBoxplotSettable = null;
	private boolean drawBoxPlotSupportingRadius = false;
	private double relativeGapSizeBetweenArcAndBoxplot = 0.05;
	private double boxplotStokeWidthRelative = 0.1;
	private Paint meanLinePaint = null;
	private double maxAngle = 90;
	private boolean drawLegend = false;

	public AngleDistributionCircularPainter(StatisticsSupport dataStatistics) {
		this.dataStatistics = dataStatistics;

		initialize();

		setBackgroundPaint(null);
	}

	private void initialize() {
		drawCircleOutlineLOD = true;
		innerRect = new Rectangle2D.Double();
		outerRect = new Rectangle2D.Double();
		innerArc = new Arc2D.Double();
		outerArc = new Arc2D.Double();

		if (dataStatistics != null) {
			rmed = Math.max(-90, Math.min(90, dataStatistics.getMedian() * (90 / maxAngle)));
			r25 = Math.max(-90, Math.min(90, dataStatistics.getPercentile(25) * (90 / maxAngle)));
			r75 = Math.max(-90, Math.min(90, dataStatistics.getPercentile(75) * (90 / maxAngle)));
			rlowerW = Math.max(-90, Math.min(90, dataStatistics.getPercentile(2.5) * (90 / maxAngle)));
			rupperW = Math.max(-90, Math.min(90, dataStatistics.getPercentile(97.5) * (90 / maxAngle)));
		}
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		// dimensions
		double width = rectangle.getWidth();
		double height = rectangle.getHeight();
		double x = rectangle.getX();
		double y = rectangle.getY();

		double edgeLength = height; // Math.min(width * 2, height);
		double edgeLengthBoxPlot = edgeLength * (1.0 - boxplotStokeWidthRelative);
		double squareGap = Math.round(relativeGapSizeBetweenArcAndBoxplot * edgeLengthBoxPlot)
				+ (edgeLength - edgeLengthBoxPlot);

		double x0 = x - edgeLength * 0.5;
		double y0 = y;
		outerRect.setRect(x0 + (edgeLength - edgeLengthBoxPlot) * 0.5, y0 + (edgeLength - edgeLengthBoxPlot) * 0.5,
				edgeLengthBoxPlot, edgeLengthBoxPlot);
		outerArc.setArc(outerRect, 90, -180, Arc2D.PIE);

		innerRect.setRect(x0 + squareGap, y0 + squareGap, edgeLength - 2 * squareGap, edgeLength - 2 * squareGap);
		innerArc.setArc(innerRect, 90, -180, Arc2D.PIE);

		// LOD
		if (LODdisabled) {
			drawCircleOutlineLOD = true;
			drawCircleAreasLOD = true;
			drawOuterRadialBoxplotLOD = true;
		} else {
			if (edgeLength < 10)
				drawCircleOutlineLOD = false;
			else
				drawCircleOutlineLOD = true;

			if (edgeLength < 30)
				drawCircleAreasLOD = false;
			else
				drawCircleAreasLOD = true;

			if (edgeLength < 50)
				drawOuterRadialBoxplotLOD = false;
			else
				drawOuterRadialBoxplotLOD = true;
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		Paint p = g2.getPaint();
		Font f = g2.getFont();

		g2.setFont(getFont());

		if (drawLegend)
			drawLegend(g2);

		if (drawZeroLine || drawLegend)
			drawZeroLine(g2);

		if (drawCircleAreasSettable == null && drawCircleAreasLOD
				|| drawCircleAreasSettable != null && drawCircleAreasSettable)
			drawCircleAreas(g2);

		if (drawCircleOutlineSettable == null && drawCircleOutlineLOD
				|| drawCircleOutlineSettable != null && drawCircleOutlineSettable)
			drawCircleOutline(g2);

		if (drawBoxPlotSupportingRadius)
			rawBoxPlotSupportingRadius(g2);

		if (drawOuterRadialBoxplotSettable == null && drawOuterRadialBoxplotLOD
				|| drawOuterRadialBoxplotSettable != null && drawOuterRadialBoxplotSettable)
			drawOuterRadialBoxplot(g2);

		if (drawMeanLine)
			drawMeanLine(g2);

		g2.setFont(f);
		g2.setPaint(p);
	}

	private void drawLegend(Graphics2D g2) {
		drawLegendTickAndLabel(g2, 45.0);
		drawLegendTickAndLabel(g2, 0.0);
	}

	private void drawLegendTickAndLabel(Graphics2D g2, double angle) {
		Stroke stroke = g2.getStroke();

		double w = outerRect.getWidth();
		double x0 = outerRect.getCenterX();
		double y0 = outerRect.getCenterY();
		double tickLength = Math.round(boxplotStokeWidthRelative * 2 * w) * 0.5;

		x0 = x0 + (w * 0.5) * Math.abs(Math.cos(angle * Math.PI / 180));
		y0 = y0 - (w * 0.5) * Math.sin(angle * Math.PI / 180);
		double x1 = outerRect.getCenterX() + (w * 0.5 + tickLength) * Math.abs(Math.cos(angle * Math.PI / 180));
		double y1 = outerRect.getCenterY() - (w * 0.5 + tickLength) * Math.sin(angle * Math.PI / 180);

		Line2D.Double tickLine = new Line2D.Double(x0, y0, x1, y1);

		// draw tick
		g2.setStroke(BasicStrokes.get(0.5f));
		g2.setPaint(getBorderPaint());
		g2.draw(tickLine);

		// draw actual value at degree°
		DisplayTools.drawRotatedString(g2, MathFunctions.round(angle / 90 * maxAngle, 1) + "", (float) (x1 + 3),
				(float) (y1 + Math.cos(tickLength)), 0.0f);

		g2.setStroke(stroke);
	}

	private void drawZeroLine(Graphics2D g2) {
		Line2D.Double meanLine = new Line2D.Double(rectangle.getMinX(), rectangle.getCenterY(), outerRect.getMaxX(),
				rectangle.getCenterY());

		g2.setStroke(BasicStrokes.get(0.5f));
//		g2.setPaint(ColorTools.setAlpha(getBorderPaint(), 0.33f));
		g2.setPaint(getBorderPaint());
		g2.draw(meanLine);
	}

	private void rawBoxPlotSupportingRadius(Graphics2D g2) {
		g2.setStroke(BasicStrokes.get(0.5f));
//		g2.setPaint(ColorTools.setAlpha(getBorderPaint(), 0.33f));
		g2.setPaint(getBorderPaint());
		g2.draw(outerArc);
	}

	private void drawCircleOutline(Graphics2D g2) {
		g2.setPaint(getBorderPaint());
		g2.draw(innerArc);
	}

	private void drawCircleAreas(Graphics2D g2) {

		double degree_quantil97;
		if (rlowerW > 0 && rupperW > 0 || rlowerW < 0 && rupperW < 0)
			degree_quantil97 = Math.abs(rlowerW - rupperW);
		else
			degree_quantil97 = Math.abs(rlowerW) + Math.abs(rupperW);

		double degree_quantil75;
		if (r25 > 0 && r75 > 0 || r25 < 0 && r75 < 0)
			degree_quantil75 = Math.abs(r25 - r75);
		else
			degree_quantil75 = Math.abs(r25) + Math.abs(r75);

		g2.setPaint(ColorTools.setAlpha(getPaint(), 0.25f));
		Arc2D.Double arc97 = new Arc2D.Double(innerRect, rlowerW, degree_quantil97, Arc2D.PIE);
		g2.fill(arc97);

		Arc2D.Double arc75 = new Arc2D.Double(innerRect, r25, degree_quantil75, Arc2D.PIE);
		g2.fill(arc75);
	}

	/**
	 * outer radial boxplot
	 * 
	 * @param g2
	 */
	private final void drawOuterRadialBoxplot(Graphics2D g2) {
		Stroke stroke = g2.getStroke();

		double degree_quantil97;
		if (rlowerW > 0 && rupperW > 0 || rlowerW < 0 && rupperW < 0)
			degree_quantil97 = Math.abs(rlowerW - rupperW);
		else
			degree_quantil97 = Math.abs(rlowerW) + Math.abs(rupperW);

		double degree_quantil75;
		if (r25 > 0 && r75 > 0 || r25 < 0 && r75 < 0)
			degree_quantil75 = Math.abs(r25 - r75);
		else
			degree_quantil75 = Math.abs(r25) + Math.abs(r75);

		g2.setPaint(getBorderPaint());
		Arc2D.Double box97 = new Arc2D.Double(outerRect, rlowerW, degree_quantil97, Arc2D.OPEN);
		g2.draw(box97);

		float strokeWidth = Math.round(boxplotStokeWidthRelative * outerRect.getWidth());
		g2.setStroke(BasicStrokes.get(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setPaint(ColorTools.setAlpha(getPaint(), 0.75f));
		Arc2D.Double box75 = new Arc2D.Double(outerRect, r25, degree_quantil75, Arc2D.OPEN);
		g2.draw(box75);

		// draw outer elements
		g2.setPaint(getBorderPaint());

		Arc2D.Double r25Stroke = new Arc2D.Double(outerRect, r25, 1, Arc2D.OPEN);
		g2.draw(r25Stroke);

		Arc2D.Double r75Stroke = new Arc2D.Double(outerRect, r75, 1, Arc2D.OPEN);
		g2.draw(r75Stroke);

		Arc2D.Double rLowerStroke = new Arc2D.Double(outerRect, rlowerW, 1.0, Arc2D.OPEN);
		g2.draw(rLowerStroke);

		Arc2D.Double rOuterStroke = new Arc2D.Double(outerRect, rupperW, 1, Arc2D.OPEN);
		g2.draw(rOuterStroke);

		g2.setStroke(stroke);
	}

	/**
	 * thick speedometer line from the circle center
	 * 
	 * @param g2
	 */
	private final void drawMeanLine(Graphics2D g2) {
		Stroke stroke = g2.getStroke();

		float boxPlotStrokeWidth = Math.round(boxplotStokeWidthRelative * outerRect.getWidth());
		double x0 = outerRect.getCenterX();
		double y0 = outerRect.getCenterY();
		double r = outerRect.getWidth() * 0.5 + boxPlotStrokeWidth * 0.5;
		double x1 = x0 + r * Math.abs(Math.cos(rmed * Math.PI / 180));
		double y1 = y0 - r * Math.sin(rmed * Math.PI / 180);

		Line2D.Double meanLine = new Line2D.Double(x0, y0, x1, y1);

		g2.setStroke(BasicStrokes.get(Math.max(1, Math.round(0.1 * boxPlotStrokeWidth)), BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL));
		g2.setPaint((meanLinePaint == null) ? getBorderPaint() : meanLinePaint);
		g2.draw(meanLine);

		g2.setStroke(stroke);
	}

	public Boolean getDrawMeanLine() {
		return drawMeanLine;
	}

	public void setDrawMeanLine(Boolean drawMeanLine) {
		this.drawMeanLine = drawMeanLine;
	}

	public void setDrawCircleOutline(boolean drawCircleOutline) {
		this.drawCircleOutlineSettable = drawCircleOutline;
		this.LODdisabled = true;
	}

	public void setDrawCircleArea(boolean drawCircleArea) {
		this.drawCircleAreasSettable = drawCircleArea;
		this.LODdisabled = true;
	}

	public void setDrawOuterRadialBoxplot(boolean drawOuterRadialBoxplot) {
		this.drawOuterRadialBoxplotSettable = drawOuterRadialBoxplot;
		this.LODdisabled = true;
	}

	public double getBoxplotStokeWidthRelative() {
		return boxplotStokeWidthRelative;
	}

	public void setBoxplotStokeWidthRelative(double boxplotStokeWidthRelative) {
		this.boxplotStokeWidthRelative = boxplotStokeWidthRelative;

		setRectangle(rectangle);
	}

	public double getRelativeGapSizeBetweenArcAndBoxplot() {
		return relativeGapSizeBetweenArcAndBoxplot;
	}

	public void setRelativeGapSizeBetweenArcAndBoxplot(double relativeGapSizeBetweenArcAndBoxplot) {
		this.relativeGapSizeBetweenArcAndBoxplot = relativeGapSizeBetweenArcAndBoxplot;

		setRectangle(rectangle);
	}

	public boolean isDrawZeroLine() {
		return drawZeroLine;
	}

	public void setDrawZeroLine(boolean drawZeroLine) {
		this.drawZeroLine = drawZeroLine;
	}

	public Paint getMeanLinePaint() {
		return meanLinePaint;
	}

	public void setMeanLinePaint(Paint meanLinePaint) {
		this.meanLinePaint = meanLinePaint;
		this.drawMeanLine = true;
	}

	public boolean isDrawBoxPlotSupportingRadius() {
		return drawBoxPlotSupportingRadius;
	}

	public void setDrawBoxPlotSupportingRadius(boolean drawBoxPlotSupportingRadius) {
		this.drawBoxPlotSupportingRadius = drawBoxPlotSupportingRadius;
	}

	public boolean isDrawLegend() {
		return drawLegend;
	}

	public void setDrawLegend(boolean drawLegend) {
		this.drawLegend = drawLegend;
	}

	public double getMaxAngle() {
		return maxAngle;
	}

	public void setMaxAngle(double maxAngle) {
		this.maxAngle = maxAngle;

		initialize();
	}

}
