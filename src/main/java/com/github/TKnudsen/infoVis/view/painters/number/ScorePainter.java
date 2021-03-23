package com.github.TKnudsen.infoVis.view.painters.number;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

import javafx.geometry.Orientation;

/**
 * Paints a score between 0 and 1 as bar.
 * 
 * @author Christian Ritter
 *
 */
public class ScorePainter extends ChartPainter {

	private Color color;
	private Rectangle2D rect;
	private double score;
	private final Orientation orientation;

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
	 * @param score the score to be paint in a range of [0,1]
	 * @param color the color of the bar
	 */
	public ScorePainter(double score, Color color, Orientation orientation) {
		this.score = score;
		this.color = color;
		this.orientation = orientation;

		calcRect();
	}

	private void calcRect() {
		if (rectangle != null) {
			rect = new Rectangle2D.Double();

			if (this.orientation.equals(Orientation.HORIZONTAL))
				rect.setRect(this.rectangle.getX(), this.rectangle.getY(), this.rectangle.getWidth() * score,
						this.rectangle.getHeight());
			else
				rect.setRect(this.rectangle.getX(), this.rectangle.getMaxY() - this.rectangle.getHeight() * score,
						this.rectangle.getWidth(), this.rectangle.getHeight() * score);
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);
		Color c = g2.getColor();
		g2.setColor(color);
		if (rect != null)
			g2.fill(rect);

		if (isDrawOutline())
			DisplayTools.drawRectangle(g2, chartRectangle, getBorderPaint());

		g2.setColor(c);
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
		super.setRectangle(rectangle);
		calcRect();
	}

	public void setScore(double score) {
		this.score = score;
		calcRect();
	}

}
