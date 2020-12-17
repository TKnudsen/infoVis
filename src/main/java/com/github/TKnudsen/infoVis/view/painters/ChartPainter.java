package com.github.TKnudsen.infoVis.view.painters;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Basic class for all painters. contains fields relevant for most painters. In
 * future the number of fields may be decreased if one or another field does not
 * proof to be general enough.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.01
 */
public abstract class ChartPainter {

	/**
	 * Background color
	 */
	protected Paint backgroundPaint = Color.WHITE;

	/**
	 * Border color
	 */
	private Paint borderPaint = Color.DARK_GRAY;

	/**
	 * @deprecated use colorPaint.
	 */
	protected Color color = Color.BLACK;

	/**
	 * Default effect paint, e.g., used for linking objects.
	 */
	private Paint paint = Color.BLACK;

	/**
	 * Stroke of the painter
	 */
	protected BasicStroke stroke = DisplayTools.standardStroke;

	/**
	 * Rectangle defining the bounds of the painter.
	 */
	protected Rectangle2D rectangle = null;

	/**
	 * Rectangle defining the drawing area of the painter
	 */
	protected Rectangle2D chartRectangle = null;

	/**
	 * Interaction feature. can be used to shift the painter in a selected-state.
	 */
	protected boolean highlighted = false;

	@Deprecated
	/**
	 * Variable that characterizes whether the painter is drawn with an outline or
	 * not.
	 * 
	 * Try to avoid this in such a general sense.
	 */
	protected boolean drawOutline = false;

	/**
	 * Font
	 */
	protected Font font = new Font("Tahoma", java.awt.Font.PLAIN, 9);

	/**
	 * font color
	 */
	protected Color fontColor = Color.black;

	/**
	 * Paints the rendering information
	 * 
	 * @param g2
	 */
	public void draw(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (rectangle == null)
			return;

		if (getBackgroundPaint() == null)
			return;

		Color color = g2.getColor();

		g2.setPaint(getBackgroundPaint());
		g2.fill(rectangle);

		g2.setColor(color);
	}

	public BufferedImage getBufferedImage() {
		if (rectangle == null)
			return null;
		BufferedImage bufferedImage = new BufferedImage((int) rectangle.getWidth(), (int) rectangle.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufferedImage.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		AffineTransform old = g2.getTransform();
		AffineTransform translate = AffineTransform.getTranslateInstance(-rectangle.getX(), -rectangle.getY());
		g2.transform(translate);
		draw(g2);
		g2.setTransform(old);

		return bufferedImage;
	}

	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		setChartRectangle(rectangle);
	}

	public Rectangle2D getRectangle() {
		return this.rectangle;
	}

	public void setBackgroundPaint(Paint backgroundColor) {
		this.backgroundPaint = backgroundColor;
	}

	public Paint getBackgroundPaint() {
		return backgroundPaint;
	}

	public Paint getBorderPaint() {
		return borderPaint;
	}

	public void setBorderPaint(Paint borderColor) {
		this.borderPaint = borderColor;
	}

	public Rectangle2D getPanel() {
		return rectangle;
	}

	/**
	 * @deprecated use getPaint
	 * @return
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @deprecated uses setPaint
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;

		setPaint(color);
	}

	public Paint getPaint() {
		return paint;
	}

	public void setPaint(Paint colorPaint) {
		this.paint = colorPaint;

		if (colorPaint instanceof Color)
			this.color = (Color) colorPaint;
	}

	public void setStroke(BasicStroke stroke) {
		this.stroke = stroke;
	}

	public BasicStroke getStroke() {
		return stroke;
	}

	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	public void setFontSize(int fontSize) {
		this.font = new Font(font.getName(), font.getStyle(), fontSize);
	}

	public int getFontSize() {
		if (font == null)
			throw new NullPointerException("ChartPainter: using font size requires having a font reference.");

		return font.getSize();
	}

	public String getFontName() {
		if (font == null)
			return null;

		return font.getFontName();
	}

	public void setFontStyle(int fontStyle) {
		this.font = new Font(font.getName(), fontStyle, font.getSize());
	}

	public final int getFontStyle() {
		if (font == null)
			return 0;// default (plain)

		return font.getStyle();
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public void setFontColor(Color fontColor) {
		this.fontColor = fontColor;
	}

	public final Color getFontColor() {
		return fontColor;
	}

	public boolean isDrawOutline() {
		return drawOutline;
	}

	public void setDrawOutline(boolean drawOutline) {
		this.drawOutline = drawOutline;
	}

	public boolean contains(Point2D p) {
		if (rectangle == null)
			return false;

		return rectangle.contains(p);
	}

	public Rectangle2D getChartRectangle() {
		return chartRectangle;
	}

	public void setChartRectangle(Rectangle2D chartRectangle) {
		this.chartRectangle = chartRectangle;
	}

}
