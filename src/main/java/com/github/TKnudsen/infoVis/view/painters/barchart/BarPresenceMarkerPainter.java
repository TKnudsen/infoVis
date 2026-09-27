package com.github.TKnudsen.infoVis.view.painters.barchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoding;

/**
 * <p>
 * Draws a short marker line at each non-zero bar's baseline -- a visible
 * "there is a real, nonzero value here" tick, independent of whether the bar
 * itself is drawn tall enough, or opaque enough, to show that on its own.
 * Most useful for near-zero bars, or bars drawn semi-transparent/overlaid as
 * part of a
 * {@link com.github.TKnudsen.infoVis.view.panels.barchart.BarChartsValueBased#createLayeredBarChart
 * layered chart}, where the bar's own fill/outline alone can fail to
 * communicate that the value is nonzero.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class BarPresenceMarkerPainter extends ChartPainter {

	private final BarChartPainter barChartPainter;
	private final boolean vertical;

	private Paint markerPaint = Color.BLACK;
	private BasicStroke markerStroke = DisplayTools.standardStroke;

	public BarPresenceMarkerPainter(BarChartPainter barChartPainter) {
		if (barChartPainter == null)
			throw new IllegalArgumentException("BarPresenceMarkerPainter: barChartPainter must not be null");
		if (!(barChartPainter instanceof IYPositionEncoding) && !(barChartPainter instanceof IXPositionEncoding))
			throw new IllegalArgumentException(
					"BarPresenceMarkerPainter: barChartPainter must be a BarChartVerticalPainter or BarChartHorizontalPainter");

		this.barChartPainter = barChartPainter;
		this.vertical = barChartPainter instanceof IYPositionEncoding;

		this.setBackgroundPaint(null);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (chartRectangle == null)
			return;

		List<Rectangle2D> barRectangles = barChartPainter.getBarRectangles();
		List<? extends Number> data = barChartPainter.getData();
		IPositionEncodingFunction positionEncoding = barChartPainter.getPositionEncodingFunction();
		double baselinePixel = positionEncoding.apply(barChartPainter.getMinValue());

		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();

		g2.setPaint(markerPaint);
		g2.setStroke(markerStroke);

		for (int i = 0; i < barRectangles.size() && i < data.size(); i++) {
			Rectangle2D bar = barRectangles.get(i);
			Number value = data.get(i);
			if (bar == null || value == null || value.doubleValue() == 0)
				continue;

			if (vertical)
				DisplayTools.drawLine(g2, bar.getMinX(), baselinePixel, bar.getMaxX(), baselinePixel);
			else
				DisplayTools.drawLine(g2, baselinePixel, bar.getMinY(), baselinePixel, bar.getMaxY());
		}

		g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);
	}

	public Paint getMarkerPaint() {
		return markerPaint;
	}

	public void setMarkerPaint(Paint markerPaint) {
		this.markerPaint = markerPaint;
	}

	public BasicStroke getMarkerStroke() {
		return markerStroke;
	}

	public void setMarkerStroke(BasicStroke markerStroke) {
		this.markerStroke = markerStroke;
	}

}
