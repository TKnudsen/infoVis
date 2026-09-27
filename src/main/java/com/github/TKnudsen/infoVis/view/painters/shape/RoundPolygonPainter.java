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
 * @version 1.0
 * @since 2026
 */
public class RoundPolygonPainter extends ChartPainter {

	private final int edges;

	private RegularPolygon regularPolygon;
	private RoundPolygon roundPolygon;

	public RoundPolygonPainter(int edges) {
		this.edges = edges;
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
		int r = (int) (Math.min(rectangle.getWidth(), rectangle.getHeight()) * 0.5);
		double startAngle = -Math.PI / 2;
		int arcWidth = (int) (r * 0.2);

		regularPolygon = new RegularPolygon(x, y, r, edges, startAngle);
		roundPolygon = new RoundPolygon(regularPolygon, arcWidth);
	}

	public RoundPolygon getData() {
		return roundPolygon;
	}

	public int getEdges() {
		return edges;
	}
}
