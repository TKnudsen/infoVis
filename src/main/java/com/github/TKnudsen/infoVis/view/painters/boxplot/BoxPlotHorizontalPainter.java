package com.github.TKnudsen.infoVis.view.painters.boxplot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints a horizontal boxplot
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.01
 */
public class BoxPlotHorizontalPainter extends BoxPlotPainter implements IXPositionEncoding {

	public BoxPlotHorizontalPainter(double[] data) {
		super(data);
	}

	public BoxPlotHorizontalPainter(List<Double> data) {
		super(data);
	}

	public BoxPlotHorizontalPainter(StatisticsSupport dataStatistics) {
		super(dataStatistics);
	}

	@Override
	protected void updatePositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		this.getPositionEncodingFunction().setMinPixel(rectangle.getMinX());
		this.getPositionEncodingFunction().setMaxPixel(rectangle.getMaxX());
	}

	@Override
	protected void drawMedian(Graphics2D g2) {
		Stroke s = g2.getStroke();

		g2.setStroke(stroke);
		if (chartRectangle != null && chartRectangle.getHeight() > 0) {
			float stokeWidth = (float) (chartRectangle.getWidth() * 0.01);
			g2.setStroke(new BasicStroke(stokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		}

		drawLevel(g2, med, getPaint());

		g2.setStroke(s);
	}

	@Override
	protected void drawDashedConnectors(Graphics2D g2) {
		Color c = g2.getColor();
		g2.setPaint(getPaint());
		Stroke s = g2.getStroke();
		g2.setStroke(dashedstroke);

		if (chartRectangle != null) {
			DisplayTools.drawLine(g2, lowerWhisker, chartRectangle.getCenterY(), lowerQuartile,
					chartRectangle.getCenterY());
			DisplayTools.drawLine(g2, upperQuartile, chartRectangle.getCenterY(), upperWhisker,
					chartRectangle.getCenterY());
		}

		g2.setStroke(s);
		g2.setColor(c);
	}

	@Override
	protected void drawOutliers(Graphics2D g2) {
		if (outlierScreenCoordinates == null)
			return;

		int radius = (int) Math.min(chartRectangle.getWidth() * 0.25,
				Math.min(chartRectangle.getHeight() * 0.25, stroke.getLineWidth() * 3));

		for (int i = 0; i < outlierScreenCoordinates.length; i++)
			DisplayTools.drawPoint(g2, outlierScreenCoordinates[i], (int) chartRectangle.getCenterY(), radius,
					getBorderPaint(), false);
	}

	@Override
	protected void drawLevel(Graphics2D g2, double ratioOfAxis, Paint color) {
		if (chartRectangle == null)
			return;

		Color c = g2.getColor();
		g2.setPaint(color);

		Stroke s = g2.getStroke();
		g2.setStroke(stroke);

		if (chartRectangle != null)
			DisplayTools.drawLine(g2, ratioOfAxis, chartRectangle.getMinY(), ratioOfAxis, chartRectangle.getMaxY());

		g2.setStroke(s);
		g2.setColor(c);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (chartRectangle == null)
			return;

		// this is different to the vertical painter
		quartilesRectangle = new Rectangle2D.Double(Math.min(upperQuartile, lowerQuartile), chartRectangle.getMinY(),
				Math.abs(upperQuartile - lowerQuartile), chartRectangle.getHeight());
	}

	@Override
	public void setXPositionEncodingFunction(IPositionEncodingFunction xPositionEncodingFunction) {
		this.setPositionEncodingFunction(xPositionEncodingFunction);
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!isToolTipping())
			return null;

		if (p == null)
			return null;

		return ToolTipTools.getTooltipForPositionMapping1D(p, p.getX(),
				getPositionEncodingFunction().inverseMapping(p.getX()), chartRectangle);
	}

	@Override
	public List<Double> getElementsInRectangle(RectangularShape rectangle) {
		if (rectangle == null)
			return null;

		Number v1 = getPositionEncodingFunction().inverseMapping(rectangle.getMinX());
		Number v2 = getPositionEncodingFunction().inverseMapping(rectangle.getMaxX());

		// double[] values = data;
		double[] values = null;
		if (values == null)
			values = dataStatistics.getValues();
		if (values == null)
			return null;

		List<Double> elements = new ArrayList<>();
		for (double d : values)
			if (d >= v1.doubleValue() && d <= v2.doubleValue())
				elements.add(d);

		return elements;
	}

	@Override
	protected boolean isInvertedAxis() {
		return false;
	}

}
