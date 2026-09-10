package com.github.TKnudsen.infoVis.view.painters.glyph.primitives;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Layers several {@link TrendPainter}s over the same rectangle, one per
 * trend value -- e.g. to compare a short-term and a long-term trend arrow
 * side by side in the same glyph.
 *
 * @version 1.0
 * @since 2014
 */
public class TrendBundlePainter extends ChartPainter {

	private final List<Double> trends;
	private final List<TrendPainter> trendPainters = new ArrayList<>();

	public TrendBundlePainter(List<Double> trends) {
		this.trends = trends;

		for (Double trend : trends) {
			TrendPainter trendPainter = new TrendPainter(trend);
			trendPainter.setRectangle(rectangle);
			trendPainter.setStroke(stroke);
			trendPainter.setBackgroundPaint(null);
			trendPainters.add(trendPainter);
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (rectangle != null && getBackgroundPaint() != null) {
			g2.setPaint(getBackgroundPaint());
			g2.fill(rectangle);
		}

		for (TrendPainter trendPainter : trendPainters)
			trendPainter.draw(g2);
	}

	@Override
	public void setPaint(Paint paint) {
		super.setPaint(paint);

		for (TrendPainter trendPainter : trendPainters)
			trendPainter.setPaint(paint);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		for (TrendPainter trendPainter : trendPainters)
			trendPainter.setRectangle(rectangle);
	}

	public List<Double> getTrends() {
		return trends;
	}

}
