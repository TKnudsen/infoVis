package com.github.TKnudsen.infoVis.view.painters.string;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * <p>
 * {@link StringPainter} variant for the "label\tvalue" per-line convention
 * used by chart tooltips (see
 * {@code AbstractGPUScatterPlotPainter.createTooltipPainter} -- the tooltip
 * string it builds separates each attribute name from its value with a tab).
 * Each line's value is drawn in a shared column, computed as the widest label
 * across all lines plus a fixed gap -- real tab-stop alignment, which a plain
 * {@link StringPainter}'s fixed-space tab substitution cannot achieve in a
 * proportional-width font.
 * </p>
 * <p>
 * Deliberately not folded into {@link StringPainter} itself: that class is a
 * general-purpose string renderer with no notion of "label/value" text, and
 * most of its many other callers (matrix bin labels, legends, axis labels,
 * ...) have no such convention. This subclass exists to hold that one
 * caller-specific assumption without imposing it on the base class.
 * </p>
 *
 * @since 2026
 */
public class TooltipStringPainter extends StringPainter {

	public TooltipStringPainter(String string) {
		super(string);
	}

	@Override
	protected String normalizeWhitespaceForRendering(String s, int spacesPerTab) {
		// preserve tabs -- drawHorizontalStringMultilineLeftBound computes a real
		// tab-stop column from them instead of substituting fixed spaces
		return s == null ? "" : s;
	}

	@Override
	protected void drawHorizontalStringMultilineLeftBound(Graphics2D g2, FontMetrics fm, String displayString) {
		if (g2 == null || fm == null || displayString == null || displayString.isEmpty())
			return;

		if (displayString.indexOf('\t') < 0) {
			super.drawHorizontalStringMultilineLeftBound(g2, fm, displayString);
			return;
		}

		Rectangle2D rect = getRectangle();
		int offset = getOffset();

		String[] lines = displayString.split("\n", -1);

		int lineHeight = fm.getHeight();
		if (lineHeight <= 0)
			return;

		int blockHeight = lines.length * lineHeight;

		double topY;
		switch (getVerticalStringAlignment()) {
		case UP:
			topY = rect.getY() + offset;
			break;
		case DOWN:
			topY = rect.getMaxY() - offset - blockHeight;
			break;
		default: // CENTER
			topY = rect.getCenterY() - blockHeight / 2.0;
			break;
		}

		int y = (int) Math.round(topY + fm.getAscent());
		final int x = (int) Math.round(rect.getX() + offset);
		final int tabGap = fm.stringWidth("  "); // two spaces worth of gap

		int labelColumnWidth = 0;
		for (String line : lines) {
			int tabIndex = line.indexOf('\t');
			String label = tabIndex >= 0 ? line.substring(0, tabIndex) : line;
			labelColumnWidth = Math.max(labelColumnWidth, fm.stringWidth(label));
		}

		int valueX = x + labelColumnWidth + tabGap;

		if (getBackgroundPaint() != null) {
			int maxLineWidth = 0;
			for (String line : lines) {
				int tabIndex = line.indexOf('\t');
				int w = tabIndex >= 0 ? (labelColumnWidth + tabGap + fm.stringWidth(line.substring(tabIndex + 1)))
						: fm.stringWidth(line);
				if (w > maxLineWidth)
					maxLineWidth = w;
			}

			g2.setPaint(getBackgroundPaint());
			g2.fillRect(x - 2, (int) Math.round(topY) - 2, maxLineWidth + 4, blockHeight + 4);
		}

		g2.setColor(getFontColor());
		for (String line : lines) {
			int tabIndex = line.indexOf('\t');
			if (tabIndex < 0) {
				g2.drawString(line, x, y);
			} else {
				g2.drawString(line.substring(0, tabIndex), x, y);
				g2.drawString(line.substring(tabIndex + 1), valueX, y);
			}
			y += lineHeight;
		}
	}
}
