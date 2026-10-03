package com.github.TKnudsen.infoVis.view.painters.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.shape.shapes.RegularPolygon;

/**
 * Draws a regular N-sided polygon inscribed in the painter's rectangle.
 *
 * @version 1.0
 * @since 2018
 */
public class RegularPolygonPainter extends ChartPainter {

	private final int edges;

	private RegularPolygon regularPolygon;

	public RegularPolygonPainter(int edges) {
		this.edges = edges;
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		Color color = g2.getColor();

		if (regularPolygon != null)
			g2.draw(regularPolygon);

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
		if (getRectangle() == null)
			return;

		int x = (int) getRectangle().getCenterX();
		int y = (int) getRectangle().getCenterY();
		int r = (int) (Math.min(getRectangle().getWidth(), getRectangle().getHeight()) * 0.5);
		double startAngle = -Math.PI / 2;

		regularPolygon = new RegularPolygon(x, y, r, edges, startAngle);
	}

	public Polygon getData() {
		return regularPolygon;
	}

	public int getEdges() {
		return edges;
	}
}
