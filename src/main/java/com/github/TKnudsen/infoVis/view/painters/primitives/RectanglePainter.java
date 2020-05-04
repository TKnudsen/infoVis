package com.github.TKnudsen.infoVis.view.painters.primitives;

import java.awt.Color;
import java.awt.Graphics2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 *
 */
public class RectanglePainter extends ChartPainter {

	private boolean fill = true;

	public void draw(Graphics2D g2) {
		super.draw(g2);

		Color c = g2.getColor();

		if (fill && rectangle != null && getPaint() != null) {
			g2.setPaint(getPaint());
			g2.fill(rectangle);
		}

		if (isDrawOutline() && rectangle != null) {
			g2.setPaint(getBorderPaint());
			g2.draw(rectangle);
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
