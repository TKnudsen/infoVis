package com.github.TKnudsen.infoVis.view.painters.string;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map.Entry;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * Renders a legend box: one colored line swatch plus a label per entry,
 * stacked vertically, aligned to either the left or right edge of the
 * painter's rectangle.
 *
 * @version 1.0
 * @since 2018
 */
public class LegendaryPainter extends ChartPainter {

	private static final int FONT_SIZE = 12;
	private static final float LINE_OFFSET = FONT_SIZE / 2f;
	private static final int OFFSET_VERTICAL = 8;
	private static final int OFFSET_HORIZONTAL = 10;
	private static final int SWATCH_WIDTH = 16;
	private static final int SWATCH_TEXT_GAP = 5;
	private static final int TEXT_RIGHT_MARGIN = 5;

	private final Font legendFont = new Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE);

	private List<Entry<String, Paint>> entries;

	private boolean alignLeft = true;

	public LegendaryPainter() {
		setBackgroundPaint(new Color(10, 10, 10, 0x1F));
		setPaint(new Color(10, 10, 10, 0x3F));
		stroke = DisplayTools.mediumStroke;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (entries == null || entries.isEmpty())
			return;

		double width = SWATCH_TEXT_GAP + SWATCH_WIDTH + SWATCH_TEXT_GAP + determineLabelWidth(g2) + TEXT_RIGHT_MARGIN
				+ OFFSET_HORIZONTAL;

		double xStart = alignLeft ? rectangle.getMinX() + OFFSET_HORIZONTAL
				: rectangle.getMinX() + rectangle.getWidth() - width - OFFSET_HORIZONTAL;

		Rectangle2D.Double legendBox = new Rectangle2D.Double(xStart, rectangle.getMinY() + OFFSET_VERTICAL, width,
				entries.size() * (FONT_SIZE + LINE_OFFSET) + 2 * LINE_OFFSET);

		float textX = (float) (legendBox.getX() + SWATCH_TEXT_GAP + SWATCH_WIDTH + SWATCH_TEXT_GAP);
		float lineX = textX - SWATCH_WIDTH - SWATCH_TEXT_GAP;
		float textY = (float) (legendBox.getY() + FONT_SIZE + LINE_OFFSET);
		float lineY = textY - 0.33f * FONT_SIZE;

		Shape oldClip = g2.getClip();
		Paint oldPaint = g2.getPaint();
		Font oldFont = g2.getFont();

		g2.setPaint(getBackgroundPaint());
		g2.fill(legendBox);
		g2.setPaint(getPaint());
		g2.setStroke(stroke);
		g2.draw(legendBox);
		g2.setFont(legendFont);
		g2.setClip(legendBox);

		for (Entry<String, Paint> entry : entries) {
			if (entry.getKey() == null || entry.getValue() == null)
				continue;

			g2.setPaint(entry.getValue());
			g2.drawLine((int) lineX, (int) lineY, (int) (lineX + SWATCH_WIDTH), (int) lineY);
			g2.drawString(entry.getKey(), textX, textY);
			textY += FONT_SIZE + LINE_OFFSET;
			lineY += FONT_SIZE + LINE_OFFSET;
		}

		g2.setClip(oldClip);
		g2.setPaint(oldPaint);
		g2.setFont(oldFont);
	}

	private double determineLabelWidth(Graphics2D g2) {
		double width = 0.4 * rectangle.getWidth();
		double labelWidth = 0;

		for (Entry<String, Paint> entry : entries)
			if (entry != null && entry.getKey() != null)
				labelWidth = Math.max(labelWidth, g2.getFontMetrics(legendFont).stringWidth(entry.getKey()));

		return Math.max(width, labelWidth);
	}

	public List<Entry<String, Paint>> getData() {
		return entries;
	}

	public void setData(List<Entry<String, Paint>> entries) {
		this.entries = entries;
	}

	public boolean isAlignLeft() {
		return alignLeft;
	}

	public void setAlignLeft(boolean alignLeft) {
		this.alignLeft = alignLeft;
	}

}
