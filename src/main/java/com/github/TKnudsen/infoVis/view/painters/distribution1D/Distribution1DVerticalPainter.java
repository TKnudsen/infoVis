package com.github.TKnudsen.infoVis.view.painters.distribution1D;

import java.awt.BasicStroke;
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
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints the distribution of numerical values in a vertical arrangement.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.05
 */
public class Distribution1DVerticalPainter extends Distribution1DPainter implements IYPositionEncoding {

	public Distribution1DVerticalPainter(Collection<Double> values) {
		super(values);
	}

	@Override
	public void drawValue(Graphics2D g2, double worldValue, Paint color) {
		IPositionEncodingFunction encodingFunction = getPositionEncodingFunction();

		double size = 0.0;
		if (g2.getStroke() != null) {
			Stroke stroke = g2.getStroke();
			if (stroke instanceof BasicStroke)
				size = ((BasicStroke) stroke).getLineWidth() * 0.5 - 1;
		}

		if (!Double.isNaN(worldValue)) {
			Double screen = encodingFunction.apply(worldValue);
			g2.setPaint(color);
			DisplayTools.drawLine(g2, chartRectangle.getMinX() + size, screen, getValueXEndPosition() - size, screen);
		}
	}

	protected double getValueXEndPosition() {
		return chartRectangle.getMaxX();
	}

	@Override
	protected void updatePositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		this.getPositionEncodingFunction().setMinPixel(rectangle.getMinY());
		this.getPositionEncodingFunction().setMaxPixel(rectangle.getMaxY());
	}

	@Override
	public List<Double> getElementsInRectangle(RectangularShape rectangle) {
		if (rectangle == null)
			return null;

		Number v1 = getPositionEncodingFunction().inverseMapping(rectangle.getMinY());
		Number v2 = getPositionEncodingFunction().inverseMapping(rectangle.getMaxY());

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

		return ToolTipTools.getTooltipForPositionMapping1D(p, p.getY(),
				getPositionEncodingFunction().inverseMapping(p.getY()), chartRectangle);
	}

	@Override
	public void setYPositionEncodingFunction(IPositionEncodingFunction yPositionEncodingFunction) {
		this.setPositionEncodingFunction(yPositionEncodingFunction);
	}
}
