package com.github.TKnudsen.infoVis.view.painters.axis.categorical;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Abstract basis class for axe drawings.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 */
public abstract class AxisCategoricalPainter<T extends List<String>> extends AxisPainter {

	protected T markerLabels;
	protected List<Color> labelColors;

	protected double offsetWithinChartRectangle = 0.0;
	protected boolean drawAxisBetweenAxeMarkersOnly = false;
	protected boolean drawInnerAxisLegend = false;

	protected Rectangle2D axisRectangle;
	protected List<StringPainter> labelPainters;

	protected List<Entry<Double, String>> markerPositionsWithLabels = new ArrayList<Entry<Double, String>>();

	public AxisCategoricalPainter(T markerLabels) {
		this(markerLabels, null);
	}

	public AxisCategoricalPainter(T markerLabels, List<Color> labelColors) {
		this.markerLabels = markerLabels;
		this.labelColors = labelColors;
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (rectangle != null)
			drawAxis(g2);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		refreshLabelPainters();
	}

	protected abstract void refreshLabelPainters();

	protected void drawAxis(Graphics2D g2) {
		for (StringPainter stringPainter : labelPainters) {
			stringPainter.draw(g2);
		}
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		for (StringPainter stringPainter : labelPainters)
			if (stringPainter.getRectangle().contains(p)) {
				StringPainter sp = new StringPainter(stringPainter.getData());
				sp.setRectangle(new Rectangle2D.Double(p.getX() - 80, p.getY() - 40, 80, 40));
				sp.setFontColor(fontColor);
				sp.setFontSize((int) Math.max(12, this.rectangle.getHeight() * 0.66));
				return sp;
			}
		return null;
	}

	public boolean isDrawInnerAxisLegend() {
		return drawInnerAxisLegend;
	}

	public void setDrawInnerAxisLegend(boolean drawInnerAxisLegend) {
		this.drawInnerAxisLegend = drawInnerAxisLegend;
	}

	public boolean isDrawAxisBetweenAxeMarkersOnly() {
		return drawAxisBetweenAxeMarkersOnly;
	}

	public void setDrawAxisBetweenAxeMarkersOnly(boolean drawAxisBetweenAxeMarkersOnly) {
		this.drawAxisBetweenAxeMarkersOnly = drawAxisBetweenAxeMarkersOnly;
	}

	public double getOffsetWithinChartRectangle() {
		return offsetWithinChartRectangle;
	}

	public void setOffsetWithinChartRectangle(double offsetWithinChartRectangle) {
		this.offsetWithinChartRectangle = offsetWithinChartRectangle;
	}

	public T getMarkerLabels() {
		return markerLabels;
	}

	public void setMarkerLabels(T markerLabels) {
		this.markerLabels = markerLabels;
		refreshLabelPainters();
	}

	public List<Entry<Double, String>> getMarkerPositionsWithLabels() {
		return markerPositionsWithLabels;
	}

	public void setMarkerPositionsWithLabels(List<Entry<Double, String>> markerPositionsWithLabels) {
		this.markerPositionsWithLabels = markerPositionsWithLabels;
	}
}
