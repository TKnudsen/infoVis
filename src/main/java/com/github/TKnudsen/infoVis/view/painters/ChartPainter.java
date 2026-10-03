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
import com.github.TKnudsen.infoVis.view.ui.LookAndFeelTools;

/**
 * <p>
 * ChartPainter provides common functionality and properties for rendering
 * charts, including colors, fonts, strokes, rectangles, and background/border
 * paints. All concrete painter implementations should extend this class.
 * 
 * Common features provided:
 * <ul>
 * <li>Configurable background and border paints</li>
 * <li>Font management (family, size, style, color)</li>
 * <li>Stroke configuration for borders and outlines</li>
 * <li>Rectangle bounds for layout and clipping</li>
 * <li>Highlight state for interactive selection</li>
 * <li>Anti-aliasing rendering hints</li>
 * <li>BufferedImage export capability</li>
 * </ul>
 * 
 * Subclasses should override the {@link #draw(Graphics2D)} method to implement
 * their specific rendering logic, calling {@code super.draw(g2)} first to
 * ensure proper background rendering and anti-aliasing setup.
 * </p>
 *
 * @version 2.10, revised in September 2026
 * @since 2016
 */
public abstract class ChartPainter {

	/**
	 * Background paint for the chart. Left as a plain literal, unlike the fields
	 * below -- the constructor immediately overwrites it via
	 * {@code setBackgroundPaint(null)} (transparent, so a hosting panel's own
	 * background shows through), so this initial value is never actually observed.
	 * 
	 * Overall, the applications' look and feel and its corresponding panels have
	 * background colors, so that the painter by default does not need one.
	 */
	protected Paint backgroundPaint = null;

	/**
	 * Border paint for the chart. Resolved from the active look-and-feel (see
	 * {@link LookAndFeelTools#uiColor(String, Color)}) so a painter constructed
	 * under e.g. a dark Nimbus theme doesn't come out with a fixed light-theme
	 * literal.
	 */
	private Paint borderPaint = LookAndFeelTools.uiColor("controlDkShadow", Color.DARK_GRAY);

	/**
	 * Default paint, e.g., used for linking objects. Resolved from the active
	 * look-and-feel, see {@link LookAndFeelTools#uiColor(String, Color)}.
	 */
	private Paint paint = LookAndFeelTools.uiColor("Label.foreground", Color.BLACK);

	/**
	 * Stroke for drawing borders and outlines
	 */
	protected BasicStroke stroke = DisplayTools.standardStroke;

	/**
	 * Rectangle defining the bounds of the painter. Private -- subclasses must go
	 * through {@link #getRectangle()}/{@link #setRectangle(Rectangle2D)}, never
	 * touch it directly, so it can never be mutated in place behind the painter's
	 * back.
	 */
	private Rectangle2D rectangle = null;

	/**
	 * Rectangle defining the actual drawing area of the painter. Private --
	 * subclasses must go through {@link #getChartRectangle()}/
	 * {@link #setChartRectangle(Rectangle2D)}, never touch it directly, so it can
	 * never be mutated in place behind the painter's back.
	 */
	private Rectangle2D chartRectangle = null;

	/**
	 * Interaction feature indicating if painter is in selected/highlighted state.
	 * Visibility changed to private in 2026, as getter and setter exists for years
	 * now.
	 */
	private boolean highlighted = false;

	/**
	 * Whether the painter should be drawn with an outline. Visibility changed to
	 * private in 2026, as getter and setter exists for years now.
	 * 
	 * Try to avoid direct access, use {@link #isDrawOutline()} instead
	 */
	private boolean drawOutline = false;

	/**
	 * Font for text rendering
	 */
	protected Font font = new Font("Tahoma", Font.PLAIN, 9);

	/**
	 * Color for text rendering. Resolved from the active look-and-feel, see
	 * {@link LookAndFeelTools#uiColor(String, Color)}.
	 */
	protected Color fontColor = LookAndFeelTools.uiColor("Label.foreground", Color.BLACK);

	public ChartPainter() {
		setBackgroundPaint(null);
	}

	/**
	 * Draws this painter using the specified graphics context.
	 * 
	 * <p>
	 * This base implementation:
	 * <ol>
	 * <li>Enables anti-aliasing</li>
	 * <li>Fills the background rectangle if background paint is set</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>
	 * Subclasses should override this method to add their specific rendering,
	 * calling {@code super.draw(g2)} first.
	 * </p>
	 *
	 * <p>
	 * <b>{@code rectangle} starts out {@code null} and is guaranteed to be
	 * {@code null} at least once:</b> a hosting panel (e.g.
	 * {@code InfoVisChartPanel}) calls {@link #setRectangle(Rectangle2D)} with
	 * {@code null} once at construction, before it has a real size to lay out
	 * against. This base implementation already handles that (it simply returns),
	 * but {@code super.draw(g2)} returning early does not stop an overriding
	 * subclass's own code from running afterwards -- every override must
	 * independently guard against {@code rectangle}/{@code chartRectangle} (via
	 * {@link #getRectangle()}/{@link #getChartRectangle()}) being {@code null},
	 * typically by returning immediately if so.
	 * </p>
	 *
	 * @param g2 the graphics context
	 */
	public void draw(Graphics2D g2) {
		if (g2 == null)
			return;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (rectangle == null || getBackgroundPaint() == null)
			return;

		// Save and restore color
		Color originalColor = g2.getColor();
		try {
			g2.setPaint(getBackgroundPaint());
			g2.fill(rectangle);
		} finally {
			g2.setColor(originalColor);
		}
	}

	/**
	 * Creates a BufferedImage representation of this painter.
	 * 
	 * @return BufferedImage containing the rendered chart, or null if rectangle is
	 *         invalid
	 */
	public BufferedImage getBufferedImage() {
		if (rectangle == null) {
			return null;
		}

		// Check for valid dimensions (FIXED: was >= 0, should be <= 0)
		if (rectangle.getWidth() <= 0 || rectangle.getHeight() <= 0) {
			return null;
		}

		int width = (int) Math.ceil(rectangle.getWidth());
		int height = (int) Math.ceil(rectangle.getHeight());

		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufferedImage.createGraphics();

		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Translate to origin
			AffineTransform old = g2.getTransform();
			AffineTransform translate = AffineTransform.getTranslateInstance(-rectangle.getX(), -rectangle.getY());
			g2.transform(translate);

			draw(g2);

			g2.setTransform(old);
		} finally {
			g2.dispose();
		}

		return bufferedImage;
	}

	/**
	 * Sets the bounding rectangle for this painter. Also updates the chart
	 * rectangle to match.
	 *
	 * <p>
	 * Called with {@code null} at least once by most hosting panels, before a real
	 * size is known -- see the note on {@link #draw(Graphics2D)}. An override must
	 * handle {@code null} explicitly (it is not rejected or substituted here).
	 * </p>
	 *
	 * @param rectangle the bounding rectangle; null to clear
	 */
	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		setChartRectangle(rectangle);
	}

	/**
	 * Returns the bounding rectangle of this painter.
	 * 
	 * @return the bounding rectangle, or null if not set
	 */
	public Rectangle2D getRectangle() {
		return this.rectangle;
	}

	/**
	 * Sets the background paint.
	 * 
	 * @param backgroundPaint the background paint; null for transparent background
	 */
	public void setBackgroundPaint(Paint backgroundPaint) {
		this.backgroundPaint = backgroundPaint;
	}

	/**
	 * Returns the background paint.
	 * 
	 * @return the background paint, or null if transparent
	 */
	public Paint getBackgroundPaint() {
		return backgroundPaint;
	}

	/**
	 * Returns the border paint.
	 * 
	 * @return the border paint, or null if no border
	 */
	public Paint getBorderPaint() {
		return borderPaint;
	}

	/**
	 * Sets the border paint.
	 * 
	 * @param borderPaint the border paint; null for no border
	 */
	public void setBorderPaint(Paint borderPaint) {
		this.borderPaint = borderPaint;
	}

	/**
	 * Returns the panel rectangle (alias for {@link #getRectangle()}).
	 * 
	 * @return the bounding rectangle
	 * @deprecated use {@link #getRectangle()} instead
	 */
	@Deprecated
	public Rectangle2D getPanel() {
		return rectangle;
	}

	/**
	 * A type-narrowing view onto {@link #getPaint()}: returns it as a {@code Color}
	 * if it is one, or {@code Color.BLACK} if the current paint is something else
	 * (e.g. a gradient or texture) that doesn't reduce to a single color.
	 *
	 * @return the current paint as a {@code Color}, or {@code Color.BLACK} if it
	 *         isn't one
	 * @deprecated use {@link #getPaint()} instead, which doesn't lose
	 *             non-{@code Color} paints
	 */
	@Deprecated
	public Color getColor() {
		return paint instanceof Color ? (Color) paint : Color.BLACK;
	}

	/**
	 * Equivalent to {@link #setPaint(Paint)}; kept only because {@code Color} is
	 * such a common argument type in caller code.
	 *
	 * @param color the color
	 * @deprecated use {@link #setPaint(Paint)} instead
	 */
	@Deprecated
	public void setColor(Color color) {
		setPaint(color);
	}

	/**
	 * Returns the current paint.
	 *
	 * @return the paint
	 */
	public Paint getPaint() {
		return paint;
	}

	/**
	 * Sets the paint.
	 *
	 * @param paint the paint to set
	 */
	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	/**
	 * Sets the stroke for drawing.
	 * 
	 * @param stroke the stroke to use
	 * @throws IllegalArgumentException if stroke is null
	 */
	public void setStroke(BasicStroke stroke) {
		if (stroke == null) {
			throw new IllegalArgumentException("stroke cannot be null");
		}
		this.stroke = stroke;
	}

	/**
	 * Returns the stroke.
	 * 
	 * @return the current stroke
	 */
	public BasicStroke getStroke() {
		return stroke;
	}

	/**
	 * Sets the highlight state.
	 * 
	 * @param highlighted true to highlight, false otherwise
	 */
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	/**
	 * Returns whether this painter is highlighted.
	 * 
	 * @return true if highlighted
	 */
	public boolean isHighlighted() {
		return highlighted;
	}

	/**
	 * Sets the font size.
	 * 
	 * @param fontSize the font size in points
	 * @throws IllegalArgumentException if fontSize is less than 1
	 * @throws IllegalStateException    if font is null
	 */
	public void setFontSize(int fontSize) {
		if (fontSize < 1) {
			throw new IllegalArgumentException("fontSize must be at least 1");
		}
		if (font == null) {
			throw new IllegalStateException("Cannot set font size when font is null");
		}
		this.font = new Font(font.getName(), font.getStyle(), fontSize);
	}

	/**
	 * Returns the font size.
	 * 
	 * @return the font size in points
	 * @throws NullPointerException if font is null
	 */
	public int getFontSize() {
		if (font == null) {
			throw new NullPointerException("ChartPainter: using font size requires having a font reference.");
		}
		return font.getSize();
	}

	/**
	 * Returns the font name.
	 * 
	 * @return the font name, or null if font is not set
	 */
	public String getFontName() {
		if (font == null) {
			return null;
		}
		return font.getFontName();
	}

	/**
	 * Sets the font style.
	 * 
	 * @param fontStyle the font style (e.g., Font.PLAIN, Font.BOLD, Font.ITALIC)
	 * @throws IllegalStateException if font is null
	 */
	public void setFontStyle(int fontStyle) {
		if (font == null) {
			throw new IllegalStateException("Cannot set font style when font is null");
		}
		this.font = new Font(font.getName(), fontStyle, font.getSize());
	}

	/**
	 * Returns the font style.
	 * 
	 * @return the font style (e.g., Font.PLAIN, Font.BOLD), or Font.PLAIN if font
	 *         is null
	 */
	public final int getFontStyle() {
		if (font == null) {
			return Font.PLAIN;
		}
		return font.getStyle();
	}

	/**
	 * Returns the font.
	 * 
	 * @return the current font
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * Sets the font.
	 * 
	 * @param font the font to use
	 * @throws IllegalArgumentException if font is null
	 */
	public void setFont(Font font) {
		if (font == null) {
			throw new IllegalArgumentException("font cannot be null");
		}
		this.font = font;
	}

	/**
	 * Sets the font color.
	 * 
	 * @param fontColor the font color
	 * @throws IllegalArgumentException if fontColor is null
	 */
	public void setFontColor(Color fontColor) {
		if (fontColor == null) {
			throw new IllegalArgumentException("fontColor cannot be null");
		}
		this.fontColor = fontColor;
	}

	/**
	 * Returns the font color.
	 * 
	 * @return the font color
	 */
	public final Color getFontColor() {
		return fontColor;
	}

	/**
	 * Returns whether outlines are drawn.
	 * 
	 * @return true if outlines are drawn
	 */
	public boolean isDrawOutline() {
		return drawOutline;
	}

	/**
	 * Sets whether outlines should be drawn.
	 * 
	 * @param drawOutline true to draw outlines
	 */
	public void setDrawOutline(boolean drawOutline) {
		this.drawOutline = drawOutline;
	}

	/**
	 * Tests whether the specified point is contained within this painter's bounds.
	 * 
	 * @param p the point to test
	 * @return true if the point is contained, false otherwise
	 */
	public boolean contains(Point2D p) {
		if (p == null || rectangle == null) {
			return false;
		}
		return rectangle.contains(p);
	}

	/**
	 * Returns the chart rectangle (the actual drawing area).
	 * 
	 * @return the chart rectangle, or null if not set
	 */
	public Rectangle2D getChartRectangle() {
		return chartRectangle;
	}

	/**
	 * Sets the chart rectangle (the actual drawing area).
	 * 
	 * @param chartRectangle the chart rectangle; null to clear
	 */
	public void setChartRectangle(Rectangle2D chartRectangle) {
		this.chartRectangle = chartRectangle;
	}

	// ==================== CONVENIENCE METHODS ====================

	/**
	 * Returns whether this painter has a valid rectangle set.
	 * 
	 * @return true if rectangle is set and has positive dimensions
	 */
	public boolean hasValidRectangle() {
		return rectangle != null && rectangle.getWidth() > 0 && rectangle.getHeight() > 0;
	}

	/**
	 * Returns whether a background paint is set.
	 * 
	 * @return true if background paint is not null
	 */
	public boolean hasBackground() {
		return backgroundPaint != null;
	}

	/**
	 * Returns whether a border paint is set.
	 * 
	 * @return true if border paint is not null
	 */
	public boolean hasBorder() {
		return borderPaint != null;
	}

	/**
	 * Resets all visual properties to their defaults -- the same
	 * look-and-feel-aware colors the constructor would produce if called now (see
	 * {@link LookAndFeelTools#uiColor(String, Color)}), not the fixed literals a
	 * newly-constructed painter might have gotten under a different, earlier L&F.
	 */
	public void resetToDefaults() {
		this.backgroundPaint = LookAndFeelTools.uiColor("Panel.background", Color.WHITE);
		this.borderPaint = LookAndFeelTools.uiColor("controlDkShadow", Color.DARK_GRAY);
		this.paint = LookAndFeelTools.uiColor("Label.foreground", Color.BLACK);
		this.stroke = DisplayTools.standardStroke;
		this.font = new Font("Tahoma", Font.PLAIN, 9);
		this.fontColor = LookAndFeelTools.uiColor("Label.foreground", Color.BLACK);
		this.drawOutline = false;
		this.highlighted = false;
	}

	/**
	 * Compact one-line summary of this painter's visual state -- useful for
	 * debugging (e.g. a hosting panel's list of chart painters prints readably
	 * instead of as opaque {@code Object@hashcode} entries). Subclasses that add
	 * their own visually-relevant fields may override this and append to
	 * {@code super.toString()}.
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[rectangle=" + rectangle + ", chartRectangle=" + chartRectangle
				+ ", paint=" + paint + ", stroke=" + stroke + ", drawOutline=" + drawOutline + ", highlighted="
				+ highlighted + "]";
	}
}
