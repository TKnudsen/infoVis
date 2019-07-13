package com.github.TKnudsen.infoVis.view.painters.barchart.bar;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints a single bar
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.03
 */
public abstract class BarPainter extends ChartPainter implements ITooltip {

	// external attributes
	private final Double minValue;
	private final Double value;

	private boolean fillBar = true;
	private boolean drawBarOutlines = true;
	private BasicStroke lineStroke = DisplayTools.standardStroke;
	private boolean toolTipping = true;

	// visuals
	private Rectangle2D barRectangle;

	private IPositionEncodingFunction positionEncodingFunction;

	public BarPainter(Double value, Double minValue, Paint color) {
		this.value = value;
		this.minValue = minValue;

		this.setPaint(color);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (chartRectangle == null)
			return;

		Paint c = g2.getPaint();
		g2.setPaint(getPaint());

		if (isFillBar())
			g2.fill(barRectangle);

		// outline
		if (isDrawBarOutlines()) {
			g2.setPaint(getBorderPaint());
			g2.setStroke(getLineStroke());
			g2.draw(barRectangle);
		}

		g2.setPaint(c);
	}

	protected abstract Rectangle2D calculateBarRectangle();

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		this.barRectangle = calculateBarRectangle();
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!isToolTipping())
			return null;

		if (p == null)
			return null;

		return ToolTipTools.getTooltipForPositionMapping1D(p, p.getY(),
				positionEncodingFunction.inverseMapping(p.getY()), chartRectangle);
	}

	@Override
	public boolean isToolTipping() {
		return this.toolTipping;
	}

	@Override
	public void setToolTipping(boolean toolTipping) {
		this.toolTipping = toolTipping;
	}

	public boolean isDrawBarOutlines() {
		return drawBarOutlines;
	}

	public void setDrawBarOutlines(boolean drawBarOutlines) {
		this.drawBarOutlines = drawBarOutlines;
	}

	public BasicStroke getLineStroke() {
		return lineStroke;
	}

	public void setLineStroke(BasicStroke lineStroke) {
		this.lineStroke = lineStroke;
	}

	public boolean isFillBar() {
		return fillBar;
	}

	public void setFillBar(boolean fillBar) {
		this.fillBar = fillBar;
	}

	public Double getValue() {
		return value;
	}

	public Double getMinValue() {
		return minValue;
	}

	public Rectangle2D getBarRectangle() {
		return barRectangle;
	}

	public void setBarRectangle(Rectangle2D barRectangle) {
		this.barRectangle = barRectangle;
	}

	public IPositionEncodingFunction getPositionEncodingFunction() {
		return positionEncodingFunction;
	}

	public void setPositionEncodingFunction(IPositionEncodingFunction positionEncodingFunction) {
		this.positionEncodingFunction = positionEncodingFunction;
	}

}
