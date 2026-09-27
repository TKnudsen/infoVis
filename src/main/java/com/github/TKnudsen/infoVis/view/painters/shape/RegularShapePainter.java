package com.github.TKnudsen.infoVis.view.painters.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Draws a regular N-sided polygon like {@link RegularPolygonPainter}, but
 * built from a double-precision {@link Path2D} instead of an int-precision
 * {@link Polygon}, and also exposes that int-precision polygon for callers
 * that need it (e.g. hit-testing).
 *
 * @version 1.0
 * @since 2026
 */
public class RegularShapePainter extends ChartPainter {

	private final int edges;

	private Polygon polygonIntVariant;
	private Path2D pathDoubleVariant;

	public RegularShapePainter(int edges) {
		this.edges = edges;
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		Color color = g2.getColor();

		if (pathDoubleVariant != null)
			g2.draw(pathDoubleVariant);

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
		polygonIntVariant = new Polygon();
		pathDoubleVariant = new Path2D.Double();

		for (int i = 0; i < edges; i++) {
			double x = (chartRectangle.getWidth() / 2);
			double y = (chartRectangle.getHeight() / 2);
			double radius = (Math.min(chartRectangle.getWidth(), chartRectangle.getHeight()) * 0.5);
			double angle = 2 * Math.PI / edges;

			polygonIntVariant.addPoint((int) (x + radius * Math.cos(i * angle)),
					(int) (y - radius * Math.sin(i * angle)));

			if (pathDoubleVariant.getCurrentPoint() == null)
				pathDoubleVariant.moveTo((x + radius * Math.cos(i * angle)), (y - radius * Math.sin(i * angle)));
			else
				pathDoubleVariant.lineTo((x + radius * Math.cos(i * angle)), (y - radius * Math.sin(i * angle)));
		}
		pathDoubleVariant.closePath();
	}

	public Path2D getData() {
		return pathDoubleVariant;
	}

	public Polygon getPolygon() {
		return polygonIntVariant;
	}

	public Path2D getPath() {
		return pathDoubleVariant;
	}

	public int getEdges() {
		return edges;
	}
}
