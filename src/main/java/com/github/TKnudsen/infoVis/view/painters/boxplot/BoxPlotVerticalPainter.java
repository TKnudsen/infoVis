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
import java.util.Collection;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints a vertical boxplot
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.02
 */
public class BoxPlotVerticalPainter extends BoxPlotPainter implements IYPositionEncoding {

	public BoxPlotVerticalPainter(double[] data) {
		super(data);
	}

	public BoxPlotVerticalPainter(Collection<Double> data) {
		super(data);
	}

	public BoxPlotVerticalPainter(StatisticsSupport dataStatistics) {
		super(dataStatistics);
	}

	@Override
	protected void updatePositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		this.getPositionEncodingFunction().setMinPixel(rectangle.getMinY());
		this.getPositionEncodingFunction().setMaxPixel(rectangle.getMaxY());
	}

	@Override
	protected void drawMedian(Graphics2D g2) {
		Stroke s = g2.getStroke();

		g2.setStroke(stroke);
		if (chartRectangle != null && chartRectangle.getHeight() > 0) {
			float stokeWidth = (float) (chartRectangle.getHeight() * 0.01);
			g2.setStroke(new BasicStroke(stokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		}

		drawLevel(g2, med, getPaint());

		g2.setStroke(s);
	}

	@Override
	protected void drawDashedConnectors(Graphics2D g2) {
		if (chartRectangle == null)
			return;

		Color c = g2.getColor();
		g2.setPaint(getPaint());
		Stroke s = g2.getStroke();
		g2.setStroke(dashedstroke);

		DisplayTools.drawLine(g2, chartRectangle.getCenterX(), lowerWhisker, chartRectangle.getCenterX(),
				lowerQuartile);
		DisplayTools.drawLine(g2, chartRectangle.getCenterX(), upperQuartile, chartRectangle.getCenterX(),
				upperWhisker);

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
			DisplayTools.drawPoint(g2, chartRectangle.getCenterX(), outlierScreenCoordinates[i], radius,
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

		DisplayTools.drawLine(g2, chartRectangle.getMinX(), ratioOfAxis, chartRectangle.getMaxX(), ratioOfAxis);

		g2.setStroke(s);
		g2.setColor(c);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (chartRectangle == null)
			return;

		quartilesRectangle = new Rectangle2D.Double(chartRectangle.getMinX(), Math.min(upperQuartile, lowerQuartile),
				chartRectangle.getWidth(), Math.abs(upperQuartile - lowerQuartile));
	}

	@Override
	public void setYPositionEncodingFunction(IPositionEncodingFunction yPositionEncodingFunction) {
		this.setPositionEncodingFunction(yPositionEncodingFunction);
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!isToolTipping())
			return null;

		if (p == null)
			return null;

		return ToolTipTools.getTooltipForPositionMapping1D(p, p.getY(),
				getPositionEncodingFunction().inverseMapping(p.getY()), chartRectangle);
	}

	@Override
	public List<Double> getElementsInRectangle(RectangularShape rectangle) {
		if (rectangle == null)
			return null;

		Number v1 = getPositionEncodingFunction().inverseMapping(rectangle.getMinY());
		Number v2 = getPositionEncodingFunction().inverseMapping(rectangle.getMaxY());

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
		return true;
	}
}
