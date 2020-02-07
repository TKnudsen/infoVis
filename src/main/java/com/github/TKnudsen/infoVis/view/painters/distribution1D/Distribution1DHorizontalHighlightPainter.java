package com.github.TKnudsen.infoVis.view.painters.distribution1D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.visualChannels.ShapeAttributes;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints highlighted elements on top of a distribution painter
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public class Distribution1DHorizontalHighlightPainter<T> extends Distribution1DHorizontalPainter<T> {

	private double relativeHighlightHeight = 0.3;
	private float highlightLineStroke = 3.0f;

	/**
	 * forgot what this was good for
	 */
	private boolean highlightsAtTheUpperBound = true;

	/**
	 * whether or not the triangles are to be filled
	 */
	private boolean fillHighlights = true;

	public Distribution1DHorizontalHighlightPainter(Collection<T> data,
			Function<? super T, Double> worldToDoubleMapping) {
		super(data, worldToDoubleMapping);
	}

	public Distribution1DHorizontalHighlightPainter(Collection<T> data,
			Function<? super T, Double> worldToDoubleMapping,
			Function<? super T, ? extends Paint> colorEncodingFunction) {
		super(data, worldToDoubleMapping, colorEncodingFunction);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		Color color = g2.getColor();
		Stroke stroke = g2.getStroke();

		for (Entry<T, ShapeAttributes> entry : specialValues) {
			T worldData = entry.getKey();

			Paint paint = getPaint();
			if (entry.getValue() != null)
				paint = entry.getValue().getColor();

			drawHighlightTriangle(g2, worldData, paint);
		}

		g2.setStroke(stroke);
		g2.setColor(color);
	}

	@Override
	protected double getValueYEndPosition() {
		double hReduced = 0;

		if (!specialValues.isEmpty() && highlightsAtTheUpperBound)
			hReduced = chartRectangle.getHeight() * getRelativeHighlightHeight();

		return chartRectangle.getMinY() + hReduced;
	}

	public void drawHighlightTriangle(Graphics2D g2, T worldData, Paint paint) {
		Color c = g2.getColor();
		Stroke s = g2.getStroke();

		Double worldValue = getWorldToDoubleMapping().apply(worldData);
		double xCord = getPositionEncodingFunction().apply(worldValue);

		double height = chartRectangle.getHeight() * getRelativeHighlightHeight();

		Path2D path = new Path2D.Double();

		if (highlightsAtTheUpperBound) {
			path.moveTo(xCord, this.chartRectangle.getMinY() + height);
			path.lineTo(xCord + 0.5 * height, this.chartRectangle.getMinY());
			path.lineTo(xCord - 0.5 * height, this.chartRectangle.getMinY());
			path.lineTo(xCord, this.chartRectangle.getMinY() + height);
		} else {
			path.moveTo(xCord, this.chartRectangle.getMaxY() - height);
			path.lineTo(xCord + 0.5 * height, this.chartRectangle.getMaxY());
			path.lineTo(xCord - 0.5 * height, this.chartRectangle.getMaxY());
			path.lineTo(xCord, this.chartRectangle.getMaxY() - height);
		}

		g2.setStroke(DisplayTools.standardStroke);
		g2.setPaint(paint);

		if (fillHighlights || !highlightsAtTheUpperBound)
			g2.fill(path);

		g2.draw(path);

		g2.setStroke(s);
		g2.setColor(c);
	}

	public double getRelativeHighlightHeight() {
		return relativeHighlightHeight;
	}

	public void setRelativeHighlightHeight(double relativeHighlightHeight) {
		this.relativeHighlightHeight = relativeHighlightHeight;
	}

	public float getHighlightLineStroke() {
		return highlightLineStroke;
	}

	public void setHighlightLineStroke(float highlightLineStroke) {
		this.highlightLineStroke = highlightLineStroke;
	}

	public boolean isHighlightsAtTheUpperBound() {
		return highlightsAtTheUpperBound;
	}

	public void setHighlightsAtTheUpperBound(boolean highlightsAtTheUpperBound) {
		this.highlightsAtTheUpperBound = highlightsAtTheUpperBound;
	}

	public boolean isFillHighlights() {
		return fillHighlights;
	}

	public void setFillHighlights(boolean fillHighlights) {
		this.fillHighlights = fillHighlights;
	}

}
