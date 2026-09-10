package com.github.TKnudsen.infoVis.view.painters.string;

import java.awt.Graphics2D;
import java.awt.Paint;

import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * A {@link StringPainter} with a rounded-cube (oval-like) outline drawn
 * behind the text, filling its own background paint if set -- e.g. for a
 * count badge or a pill-shaped label.
 *
 * @version 1.0
 * @since 2016
 */
public class StringOvalOutlinePainter extends StringPainter {

	private static final double ARC_FRACTION = 0.25;

	private Paint ovalBackgroundPaint = null;

	public StringOvalOutlinePainter(String string) {
		super(string);

		setBackgroundPaint(null);
	}

	@Override
	public void draw(Graphics2D g2) {
		if (chartRectangle != null) {
			double arc = Math.min(chartRectangle.getWidth() * ARC_FRACTION, chartRectangle.getHeight() * ARC_FRACTION);
			DisplayTools.drawCube(g2, chartRectangle, arc, arc, ovalBackgroundPaint, DisplayTools.standardStroke,
					getBorderPaint());
		}

		super.draw(g2);
	}

	public Paint getOvalBackgroundPaint() {
		return ovalBackgroundPaint;
	}

	public void setOvalBackgroundPaint(Paint ovalBackgroundPaint) {
		this.ovalBackgroundPaint = ovalBackgroundPaint;
	}

}
