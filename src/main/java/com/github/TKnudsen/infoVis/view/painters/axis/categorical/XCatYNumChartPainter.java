package com.github.TKnudsen.infoVis.view.painters.axis.categorical;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.TKnudsen.infoVis.view.chartLayouts.XYAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.chartLayouts.XYYAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.IXAxisCategorical;
import com.github.TKnudsen.infoVis.view.painters.axis.IYAxis;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public abstract class XCatYNumChartPainter<X extends List<String>, Y extends Number> extends AxisPainter
		implements IXAxisCategorical<X>, IYAxis<Y> {

	private boolean drawYAxisSecondRight = false;

	/**
	 * enables setting the margin explicitly
	 */
	private double margin = Double.NaN;

	protected XYAxisChartRectangleLayout xyAxisChartRectangleLayout = new XYAxisChartRectangleLayout();
	protected XAxisCategoricalPainter<X> xAxisPainter;
	protected YAxisNumericalPainter<Y> yAxisPainter;
	protected YAxisNumericalPainter<Double> yAxisPainterRight;

	protected abstract void initializeXAxisPainter(X labels);

	protected abstract void initializeYAxisPainter(Y min, Y max);

	protected void initializeYAxisPainterRight(Y min, Y max) {
		yAxisPainterRight = new YAxisNumericalPainter<Double>(0.0, 100.0);
		yAxisPainterRight.setAxisLineAlignment(AxisLineAlignment.LEFT);
		yAxisPainterRight.setPhysicalUnit("%");
		yAxisPainterRight.setDrawPhysicalUnit(true);
		yAxisPainterRight.setFont(font);
		yAxisPainterRight.setFontColor(fontColor);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		double border = Double.isNaN(margin)
				? Math.max(1, Math.min(rectangle.getWidth(), rectangle.getHeight()) * 0.005)
				: margin;
		xyAxisChartRectangleLayout.setBorder(border);

		xyAxisChartRectangleLayout.setRectangle(rectangle);
		this.chartRectangle = xyAxisChartRectangleLayout.getChartRectangle();
		xAxisPainter.setRectangle(xyAxisChartRectangleLayout.getXAxisRectangle());
		yAxisPainter.setRectangle(xyAxisChartRectangleLayout.getYAxisRectangle());

		if (drawYAxisSecondRight && xyAxisChartRectangleLayout instanceof XYYAxisChartRectangleLayout) {
			XYYAxisChartRectangleLayout layout = (XYYAxisChartRectangleLayout) xyAxisChartRectangleLayout;
			yAxisPainterRight.setRectangle(layout.getyAxisRectangleRight());
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		Color c = g2.getColor();

		super.draw(g2);

		if (xyAxisChartRectangleLayout.isDrawXAxis())
			drawXAxis(g2);

		if (xyAxisChartRectangleLayout.isDrawYAxis())
			drawYAxis(g2);

		if (drawYAxisSecondRight)
			drawYAxisright(g2);

		drawChart(g2);

		g2.setPaint(getPaint());

		if (isDrawOutline())
			DisplayTools.drawRectangle(g2, rectangle, getBorderPaint());

		g2.setColor(c);
	}

	public void drawXAxis(Graphics2D g2) {
		xAxisPainter.draw(g2);
	}

	public void drawYAxis(Graphics2D g2) {
		yAxisPainter.draw(g2);
	}

	public void drawYAxisright(Graphics2D g2) {
		if (drawYAxisSecondRight) {
			yAxisPainterRight.draw(g2);
		}
	}

	public abstract void drawChart(Graphics2D g2);

	@Override
	public void setYAxisPhysicalUnit(String physicalUnit) {
		this.yAxisPainter.setPhysicalUnit(physicalUnit);
	}

	public Y getYAxisMinValue() {
		return yAxisPainter.getMinValue();
	}

	@Override
	public void setYAxisMinValue(Y yAxisMinValue) {
		yAxisPainter.setMinValue(yAxisMinValue);
		setRectangle(rectangle);
	}

	public Y getYAxisMaxValue() {
		return yAxisPainter.getMaxValue();
	}

	@Override
	public void setYAxisMaxValue(Y yAxisMaxValue) {
		yAxisPainter.setMaxValue(yAxisMaxValue);
		setRectangle(rectangle);
	}

	public boolean isDrawYAxis() {
		return this.xyAxisChartRectangleLayout.isDrawYAxis();
	}

	@Override
	public void setDrawYAxis(boolean drawYAxis) {
		this.xyAxisChartRectangleLayout.setDrawYAxis(drawYAxis);
		setRectangle(rectangle);
	}

	public double getYAxisLegendWidth() {
		return xyAxisChartRectangleLayout.getYAxisLegendWidth();
	}

	@Override
	public void setYAxisLegendWidth(double yAxisLegendWidth) {
		this.xyAxisChartRectangleLayout.setYAxisLegendWidth(yAxisLegendWidth);
		setRectangle(rectangle);
	}

	public boolean isDrawXAxis() {
		return this.xyAxisChartRectangleLayout.isDrawXAxis();
	}

	@Override
	public void setDrawXAxis(boolean drawXAxis) {
		this.xyAxisChartRectangleLayout.setDrawXAxis(drawXAxis);
		setRectangle(rectangle);
	}

	@Override
	public void setXAxisLabels(X labels) {
		this.xAxisPainter.setMarkerLabels(labels);
	}

	@Override
	public void setYAxisMarkerDistanceInPixels(int markerDistanceInPixels) {
		this.yAxisPainter.setMarkerDistanceInPixels(markerDistanceInPixels);
		setRectangle(rectangle);
	}

	@Override
	public void setXAxisLegendHeight(double xAxisLegendHeight) {
		this.xyAxisChartRectangleLayout.setXAxisLegendHeight(xAxisLegendHeight);
		setRectangle(rectangle);
	}

	public XYAxisChartRectangleLayout getXyAxisChartRectangleLayout() {
		return xyAxisChartRectangleLayout;
	}

	public void setXyAxisChartRectangleLayout(XYAxisChartRectangleLayout xyAxisChartRectangleLayout) {
		this.xyAxisChartRectangleLayout = xyAxisChartRectangleLayout;
	}

	public XAxisCategoricalPainter<X> getXAxisPainter() {
		return xAxisPainter;
	}

	public void setXAxisPainter(XAxisCategoricalPainter<X> xAxisPainter) {
		this.xAxisPainter = xAxisPainter;
	}

	public YAxisNumericalPainter<Y> getYAxisPainter() {
		return yAxisPainter;
	}

	public void setYAxisPainter(YAxisNumericalPainter<Y> yAxisPainter) {
		this.yAxisPainter = yAxisPainter;
	}

	public boolean isDrawYAxisSecondRight() {
		return drawYAxisSecondRight;
	}

	public void setDrawYAxisSecondRight(boolean drawYAxisSecondRight) {
		if (drawYAxisSecondRight && !this.drawYAxisSecondRight) {
			xyAxisChartRectangleLayout = new XYYAxisChartRectangleLayout();
			initializeYAxisPainterRight(getYAxisMinValue(), getYAxisMaxValue());
		} else if (!drawYAxisSecondRight && this.drawYAxisSecondRight)
			xyAxisChartRectangleLayout = new XYAxisChartRectangleLayout();

		this.drawYAxisSecondRight = drawYAxisSecondRight;
	}

	public double getMargin() {
		return margin;
	}

	public void setMargin(double margin) {
		this.margin = margin;
		setRectangle(rectangle);
	}

}
