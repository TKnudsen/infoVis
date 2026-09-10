package com.github.TKnudsen.infoVis.view.painters.glyph.primitives;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Abstract base offering icon/bar/description drawing helpers for rectangular
 * info-card-style glyphs (an icon, a single-line title bar that falls back to a
 * two-line wrap if it doesn't fit, and a multi-line paragraph clipped to a
 * given height). Subclasses combine these building blocks and lay them out;
 * this class draws nothing on its own besides the background fill inherited
 * from {@link ChartPainter}.
 *
 * @version 1.0
 * @since 2014
 */
public abstract class RectangularPainter extends ChartPainter {

	protected static final int ICON_SIZE = 16;
	protected static final int BAR_HEIGHT = ICON_SIZE;

	protected static final int OFFSET = 5;

	/**
	 * drawDescription() stops adding lines once the space left is under this many
	 * pixels -- roughly one text line
	 */
	private static final double MIN_REMAINING_HEIGHT_FOR_LINE = 18;

	protected Font descriptionFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	public RectangularPainter() {
		setBackgroundPaint(Color.WHITE);
		setBorderPaint(Color.WHITE);
		setPaint(Color.BLACK);
	}

	@Override
	public void draw(Graphics2D g2) {
		if (rectangle == null)
			return;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(getBackgroundPaint());
		g2.fill(rectangle);
		g2.setPaint(getPaint());
	}

	/** @param size the icon's target width and height in pixels */
	protected void drawIcon(Graphics2D g2d, double x, double y, double size, BufferedImage icon) {
		int x2 = (int) (x + size);
		int y2 = (int) (y + size);

		g2d.drawImage(icon, (int) x, (int) y, x2, y2, 0, 0, icon.getWidth(), icon.getHeight(), new JPanel());
	}

	/**
	 * Draws {@code text} as a single line if it fits within {@code width} at font
	 * {@code standard}; otherwise wraps it across (up to) two lines.
	 */
	protected void drawBar(Graphics2D g2d, double x, double y, double width, String text, Font standard) {
		FontMetrics metrics = g2d.getFontMetrics(standard);
		int stringWidth = metrics.stringWidth(text);
		if (stringWidth < width) {
			g2d.setFont(standard);
			g2d.drawString(text, (float) x, (float) y + BAR_HEIGHT / 2 + OFFSET);
		} else {
			AttributedString string = new AttributedString(text);
			string.addAttribute(TextAttribute.FONT, standard);
			AttributedCharacterIterator paragraph = string.getIterator();

			FontRenderContext frc = new FontRenderContext(standard.getTransform(), true, true);
			LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, frc);

			try {
				TextLayout layout = lineMeasurer.nextLayout((float) width);
				y += layout.getAscent();
				layout.draw(g2d, (float) x, (float) y);
				y += layout.getDescent() + layout.getLeading();

				layout = lineMeasurer.nextLayout((float) width);
				y += layout.getAscent();
				layout.draw(g2d, (float) x, (float) y);
			} catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
				// text has fewer than two line breaks worth of content -- nothing more to draw
			}
		}
	}

	/**
	 * Draws {@code text} word-wrapped across multiple lines within {@code width},
	 * stopping once {@code height} is exhausted.
	 *
	 * @return the total height (in pixels) actually used
	 */
	protected double drawDescription(Graphics2D g2d, double x, double y, double width, double height, String text) {
		if (text == null || text.isEmpty())
			return 0d;

		AttributedString string = new AttributedString(text);
		string.addAttribute(TextAttribute.FONT, descriptionFont);
		AttributedCharacterIterator paragraph = string.getIterator();
		int paragraphEnd = paragraph.getEndIndex();
		FontRenderContext frc = new FontRenderContext(descriptionFont.getTransform(), true, true);
		LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, frc);

		float cumulativeHeight = 0;

		while (lineMeasurer.getPosition() < paragraphEnd && cumulativeHeight + MIN_REMAINING_HEIGHT_FOR_LINE < height) {
			TextLayout layout = lineMeasurer.nextLayout((float) width);

			// right-to-left paragraphs align to the right edge instead
			double lineX = x + (layout.isLeftToRight() ? 0 : width - layout.getAdvance());

			cumulativeHeight += layout.getAscent();
			layout.draw(g2d, (float) lineX, (float) (y + cumulativeHeight));

			cumulativeHeight += layout.getDescent() + layout.getLeading();
		}

		return cumulativeHeight;
	}

}
