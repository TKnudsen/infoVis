package com.github.TKnudsen.infoVis.view.painters.buttons;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import com.github.TKnudsen.infoVis.view.interaction.IFocusedStatus;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * <p>
 * Base painter for small, focus-aware buttons (see {@link MinusPainter},
 * {@link PlusPainter}).
 * </p>
 *
 * @version 1.1
 * @since 2017
 */
public abstract class ButtonPainter extends ChartPainter implements IFocusedStatus {

	private boolean focused;
	private final Color focusedColor;
	private final Color notFocusedColor;

	public ButtonPainter(Color focusedColor, Color notFocusedColor) {
		this.focusedColor = focusedColor;
		this.notFocusedColor = notFocusedColor;

		this.focused = false;
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (rectangle == null)
			return;

		Color c = g2.getColor();
		Stroke stroke = g2.getStroke();

		if (focused) {
			g2.setColor(focusedColor);
			g2.fill(rectangle);
		} else {
			g2.setColor(notFocusedColor);
			g2.fill(rectangle);
		}

		g2.setColor(c);
		g2.setStroke(stroke);
	}

	@Override
	public boolean isFocused() {
		return focused;
	}

	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	public Color getFocusedColor() {
		return focusedColor;
	}

	public Color getNotFocusedColor() {
		return notFocusedColor;
	}

}
