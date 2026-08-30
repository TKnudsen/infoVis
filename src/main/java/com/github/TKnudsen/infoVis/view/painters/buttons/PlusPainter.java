package com.github.TKnudsen.infoVis.view.painters.buttons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import com.github.TKnudsen.infoVis.view.painters.primitives.CirclePainter;
import com.github.TKnudsen.infoVis.view.tools.BasicStrokeTools;

/**
 * @version 1.0
 * @since 2017
 */
public class PlusPainter extends ButtonPainter {

	private boolean drawCircle = false;
	private final CirclePainter circlePainter;
	private final Color plusColor;

	public PlusPainter(Color focusedColor, Color notFocusedColor, Color plusColor) {
		super(focusedColor, notFocusedColor);

		this.plusColor = plusColor;
		setBackgroundPaint(null);
		setDrawOutline(false);

		circlePainter = new CirclePainter();
		circlePainter.setPaint(new Color(213, 213, 213));
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (rectangle == null)
			return;

		if (drawCircle)
			circlePainter.draw(g2);

		double width = rectangle.getWidth();
		double space = Math.min(width, rectangle.getHeight());
		float strokeWidth = (float) (0.15 * space);
		float lineWidth = (float) (width * 0.8);
		if (drawCircle)
			lineWidth = (float) (width * 0.6);

		Stroke s = g2.getStroke();
		Color c = g2.getColor();

		if (isDrawOutline()) {
			g2.setPaint(getBorderPaint());
			g2.draw(rectangle);
		}

		g2.setStroke(BasicStrokeTools.get(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setColor(plusColor);

		Line2D horz = new Line2D.Double(rectangle.getCenterX() - lineWidth * 0.5, rectangle.getCenterY(),
				rectangle.getCenterX() + lineWidth * 0.5, rectangle.getCenterY());
		Line2D vert = new Line2D.Double(rectangle.getCenterX(), rectangle.getCenterY() - lineWidth * 0.5,
				rectangle.getCenterX(), rectangle.getCenterY() + lineWidth * 0.5);

		g2.draw(horz);
		g2.draw(vert);

		g2.setStroke(s);
		g2.setColor(c);
	}

}
