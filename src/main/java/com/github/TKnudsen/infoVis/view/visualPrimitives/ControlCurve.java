package com.github.TKnudsen.infoVis.view.visualPrimitives;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;

/**
 * A curve defined by a sequence of control points.
 *
 * @version 1.1 -- dropped the unused interactive-editing API (selectPoint,
 *          setPoint, removePoint, numerOfPoints): nothing outside this class
 *          ever called it.
 * @since 2026
 */
public class ControlCurve {

	protected Polygon pts;

	public ControlCurve() {
		pts = new Polygon();
	}

	static Font f = new Font("Courier", Font.PLAIN, 12);

	public void paint(Graphics g) {
		FontMetrics fm = g.getFontMetrics(f);
		g.setFont(f);
		int h = fm.getAscent() / 2;

		for (int i = 0; i < pts.npoints; i++) {
			String s = Integer.toString(i);
			int w = fm.stringWidth(s) / 2;
			g.drawString(Integer.toString(i), pts.xpoints[i] - w, pts.ypoints[i] + h);
		}
	}

	public int addPoint(int x, int y) {
		pts.addPoint(x, y);
		return pts.npoints - 1;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < pts.npoints; i++) {
			result.append(" " + pts.xpoints[i] + " " + pts.ypoints[i]);
		}
		return result.toString();
	}
}
