package com.github.TKnudsen.infoVis.view.painters.number;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.ui.Orientation;

/**
 * <p>
 * Paints a score between 0 and 1 as bar. Optimized for frequent redraws (e.g.,
 * in tables or dashboards).
 * </p>
 *
 * @author Christian Ritter
 */
public class ScorePainter extends ChartPainter {

	private static final long serialVersionUID = 1L;

	private Color color;
	private final Orientation orientation;

	private double score;
	private final Rectangle2D barRect = new Rectangle2D.Double();

	/**
	 * 
	 * @param score the score to be paint in a range of [0,1]
	 * @param color the color of the bar
	 */
	public ScorePainter(double score, Color color) {
		this(score, color, Orientation.HORIZONTAL);
	}

	/**
	 * 
	 * @param score       the score to be paint in a range of [0,1]
	 * @param color       the color of the bar
	 * @param orientation orientation
	 */
	public ScorePainter(double score, Color color, Orientation orientation) {
		this.color = color;
		this.orientation = orientation;

		setScore(score); // automatically clamps and recalculates
	}

	/**
	 * Recalculate the bar rectangle relative to the assigned chart rectangle.
	 */
	private void calcRect() {
		Rectangle2D rect = getRectangle();
		if (rect == null)
			return;

		final double w = rect.getWidth();
		final double h = rect.getHeight();
		final double x = rect.getX();
		final double y = rect.getY();

		if (orientation == Orientation.HORIZONTAL) {
			barRect.setRect(x, y, w * score, h);
		} else { // VERTICAL
			double filledHeight = h * score;
			barRect.setRect(x, y + (h - filledHeight), w, filledHeight);
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		if (barRect == null || color == null)
			return;

		// enable anti-aliasing for smoother edges
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setClip(getRectangle()); // enforce clipping

		final Color oldColor = g2.getColor();
		g2.setColor(color);
		g2.fill(barRect);

		if (isDrawOutline()) {
			g2.setPaint(getBorderPaint());
			DisplayTools.drawRectangle(g2, getRectangle());
		}

		g2.setColor(oldColor);
	}

	public Color getColor() {
		return color;
	}

	public Double getData() {
		return score;
	}

	public double getScore() {
		return score;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		if (rectangle == null)
			return;
		super.setRectangle(rectangle);
		calcRect();
	}

	/**
	 * Sets score and updates rectangle. Clamps to [0,1] for safety.
	 */
	public void setScore(double score) {
		double clamped = Math.max(0.0, Math.min(1.0, score));
		if (this.score != clamped) {
			this.score = clamped;
			calcRect();
		}
	}

}
