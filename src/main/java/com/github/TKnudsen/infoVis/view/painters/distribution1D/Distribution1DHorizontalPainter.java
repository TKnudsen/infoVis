package com.github.TKnudsen.infoVis.view.painters.distribution1D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints the distribution of numerical values in a horizontal arrangement
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.04
 */
public class Distribution1DHorizontalPainter extends Distribution1DPainter implements IXPositionEncoding {

	public Distribution1DHorizontalPainter(Collection<Double> values) {
		super(values);
	}

	@Override
	public void drawValue(Graphics2D g2, double worldValue, Paint color) {
		Color c = g2.getColor();

		IPositionEncodingFunction positionEncoding = getPositionEncodingFunction();

		double size = 0.0;
		if (g2.getStroke() != null) {
			Stroke stroke = g2.getStroke();
			if (stroke instanceof BasicStroke)
				size = ((BasicStroke) stroke).getLineWidth() * 0.5 - 1;
		}

		if (!Double.isNaN(worldValue)) {
			Double screen = positionEncoding.apply(worldValue);
			g2.setPaint(color);
			DisplayTools.drawLine(g2, screen, chartRectangle.getMaxY() - size, screen, getValueYEndPosition() + size);
		}

		g2.setColor(c);
	}

	protected double getValueYEndPosition() {
		return chartRectangle.getMinY();
	}

	@Override
	protected void updatePositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		this.getPositionEncodingFunction().setMinPixel(rectangle.getMinX());
		this.getPositionEncodingFunction().setMaxPixel(rectangle.getMaxX());
	}

	@Override
	public List<Double> getElementsInRectangle(RectangularShape rectangle) {
		if (rectangle == null)
			return null;

		Number v1 = getPositionEncodingFunction().inverseMapping(rectangle.getMinX());
		Number v2 = getPositionEncodingFunction().inverseMapping(rectangle.getMaxX());

		List<Double> elements = new ArrayList<>();
		for (double d : values)
			if (d >= v1.doubleValue() && d <= v2.doubleValue())
				elements.add(d);

		return elements;
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!isToolTipping())
			return null;

		if (p == null)
			return null;

		return ToolTipTools.getTooltipForPositionMapping1D(p, p.getX(),
				getPositionEncodingFunction().inverseMapping(p.getX()), chartRectangle);
	}

	@Override
	public void setXPositionEncodingFunction(IPositionEncodingFunction xPositionEncodingFunction) {
		this.setPositionEncodingFunction(xPositionEncodingFunction);
	}

}
