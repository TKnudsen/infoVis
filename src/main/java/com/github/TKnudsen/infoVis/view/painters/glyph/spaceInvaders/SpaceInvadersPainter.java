package com.github.TKnudsen.infoVis.view.painters.glyph.spaceInvaders;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * Renders a rounded, capsule-shaped "retro game cabinet" frame -- a pill
 * outline with a title bar (a rounded cube glyph with a text headline, sized to
 * fit) and an optional second accessory cube at the top-right, both drawn via
 * {@link DisplayTools#drawCube}. Stroke widths and headline sizing all scale
 * with the painter's own rectangle, so the same glyph reads correctly from icon
 * size up to a full window.
 *
 * @version 1.1
 * @since 2011
 */
public class SpaceInvadersPainter extends ChartPainter {

	// size (in pixels, the shorter side of the rectangle) below which each stroke
	// tier applies
	private static final double SIZE_TIER_THIN = 200;
	private static final double SIZE_TIER_MEDIUM = 400;
	private static final double SIZE_TIER_THICK = 1000;

	// head/tail arc radii are the rectangle's width/height divided by these
	private static final double HEAD_ARC_DIVISOR = 4;
	private static final double TAIL_ARC_DIVISOR = 6;

	// headline bar is only drawn once it is tall enough to hold readable text
	private static final double MIN_HEADLINE_HEIGHT = 25;
	private static final double HEADLINE_FONT_HEIGHT_FACTOR = 0.7;
	private static final double HEADLINE_BASELINE_FACTOR = 0.88;
	private static final double HEAD_ARC_INSET_FACTOR = 0.35;
	private static final double UPPER_RIGHT_ASPECT_RATIO = 1.5;

	private Rectangle2D.Double centerRectangle;
	private Rectangle2D.Double upperRightRectangle;
	private Rectangle2D.Double headlineRect;
	private BasicStroke outlineStroke = DisplayTools.thickStroke;

	private String headline = "";
	private Color headlineBackgroundColor;

	private double headArcWidth;
	private double headArcHeight;
	private double tailArcWidth;
	private double tailArcHeight;
	private double strokeOffset = 1;

	@Override
	public void draw(Graphics2D g2) {
		if (rectangle == null)
			return;

		Paint oldPaint = g2.getPaint();

		double x = rectangle.getX();
		double y = rectangle.getY();
		double width = rectangle.getWidth();
		double height = rectangle.getHeight();
		double halfStroke = strokeOffset / 2;

		g2.setPaint(getPaint());
		g2.fillArc((int) (x + halfStroke), (int) (y + halfStroke), (int) headArcWidth, (int) headArcHeight, 90, 360);
		g2.fillArc((int) (x + width - headArcWidth - halfStroke), (int) (y + halfStroke), (int) headArcWidth,
				(int) headArcHeight, 0, 360);
		g2.fillArc((int) (x + halfStroke), (int) (y + height - tailArcHeight - halfStroke), (int) tailArcWidth,
				(int) tailArcHeight, 180, 360);
		g2.fillArc((int) (x + width - tailArcWidth - halfStroke), (int) (y + height - tailArcHeight - halfStroke),
				(int) tailArcWidth, (int) tailArcHeight, 270, 360);
		// upper background
		g2.fillRect((int) (x + headArcWidth / 2), (int) y, (int) (width - headArcWidth),
				(int) (height - 0.5 * tailArcHeight));
		// fill grid rect with background color
		g2.fillRect((int) (x + strokeOffset), (int) (y + headArcHeight / 2 + strokeOffset),
				(int) (width - strokeOffset),
				(int) (height - 2 * strokeOffset - headArcHeight / 2 - tailArcHeight / 2));

		drawTitle(g2);

		if (upperRightRectangle != null)
			DisplayTools.drawCube(g2, upperRightRectangle,
					Math.min(upperRightRectangle.getWidth() / 3, upperRightRectangle.getHeight() / 3),
					Math.min(upperRightRectangle.getWidth() / 3, upperRightRectangle.getHeight() / 3),
					headlineBackgroundColor, stroke, Color.BLACK);

		// frame outline
		g2.setPaint(Color.BLACK);
		g2.setStroke(outlineStroke);
		g2.drawArc((int) (x + halfStroke), (int) (y + halfStroke), (int) headArcWidth, (int) headArcHeight, 90, 90);
		g2.drawArc((int) (x + width - headArcWidth - halfStroke), (int) (y + halfStroke), (int) headArcWidth,
				(int) headArcHeight, 0, 90);
		g2.drawLine((int) (x + headArcWidth / 2 + halfStroke), (int) (y + halfStroke),
				(int) (x + width - headArcWidth / 2 - halfStroke), (int) (y + halfStroke));

		g2.drawArc((int) (x + halfStroke), (int) (y + height - tailArcHeight - halfStroke), (int) tailArcWidth,
				(int) tailArcHeight, 180, 177);
		g2.drawArc((int) (x + width - tailArcWidth - halfStroke), (int) (y + height - tailArcHeight - halfStroke),
				(int) tailArcWidth, (int) tailArcHeight, 183, 177);
		g2.drawLine((int) (x + tailArcWidth + halfStroke), (int) (y + height - tailArcHeight / 2 - halfStroke) - 1,
				(int) (x + width - tailArcWidth - halfStroke), (int) (y + height - tailArcHeight / 2 - halfStroke) - 1);

		g2.drawLine((int) (x + halfStroke), (int) (y + headArcHeight / 2 + strokeOffset), (int) (x + halfStroke),
				(int) (y + height - tailArcHeight / 2 - strokeOffset));
		g2.drawLine((int) (x + width - halfStroke), (int) (y + headArcHeight / 2 + strokeOffset),
				(int) (x + width - halfStroke), (int) (y + height - tailArcHeight / 2 - strokeOffset));

		g2.setPaint(oldPaint);
	}

	private void drawTitle(Graphics2D g2) {
		if (headlineRect == null)
			return;

		int cubeRadius = (int) Math.min(headlineRect.getWidth() / 3, headlineRect.getHeight() / 3);
		DisplayTools.drawCube(g2, headlineRect, cubeRadius, cubeRadius, headlineBackgroundColor, stroke, Color.BLACK);

		String headlineToDraw = headline;
		int fontSize = (int) (headlineRect.getHeight() * HEADLINE_FONT_HEIGHT_FACTOR);
		g2.setFont(new Font("Tahoma", Font.PLAIN, fontSize));
		int maxTextWidth = (int) (headlineRect.getWidth() - 2 * cubeRadius);
		FontMetrics metrics = g2.getFontMetrics();
		while (metrics.stringWidth(headlineToDraw) > maxTextWidth && !headlineToDraw.isEmpty())
			headlineToDraw = headlineToDraw.substring(0, headlineToDraw.length() - 1);

		g2.setPaint(Color.BLACK);
		g2.drawString(headlineToDraw, (float) (headlineRect.getX() + cubeRadius),
				(float) (headlineRect.getY() + headlineRect.getHeight() * HEADLINE_BASELINE_FACTOR));
	}

	@Override
	public void setBackgroundPaint(Paint backgroundPaint) {
		super.setBackgroundPaint(backgroundPaint);

		headlineBackgroundColor = backgroundPaint instanceof Color ? lighten((Color) backgroundPaint) : Color.CYAN;
	}

	/** a lighter tint of {@code color}, halfway toward white on each channel */
	private static Color lighten(Color color) {
		return new Color(255 - (255 - color.getRed()) / 2, 255 - (255 - color.getGreen()) / 2,
				255 - (255 - color.getBlue()) / 2);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		if (rectangle == null)
			return;

		headArcWidth = rectangle.getWidth() / HEAD_ARC_DIVISOR;
		headArcHeight = rectangle.getHeight() / HEAD_ARC_DIVISOR;
		tailArcWidth = rectangle.getWidth() / TAIL_ARC_DIVISOR;
		tailArcHeight = rectangle.getHeight() / TAIL_ARC_DIVISOR;

		double shorterSide = Math.min(rectangle.getWidth(), rectangle.getHeight());
		if (shorterSide < SIZE_TIER_THIN) {
			outlineStroke = DisplayTools.standardStroke;
			stroke = DisplayTools.standardStroke;
		} else if (shorterSide < SIZE_TIER_MEDIUM) {
			outlineStroke = DisplayTools.thickStroke;
			stroke = DisplayTools.standardStroke;
		} else if (shorterSide < SIZE_TIER_THICK) {
			outlineStroke = DisplayTools.ultraThickStroke;
			stroke = DisplayTools.thickStroke;
		} else {
			outlineStroke = new BasicStroke(9);
			stroke = DisplayTools.ultraThickStroke;
		}
		strokeOffset = (int) stroke.getLineWidth();

		double x = (rectangle.getX() + 2 * strokeOffset);
		double y = (rectangle.getY() + strokeOffset + headArcHeight);
		double width = (rectangle.getWidth() - 4 * strokeOffset);
		double height = (rectangle.getHeight() - tailArcHeight * 0.5 - headArcHeight);
		centerRectangle = new Rectangle2D.Double(x, y, width, height);

		double upperLineYMin = rectangle.getY() + strokeOffset + 2 * stroke.getLineWidth();

		headlineRect = new Rectangle2D.Double(rectangle.getX() + headArcWidth * HEAD_ARC_INSET_FACTOR, upperLineYMin,
				rectangle.getWidth() - headArcWidth * 0.7,
				centerRectangle.getY() - rectangle.getY() - strokeOffset - 2 * stroke.getLineWidth());

		if (headlineRect.getHeight() > MIN_HEADLINE_HEIGHT) {
			int lcHeight = (int) (centerRectangle.getY() - rectangle.getY() - strokeOffset - 2 * stroke.getLineWidth());
			int lcWidth = (int) (lcHeight * UPPER_RIGHT_ASPECT_RATIO);
			upperRightRectangle = new Rectangle2D.Double(
					rectangle.getX() + rectangle.getWidth() - headArcWidth * HEAD_ARC_INSET_FACTOR - lcWidth,
					upperLineYMin, lcWidth, lcHeight);
		} else
			upperRightRectangle = null;

		if (upperRightRectangle != null)
			headlineRect = new Rectangle2D.Double(
					rectangle.getX() + strokeOffset + headArcWidth * HEAD_ARC_INSET_FACTOR, upperLineYMin,
					rectangle.getWidth() - headArcWidth * 0.8 - upperRightRectangle.getWidth(),
					centerRectangle.getY() - rectangle.getY() - strokeOffset - 2 * stroke.getLineWidth());
	}

	/**
	 * @return the inner content area (below the headline bar, inside the frame), or
	 *         null before {@link #setRectangle}
	 */
	public Rectangle2D.Double getCenterRectangle() {
		return centerRectangle;
	}

	/**
	 * @return the optional accessory cube area at the top-right of the headline
	 *         bar, or null if there is no room for one
	 */
	public Rectangle2D.Double getUpperRightRectangle() {
		return upperRightRectangle;
	}

	public String getTitle() {
		return headline;
	}

	public void setTitle(String headline) {
		this.headline = headline;
	}

}
