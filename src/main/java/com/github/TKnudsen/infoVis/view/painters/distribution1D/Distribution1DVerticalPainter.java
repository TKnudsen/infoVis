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
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.06
 */
public class Distribution1DVerticalPainter<T> extends Distribution1DPainter<T> implements IYPositionEncoding {

	public Distribution1DVerticalPainter(Collection<T> data, Function<? super T, Double> worldToDoubleMapping) {
		super(data, worldToDoubleMapping);
	}

	public Distribution1DVerticalPainter(Collection<T> data, Function<? super T, Double> worldToDoubleMapping,
			Function<? super T, ? extends Paint> colorEncodingFunction) {
		super(data, worldToDoubleMapping, colorEncodingFunction);
	}

	@Override
	public void drawLine(Graphics2D g2, Double positionValue, double capSize) {
		DisplayTools.drawLine(g2, chartRectangle.getMinX() + capSize, positionValue, getValueXEndPosition() - capSize,
				positionValue);
	}

	/**
	 * needed for the highlight painter
	 * 
	 * @return
	 */
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
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		if (rectangle == null)
			return null;

		// y axis is inverted. switching min and max
		Number worldMax = getPositionEncodingFunction().inverseMapping(rectangle.getMinY());
		Number worldMin = getPositionEncodingFunction().inverseMapping(rectangle.getMaxY());

		List<T> elements = new ArrayList<>();
		for (T t : data) {
			double world = getWorldToDoubleMapping().apply(t);

			if (world >= worldMin.doubleValue() && world <= worldMax.doubleValue())
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

		return ToolTipTools.getTooltipForPositionMapping1D(p, p.getY(),
				getPositionEncodingFunction().inverseMapping(p.getY()), chartRectangle);
	}

	@Override
	public void setYPositionEncodingFunction(IPositionEncodingFunction yPositionEncodingFunction) {
		this.setPositionEncodingFunction(yPositionEncodingFunction);
	}

}
