package com.github.TKnudsen.infoVis.view.painters.buttons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import com.github.TKnudsen.infoVis.view.tools.BasicStrokeTools;

/**
 * @version 1.0
 * @since 2012
 */
public class CloseButtonPainter extends ButtonPainter {

	public CloseButtonPainter(Color focusedColor, Color notFocusedColor) {
		super(focusedColor, notFocusedColor);

		setDrawOutline(false);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (rectangle == null)
			return;

		Color c = g2.getColor();
		Stroke stroke = g2.getStroke();

		float strokeWidth = (float) (rectangle.getWidth() * 0.15);

		g2.setColor(Color.WHITE);
		g2.setStroke(BasicStrokeTools.get(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		g2.drawLine((int) (rectangle.getX() + 1.5 * strokeWidth), (int) (rectangle.getY() + 1.5 * strokeWidth),
				(int) (rectangle.getX() + rectangle.getWidth() - 1.5 * strokeWidth),
				(int) (rectangle.getY() + rectangle.getHeight() - 1.5 * strokeWidth));
		g2.drawLine((int) (rectangle.getX() + rectangle.getWidth() - 1.5 * strokeWidth),
				(int) (rectangle.getY() + 1.5 * strokeWidth), (int) (rectangle.getX() + 1.5 * strokeWidth),
				(int) (rectangle.getY() + rectangle.getHeight() - 1.5 * strokeWidth));

		if (drawOutline) {
			g2.setPaint(getBorderPaint());
			g2.setStroke(BasicStrokeTools.get(1));
			g2.draw(rectangle);
		}

		g2.setColor(c);
		g2.setStroke(stroke);
	}

}
