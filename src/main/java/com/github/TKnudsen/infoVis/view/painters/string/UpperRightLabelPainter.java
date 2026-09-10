package com.github.TKnudsen.infoVis.view.painters.string;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * Renders a small text badge (a rounded-cube outline around a label) pinned
 * to the upper-right corner of the painter's rectangle -- e.g. a category
 * tag or a small annotation overlaid on a larger chart element. The badge's
 * font size scales with the host rectangle, within a fixed minimum.
 *
 * @version 1.0
 * @since 2012
 */
public class UpperRightLabelPainter extends ChartPainter {

	private static final int MIN_FONT_SIZE = 8;
	private static final double FONT_SIZE_DIVISOR = 8;
	private static final double MIN_WIDTH_HEIGHT_FACTOR = 0.8;

	// hand-tuned proportions of the badge box, relative to the label's measured width
	private static final double RIGHT_INSET_FACTOR = 1.5;
	private static final double TOP_INSET_FACTOR = 0.35;
	private static final double WIDTH_PADDING_FACTOR = 1.2;
	private static final double HEIGHT_PADDING_FACTOR = 1.25;
	private static final double TEXT_X_OFFSET_FACTOR = 1.4;
	private static final double ARC_DIVISOR = 4;

	private final String label;

	private int fontSize = 12;

	public UpperRightLabelPainter(String label) {
		this.label = label;
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		if (rectangle == null)
			return;

		fontSize = (int) Math.max(MIN_FONT_SIZE,
				Math.min(rectangle.getWidth() / FONT_SIZE_DIVISOR, rectangle.getHeight() / FONT_SIZE_DIVISOR));
	}

	@Override
	public void draw(Graphics2D g2) {
		if (rectangle == null)
			return;

		Font oldFont = g2.getFont();

		g2.setFont(new Font("Tahoma", Font.PLAIN, fontSize));
		FontMetrics metrics = g2.getFontMetrics();
		double stringWidth = Math.max(metrics.stringWidth(label), metrics.getHeight() * MIN_WIDTH_HEIGHT_FACTOR);

		Rectangle2D.Double labelRect = new Rectangle2D.Double(
				rectangle.getX() + rectangle.getWidth() - RIGHT_INSET_FACTOR * stringWidth - 1.5,
				rectangle.getY() + TOP_INSET_FACTOR * stringWidth, (stringWidth + 1) * WIDTH_PADDING_FACTOR,
				stringWidth * HEIGHT_PADDING_FACTOR);

		DisplayTools.drawCube(g2, labelRect, labelRect.getWidth() / ARC_DIVISOR, labelRect.getHeight() / ARC_DIVISOR,
				null, DisplayTools.standardStroke, getBorderPaint());

		g2.setPaint(getBorderPaint());
		g2.drawString(label,
				(float) (rectangle.getX() + rectangle.getWidth() - TEXT_X_OFFSET_FACTOR * stringWidth
						+ (stringWidth - metrics.stringWidth(label))),
				(float) (labelRect.getY() + labelRect.getWidth() - (labelRect.getHeight() - fontSize * 0.8) / 2));

		g2.setFont(oldFont);
	}

	public String getLabel() {
		return label;
	}

}
