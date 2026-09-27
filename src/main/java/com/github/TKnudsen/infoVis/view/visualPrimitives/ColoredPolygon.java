package com.github.TKnudsen.infoVis.view.visualPrimitives;

import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;

/**
 * A {@link Polygon} whose vertices each carry their own {@link Color}, so a
 * painter can render a polygon outline/fill with a per-vertex color gradient
 * rather than a single uniform color.
 *
 * @version 1.0
 * @since 2026
 */
public class ColoredPolygon extends Polygon {

	private static final long serialVersionUID = 3997048156912426313L;

	private ArrayList<Color> colors;

	public ColoredPolygon() {
		super();
		colors = new ArrayList<Color>();
	}

	public ColoredPolygon(Polygon p) {
		super(p.xpoints, p.ypoints, p.npoints);
		for (int i = 0; i < p.xpoints.length; i++)
			colors.add(Color.BLACK);
	}

	public void addPoint(int x, int y, Color c) {
		super.addPoint(x, y);
		colors.add(c);
	}

	@Override
	public void addPoint(int x, int y) {
		super.addPoint(x, y);
		if (colors.size() > 0)
			colors.add(colors.get(colors.size()));
		else
			colors.add(Color.BLACK);
	}

	public Color getColor(int i) {
		if (i <= npoints)
			return colors.get(i);
		return null;
	}

	public ArrayList<Color> getColors() {
		return colors;
	}

	public void setColor(int i, Color c) {
		if (i < npoints)
			colors.set(i, c);
	}

	public ColoredPoint getPoint(int i) {
		if (i <= npoints)
			return new ColoredPoint(xpoints[i], ypoints[i], colors.get(i));
		return null;
	}

	@Override
	public String toString() {
		return "Size: " + npoints;
	}
}
