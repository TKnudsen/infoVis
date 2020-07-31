package com.github.TKnudsen.infoVis.view.painters.primitives;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class CirclePainter extends ChartPainter {

	private Rectangle2D.Double square = new Rectangle2D.Double();
	private Arc2D.Double arc = new Arc2D.Double();

	protected boolean fill = true;

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		double width = rectangle.getWidth();
		double height = rectangle.getHeight();
		double x = rectangle.getX();
		double y = rectangle.getY();

		double edgeLength = Math.min(width, height) - 2;

		square.setRect(x + (width - edgeLength) * 0.5, y + (height - edgeLength) * 0.5, edgeLength, edgeLength);
		arc.setArc(square, 90, 360, Arc2D.OPEN);
	}

	public void draw(Graphics2D g2) {
		super.draw(g2);

		Color c = g2.getColor();

		if (fill && rectangle != null && getPaint() != null) {
			g2.setPaint(getPaint());
			g2.fill(arc);
		}

		if (isDrawOutline() && rectangle != null) {
			g2.setPaint(getBorderPaint());
			g2.draw(arc);
		}

		g2.setColor(c);
	}

	public boolean isFill() {
		return fill;
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}
}
