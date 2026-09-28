package com.github.TKnudsen.infoVis.view.painters.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.shape.shapes.StarPolygon;

/**
 * Draws an N-pointed star polygon inscribed in the painter's rectangle.
 *
 * @version 1.0
 * @since 2018
 */
public class StarPolygonPainter extends ChartPainter {

	private final int edges;

	private StarPolygon starPolygon;

	public StarPolygonPainter(int edges) {
		this.edges = edges;
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		Color color = g2.getColor();

		if (starPolygon != null)
			g2.draw(starPolygon);

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
		int innerR = (int) (Math.max(1, r * 0.4));
		double startAngle = -Math.PI / 2;

		starPolygon = new StarPolygon(x, y, r, innerR, edges, startAngle);
	}

	public Polygon getData() {
		return starPolygon;
	}

	public int getEdges() {
		return edges;
	}
}
