package com.github.TKnudsen.infoVis.view.painters.distribution1D;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

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
 * Copyright: (c) 2016-2022 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.06
 */
public class Distribution1DHorizontalPainter<T> extends Distribution1DPainter<T> implements IXPositionEncoding {

	public Distribution1DHorizontalPainter(Collection<T> data,
			Function<? super T, ? extends Number> worldToDoubleMapping) {
		super(data, worldToDoubleMapping);
	}

	public Distribution1DHorizontalPainter(Collection<T> data,
			Function<? super T, ? extends Number> worldToDoubleMapping,
			Function<? super T, ? extends Paint> colorEncodingFunction) {
		super(data, worldToDoubleMapping, colorEncodingFunction);
	}

	@Override
	public void drawLine(Graphics2D g2, Double positionValue, double capSize) {
		DisplayTools.drawLine(g2, positionValue, chartRectangle.getMinY() - capSize, positionValue,
				getValueYEndPosition() + capSize);
	}

	/**
	 * needed for the highlight painter
	 * 
	 * @return double
	 */
	protected double getValueYEndPosition() {
		return chartRectangle.getMaxY();
	}

	@Override
	protected void updatePositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		this.getPositionEncodingFunction().setMinPixel(rectangle.getMinX());
		this.getPositionEncodingFunction().setMaxPixel(rectangle.getMaxX());
	}

	@Override
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		if (rectangle == null)
			return null;

		Number v1 = getPositionEncodingFunction().inverseMapping(rectangle.getMinX());
		Number v2 = getPositionEncodingFunction().inverseMapping(rectangle.getMaxX());

		List<T> elements = new ArrayList<>();
		for (T t : data) {
			double d = getWorldToDoubleMapping().apply(t).doubleValue();
			if (d >= v1.doubleValue() && d <= v2.doubleValue())
				elements.add(t);
		}

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
		this.externalPositionEncodingFunction = true;
	}

}
