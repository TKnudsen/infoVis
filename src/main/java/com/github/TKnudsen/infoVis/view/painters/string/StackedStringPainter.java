package com.github.TKnudsen.infoVis.view.painters.string;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;

/**
 * Renders a list of strings stacked either vertically or horizontally, each
 * in its own evenly-sized {@link StringPainter} slot with configurable
 * spacing between slots.
 *
 * @version 1.0
 * @since 2025
 */
public final class StackedStringPainter extends ChartPainter implements ITooltip {

	private static final double DEFAULT_OFFSET = 3.0;

	private final List<String> strings;
	private final List<StringPainter> stringPainters;

	private boolean verticalAlignment = false;
	private double offset = DEFAULT_OFFSET;
	private boolean toolTipping = true;

	/**
	 * Creates a stacked string painter with uniform default colors.
	 *
	 * @param strings list of strings to display
	 * @throws IllegalArgumentException if strings is null or empty
	 */
	public StackedStringPainter(List<String> strings) {
		Objects.requireNonNull(strings, "strings must not be null");
		if (strings.isEmpty())
			throw new IllegalArgumentException("strings must not be empty");

		this.strings = new ArrayList<>(strings);
		this.stringPainters = new ArrayList<>();
		for (String string : strings)
			stringPainters.add(new StringPainter(string));
	}

	/**
	 * Creates a stacked string painter with an individual color per string.
	 *
	 * @param strings list of strings to display
	 * @param colors  list of colors, matching {@code strings} in size
	 * @throws IllegalArgumentException if the lists are null, empty, or differently sized
	 */
	public StackedStringPainter(List<String> strings, List<Color> colors) {
		Objects.requireNonNull(strings, "strings must not be null");
		Objects.requireNonNull(colors, "colors must not be null");
		if (strings.isEmpty())
			throw new IllegalArgumentException("strings must not be empty");
		if (strings.size() != colors.size())
			throw new IllegalArgumentException(
					"strings and colors must have the same size (strings: " + strings.size() + ", colors: "
							+ colors.size() + ")");

		this.strings = new ArrayList<>(strings);
		this.stringPainters = new ArrayList<>();
		for (int i = 0; i < strings.size(); i++) {
			StringPainter painter = new StringPainter(strings.get(i));
			painter.setFontColor(colors.get(i));
			painter.setColor(colors.get(i));
			stringPainters.add(painter);
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		for (StringPainter painter : stringPainters)
			painter.draw(g2);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		if (rectangle == null || stringPainters.isEmpty())
			return;

		List<Rectangle2D> slots = splitEvenly(rectangle, stringPainters.size(), verticalAlignment, offset);
		for (int i = 0; i < stringPainters.size(); i++)
			stringPainters.get(i).setRectangle(slots.get(i));
	}

	/**
	 * Splits {@code rectangle} into {@code count} evenly-sized slots, side by
	 * side (if {@code vertical} is false, i.e. splitting the width) or stacked
	 * (if {@code vertical} is true, i.e. splitting the height), with
	 * {@code spacing} pixels between adjacent slots.
	 */
	private static List<Rectangle2D> splitEvenly(Rectangle2D rectangle, int count, boolean vertical,
			double spacing) {
		List<Rectangle2D> slots = new ArrayList<>(count);

		double totalLength = vertical ? rectangle.getHeight() : rectangle.getWidth();
		double slotLength = (totalLength - (count - 1) * spacing) / count;

		double position = vertical ? rectangle.getY() : rectangle.getX();
		for (int i = 0; i < count; i++) {
			slots.add(vertical
					? new Rectangle2D.Double(rectangle.getX(), position, rectangle.getWidth(), slotLength)
					: new Rectangle2D.Double(position, rectangle.getY(), slotLength, rectangle.getHeight()));
			position += slotLength + spacing;
		}

		return slots;
	}

	// ==================== TOOLTIP ====================

	@Override
	public ChartPainter getTooltip(Point p) {
		if (p == null || !toolTipping)
			return null;

		for (StringPainter painter : stringPainters)
			if (painter.getRectangle() != null && painter.getRectangle().contains(p))
				return painter.getTooltip(p);

		return null;
	}

	@Override
	public boolean isToolTipping() {
		return toolTipping;
	}

	@Override
	public void setToolTipping(boolean toolTipping) {
		this.toolTipping = toolTipping;
	}

	// ==================== DATA ====================

	public List<String> getData() {
		return Collections.unmodifiableList(strings);
	}

	public int getStringCount() {
		return strings.size();
	}

	// ==================== STYLE PROPAGATION ====================

	@Override
	public void setBackgroundPaint(Paint backgroundPaint) {
		super.setBackgroundPaint(backgroundPaint);

		if (stringPainters != null)
			for (StringPainter painter : stringPainters)
				painter.setBackgroundPaint(backgroundPaint);
	}

	@Override
	public void setBorderPaint(Paint borderPaint) {
		super.setBorderPaint(borderPaint);

		for (StringPainter painter : stringPainters)
			painter.setBorderPaint(borderPaint);
	}

	@Override
	public void setFontColor(Color fontColor) {
		super.setFontColor(fontColor);

		for (StringPainter painter : stringPainters)
			painter.setFontColor(fontColor);
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);

		for (StringPainter painter : stringPainters)
			painter.setFont(font);
	}

	@Override
	public void setFontSize(int fontSize) {
		super.setFontSize(fontSize);

		for (StringPainter painter : stringPainters)
			painter.setFontSize(fontSize);
	}

	@Override
	public void setFontStyle(int fontStyle) {
		super.setFontStyle(fontStyle);

		for (StringPainter painter : stringPainters)
			painter.setFontStyle(fontStyle);
	}

	@Override
	public void setDrawOutline(boolean drawOutline) {
		super.setDrawOutline(drawOutline);

		for (StringPainter painter : stringPainters)
			painter.setDrawOutline(drawOutline);
	}

	// ==================== ALIGNMENT ====================

	public boolean isVerticalAlignment() {
		return verticalAlignment;
	}

	public void setVerticalAlignment(boolean verticalAlignment) {
		this.verticalAlignment = verticalAlignment;

		// stacking into multiple rows (vertical alignment) means each row is wide
		// and short -- text reads normally. Arranging into multiple columns (side
		// by side) means each column is narrow and tall -- text must rotate to fit.
		for (StringPainter painter : stringPainters)
			painter.setVerticalOrientation(!verticalAlignment);

		if (rectangle != null)
			setRectangle(rectangle);
	}

	public void setStringPosition(HorizontalStringAlignment alignment) {
		Objects.requireNonNull(alignment, "alignment must not be null");

		for (StringPainter painter : stringPainters)
			painter.setHorizontalStringAlignment(alignment);
	}

	public void setFontColors(List<Color> colors) {
		Objects.requireNonNull(colors, "colors must not be null");
		if (stringPainters.size() != colors.size())
			throw new IllegalArgumentException(
					"size mismatch: " + stringPainters.size() + " painters but " + colors.size() + " colors");

		for (int i = 0; i < colors.size(); i++)
			stringPainters.get(i).setFontColor(colors.get(i));
	}

	// ==================== OFFSET ====================

	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		if (offset < 0)
			throw new IllegalArgumentException("offset must be non-negative");
		this.offset = offset;

		if (rectangle != null)
			setRectangle(rectangle);
	}

	// ==================== PAINTERS ACCESS ====================

	public StringPainter getStringPainter(int index) {
		return stringPainters.get(index);
	}

	public List<StringPainter> getStringPainters() {
		return Collections.unmodifiableList(stringPainters);
	}

}
