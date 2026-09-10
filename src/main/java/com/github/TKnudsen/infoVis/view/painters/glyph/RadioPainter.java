package com.github.TKnudsen.infoVis.view.painters.glyph;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * Renders a radio-button glyph (a circle, optionally filled to indicate the
 * ticked state, followed by a text label) within its rectangle's left edge --
 * the label extends past the circle to the right, so the rectangle's height
 * determines the circle's size while its width should accommodate
 * {@link #getPreferredWidth()}.
 *
 * <p>
 * This class only draws the glyph and tracks its own ticked state; grouping
 * several instances into mutually-exclusive "radio button" behavior (only
 * one ticked at a time) is left to the caller -- see
 * {@link com.github.TKnudsen.infoVis.view.painters.glyph.test.RadioPainterTester
 * RadioPainterTester} for an example.
 * </p>
 *
 * @version 1.03
 * @since 2012
 */
public class RadioPainter extends ChartPainter {

	private static final int OFFSET = 3;
	private static final float FONT_SIZE = 12f;
	private static final double TICK_MARK_INSET_FRACTION = 0.125;
	private static final double TICK_MARK_SIZE_FRACTION = 0.75;

	private final String description;

	private boolean ticked = false;

	private int preferredWidth = -1;

	public RadioPainter(String description) {
		this.description = description;

		setBackgroundPaint(Color.WHITE);
		setBorderPaint(Color.WHITE);
		setColor(Color.BLACK);
		stroke = DisplayTools.mediumStroke;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (rectangle == null)
			return;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(getBackgroundPaint());
		g2.fill(rectangle);
		g2.setPaint(getPaint());

		double x = rectangle.getX();
		double y = rectangle.getY();
		double h = rectangle.getHeight();

		Ellipse2D outline = new Ellipse2D.Double(x + OFFSET, y + OFFSET, h - 2 * OFFSET, h - 2 * OFFSET);

		if (ticked) {
			double inset = OFFSET + TICK_MARK_INSET_FRACTION * (h - OFFSET);
			double size = TICK_MARK_SIZE_FRACTION * (h - 2 * OFFSET);
			g2.fill(new Ellipse2D.Double(x + inset, y + inset, size, size));
		}

		g2.setFont(g2.getFont().deriveFont(FONT_SIZE));
		FontMetrics metrics = g2.getFontMetrics();
		preferredWidth = (int) h + OFFSET + metrics.stringWidth(description);

		g2.setStroke(stroke);
		g2.draw(outline);
		g2.drawString(description, (float) (h + OFFSET), (float) (y + OFFSET + h * 0.5));
	}

	/**
	 * @return the glyph's preferred pixel width (circle diameter + label width),
	 *         valid only after at least one {@link #draw(Graphics2D)} call --
	 *         the label width depends on font metrics only available at draw time
	 */
	public int getPreferredWidth() {
		return preferredWidth;
	}

	public boolean isTicked() {
		return ticked;
	}

	public void tick() {
		ticked = true;
	}

	public void untick() {
		ticked = false;
	}

}
