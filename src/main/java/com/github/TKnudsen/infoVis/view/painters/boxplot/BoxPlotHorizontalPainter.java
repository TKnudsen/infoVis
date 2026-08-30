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
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;

/**
 * <p>
 * Paints a horizontal boxplot using optimized, float-based drawing routines.
 * </p>
 *
 * @version 2.1 (refactored for performance)
 * @since 2016
 */
public class BoxPlotHorizontalPainter extends BoxPlotPainter implements IXPositionEncoding {

	public BoxPlotHorizontalPainter(double[] data) {
		super(data);
	}

	public BoxPlotHorizontalPainter(Collection<? extends Number> data) {
		super(data);
	}

	public BoxPlotHorizontalPainter(StatisticsSupport dataStatistics) {
		super(dataStatistics);
	}

	@Override
	protected void updatePositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		IPositionEncodingFunction f = getPositionEncodingFunction();
		f.setMinPixel(rectangle.getMinX());
		f.setMaxPixel(rectangle.getMaxX());
	}

	@Override
	protected void drawMedian(Graphics2D g2) {
		if (chartRectangle == null || chartRectangle.getHeight() <= 0)
			return;

		final Stroke oldStroke = g2.getStroke();

		// Scale stroke width based on chart width
		final float strokeWidth = (float) Math.min(5f, Math.max(1f, chartRectangle.getWidth() * 0.01));
		final Stroke medianStroke = BasicStrokeTools.get(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

		g2.setStroke(medianStroke);
		g2.setPaint(getPaint());

		// Draw vertical median line
		final float x = (float) medScreen;
		final float y1 = (float) chartRectangle.getMinY();
		final float y2 = (float) chartRectangle.getMaxY();

		DisplayTools.drawLine(g2, x, y1, x, y2);

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

		final float y = (float) chartRectangle.getCenterY();

		// Left connector
		DisplayTools.drawLine(g2, (float) lowerWhiskerScreen, y, (float) lowerQuartileScreen, y);

		// Right connector
		DisplayTools.drawLine(g2, (float) upperQuartileScreen, y, (float) upperWhiskerScreen, y);

		g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);
	}

	@Override
	protected void drawOutliers(Graphics2D g2) {
		if (chartRectangle == null || outlierScreenCoordinates == null || outlierScreenCoordinates.length == 0)
			return;

		// Pre-compute constants and reuse paint
		final int centerY = (int) Math.round(chartRectangle.getCenterY());
		final int radius = (int) Math.max(1, Math.min(chartRectangle.getHeight() * 0.15, stroke.getLineWidth() * 4));

		final Paint oldPaint = g2.getPaint();
		g2.setPaint(getBorderPaint());

		for (double x : outlierScreenCoordinates)
			DisplayTools.drawPoint(g2, (int) Math.round(x), centerY, radius, false);

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

		final float x = (float) ratioOfAxis;
		final float y1 = (float) chartRectangle.getMinY();
		final float y2 = (float) chartRectangle.getMaxY();

		DisplayTools.drawLine(g2, x, y1, x, y2);

		g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);
	}

	@Override
	Rectangle2D calculateQuartilesRectangle() {
		return new Rectangle2D.Double(Math.min(upperQuartileScreen, lowerQuartileScreen), chartRectangle.getMinY(),
				Math.abs(upperQuartileScreen - lowerQuartileScreen), chartRectangle.getHeight());
	}

	@Override
	public void setXPositionEncodingFunction(IPositionEncodingFunction xPositionEncodingFunction) {
		this.setPositionEncodingFunction(xPositionEncodingFunction);
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!isToolTipping() || p == null)
			return null;

		return ToolTipTools.getTooltipForPositionMapping1D(p, p.getX(),
				getPositionEncodingFunction().inverseMapping(p.getX()), chartRectangle);
	}

	@Override
	public List<Double> getElementsInRectangle(RectangularShape rectangle) {
		if (rectangle == null)
			return null;

		IPositionEncodingFunction f = getPositionEncodingFunction();
		Number v1 = f.inverseMapping(rectangle.getMinX());
		Number v2 = f.inverseMapping(rectangle.getMaxX());

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
		return false;
	}

}
