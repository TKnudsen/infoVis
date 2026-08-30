package com.github.TKnudsen.infoVis.view.painters.buttons;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.github.TKnudsen.infoVis.view.painters.bufferedImage.BufferedImagePainter;

/**
 * <p>
 * A {@link ButtonPainter} whose icon is any caller-supplied
 * {@link BufferedImage} -- no dependency on a specific icon file.
 * </p>
 *
 * @version 1.0
 * @since 2017
 */
public class IconBasedButtonPainter extends ButtonPainter {

	private final BufferedImagePainter bufferedImagePainter;

	public IconBasedButtonPainter(Color focusedColor, Color notFocusedColor, BufferedImage icon) {
		super(focusedColor, notFocusedColor);

		this.bufferedImagePainter = new BufferedImagePainter(icon);
		this.bufferedImagePainter.setBackgroundPaint(null);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (rectangle == null)
			return;

		bufferedImagePainter.draw(g2);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		bufferedImagePainter.setRectangle(rectangle);
	}

}
