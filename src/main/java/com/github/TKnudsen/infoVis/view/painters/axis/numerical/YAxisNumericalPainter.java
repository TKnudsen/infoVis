package com.github.TKnudsen.infoVis.view.painters.axis.numerical;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Map.Entry;

import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.VerticalStringAlignment;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Draws y axes. starts at the bottom with lowest values. Thus, the value domain
 * is mirrored since the swing 0-orientation is at the top.
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.02
 */
public class YAxisNumericalPainter<T extends Number> extends AxisNumericalPainter<T> {

	public YAxisNumericalPainter(T minValue, T maxValue) {
		this(minValue, maxValue, true);
	}

	public YAxisNumericalPainter(T minValue, T maxValue, boolean flipAxisValues) {
		super(minValue, maxValue, flipAxisValues);

		this.axisLineAlignment = AxisLineAlignment.RIGHT;
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		if (rectangle.getHeight() == 0)
			return;

		double minPixel = rectangle.getMinY();
		double maxPixel = rectangle.getMaxY();

		if (getTickCount() < 2)
			setAxisWorldCoordinates(minPixel, maxPixel);
		else
			setAxisWorldCoordinatesAndCalculateMarkers(minPixel, maxPixel, getTickCount());
	}

	@Override
	public void drawAxis(Graphics2D g2) {
		Color c = g2.getColor();
		Stroke s = g2.getStroke();

		if (rectangle == null)
			return;

		// should not be the case any more. delete after verification.
		if (markerPositionsWithLabels == null)
			setAxisWorldCoordinates(rectangle.getMinY(), rectangle.getMaxY());

		if (markerPositionsWithLabels.isEmpty())
			return;

		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics();

		// draw Y-Axis
		g2.setColor(color);
		g2.setStroke(stroke);
		double x = getAxisAlignmentCoordinate();

		if (drawAxisBetweenAxeMarkersOnly)
			DisplayTools.drawLine(g2, x, markerPositionsWithLabels.get(0).getKey(), x,
					markerPositionsWithLabels.get(markerPositionsWithLabels.size() - 1).getKey());
		else
			DisplayTools.drawLine(g2, x, rectangle.getMinY(), x, rectangle.getMaxY());

		double maxStringWidth = 0;
		for (Entry<Double, String> pair : markerPositionsWithLabels)
			maxStringWidth = Math.max(maxStringWidth, fm.stringWidth(pair.getValue()));

		double xAxisOffset = getAxisAlignmentCoordinate();
		double xAxisTickOffset = 0;
		if (axisLineAlignment.equals(AxisLineAlignment.LEFT)) {
			xAxisTickOffset = xAxisOffset;
		} else if (axisLineAlignment.equals(AxisLineAlignment.CENTER)) {
			xAxisTickOffset = xAxisOffset - 0.5 * markerLineWidth;
		} else if (axisLineAlignment.equals(AxisLineAlignment.RIGHT)) {
			xAxisTickOffset = xAxisOffset - markerLineWidth;
		} else
			xAxisTickOffset = 0;

		// draw markers
		for (Entry<Double, String> pair : markerPositionsWithLabels) {

			// invert points for the y-axis
			double yValue = pair.getKey();

			// draw ticks
			g2.drawLine((int) (xAxisTickOffset), (int) (yValue), (int) (xAxisTickOffset + markerLineWidth),
					(int) (yValue));

			if (drawLabels) {
				double x0 = rectangle.getX() + markerLineWidth + 2;
				double y0 = (pair.equals(markerPositionsWithLabels.get(markerPositionsWithLabels.size() - 1)))
						? pair.getKey() - 3
						: pair.getKey() - fm.getHeight() * 0.55;
				double w = rectangle.getWidth() - markerLineWidth;
				double h = fm.getHeight();
				if (axisLineAlignment.equals(AxisLineAlignment.CENTER)) {
					x0 = rectangle.getX();
					w = rectangle.getWidth() * 0.5 - 2;
				} else if (axisLineAlignment.equals(AxisLineAlignment.RIGHT))
					x0 -= 2;

				StringPainter sp = new StringPainter(pair.getValue());
				sp.setRectangle(new Rectangle2D.Double(x0, y0, w, h));

				sp.setBackgroundPaint(null);
				if (axisLineAlignment.equals(AxisLineAlignment.LEFT))
					sp.setHorizontalStringAlignment(HorizontalStringAlignment.LEFT);
				else if (axisLineAlignment.equals(AxisLineAlignment.CENTER))
					sp.setHorizontalStringAlignment(HorizontalStringAlignment.RIGHT);
				else
					sp.setHorizontalStringAlignment(HorizontalStringAlignment.RIGHT);
				sp.setVerticalStringAlignment(VerticalStringAlignment.CENTER);
				sp.setFont(font);
				sp.setFontColor(fontColor);
				sp.draw(g2);
			}
		}

		if (drawOutline)
			DisplayTools.drawRectangle(g2, rectangle, getBorderPaint());

		g2.setStroke(s);
		g2.setColor(c);
	}

	@Override
	protected void drawPhysU(Graphics2D g2) {
		if (rectangle == null)
			return;
		g2.setColor(fontColor);
		FontMetrics m = g2.getFontMetrics();
		double y_offset = (rectangle.getY() + getFontSize() * 2.3);
		double X_offset = ((rectangle.getWidth() - m.stringWidth(physicalUnit)) / 2.0 - 2);
		if (physicalUnit != null && !physicalUnit.equals(""))
			g2.drawString("[" + physicalUnit + "]", (int) ((int) this.rectangle.getX() + X_offset), (int) y_offset);
	}

	@Override
	public void setAxisLineAlignment(AxisLineAlignment axisLineAlignment) {
		if (axisLineAlignment.equals(AxisLineAlignment.LEFT) || axisLineAlignment.equals(AxisLineAlignment.RIGHT)
				|| axisLineAlignment.equals(AxisLineAlignment.CENTER))
			super.setAxisLineAlignment(axisLineAlignment);
		else
			throw new IllegalArgumentException("YAxisNumericalPainter: axis alignment must be left or right");
	}

	@Override
	public double getAxisAlignmentCoordinate() {
		switch (axisLineAlignment) {
		case LEFT:
			return this.rectangle.getMinX();
		case RIGHT:
			return this.rectangle.getMaxX();
		case CENTER:
			return this.rectangle.getCenterX();
		case TOP:
			throw new IllegalArgumentException(
					getClass().getSimpleName() + ": illegal AxisLineAlignment: " + axisLineAlignment);
		case BOTTOM:
			throw new IllegalArgumentException(
					getClass().getSimpleName() + ": illegal AxisLineAlignment: " + axisLineAlignment);
		default:
			throw new IllegalArgumentException(
					getClass().getSimpleName() + ": unknown AxisLineAlignment: " + axisLineAlignment);
		}
	}

}
