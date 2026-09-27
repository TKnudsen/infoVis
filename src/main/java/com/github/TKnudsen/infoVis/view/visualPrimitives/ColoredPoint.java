package com.github.TKnudsen.infoVis.view.visualPrimitives;

import java.awt.Color;
import java.awt.Point;

/**
 * A {@link Point} that carries its own {@link Color}.
 *
 * @version 1.0
 * @since 2026
 */
public class ColoredPoint extends Point {

	private static final long serialVersionUID = -7813291871934374815L;

	public Color color = Color.BLACK;

	public ColoredPoint(Color c) {
		super();
		this.color = c;
	}

	public ColoredPoint(Point p, Color c) {
		super(p);
		this.color = c;
	}

	public ColoredPoint(int x, int y, Color c) {
		super(x, y);
		this.color = c;
	}

	@Override
	public String toString() {
		return "X: " + this.x + ", Y: " + this.y + ", Color: " + this.color.toString();
	}
}
