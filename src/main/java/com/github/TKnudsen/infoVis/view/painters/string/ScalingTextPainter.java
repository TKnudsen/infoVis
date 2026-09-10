package com.github.TKnudsen.infoVis.view.painters.string;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Renders a single line of text scaled to fill its rectangle as closely as
 * possible -- unlike {@link StringPainter}, which truncates text that
 * doesn't fit, this grows or shrinks the font size instead (down to a fixed
 * minimum), so the same text reads consistently across very differently
 * sized rectangles (e.g. one label style shared by many differently-sized
 * pie slices).
 *
 * @version 1.0
 * @since 2015
 */
public class ScalingTextPainter extends ChartPainter {

	private static final double MIN_FONT_SIZE = 6;

	/** width-fit target: how much of the rectangle's width the text should occupy */
	private static final double WIDTH_FIT_FACTOR = 0.915;

	/** height-fit target: how much larger than the font's own line height the rectangle's height may be */
	private static final double HEIGHT_FIT_FACTOR = 1.6;

	private final String text;
	private final String fontName;
	private final int fontStyle;

	public ScalingTextPainter(String text, String fontName, int fontStyle) {
		this.text = text;
		this.fontName = fontName;
		this.fontStyle = fontStyle;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (rectangle == null)
			return;

		double width = rectangle.getWidth();
		double height = rectangle.getHeight();

		Font oldFont = g2.getFont();

		// probe at a reference size, then scale to fit both width and height
		double probeSize = 10;
		Font probeFont = new Font(fontName, fontStyle, (int) Math.floor(probeSize));
		FontMetrics probeMetrics = g2.getFontMetrics(probeFont);

		double widthFitSize = width * probeSize / probeMetrics.stringWidth(text) * WIDTH_FIT_FACTOR;
		double heightFitSize = height * probeSize / probeMetrics.getHeight() * HEIGHT_FIT_FACTOR;
		double fontSize = Math.max(Math.min(widthFitSize, heightFitSize), MIN_FONT_SIZE);

		Font font = new Font(fontName, fontStyle, (int) Math.floor(fontSize));
		FontMetrics metrics = g2.getFontMetrics(font);
		double centerOffset = 0.5 * (metrics.getAscent() + metrics.getDescent()) - metrics.getDescent();
		double xOffset = 0.5 * (width - metrics.stringWidth(text));

		Paint oldPaint = g2.getPaint();
		g2.setFont(font);
		g2.setPaint(getPaint());
		g2.drawString(text, (float) (rectangle.getX() + xOffset), (float) (rectangle.getCenterY() + centerOffset));
		g2.setFont(oldFont);
		g2.setPaint(oldPaint);
	}

	public String getText() {
		return text;
	}

}
