package com.github.TKnudsen.infoVis.view.painters.string;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Draws a String object.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.14
 */
public class StringPainter extends ChartPainter implements ITooltip {

	protected String string;

	private boolean verticalOrientation = false;
	private int offset = 2;
	private HorizontalStringAlignment horizontalStringAlignment = HorizontalStringAlignment.CENTER;
	private VerticalStringAlignment verticalStringAlignment = VerticalStringAlignment.CENTER;

	private boolean toolTipping = true;

	public StringPainter(String string) {
		if (string == null)
			this.string = "";
		else
			this.string = string;
	}

	public enum HorizontalStringAlignment {
		LEFT, CENTER, RIGHT
	}

	public enum VerticalStringAlignment {
		UP, CENTER, DOWN
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (rectangle == null)
			return;

		Color c = g2.getColor();
		g2.setColor(fontColor);
		Font f = g2.getFont();
		g2.setFont(font);
		Stroke str = g2.getStroke();
		g2.setStroke(stroke);

		FontMetrics fm = g2.getFontMetrics();

		String s = string;
		double verticalOffset = 0;
		if (!verticalOrientation) {
			while (fm.stringWidth(s) > this.rectangle.getWidth() - 2 * offset && s.length() > 1)
				s = s.substring(0, s.length() - 1);
		} else {
			while (fm.stringWidth(s) > this.rectangle.getHeight() - 2 * offset && s.length() > 1)
				s = s.substring(0, s.length() - 1);
		}

		// position
		switch (horizontalStringAlignment) {
		case LEFT:
			verticalOffset = offset;
			break;
		case CENTER:
			verticalOffset = (rectangle.getWidth() - fm.stringWidth(s)) / 2;
			break;
		case RIGHT:
			verticalOffset = rectangle.getWidth() - fm.stringWidth(s) - offset;
			break;
		default:
			break;
		}

		if (!verticalOrientation) {
			double additionalHeightOffset = 0;
			double space = 0;
			if (!verticalOrientation)
				space = rectangle.getHeight();
			else
				space = rectangle.getWidth();
			if (space > 0.0)
				switch (verticalStringAlignment) {
				case UP:
					additionalHeightOffset = -space * 0.42;
					break;
				case CENTER:
					additionalHeightOffset = 0;
					break;
				case DOWN:
					additionalHeightOffset = +space * 0.42;
					break;
				default:
					break;
				}

			g2.drawString(s, (int) Math.round(rectangle.getMinX() + verticalOffset),
					(int) (rectangle.getCenterY() + font.getSize() * 0.5 + additionalHeightOffset));
		} else {
			double xo = rectangle.getWidth() - font.getSize();
			double yo = fm.stringWidth(s);
			switch (verticalStringAlignment) {
			case UP:
				yo += 2 * offset;
				break;
			case CENTER:
				yo = rectangle.getHeight() * 0.5 + fm.stringWidth(s) * 0.5;
				break;
			case DOWN:
				yo = rectangle.getHeight() - 2 * offset;
				break;
			default:
				break;
			}

			DisplayTools.drawRotatedString(g2, s, (int) (rectangle.getX() + xo * 0.5 + getFontSize()), (float) yo,
					-Math.PI / 2);
		}

		if (drawOutline)
			DisplayTools.drawRectangle(g2, rectangle, getBorderPaint());

		g2.setStroke(str);
		g2.setFont(f);
		g2.setColor(c);
	}

	public void setData(String string) {
		if (string == null)
			this.string = "";
		else
			this.string = string;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (isToolTipping()) {
			if (this.rectangle != null && this.rectangle.contains(p)) {
				StringPainter sp = new StringPainter(this.string);
				sp.setRectangle(new Rectangle2D.Double(p.getX() - ((String) sp.getData()).length() * 5, p.getY() - 40,
						((String) sp.getData()).length() * 10, 20));
				return sp;
			}
		}
		return null;
	}

	@Override
	public boolean isToolTipping() {
		return toolTipping;
	}

	@Override
	public void setToolTipping(boolean enableToolTipping) {
		this.toolTipping = enableToolTipping;
	}

	@Deprecated
	public String getData() {
		return string;
	}

	@Deprecated // use vertical orientation
	public boolean isVerticalAlignment() {
		return verticalOrientation;
	}

	@Deprecated // use vertical orientation
	public void setVerticalAlignment(boolean verticalOrientation) {
		this.verticalOrientation = verticalOrientation;
	}

	public boolean isVerticalOrientation() {
		return verticalOrientation;
	}

	public void setVerticalOrientation(boolean verticalOrientation) {
		this.verticalOrientation = verticalOrientation;
	}

	public HorizontalStringAlignment getHorizontalStringAlignment() {
		return horizontalStringAlignment;
	}

	public void setHorizontalStringAlignment(HorizontalStringAlignment horizontalStringAlignment) {
		this.horizontalStringAlignment = horizontalStringAlignment;
	}

	public VerticalStringAlignment getVerticalStringAlignment() {
		return verticalStringAlignment;
	}

	public void setVerticalStringAlignment(VerticalStringAlignment verticalStringAlignment) {
		this.verticalStringAlignment = verticalStringAlignment;
	}
}
