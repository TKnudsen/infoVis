package com.github.TKnudsen.infoVis.view.painters.string;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.ComplexDataObject.model.tools.StringTools;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * StringPainter
 * 
 * Draws a String object with optimized performance through caching and binary
 * search truncation. String truncation is computed lazily and cached to avoid
 * redundant calculations during repeated draw calls. Makes use if aggressive
 * caching for higher performance. Can handle 1000+ painters at 60 FPS.
 * 
 * Optimizations:
 * <ul>
 * <li>Binary search truncation (O(log n))</li>
 * <li>Cached truncated strings with fast primitive comparison</li>
 * <li>Cached FontMetrics to avoid expensive lookups</li>
 * <li>Reused Rectangle objects to minimize allocations</li>
 * <li>Early exits for empty strings</li>
 * </ul>
 * </p>
 *
 * @version 1.21
 * @since 2016
 */
public class StringPainter extends ChartPainter implements ITooltip {

	/** The string to be rendered (never null) */
	protected String string;

	/** Whether to render the string vertically (rotated 90 degrees) */
	private boolean verticalOrientation = false;

	/** Padding offset from rectangle edges in pixels */
	private int offset = 2;

	/**
	 * Horizontal alignment of the string within the rectangle (used for single-line
	 * only)
	 */
	private HorizontalStringAlignment horizontalStringAlignment = HorizontalStringAlignment.CENTER;

	/** Vertical alignment of the string within the rectangle (applies to block) */
	private VerticalStringAlignment verticalStringAlignment = VerticalStringAlignment.CENTER;

	/** Whether to show tool tip on hover */
	private boolean toolTipping = true;

	// ==================== PERFORMANCE CACHE ====================
	private String cachedTruncatedString = null;
	private FontMetrics cachedFontMetrics = null;

	// Cache keys (primitive + minimal object)
	private double cachedX, cachedY, cachedWidth, cachedHeight;
	private String cachedFontName;
	private int cachedFontSize, cachedFontStyle;
	private boolean cachedVerticalOrientation = false;
	private int cachedOffset = -1;

	// Reusable objects
	private final Rectangle backgroundRect = new Rectangle();

	// Configuration
	private static final int SPACES_PER_TAB = 4;

	public enum HorizontalStringAlignment {
		LEFT, CENTER, RIGHT
	}

	public enum VerticalStringAlignment {
		UP, CENTER, DOWN
	}

	public StringPainter(String string) {
		this.string = (string != null) ? string : "";
		setBackgroundPaint(null);
	}

	@Override
	public void draw(Graphics2D g2) {
		if (g2 == null || rectangle == null)
			return;

		if (string == null || string.isEmpty())
			return;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color originalColor = g2.getColor();
		Font originalFont = g2.getFont();
		Stroke originalStroke = g2.getStroke();

		try {
			g2.setColor(fontColor);
			g2.setFont(font);
			g2.setStroke(stroke);

			FontMetrics fm = getCachedFontMetrics(g2);
			if (fm == null)
				return;

			String displayString = getTruncatedString(fm);
			if (displayString == null || displayString.isEmpty())
				return;

			if (!verticalOrientation) {
				// Multi-line aware horizontal rendering (left-bound)
				if (displayString.indexOf('\n') >= 0) {
					drawHorizontalStringMultilineLeftBound(g2, fm, displayString);
				} else {
					// Single line: keep alignment options
					drawHorizontalStringSingleLine(g2, fm, displayString);
				}
			} else {
				// Vertical: only single-line rendering supported
				String oneLine = displayString.replace("\n", " ");
				drawVerticalString(g2, fm, oneLine);
			}

			if (drawOutline) {
				g2.setPaint(getBorderPaint());
				DisplayTools.drawRectangle(g2, rectangle);
			}
		} finally {
			g2.setStroke(originalStroke);
			g2.setFont(originalFont);
			g2.setColor(originalColor);
		}
	}

	// ==================== FONT METRICS CACHE ====================

	private FontMetrics getCachedFontMetrics(Graphics2D g2) {
		if (g2 == null || font == null)
			return null;

		if (cachedFontMetrics == null || !isCacheValid()) {
			cachedFontMetrics = g2.getFontMetrics(font);
		}

		return cachedFontMetrics;
	}

	private boolean isCacheValid() {
		return cachedTruncatedString != null && rectangle != null && font != null && rectangle.getX() == cachedX
				&& rectangle.getY() == cachedY && rectangle.getWidth() == cachedWidth
				&& rectangle.getHeight() == cachedHeight && font.getSize() == cachedFontSize
				&& font.getStyle() == cachedFontStyle && font.getName().equals(cachedFontName)
				&& verticalOrientation == cachedVerticalOrientation && offset == cachedOffset;
	}

	private void updateCacheKeys() {
		if (rectangle != null) {
			cachedX = rectangle.getX();
			cachedY = rectangle.getY();
			cachedWidth = rectangle.getWidth();
			cachedHeight = rectangle.getHeight();
		}
		if (font != null) {
			cachedFontName = font.getName();
			cachedFontSize = font.getSize();
			cachedFontStyle = font.getStyle();
		}
		cachedVerticalOrientation = verticalOrientation;
		cachedOffset = offset;
	}

	private void invalidateCache() {
		cachedTruncatedString = null;
		cachedFontMetrics = null;
	}

	// ==================== TRUNCATION ====================

	private String getTruncatedString(FontMetrics fm) {
		if (string == null)
			string = "";

		if (isCacheValid())
			return cachedTruncatedString;

		if (fm == null || rectangle == null) {
			cachedTruncatedString = "";
			updateCacheKeys();
			return cachedTruncatedString;
		}

		String normalized = normalizeWhitespaceForRendering(string, SPACES_PER_TAB);

		double availableWidth = verticalOrientation ? (rectangle.getHeight() - 2 * offset)
				: (rectangle.getWidth() - 2 * offset);

		double availableHeight = verticalOrientation ? (rectangle.getWidth() - 2 * offset)
				: (rectangle.getHeight() - 2 * offset);

		String truncated;
		if (!verticalOrientation && normalized.indexOf('\n') >= 0) {
			truncated = truncateMultiline(normalized, fm, availableWidth, availableHeight);
		} else {
			truncated = truncateSingleLine(normalized, fm, availableWidth);
		}

		if (truncated == null)
			truncated = "";

		cachedTruncatedString = truncated;
		updateCacheKeys();

		return cachedTruncatedString;
	}

	private String truncateMultiline(String str, FontMetrics fm, double availableWidth, double availableHeight) {
		if (str == null || str.isEmpty())
			return "";
		if (fm == null || availableWidth <= 0 || availableHeight <= 0)
			return "";

		String[] lines = str.split("\n", -1);

		int lineHeight = fm.getHeight();
		if (lineHeight <= 0)
			return "";

		int maxLines = (int) Math.floor(availableHeight / lineHeight);
		if (maxLines <= 0)
			return "";

		int visibleLines = Math.min(lines.length, maxLines);

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < visibleLines; i++) {
			String line = lines[i];
			String truncatedLine = truncateSingleLine(line, fm, availableWidth);

			sb.append(truncatedLine);
			if (i < visibleLines - 1) {
				sb.append('\n');
			}
		}

		// If more lines exist: add ellipsis to last visible line
		if (lines.length > visibleLines && sb.length() > 0) {
			int lastLineStart = sb.lastIndexOf("\n") + 1;
			String lastLine = sb.substring(lastLineStart);
			String ellipsisLine = appendEllipsis(lastLine, fm, availableWidth);
			sb.replace(lastLineStart, sb.length(), ellipsisLine);
		}

		return sb.toString();
	}

	private String truncateSingleLine(String line, FontMetrics fm, double availableWidth) {
		if (line == null || line.isEmpty())
			return "";
		if (fm == null || availableWidth <= 0)
			return "";

		// Fast path
		if (fm.stringWidth(line) <= availableWidth) {
			return line;
		}

		int left = 0;
		int right = line.length();
		int bestLength = 0;

		while (left <= right) {
			int mid = (left + right) >>> 1; // unsigned shift (slightly faster/safer)
			String candidate = line.substring(0, mid);
			int width = fm.stringWidth(candidate);

			if (width <= availableWidth) {
				bestLength = mid;
				left = mid + 1;
			} else {
				right = mid - 1;
			}
		}

		return bestLength > 0 ? line.substring(0, bestLength) : "";
	}

	private String appendEllipsis(String line, FontMetrics fm, double availableWidth) {
		final String ellipsis = "..."; // safer than Unicode ellipsis with some fonts
		if (fm == null || availableWidth <= 0)
			return "";

		if (line == null)
			line = "";

		if (fm.stringWidth(ellipsis) > availableWidth) {
			return "";
		}

		if (fm.stringWidth(line + ellipsis) <= availableWidth) {
			return line + ellipsis;
		}

		double widthForText = availableWidth - fm.stringWidth(ellipsis);
		String truncated = truncateSingleLine(line, fm, widthForText);
		return truncated + ellipsis;
	}

	// ==================== DRAWING ====================

	private void drawHorizontalStringSingleLine(Graphics2D g2, FontMetrics fm, String displayString) {
		if (g2 == null || fm == null || displayString == null)
			return;

		int stringWidth = fm.stringWidth(displayString);

		double additionalHeightOffset = calculateAdditionalHeightOffset(rectangle.getHeight());

		double horizontalOffset = calculateHorizontalOffset(stringWidth);

		int x = (int) Math.round(rectangle.getMinX() + horizontalOffset);
		int y = (int) Math.round(rectangle.getCenterY() + font.getSize() * 0.5 + additionalHeightOffset);

		if (getBackgroundPaint() != null) {
			backgroundRect.setBounds(x - 2, y - font.getSize(), stringWidth + 4, (int) (font.getSize() * 1.333));
			g2.setPaint(getBackgroundPaint());
			DisplayTools.fillRectangle(g2, backgroundRect);
		}

		g2.setColor(getFontColor());
		g2.drawString(displayString, x, y);
	}

	/**
	 * Horizontal multi-line drawing: ALWAYS left-bound.
	 */
	private void drawHorizontalStringMultilineLeftBound(Graphics2D g2, FontMetrics fm, String displayString) {
		if (g2 == null || fm == null || displayString == null || displayString.isEmpty())
			return;

		String[] lines = displayString.split("\n", -1);

		int lineHeight = fm.getHeight();
		if (lineHeight <= 0)
			return;

		int blockHeight = lines.length * lineHeight;

		double topY;
		switch (verticalStringAlignment) {
		case UP:
			topY = rectangle.getY() + offset;
			break;
		case DOWN:
			topY = rectangle.getMaxY() - offset - blockHeight;
			break;
		default: // CENTER
			topY = rectangle.getCenterY() - blockHeight / 2.0;
			break;
		}

		int y = (int) Math.round(topY + fm.getAscent());
		final int x = (int) Math.round(rectangle.getX() + offset);

		if (getBackgroundPaint() != null) {
			int maxLineWidth = 0;
			for (String line : lines) {
				int w = fm.stringWidth(line);
				if (w > maxLineWidth)
					maxLineWidth = w;
			}

			int bgY = (int) Math.round(topY);
			backgroundRect.setBounds(x - 2, bgY - 2, maxLineWidth + 4, blockHeight + 4);
			g2.setPaint(getBackgroundPaint());
			DisplayTools.fillRectangle(g2, backgroundRect);
		}

		g2.setColor(getFontColor());
		for (String line : lines) {
			g2.drawString(line, x, y);
			y += lineHeight;
		}
	}

	private void drawVerticalString(Graphics2D g2, FontMetrics fm, String displayString) {
		if (g2 == null || fm == null || displayString == null || displayString.isEmpty())
			return;

		int stringWidth = fm.stringWidth(displayString);

		double xOffset = rectangle.getWidth() - font.getSize();
		double yOffset = calculateVerticalYOffset(stringWidth);

		int x = (int) Math.round(rectangle.getX() + xOffset * 0.5 + getFontSize());
		int y = (int) Math.round(rectangle.getY() + yOffset);

		if (getBackgroundPaint() != null) {
			backgroundRect.setBounds(x - font.getSize(), y - stringWidth - 2, (int) (font.getSize() * 1.333),
					stringWidth + 4);
			g2.setPaint(getBackgroundPaint());
			DisplayTools.fillRectangle(g2, backgroundRect);
		}

		g2.setColor(getFontColor());
		DisplayTools.drawRotatedString(g2, displayString, (float) (rectangle.getX() + xOffset * 0.5 + getFontSize()),
				(float) (rectangle.getY() + yOffset), -Math.PI / 2);
	}

	// ==================== WHITESPACE NORMALIZATION ====================

	private static String normalizeWhitespaceForRendering(String s, int spacesPerTab) {
		if (s == null || s.isEmpty())
			return "";
		if (spacesPerTab < 1)
			spacesPerTab = 4;

		final String tabReplacement = StringTools.repeatChar(' ', spacesPerTab);
		return s.replace("\t", tabReplacement);
	}

	// ==================== ALIGNMENT CALCULATIONS ====================

	private double calculateAdditionalHeightOffset(double availableSpace) {
		if (availableSpace <= 0.0)
			return 0;

		switch (verticalStringAlignment) {
		case UP:
			return -availableSpace * 0.48;
		case DOWN:
			return availableSpace * 0.50 - font.getSize() * 0.75;
		default:
			return 0;
		}
	}

	private double calculateHorizontalOffset(int stringWidth) {
		switch (horizontalStringAlignment) {
		case LEFT:
			return offset;
		case RIGHT:
			return rectangle.getWidth() - stringWidth - offset;
		default: // CENTER
			return (rectangle.getWidth() - stringWidth) / 2.0;
		}
	}

	private double calculateVerticalYOffset(int stringWidth) {
		switch (verticalStringAlignment) {
		case UP:
			return stringWidth + 2 * offset;
		case DOWN:
			return rectangle.getHeight() - 2 * offset;
		default: // CENTER
			return rectangle.getHeight() * 0.5 + stringWidth * 0.5;
		}
	}

	// ==================== GETTERS / SETTERS ====================

	public void setData(String string) {
		this.string = (string != null) ? string : "";
		invalidateCache();
	}

	public String getData() {
		return string != null ? string : "";
	}

	public String getString() {
		return string != null ? string : "";
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		invalidateCache();
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
		invalidateCache();
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);
		invalidateCache();
	}

	public boolean isVerticalOrientation() {
		return verticalOrientation;
	}

	public void setVerticalOrientation(boolean verticalOrientation) {
		this.verticalOrientation = verticalOrientation;
		invalidateCache();
	}

	public HorizontalStringAlignment getHorizontalStringAlignment() {
		return horizontalStringAlignment;
	}

	public void setHorizontalStringAlignment(HorizontalStringAlignment horizontalStringAlignment) {
		this.horizontalStringAlignment = horizontalStringAlignment != null ? horizontalStringAlignment
				: HorizontalStringAlignment.CENTER;
		// alignment does not affect truncation currently
	}

	public VerticalStringAlignment getVerticalStringAlignment() {
		return verticalStringAlignment;
	}

	public void setVerticalStringAlignment(VerticalStringAlignment verticalStringAlignment) {
		this.verticalStringAlignment = verticalStringAlignment != null ? verticalStringAlignment
				: VerticalStringAlignment.CENTER;
	}

	@Override
	public String toString() {
		return "StringPainter['" + (string != null ? string : "") + "']";
	}

	// ==================== TOOLTIP ====================

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!toolTipping || rectangle == null || !rectangle.contains(p))
			return null;

		if (string == null || string.isEmpty())
			return null;

		StringPainter tooltip = new StringPainter(this.string);

		double tooltipWidth = Math.max(100, this.string.length() * 10.0);
		double tooltipHeight = 20.0;

		tooltip.setRectangle(
				new Rectangle2D.Double(p.getX() - tooltipWidth / 2.0, p.getY() - 40.0, tooltipWidth, tooltipHeight));

		return tooltip;
	}

	@Override
	public boolean isToolTipping() {
		return toolTipping;
	}

	@Override
	public void setToolTipping(boolean enableToolTipping) {
		this.toolTipping = enableToolTipping;
	}

	/**
	 * @deprecated Use {@link #isVerticalOrientation()} instead
	 */
	@Deprecated
	public boolean isVerticalAlignment() {
		return verticalOrientation;
	}

	/**
	 * @deprecated Use {@link #setVerticalOrientation(boolean)} instead
	 */
	@Deprecated
	public void setVerticalAlignment(boolean verticalOrientation) {
		setVerticalOrientation(verticalOrientation);
	}
}