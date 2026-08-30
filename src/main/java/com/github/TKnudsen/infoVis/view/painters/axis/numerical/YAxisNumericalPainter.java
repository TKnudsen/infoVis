package com.github.TKnudsen.infoVis.view.painters.axis.numerical;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.VerticalStringAlignment;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * @version 2.04
 * @since 2016
 */
public class YAxisNumericalPainter<T extends Number> extends AxisNumericalPainter<T> {

	private transient FontMetrics cachedFontMetrics;
	private transient java.awt.Font cachedFont;

	private boolean drawLabelsBetweenMarkers = false;

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

		if (rectangle == null || rectangle.getHeight() == 0)
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
		if (rectangle == null || markerPositionsWithLabels == null || markerPositionsWithLabels.isEmpty())
			return;

		CopyOnWriteArrayList<Entry<Double, String>> markers = markerPositionsWithLabels;

		Color c = g2.getColor();
		Stroke s = g2.getStroke();

		// cache FontMetrics (avoid re-computation)2 %ST&%
		if (cachedFont != font) {
			cachedFont = font;
			cachedFontMetrics = g2.getFontMetrics(font);
		}
		FontMetrics fm = cachedFontMetrics;

		// draw Y-Axis
		g2.setFont(font);
		g2.setColor(color);
		g2.setStroke(stroke);

		// double x = getAxisAlignmentCoordinate();

		double xAxisCoord = getAxisAlignmentCoordinate();
		double xAxisTickOffset = getAxisTickOffset();

		if (drawAxisBetweenAxeMarkersOnly) {
			Entry<Double, String> first = markers.get(0);
			Entry<Double, String> last = markers.get(markers.size() - 1);
			DisplayTools.drawLine(g2, (float) xAxisCoord, first.getKey().floatValue(), (float) xAxisCoord,
					last.getKey().floatValue());
		} else {
			DisplayTools.drawLine(g2, (float) xAxisCoord, (float) rectangle.getMinY(), (float) xAxisCoord,
					(float) rectangle.getMaxY());
		}

		// draw tick marks and labels
		Entry<Double, String> lastMarker = markers.get(markers.size() - 1);

		double ySpace = 0;
		if (drawLabelsBetweenMarkers) {
			if (markers.size() > 1) {
				ySpace = (markers.get(0).getKey() - markers.get(1).getKey()) * 0.5;
			}
		}

		for (Entry<Double, String> pair : markers) {
			double yValue = pair.getKey();
			// tick mark
			DisplayTools.drawLine(g2, (float) xAxisTickOffset, (float) yValue,
					(float) (xAxisTickOffset + markerLineWidth), (float) yValue);

			if (!drawLabels)
				continue;

			double x0 = rectangle.getX() + markerLineWidth + 2;
			// double y0 = pair.equals(lastMarker) ? yValue - 3 : yValue - fm.getHeight() *
			// 0.55;
			double y0 = yValue - fm.getHeight() * 0.55;
			y0 -= ySpace;
			y0 = Math.max(3, y0);
			double w = rectangle.getWidth() - markerLineWidth;
			double h = fm.getHeight();

			if (axisLineAlignment.equals(AxisLineAlignment.CENTER)) {
				x0 = rectangle.getX();
				w = rectangle.getWidth() * 0.5 - 2;
			} else if (axisLineAlignment.equals(AxisLineAlignment.RIGHT)) {
				x0 -= 2;
			}

			StringPainter sp = createLabelPainter(pair.getValue(), x0, y0, w, h);
			sp.setHorizontalStringAlignment(getHorizontalAlignmentForAxis());
			sp.draw(g2);
		}

		if (drawOutline) {
			g2.setPaint(getBorderPaint());
			DisplayTools.drawRectangle(g2, rectangle);
		}

		g2.setStroke(s);
		g2.setColor(c);
	}

	@Override
	protected void drawPhysU(Graphics2D g2) {
		if (rectangle == null || physicalUnit == null || physicalUnit.isEmpty())
			return;

		g2.setColor(fontColor);
		FontMetrics fm = g2.getFontMetrics(font);

		double yOffset = rectangle.getY() + getFontSize() * 2.3;
		double xOffset = (rectangle.getWidth() - fm.stringWidth(physicalUnit)) / 2.0 - 2;

		g2.drawString("[" + physicalUnit + "]", (int) (rectangle.getX() + xOffset), (int) yOffset);
	}

	@Override
	public void setAxisLineAlignment(AxisLineAlignment axisLineAlignment) {
		EnumSet<AxisLineAlignment> allowed = EnumSet.of(AxisLineAlignment.LEFT, AxisLineAlignment.RIGHT,
				AxisLineAlignment.CENTER);

		if (!allowed.contains(axisLineAlignment))
			throw new IllegalArgumentException("YAxisNumericalPainter: axis alignment must be LEFT, RIGHT, or CENTER");

		super.setAxisLineAlignment(axisLineAlignment);
	}

	@Override
	public double getAxisAlignmentCoordinate() {
		switch (axisLineAlignment) {
		case LEFT:
			return rectangle.getMinX();
		case RIGHT:
			return rectangle.getMaxX();
		case CENTER:
			return rectangle.getCenterX();
		default:
			throw new IllegalArgumentException(
					getClass().getSimpleName() + ": illegal AxisLineAlignment: " + axisLineAlignment);
		}
	}

	// ---------------------------------------------------------------------
	// Helper methods
	// ---------------------------------------------------------------------

	private double getAxisTickOffset() {
		switch (axisLineAlignment) {
		case LEFT:
			return getAxisAlignmentCoordinate();
		case CENTER:
			return getAxisAlignmentCoordinate() - 0.5 * markerLineWidth;
		case RIGHT:
			return getAxisAlignmentCoordinate() - markerLineWidth;
		default:
			return 0;
		}
	}

	private StringPainter createLabelPainter(String text, double x, double y, double w, double h) {
		StringPainter sp = new StringPainter(text);
		sp.setRectangle(new Rectangle2D.Double(x, y, w, h));
		sp.setBackgroundPaint(null);
		sp.setVerticalStringAlignment(VerticalStringAlignment.CENTER);
		sp.setFont(font);
		sp.setFontColor(fontColor);
		return sp;
	}

	private HorizontalStringAlignment getHorizontalAlignmentForAxis() {
		if (axisLineAlignment.equals(AxisLineAlignment.LEFT))
			return HorizontalStringAlignment.LEFT;
		return HorizontalStringAlignment.RIGHT;
	}

	public boolean isDrawLabelsBetweenMarkers() {
		return drawLabelsBetweenMarkers;
	}

	public void setDrawLabelsBetweenMarkers(boolean drawLabelsBetweenMarkers) {
		this.drawLabelsBetweenMarkers = drawLabelsBetweenMarkers;
	}

}
