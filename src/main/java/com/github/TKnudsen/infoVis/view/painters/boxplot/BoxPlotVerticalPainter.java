package com.github.TKnudsen.infoVis.view.painters.boxplot;

import java.awt.BasicStroke;
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
import com.github.TKnudsen.infoVis.view.tools.BasicStrokeTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoding;

/**
 * <p>
 * Paints a vertical boxplot using float-precision drawing and optimized
 * Graphics2D state handling.
 * </p>
 *
 * @version 2.1 (refactored for performance)
 * @since 2016
 */
public class BoxPlotVerticalPainter extends BoxPlotPainter implements IYPositionEncoding {

	public BoxPlotVerticalPainter(double[] data) {
		super(data);
	}

	public BoxPlotVerticalPainter(Collection<? extends Number> data) {
		super(data);
	}

	public BoxPlotVerticalPainter(StatisticsSupport dataStatistics) {
		super(dataStatistics);
	}

	@Override
	protected void updatePositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		IPositionEncodingFunction f = getPositionEncodingFunction();
		f.setMinPixel(rectangle.getMinY());
		f.setMaxPixel(rectangle.getMaxY());
	}

	@Override
	protected void drawMedian(Graphics2D g2) {
		if (chartRectangle == null || chartRectangle.getWidth() <= 0)
			return;

		final Stroke oldStroke = g2.getStroke();
		final float strokeWidth = (float) Math.max(1f, chartRectangle.getHeight() * 0.01f);
		final Stroke medianStroke = BasicStrokeTools.get(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

		g2.setStroke(medianStroke);
		g2.setPaint(getPaint());

		final float y = (float) medScreen;
		final float x1 = (float) chartRectangle.getMinX();
		final float x2 = (float) chartRectangle.getMaxX();
		DisplayTools.drawLine(g2, x1, y, x2, y);

		g2.setStroke(oldStroke);
	}

	@Override
	protected void drawDashedConnectors(Graphics2D g2) {
		if (chartRectangle == null)
			return;

		final Paint oldPaint = g2.getPaint();
		final Stroke oldStroke = g2.getStroke();
		g2.setPaint(getPaint());
		g2.setStroke(dashedstroke);

		final float x = (float) chartRectangle.getCenterX();

		// Lower connector
		DisplayTools.drawLine(g2, x, (float) lowerWhiskerScreen, x, (float) lowerQuartileScreen);
		// Upper connector
		DisplayTools.drawLine(g2, x, (float) upperQuartileScreen, x, (float) upperWhiskerScreen);

		g2.setStroke(oldStroke);
		g2.setPaint(oldPaint);
	}

	@Override
	protected void drawOutliers(Graphics2D g2) {
		if (chartRectangle == null || outlierScreenCoordinates == null || outlierScreenCoordinates.length == 0)
			return;

		final int centerX = (int) Math.round(chartRectangle.getCenterX());
		final int radius = (int) Math.max(1, Math.min(chartRectangle.getWidth() * 0.25,
				Math.min(chartRectangle.getHeight() * 0.25, stroke.getLineWidth() * 3)));

		final Paint oldPaint = g2.getPaint();
		g2.setPaint(getBorderPaint());

		for (double y : outlierScreenCoordinates)
			DisplayTools.drawPoint(g2, centerX, (int) Math.round(y), radius, false);

		g2.setPaint(oldPaint);
	}

	@Override
	protected void drawLevel(Graphics2D g2, double ratioOfAxis, Paint color) {
		if (chartRectangle == null)
			return;

		final Paint oldPaint = g2.getPaint();
		final Stroke oldStroke = g2.getStroke();

		g2.setPaint(color);
		g2.setStroke(stroke);

		final float y = (float) ratioOfAxis;
		final float x1 = (float) chartRectangle.getMinX();
		final float x2 = (float) chartRectangle.getMaxX();
		DisplayTools.drawLine(g2, x1, y, x2, y);

		g2.setStroke(oldStroke);
		g2.setPaint(oldPaint);
	}

	@Override
	Rectangle2D calculateQuartilesRectangle() {
		return new Rectangle2D.Double(chartRectangle.getMinX(), Math.min(upperQuartileScreen, lowerQuartileScreen),
				chartRectangle.getWidth(), Math.abs(upperQuartileScreen - lowerQuartileScreen));
	}

	@Override
	public void setYPositionEncodingFunction(IPositionEncodingFunction yPositionEncodingFunction) {
		this.setPositionEncodingFunction(yPositionEncodingFunction);
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!isToolTipping() || p == null)
			return null;

		return ToolTipTools.getTooltipForPositionMapping1D(p, p.getY(),
				getPositionEncodingFunction().inverseMapping(p.getY()), chartRectangle);
	}

	@Override
	public List<Double> getElementsInRectangle(RectangularShape rectangle) {
		if (rectangle == null)
			return null;

		IPositionEncodingFunction f = getPositionEncodingFunction();
		Number v1 = f.inverseMapping(rectangle.getMinY());
		Number v2 = f.inverseMapping(rectangle.getMaxY());

		double[] values = dataStatistics != null ? dataStatistics.getValues() : null;
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
