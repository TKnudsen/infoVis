package com.github.TKnudsen.infoVis.view.painters.barchart.bar;

import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints a single horizontal bar
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.03
 */
public class BarHorizontalPainter extends BarPainter implements IXPositionEncoding {

	public BarHorizontalPainter(Double value, Double minValue, Paint color) {
		super(value, minValue, color);
	}

	@Override
	protected Rectangle2D calculateBarRectangle() {
		IPositionEncodingFunction encodingFunction = getPositionEncodingFunction();

		if (encodingFunction == null)
			return null;

		double screenX = encodingFunction.apply(getValue());
		double minValueScreen = encodingFunction.apply(getMinValue());

		Rectangle2D bar = new Rectangle2D.Double(minValueScreen, chartRectangle.getMinY(),
				Math.abs(screenX - minValueScreen), chartRectangle.getHeight());

		return bar;
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
