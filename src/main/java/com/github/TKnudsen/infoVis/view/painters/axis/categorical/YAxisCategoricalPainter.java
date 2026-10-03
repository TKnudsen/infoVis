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
 * @version 1.05
 * @since 2016
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

		if (getRectangle() == null)
			return;

		axisRectangle = new Rectangle2D.Double(getRectangle().getMinX(), getRectangle().getMinY() + offsetWithinChartRectangle,
				getRectangle().getWidth(), getRectangle().getHeight() - (2 * offsetWithinChartRectangle));

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

		if (getRectangle() == null)
			return;

		// draw Y-Axis
		g2.setPaint(getPaint());
		g2.setStroke(stroke);
		if (drawInnerAxisLegend)
			if (drawAxisBetweenAxeMarkersOnly)
				DisplayTools.drawLine(g2, (int) getRectangle().getMinX(), markerPositionsWithLabels.get(0).getKey(),
						(int) getRectangle().getMinX(),
						markerPositionsWithLabels.get(markerPositionsWithLabels.size() - 1).getKey());
			else
				g2.drawLine((int) getRectangle().getMinX(), (int) (getRectangle().getMinY()), (int) getRectangle().getMinX(),
						(int) (getRectangle().getMaxY()));
		else {
			if (drawAxisBetweenAxeMarkersOnly)
				DisplayTools.drawLine(g2, (int) getRectangle().getMaxX(), markerPositionsWithLabels.get(0).getKey(),
						(int) getRectangle().getMaxX(),
						markerPositionsWithLabels.get(markerPositionsWithLabels.size() - 1).getKey());
			else
				g2.drawLine((int) getRectangle().getMaxX(), (int) (getRectangle().getMinY()), (int) getRectangle().getMaxX(),
						(int) (getRectangle().getMaxY()));
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
