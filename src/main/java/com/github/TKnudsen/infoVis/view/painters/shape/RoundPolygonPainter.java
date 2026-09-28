package com.github.TKnudsen.infoVis.view.painters.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.shape.shapes.RegularPolygon;
import com.github.TKnudsen.infoVis.view.painters.shape.shapes.RoundPolygon;

/**
 * Draws a regular N-sided polygon with rounded corners (see
 * {@link RoundPolygon}), inscribed in the painter's rectangle.
 *
 * @version 1.2 -- corner-rounding arc width is now configurable via
 *          {@link #setArcWidthRatio(double)} instead of a hard-coded 0.2;
 *          the underlying circumradius is now inflated to compensate for the
 *          rounding, so the rounded outline's own peak reaches the target
 *          radius instead of falling short of it (a quadratic Bezier corner
 *          cut never reaches its control point -- the original sharp
 *          vertex -- so without this compensation the rounded shape was
 *          always visibly smaller than its sharp-cornered counterpart)
 * @since 2018
 */
public class RoundPolygonPainter extends ChartPainter {

	private final int edges;

	/** fraction of the polygon's radius used as the corner-rounding arc width */
	private double arcWidthRatio = 0.2;

	private RegularPolygon regularPolygon;
	private RoundPolygon roundPolygon;

	public RoundPolygonPainter(int edges) {
		this.edges = edges;
	}

	public RoundPolygonPainter(int edges, double arcWidthRatio) {
		this.edges = edges;

		setArcWidthRatio(arcWidthRatio);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		Color color = g2.getColor();

		if (roundPolygon != null)
			g2.draw(roundPolygon);

		g2.setColor(color);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		calculatePolygon();
	}

	protected void calculatePolygon() {
		if (rectangle == null)
			return;

		int x = (int) rectangle.getCenterX();
		int y = (int) rectangle.getCenterY();
		double targetRadius = Math.min(rectangle.getWidth(), rectangle.getHeight()) * 0.5;
		double startAngle = -Math.PI / 2;

		// A quadratic-Bezier corner cut never reaches its control point (the
		// original sharp vertex), so rounding a polygon with circumradius
		// targetRadius produces a shape whose own peak distance from center is
		// only targetRadius * (1 - 0.5 * arcWidthRatio * sin(pi/edges)). Inflate
		// the circumradius fed into RegularPolygon by the inverse of that factor
		// so the ROUNDED outline's peak lands back on targetRadius.
		double correctionFactor = 1 - 0.5 * arcWidthRatio * Math.sin(Math.PI / edges);
		int r = (int) (targetRadius / Math.max(0.05, correctionFactor));
		int arcWidth = (int) (r * arcWidthRatio);

		regularPolygon = new RegularPolygon(x, y, r, edges, startAngle);
		roundPolygon = new RoundPolygon(regularPolygon, arcWidth);
	}

	public RoundPolygon getData() {
		return roundPolygon;
	}

	public int getEdges() {
		return edges;
	}

	/**
	 * @return the corner-rounding arc width, as a fraction of the polygon's radius
	 *         (0 = sharp corners, larger = rounder)
	 */
	public double getArcWidthRatio() {
		return arcWidthRatio;
	}

	public void setArcWidthRatio(double arcWidthRatio) {
		if (arcWidthRatio < 0)
			throw new IllegalArgumentException("RoundPolygonPainter: arcWidthRatio must not be negative");

		this.arcWidthRatio = arcWidthRatio;

		calculatePolygon();
	}
}
