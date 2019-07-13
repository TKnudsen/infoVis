package com.github.TKnudsen.infoVis.view.painters.axis.categorical;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.VerticalStringAlignment;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Paints categorical labels along a Y axis.
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 */
public class YAxisCategoricalPainter<T extends List<String>> extends AxisCategoricalPainter<T> {

	private boolean invertAxisLabels = true;

	public YAxisCategoricalPainter(T markerLabels) {
		super(markerLabels);
	}

	public YAxisCategoricalPainter(T markerLabels, List<Color> labelColors) {
		super(markerLabels, labelColors);
	}

	protected void refreshLabelPainters() {
		labelPainters = new ArrayList<>();

		if (rectangle == null)
			return;

		axisRectangle = new Rectangle2D.Double(rectangle.getMinX(), rectangle.getMinY() + offsetWithinChartRectangle,
				rectangle.getWidth(), rectangle.getHeight() - (2 * offsetWithinChartRectangle));

		if (markerLabels != null)
			for (int i = 0; i < markerLabels.size(); i++) {
				String string = markerLabels.get(i);
				StringPainter painter = new StringPainter(string);
				painter.setHighlighted(false);
				painter.setBackgroundPaint(null);
				painter.setDrawOutline(false);
				painter.setVerticalOrientation(false);
				painter.setHorizontalStringAlignment(HorizontalStringAlignment.RIGHT);
				painter.setVerticalStringAlignment(VerticalStringAlignment.CENTER);
				painter.setFont(this.font);
				painter.setFontColor(fontColor);

				if (this.labelColors != null && labelColors.size() > i)
					painter.setFontColor(labelColors.get(i));

				labelPainters.add(painter);
			}

		markerPositionsWithLabels = new ArrayList<>();
		double deltaY = axisRectangle.getHeight() / (double) labelPainters.size();
		for (int i = 0; i < labelPainters.size(); i++) {
			if (invertAxisLabels)
				labelPainters.get(i)
						.setRectangle(new Rectangle2D.Double(axisRectangle.getMinX(),
								axisRectangle.getMinY() + (labelPainters.size() - 1 - i) * deltaY,
								axisRectangle.getWidth(), deltaY));
			else
				labelPainters.get(i).setRectangle(new Rectangle2D.Double(axisRectangle.getMinX(),
						axisRectangle.getMinY() + i * deltaY, axisRectangle.getWidth(), deltaY));
			markerPositionsWithLabels.add(new AbstractMap.SimpleEntry<Double, String>(
					labelPainters.get(i).getRectangle().getCenterY(), markerLabels.get(i)));
		}

		for (StringPainter stringPainter : labelPainters)
			stringPainter.setFont(font);
	}

	@Override
	protected void drawAxis(Graphics2D g2) {

		if (rectangle == null)
			return;

		// draw Y-Axis
		g2.setColor(color);
		g2.setStroke(stroke);
		if (drawInnerAxisLegend)
			if (drawAxisBetweenAxeMarkersOnly)
				DisplayTools.drawLine(g2, (int) this.rectangle.getMinX(), markerPositionsWithLabels.get(0).getKey(),
						(int) this.rectangle.getMinX(),
						markerPositionsWithLabels.get(markerPositionsWithLabels.size() - 1).getKey());
			else
				g2.drawLine((int) this.rectangle.getMinX(), (int) (rectangle.getMinY()), (int) this.rectangle.getMinX(),
						(int) (rectangle.getMaxY()));
		else {
			if (drawAxisBetweenAxeMarkersOnly)
				DisplayTools.drawLine(g2, (int) this.rectangle.getMaxX(), markerPositionsWithLabels.get(0).getKey(),
						(int) this.rectangle.getMaxX(),
						markerPositionsWithLabels.get(markerPositionsWithLabels.size() - 1).getKey());
			else
				g2.drawLine((int) this.rectangle.getMaxX(), (int) (rectangle.getMinY()), (int) this.rectangle.getMaxX(),
						(int) (rectangle.getMaxY()));
		}

		// draw markers
		for (StringPainter stringPainter : labelPainters)
			stringPainter.draw(g2);
	}

	public boolean isInvertAxisLabels() {
		return invertAxisLabels;
	}

	public void setInvertAxisLabels(boolean invertAxisLabels) {
		this.invertAxisLabels = invertAxisLabels;
	}
}
