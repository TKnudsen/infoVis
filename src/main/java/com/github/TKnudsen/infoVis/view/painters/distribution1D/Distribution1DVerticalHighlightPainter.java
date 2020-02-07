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
 * @version 2.05
 */
public class Distribution1DVerticalHighlightPainter<T> extends Distribution1DVerticalPainter<T> {

	private double relativeHighlightLength = 0.25;
	private float highlightLineStroke = 3.0f;

	/**
	 * forgot what this was good for
	 */
	private boolean highlightsAtTheRightBound = false;

	/**
	 * whether or not the triangles are to be filled
	 */
	private boolean fillHighlights = true;

	public Distribution1DVerticalHighlightPainter(Collection<T> data,
			Function<? super T, Double> worldToDoubleMapping) {
		super(data, worldToDoubleMapping);
	}

	public Distribution1DVerticalHighlightPainter(Collection<T> data, Function<? super T, Double> worldToDoubleMapping,
			Function<? super T, ? extends Paint> colorEncodingFunction) {
		super(data, worldToDoubleMapping, colorEncodingFunction);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		Color color = g2.getColor();
		Stroke stroke = g2.getStroke();

		for (Entry<T, ShapeAttributes> entry : specialValues) {
			T t = entry.getKey();

			Paint paint = getPaint();
			if (entry.getValue() != null)
				paint = entry.getValue().getColor();

			drawHighlightTriangle(g2, t, paint);
		}

		g2.setStroke(stroke);
		g2.setColor(color);
	}

	@Override
	protected double getValueXEndPosition() {
		double hReduced = 0;

		if (!specialValues.isEmpty() && highlightsAtTheRightBound)
			hReduced = chartRectangle.getWidth() * getRelativeHighlightLength();

		return chartRectangle.getMaxX() - hReduced;
	}

	public void drawHighlightTriangle(Graphics2D g2, T worldData, Paint paint) {
		Color c = g2.getColor();
		Stroke s = g2.getStroke();

		Double d = getWorldToDoubleMapping().apply(worldData);
		double yCord = getPositionEncodingFunction().apply(d);

		double length = chartRectangle.getWidth() * getRelativeHighlightLength();

		Path2D path = new Path2D.Double();

		if (highlightsAtTheRightBound) {
			path.moveTo(this.chartRectangle.getMaxX() - length, yCord);
			path.lineTo(this.chartRectangle.getMaxX(), yCord + 0.5 * length);
			path.lineTo(this.chartRectangle.getMaxX(), yCord - 0.5 * length);
			path.lineTo(this.chartRectangle.getMaxX() - length, yCord);
		} else {
			path.moveTo(this.chartRectangle.getMinX() + length, yCord);
			path.lineTo(this.chartRectangle.getMinX(), yCord + 0.5 * length);
			path.lineTo(this.chartRectangle.getMinX(), yCord - 0.5 * length);
			path.lineTo(this.chartRectangle.getMinX() + length, yCord);
		}

		g2.setStroke(DisplayTools.standardStroke);
		g2.setPaint(paint);

		if (fillHighlights || !highlightsAtTheRightBound)
			g2.fill(path);

		g2.draw(path);

		g2.setStroke(s);
		g2.setColor(c);
	}

	public float getHighlightLineStroke() {
		return highlightLineStroke;
	}

	public void setHighlightLineStroke(float highlightLineStroke) {
		this.highlightLineStroke = highlightLineStroke;
	}

	public double getRelativeHighlightLength() {
		return relativeHighlightLength;
	}

	public void setRelativeHighlightLength(double relativeHighlightLength) {
		this.relativeHighlightLength = relativeHighlightLength;
	}

	public boolean isHighlightsAtTheRightBound() {
		return highlightsAtTheRightBound;
	}

	public void setHighlightsAtTheRightBound(boolean highlightsAtTheRightBound) {
		this.highlightsAtTheRightBound = highlightsAtTheRightBound;
	}

	public boolean isFillHighlights() {
		return fillHighlights;
	}

	public void setFillHighlights(boolean fillHighlights) {
		this.fillHighlights = fillHighlights;
	}

}
