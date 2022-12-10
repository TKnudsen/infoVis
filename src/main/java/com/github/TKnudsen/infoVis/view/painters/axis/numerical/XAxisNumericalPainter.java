package com.github.TKnudsen.infoVis.view.painters.axis.numerical;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Map.Entry;

import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.03
 */
public class XAxisNumericalPainter<T extends Number> extends AxisNumericalPainter<T> {

	public XAxisNumericalPainter(T minValue, T maxValue) {
		super(minValue, maxValue);

		this.axisLineAlignment = AxisLineAlignment.TOP;
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		if (rectangle.getWidth() == 0)
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
		Color c = g2.getColor();
		Stroke s = g2.getStroke();
		Font f = g2.getFont();

		g2.setStroke(stroke);
		g2.setFont(font);

		if (rectangle == null)
			return;

		// should not be the case any more. delete after verification.
		if (markerPositionsWithLabels == null)
			setAxisWorldCoordinates(rectangle.getMinX(), rectangle.getMaxX());

		if (markerPositionsWithLabels.isEmpty())
			return;

		// draw X-Axis
		g2.setPaint(getPaint());
		double x1 = this.rectangle.getMinX();
		double x2 = this.rectangle.getMaxX();

		if (drawAxisBetweenAxeMarkersOnly) {
			if (markerPositionsWithLabels == null || markerPositionsWithLabels.size() == 0)
				System.err.println("markerPositionsWithLabels was null. please validate");
			else {
				x1 = markerPositionsWithLabels.get(0).getKey();
				x2 = markerPositionsWithLabels.get(markerPositionsWithLabels.size() - 1).getKey();
			}
		}

		double y = getAxisAlignmentCoordinate();

		DisplayTools.drawLine(g2, x1, y, x2, y);

		drawAxisLabelsAndMarkers(g2);

		if (isDrawOutline())
			DisplayTools.drawRectangle(g2, rectangle, getBorderPaint());

		g2.setFont(f);
		g2.setStroke(s);
		g2.setColor(c);
	}

	/**
	 * general routine to draw labels and markers for every marker position (was
	 * calculated with every setRectangle()-event).
	 * 
	 * @param g2 g2
	 */
	protected void drawAxisLabelsAndMarkers(Graphics2D g2) {
		Color c = g2.getColor();
		Stroke s = g2.getStroke();
		Font f = g2.getFont();

		g2.setStroke(stroke);
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics();

		// should not be the case any more. delete after verification.
		if (markerPositionsWithLabels == null)
			setAxisWorldCoordinates(rectangle.getMinX(), rectangle.getMaxX());

		for (Entry<Double, String> pair : markerPositionsWithLabels) {
			double artificialXOffset = 0;
			if (pair.getKey() > rectangle.getMaxX() - 15)
				artificialXOffset = -getMarkerDistanceInPixels() * 0.28;

			g2.setPaint(getPaint());
			if (axisLineAlignment.equals(AxisLineAlignment.TOP))
				DisplayTools.drawLine(g2, pair.getKey(), rectangle.getMinY() + markerLineWidth, pair.getKey(),
						rectangle.getMinY());
			else
				DisplayTools.drawLine(g2, pair.getKey(), rectangle.getMaxY() - markerLineWidth, pair.getKey(),
						rectangle.getMaxY());
			g2.setColor(fontColor);

			if (drawLabels)
				g2.drawString(pair.getValue(),
						(int) (pair.getKey().intValue() + 1 + artificialXOffset
								- fm.stringWidth(pair.getValue()) * 0.4),
						(int) ((rectangle.getY()) + fm.getHeight() * 1.0) + 4);
		}

		g2.setFont(f);
		g2.setStroke(s);
		g2.setColor(c);
	}

	@Override
	protected void drawPhysU(Graphics2D g2) {
		if (rectangle == null || physicalUnit == null || physicalUnit.equals(""))
			return;

		Color c = g2.getColor();

		g2.setColor(fontColor);
		FontMetrics m = g2.getFontMetrics();
		double y_offset = this.rectangle.getY() + m.getHeight() * 2.0 + rectangle.getHeight() * 0.15;
		double X_offset = (rectangle.getWidth() - m.stringWidth(physicalUnit) * 1.2 - 2);
		// if (physicalUnit != null && !physicalUnit.equals(""))
		g2.drawString("[" + physicalUnit + "]", (int) (this.rectangle.getX() + X_offset), (int) y_offset);

		g2.setColor(c);
	}

	@Override
	public void setAxisLineAlignment(AxisLineAlignment axisLineAlignment) {
		if (axisLineAlignment.equals(AxisLineAlignment.TOP) || axisLineAlignment.equals(AxisLineAlignment.BOTTOM)
				|| axisLineAlignment.equals(AxisLineAlignment.CENTER))
			super.setAxisLineAlignment(axisLineAlignment);
		else
			throw new IllegalArgumentException("YXAxisNumericalPainter: axis alignment must be top or bottom");
	}

	@Override
	public double getAxisAlignmentCoordinate() {
		switch (axisLineAlignment) {
		case LEFT:
			throw new IllegalArgumentException(
					getClass().getSimpleName() + ": illegal AxisLineAlignment: " + axisLineAlignment);
		case RIGHT:
			throw new IllegalArgumentException(
					getClass().getSimpleName() + ": illegal AxisLineAlignment: " + axisLineAlignment);
		case CENTER:
			return this.rectangle.getCenterY();
		case TOP:
			return this.rectangle.getY();
		case BOTTOM:
			return this.rectangle.getMaxY();
		default:
			throw new IllegalArgumentException(
					getClass().getSimpleName() + ": unknown AxisLineAlignment: " + axisLineAlignment);
		}
	}

}
