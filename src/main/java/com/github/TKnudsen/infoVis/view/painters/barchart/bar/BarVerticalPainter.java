package com.github.TKnudsen.infoVis.view.painters.barchart.bar;

import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints a single vertical bar
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.03
 */
public class BarVerticalPainter extends BarPainter implements IYPositionEncoding {

	public BarVerticalPainter(Double value, Double minValue, Paint color) {
		super(value, minValue, color);
	}

	@Override
	protected Rectangle2D calculateBarRectangle() {
		IPositionEncodingFunction positionEncoding = getPositionEncodingFunction();

		if (positionEncoding == null)
			return null;

		double screenY = positionEncoding.apply(getValue());
		double minValueScreen = positionEncoding.apply(getMinValue());

		Rectangle2D bar = new Rectangle2D.Double(this.chartRectangle.getMinX(), screenY, this.chartRectangle.getWidth(),
				Math.abs(screenY - minValueScreen));

		return bar;
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
