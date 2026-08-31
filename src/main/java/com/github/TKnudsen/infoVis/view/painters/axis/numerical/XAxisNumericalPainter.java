package com.github.TKnudsen.infoVis.view.painters.axis.numerical;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;

import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * @version 2.1 2.1 (re-factored for clarity and efficiency)
 * @since 2016
 */
public class XAxisNumericalPainter<T extends Number> extends AxisNumericalPainter<T> {

	private transient FontMetrics cachedFontMetrics;
	private transient java.awt.Font cachedFont;
	// FontMetrics depends on more than just the Font -- the same Font renders
	// differently under a different transform/antialiasing/fractional-metrics
	// setup (e.g. screen vs. export/print Graphics2D). FontRenderContext (not
	// Graphics2D identity -- Swing hands out a new Graphics2D per repaint even
	// for the same on-screen panel) captures exactly that, with proper
	// value-based equals().
	private transient FontRenderContext cachedFontRenderContext;

	public XAxisNumericalPainter(T minValue, T maxValue) {
		super(minValue, maxValue);

		this.axisLineAlignment = AxisLineAlignment.TOP;
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null || rectangle.getWidth() == 0)
			return;

		double minPixel = rectangle.getMinX();
		double maxPixel = rectangle.getMaxX();

		if (getTickCount() < 2)
			setAxisWorldCoordinates(minPixel, maxPixel);
		else
			setAxisWorldCoordinatesAndCalculateMarkers(minPixel, maxPixel, getTickCount());
	}

	@Override
	/**
	 * TODO: generate abstract method that doesn't care for orientation.
	 */
	public void drawAxis(Graphics2D g2) {
		if (rectangle == null || markerPositionsWithLabels == null || markerPositionsWithLabels.isEmpty())
			return;

		// Make a defensive copy immediately to avoid race conditions
	    List<Entry<Double, String>> markers = new ArrayList<>(markerPositionsWithLabels);
	    
		Color c = g2.getColor();
		Stroke s = g2.getStroke();
		Font f = g2.getFont();

		// Cache FontMetrics
		FontRenderContext frc = g2.getFontRenderContext();
		if (cachedFont != font || !frc.equals(cachedFontRenderContext)) {
			cachedFont = font;
			cachedFontRenderContext = frc;
			cachedFontMetrics = g2.getFontMetrics(font);
		}
		FontMetrics fm = cachedFontMetrics;

		g2.setFont(font);
		g2.setStroke(stroke);
		g2.setPaint(getPaint());

		// Compute axis line coordinates
		float y = (float) getAxisAlignmentCoordinate();
		float x1 = (float) rectangle.getMinX();
		float x2 = (float) rectangle.getMaxX();

		if (drawAxisBetweenAxeMarkersOnly) {
			if (markers == null || markers.size() == 0)
				System.err.println("markerPositionsWithLabels was null. please validate");
			else {
				x1 = markers.get(0).getKey().floatValue();
				x2 = markers.get(markers.size() - 1).getKey().floatValue();
			}
		}

		// Draw main axis line
		if (rectangle.getHeight() > 0)
			DisplayTools.drawLine(g2, x1, y, x2, y);

		// Draw tick marks and labels
		drawAxisLabelsAndMarkers(g2, fm);

		if (isDrawOutline()) {
			g2.setPaint(getBorderPaint());
			DisplayTools.drawRectangle(g2, rectangle);
		}

		// Restore state
		g2.setFont(f);
		g2.setStroke(s);
		g2.setColor(c);
	}

	/**
	 * Draws tick marks and labels along the X axis.
	 */
	protected void drawAxisLabelsAndMarkers(Graphics2D g2, FontMetrics fm) {
		if (markerPositionsWithLabels == null)
			return;

		float yTop = (float) rectangle.getMinY();
		float yBottom = (float) rectangle.getMaxY();
		float tickLen = (float) markerLineWidth;

		for (Entry<Double, String> pair : markerPositionsWithLabels) {
			float x = pair.getKey().floatValue();

			// Draw tick mark
			if (axisLineAlignment.equals(AxisLineAlignment.TOP))
				DisplayTools.drawLine(g2, x, yTop + tickLen, x, yTop);
			else
				DisplayTools.drawLine(g2, x, yBottom - tickLen, x, yBottom);

			// Draw label
			if (drawLabels) {
				String label = pair.getValue();
				int labelWidth = fm.stringWidth(label);
				float artificialXOffset = 0f;

				// Avoid clipping at right edge
				if (x > rectangle.getMaxX() - 15)
					artificialXOffset = -(float) (getMarkerDistanceInPixels() * 0.28);

				float textX = x + artificialXOffset - labelWidth * 0.4f;
				float textY = (float) (rectangle.getY() + fm.getHeight() * 1.0 + 4);

				g2.setColor(fontColor);
				g2.drawString(label, textX, textY);
				g2.setPaint(getPaint());
			}
		}

	}

	@Override
	protected void drawPhysU(Graphics2D g2) {
		if (rectangle == null || physicalUnit == null || physicalUnit.isEmpty())
			return;

		g2.setColor(fontColor);
		FontMetrics fm = g2.getFontMetrics(font);

		double yOffset = rectangle.getY() + fm.getHeight() * 2.0 + rectangle.getHeight() * 0.15;
		double xOffset = rectangle.getWidth() - fm.stringWidth(physicalUnit) * 1.2 - 2;

		g2.drawString("[" + physicalUnit + "]", (int) (rectangle.getX() + xOffset), (int) yOffset);
	}

	@Override
	public void setAxisLineAlignment(AxisLineAlignment axisLineAlignment) {
		EnumSet<AxisLineAlignment> allowed = EnumSet.of(AxisLineAlignment.TOP, AxisLineAlignment.BOTTOM,
				AxisLineAlignment.CENTER);

		if (!allowed.contains(axisLineAlignment))
			throw new IllegalArgumentException("XAxisNumericalPainter: axis alignment must be TOP, BOTTOM, or CENTER");

		super.setAxisLineAlignment(axisLineAlignment);
	}

	@Override
	public double getAxisAlignmentCoordinate() {
		switch (axisLineAlignment) {
		case CENTER:
			return rectangle.getCenterY();
		case TOP:
			return rectangle.getY();
		case BOTTOM:
			return rectangle.getMaxY();
		default:
			throw new IllegalArgumentException(
					getClass().getSimpleName() + ": illegal AxisLineAlignment: " + axisLineAlignment);
		}
	}

}
