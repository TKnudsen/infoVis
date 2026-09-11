package com.github.TKnudsen.infoVis.view.painters.aggregation;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Bar chart visualization of bucketed element data (see {@link BarsPainter}),
 * adding an optional headline, a legend, and tooltip support around the raw
 * bars.
 *
 * @param <O> the element type held in each bucket
 * @since 2012
 */
public class AggregationBarChartPainter<O> extends ChartPainter implements ITooltip {

	/** Multiplier for headline vertical spacing based on font size */
	private static final double HEADLINE_SPACING_FACTOR = 1.66;

	/** Default bar color */
	private static final Color DEFAULT_BAR_COLOR = new Color(0, 0, 128);

	/** Horizontal alignment factor for centering text */
	private static final double CENTER_ALIGNMENT_FACTOR = 0.5;

	// Constructor attributes
	private String headline;

	// Internal attributes
	protected BarsPainter<O> barsPainter;
	protected Rectangle2D.Double chartRectangle = new Rectangle2D.Double();

	// Setter attributes
	private boolean alignInverseBorder = false;
	protected boolean drawLegend = true;
	protected boolean enableToolTipping = true;

	/**
	 * Creates an aggregation bar chart painter.
	 *
	 * @param elementMapping mapping of elements to bars; must not be null
	 * @param labeling       labels for each bar; may be null
	 * @param headline       optional headline for the chart; may be null
	 * @throws IllegalArgumentException if elementMapping is null
	 */
	public AggregationBarChartPainter(List<List<O>> elementMapping, List<String> labeling, String headline) {
		if (elementMapping == null) {
			throw new IllegalArgumentException("elementMapping cannot be null");
		}

		this.barsPainter = new BarsPainter<>(elementMapping, labeling);
		this.barsPainter.setBackgroundPaint(null);
		this.barsPainter.setColor(DEFAULT_BAR_COLOR);
		this.barsPainter.setFill(true);
		this.barsPainter.setVerticalOrientation(false);
		this.headline = headline;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (g2 == null || rectangle == null)
			return;

		// Save graphics state
		Color originalColor = g2.getColor();
		Paint originalPaint = g2.getPaint();
		Font originalFont = g2.getFont();

		try {
			super.draw(g2);

			// Draw bars
			if (barsPainter != null)
				barsPainter.draw(g2);

			// Draw headline
			drawHeadline(g2);

			// Draw outline
			if (isDrawOutline())
				drawOutline(g2);

		} finally {
			// Restore graphics state
			g2.setColor(originalColor);
			g2.setPaint(originalPaint);
			g2.setFont(originalFont);
		}
	}

	/**
	 * Draws the headline text if one is set.
	 *
	 * @param g2 the graphics context
	 */
	private void drawHeadline(Graphics2D g2) {
		if (headline == null || headline.isEmpty() || rectangle == null)
			return;

		g2.setColor(fontColor);
		g2.setFont(font);

		FontMetrics fm = g2.getFontMetrics();
		float x = (float) (rectangle.getX() + (rectangle.getWidth() - fm.stringWidth(headline)) * CENTER_ALIGNMENT_FACTOR);
		float y = (float) (rectangle.getY() + fm.getHeight());

		g2.drawString(headline, x, y);
	}

	/**
	 * Draws the outline around the chart and bars area.
	 *
	 * @param g2 the graphics context
	 */
	private void drawOutline(Graphics2D g2) {
		Paint borderPaint = getBorderPaint();
		if (borderPaint == null)
			return;

		g2.setPaint(borderPaint);

		if (rectangle != null)
			g2.draw(rectangle);

		if (chartRectangle != null)
			g2.draw(chartRectangle);
	}

	/**
	 * Draws the legend for the bar chart.
	 *
	 * @param g2 the graphics context
	 */
	public void drawLegend(Graphics2D g2) {
		if (g2 == null || barsPainter == null)
			return;

		barsPainter.drawLegend(g2);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null) {
			chartRectangle = null;
			if (barsPainter != null)
				barsPainter.setRectangle(null);

			return;
		}

		this.chartRectangle = calculateChartRectangle(rectangle);

		if (barsPainter != null)
			barsPainter.setRectangle(chartRectangle);
	}

	/**
	 * Calculates the chart rectangle based on the main rectangle and headline
	 * presence.
	 *
	 * @param rectangle the main rectangle
	 * @return the calculated chart rectangle
	 */
	private Rectangle2D.Double calculateChartRectangle(Rectangle2D rectangle) {
		if (headline != null && !headline.isEmpty()) {
			double headlineSpace = HEADLINE_SPACING_FACTOR * getFontSize();
			return new Rectangle2D.Double(rectangle.getX(), rectangle.getY() + headlineSpace, rectangle.getWidth(),
					rectangle.getHeight() - headlineSpace);
		} else {
			return new Rectangle2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
		}
	}

	public List<List<O>> getData() {
		return barsPainter != null ? barsPainter.getElementMapping() : null;
	}

	@Override
	public void setColor(Color color) {
		super.setPaint(color);

		if (barsPainter != null)
			barsPainter.setColor(color);
	}

	@Override
	public void setBorderPaint(Paint borderPaint) {
		super.setBorderPaint(borderPaint);

		if (barsPainter != null)
			barsPainter.setBorderPaint(borderPaint);
	}

	/**
	 * Returns the number of bars in this chart.
	 *
	 * @return the bar count
	 */
	public int getCount() {
		return barsPainter != null ? barsPainter.getCount() : 0;
	}

	/**
	 * Returns whether bars are filled.
	 *
	 * @return true if bars are filled, false if outlined
	 */
	public boolean isFill() {
		return barsPainter != null && barsPainter.isFill();
	}

	/**
	 * Sets whether bars should be filled.
	 *
	 * @param fill true to fill bars, false to outline only
	 */
	public void setFill(boolean fill) {
		if (barsPainter != null)
			barsPainter.setFill(fill);
	}

	/**
	 * Returns the element mapping for all bars.
	 *
	 * @return list of element lists, one per bar
	 */
	public List<List<O>> getElementMapping() {
		return barsPainter != null ? barsPainter.getElementMapping() : null;
	}

	/**
	 * Returns the labels for the bars.
	 *
	 * @return list of bar labels
	 */
	public List<String> getLabeling() {
		return barsPainter != null ? barsPainter.getLabeling() : null;
	}

	/**
	 * Returns the offset between bars.
	 *
	 * @return the bar offset value
	 */
	public double getOffset() {
		return barsPainter != null ? barsPainter.getOffset() : 0.0;
	}

	/**
	 * Sets the offset between bars.
	 *
	 * @param offset the new offset value
	 */
	public void setOffset(double offset) {
		if (barsPainter != null)
			barsPainter.setOffset(offset);
	}

	/**
	 * Returns whether bars are oriented vertically.
	 *
	 * @return true if vertical, false if horizontal
	 */
	public boolean isVerticalOrientation() {
		return barsPainter != null && barsPainter.isVerticalOrientation();
	}

	/**
	 * Sets the bar orientation.
	 *
	 * @param verticalOrientation true for vertical bars, false for horizontal
	 */
	public void setVerticalOrientation(boolean verticalOrientation) {
		if (barsPainter != null)
			barsPainter.setVerticalOrientation(verticalOrientation);
	}

	@Override
	public void setDrawOutline(boolean drawOutline) {
		super.setDrawOutline(drawOutline);
		if (barsPainter != null)
			barsPainter.setDrawOutline(drawOutline);
	}

	/**
	 * Returns the headline text.
	 *
	 * @return the headline, or null if none is set
	 */
	public String getHeadline() {
		return headline;
	}

	/**
	 * Sets the headline text for this chart.
	 *
	 * @param headline the headline text; null or empty to remove headline
	 */
	public void setHeadline(String headline) {
		this.headline = headline;

		// Recalculate layout if rectangle is set
		if (rectangle != null)
			setRectangle(rectangle);
	}

	/**
	 * Returns whether the border is aligned inversely.
	 *
	 * @return true if aligned inversely
	 */
	public boolean isAlignInverseBorder() {
		return alignInverseBorder;
	}

	/**
	 * Sets whether the border should be aligned inversely.
	 *
	 * @param alignInverseBorder true for inverse alignment
	 */
	public void setAlignInverseBorder(boolean alignInverseBorder) {
		this.alignInverseBorder = alignInverseBorder;
		if (barsPainter != null)
			barsPainter.setAlignInverseBorder(alignInverseBorder);
	}

	/**
	 * Returns whether the legend is drawn.
	 *
	 * @return true if legend is drawn
	 */
	public boolean isDrawLegend() {
		return drawLegend;
	}

	/**
	 * Sets whether the legend should be drawn.
	 *
	 * @param drawLegend true to draw legend
	 */
	public void setDrawLegend(boolean drawLegend) {
		this.drawLegend = drawLegend;
	}

	@Override
	public void setFontSize(int fontSize) {
		super.setFontSize(fontSize);

		if (barsPainter != null)
			barsPainter.setFontSize(fontSize);

		// Recalculate layout since headline spacing depends on font size
		if (rectangle != null && headline != null)
			setRectangle(rectangle);
	}

	@Override
	public void setFontStyle(int fontStyle) {
		super.setFontStyle(fontStyle);

		if (barsPainter != null)
			barsPainter.setFontStyle(fontStyle);
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);

		if (barsPainter != null)
			barsPainter.setFont(font);
	}

	@Override
	public void setFontColor(Color fontColor) {
		super.setFontColor(fontColor);

		if (barsPainter != null)
			barsPainter.setFontColor(fontColor);
	}

	/**
	 * Returns whether the legend alignment is inverted.
	 *
	 * @return true if inverted
	 */
	public boolean isInvertLegendAlignment() {
		return barsPainter != null && barsPainter.isInvertLegendAlignment();
	}

	/**
	 * Sets whether the legend alignment should be inverted.
	 *
	 * @param invertLegendAlignment true to invert alignment
	 */
	public void setInvertLegendAlignment(boolean invertLegendAlignment) {
		if (barsPainter != null)
			barsPainter.setInvertLegendAlignment(invertLegendAlignment);
	}

	/**
	 * Returns the colors used for bars.
	 *
	 * @return list of bar colors
	 */
	public List<Color> getColors() {
		return barsPainter != null ? barsPainter.getColors() : null;
	}

	/**
	 * Sets the colors for bars.
	 *
	 * @param colors list of colors to use
	 */
	public void setColors(List<Color> colors) {
		if (barsPainter != null)
			barsPainter.setColors(colors);
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!enableToolTipping || p == null || barsPainter == null)
			return null;

		Rectangle2D barsRect = barsPainter.getRectangle();
		if (barsRect == null || !barsRect.contains(p))
			return null;

		return barsPainter.getTooltip(p);
	}

	@Override
	public boolean isToolTipping() {
		return enableToolTipping;
	}

	@Override
	public void setToolTipping(boolean enableToolTipping) {
		this.enableToolTipping = enableToolTipping;
	}

	/**
	 * Returns the bars painter.
	 *
	 * @return the current bars painter
	 */
	public BarsPainter<O> getBarsPainter() {
		return barsPainter;
	}

	/**
	 * Sets the bars painter.
	 *
	 * @param barsPainter the new bars painter
	 * @throws IllegalArgumentException if barsPainter is null
	 */
	public void setBarsPainter(BarsPainter<O> barsPainter) {
		if (barsPainter == null)
			throw new IllegalArgumentException("barsPainter cannot be null");

		this.barsPainter = barsPainter;

		// Update layout
		if (rectangle != null)
			setRectangle(rectangle);
	}
}
