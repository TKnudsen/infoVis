package com.github.TKnudsen.infoVis.view.panels.axis;

import java.awt.Color;
import java.awt.Graphics2D;

import com.github.TKnudsen.infoVis.view.chartLayouts.ChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.chartLayouts.XYAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.painters.axis.IXAxis;
import com.github.TKnudsen.infoVis.view.painters.axis.IYAxis;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoder;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoder;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Panel for charts with numerical x and y axes
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.06
 */
public abstract class XYNumericalChartPanel<X extends Number, Y extends Number> extends InfoVisChartPanel
		implements IXAxis<X>, IYAxis<Y>, IXPositionEncoder, IYPositionEncoder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4409164863624533833L;

	/**
	 * reference to the original chartRectangleLayout with
	 * XYAxisChartRectangleLayout capability.
	 */
	protected XYAxisChartRectangleLayout xyAxisChartRectangleLayout;

	protected XAxisNumericalPainter<X> xAxisPainter;
	protected YAxisNumericalPainter<Y> yAxisPainter;

	public XYNumericalChartPanel() {
		super();

		xyAxisChartRectangleLayout = (XYAxisChartRectangleLayout) getChartRectangleLayout();
	}

	@Override
	protected ChartRectangleLayout createChartRectangleLayout() {
		return new XYAxisChartRectangleLayout();
	}

	protected abstract void initializeXAxisPainter(X min, X max);

	protected abstract void initializeYAxisPainter(Y min, Y max);

	@Override
	protected void drawChart(Graphics2D g2) {
		super.drawChart(g2);

		// axes last
		if (this.xyAxisChartRectangleLayout.isDrawXAxis())
			if (xAxisPainter != null) {
				xAxisPainter.setFont(this.getFont());
//				xAxisPainter.setFontColor(getForeground());
//				xAxisPainter.setPaint(getForeground());
				xAxisPainter.draw(g2);
			}

		if (this.xyAxisChartRectangleLayout.isDrawYAxis())
			if (yAxisPainter != null) {
				yAxisPainter.setFont(this.getFont());
//				yAxisPainter.setFontColor(getForeground());
//				yAxisPainter.setPaint(getForeground());
				yAxisPainter.draw(g2);
			}
	}

	@Override
	/**
	 * uses the rectangle information provided with the layout and assigns it to the
	 * painters. The inherited variant FIRST assigns axis bounds, then the super
	 * stuff. Reason: the position mapping is maintained in the axis painters.
	 */
	protected void updatePainterRectangles() {
		if (xAxisPainter != null)
			xAxisPainter.setRectangle(xyAxisChartRectangleLayout.getXAxisRectangle());
		if (yAxisPainter != null)
			yAxisPainter.setRectangle(xyAxisChartRectangleLayout.getYAxisRectangle());

		super.updatePainterRectangles();
	}

	public void addChartPainter(ChartPainter chartPainter, boolean registerXAsis, boolean registerYAsis) {
		addChartPainter(getChartPainters().size(), chartPainter, registerXAsis, registerYAsis);
	}

	public void addChartPainter(int index, ChartPainter chartPainter, boolean registerXAsis, boolean registerYAsis) {
		super.addChartPainter(index, chartPainter);

		if (registerXAsis) {
			if (chartPainter instanceof IXPositionEncoding)
				((IXPositionEncoding) chartPainter)
						.setXPositionEncodingFunction(xAxisPainter.getPositionEncodingFunction());
		}
		if (registerYAsis) {
			if (chartPainter instanceof IYPositionEncoding)
				((IYPositionEncoding) chartPainter)
						.setYPositionEncodingFunction(yAxisPainter.getPositionEncodingFunction());
		}

		updateBounds();
	}

	public void setBackground(Color backgroundColor) {
		super.setBackground(backgroundColor);

		if (this.xAxisPainter != null)
			xAxisPainter.setBackgroundPaint(null);

		if (this.yAxisPainter != null)
			yAxisPainter.setBackgroundPaint(null);
	}

	public void setBackgroundColor(Color backgroundColor) {
		super.setBackground(backgroundColor);

		if (this.xAxisPainter != null)
			xAxisPainter.setBackgroundPaint(null);

		if (this.yAxisPainter != null)
			yAxisPainter.setBackgroundPaint(null);
	}

	public void setXAxisPainter(XAxisNumericalPainter<X> xAxisPainter) {
		if (this.xAxisPainter != null)
			if (this.xAxisPainter.isLogarithmicScale() != xAxisPainter.isLogarithmicScale())
				System.err.println(
						"XYAxisNumericalPainter: setting axis painter implicitly switched between log and linear scale(!)");

		this.xAxisPainter = xAxisPainter;

		for (ChartPainter chartPainter : getChartPainters())
			if (chartPainter instanceof IXPositionEncoding)
				((IXPositionEncoding) chartPainter)
						.setXPositionEncodingFunction(xAxisPainter.getPositionEncodingFunction());

		setBackgroundColor(getBackgroundColor());

		updateBounds();
	}

	public void setYAxisPainter(YAxisNumericalPainter<Y> yAxisPainter) {
		if (this.yAxisPainter != null)
			if (this.yAxisPainter.isLogarithmicScale() != yAxisPainter.isLogarithmicScale())
				System.err.println(
						"XYAxisNumericalPainter: set of axis painter implicitly switched between log and linear scale(!)");

		this.yAxisPainter = yAxisPainter;

		for (ChartPainter chartPainter : getChartPainters())
			if (chartPainter instanceof IYPositionEncoding)
				((IYPositionEncoding) chartPainter)
						.setYPositionEncodingFunction(yAxisPainter.getPositionEncodingFunction());

		setBackgroundColor(getBackgroundColor());

		updateBounds();
	}

	/**
	 * 
	 * @return
	 * @deprecated naming convention. method now called isXAxisOverlay
	 */
	public boolean isOverlayOfXAxis() {
		return isXAxisOverlay();
	}

	/**
	 * lets the chart painter(s) begin on top of the x axis, not (only) in the
	 * north. Automatically sets the AxisAlignment of the xAxisPainter to BOTTOM.
	 * Automatically removes background paint of axisPainter.
	 * 
	 * @param overlayOfXAxis
	 * @deprecated naming convention. method now called setXAxisOverlay
	 */
	public void setOverlayOfXAxis(boolean overlayOfXAxis) {
		this.setXAxisOverlay(overlayOfXAxis);
	}

	public boolean isXAxisOverlay() {
		return xyAxisChartRectangleLayout.isXAxisOverlay();
	}

	/**
	 * lets the chart painter(s) begin on top of the x axis, not (only) in the
	 * north. Automatically sets the AxisAlignment of the xAxisPainter to BOTTOM.
	 * Automatically removes background paint of axisPainter.
	 * 
	 * @param overlayOfXAxis
	 */
	public void setXAxisOverlay(boolean overlayOfXAxis) {
		this.xyAxisChartRectangleLayout.setXAxisOverlay(overlayOfXAxis);

		if (overlayOfXAxis) {
			this.xAxisPainter.setAxisLineAlignment(AxisLineAlignment.BOTTOM);
			this.xAxisPainter.setBackgroundPaint(null);
		} else
			this.xAxisPainter.setAxisLineAlignment(AxisLineAlignment.TOP);

		updateBounds();
	}

	@Deprecated
	public boolean isOverlayOfYAxis() {
		return this.isYAxisOverlay();
	}

	@Deprecated
	public void setOverlayOfYAxis(boolean overlayOfYAxis) {
		this.setYAxisOverlay(overlayOfYAxis);
	}

	public boolean isYAxisOverlay() {
		return xyAxisChartRectangleLayout.isYAxisOverlay();
	}

	/**
	 * lets the chart painter(s) begin on top of the < axis, not (only) in the east.
	 * Automatically sets the AxisAlignment of the yAxisPainter to LEFT.
	 * Automatically removes background paint of axisPainter.
	 * 
	 * @param yAxisOverlay
	 */
	public void setYAxisOverlay(boolean yAxisOverlay) {
		this.xyAxisChartRectangleLayout.setYAxisOverlay(yAxisOverlay);

		if (yAxisOverlay) {
			this.yAxisPainter.setAxisLineAlignment(AxisLineAlignment.LEFT);
			this.yAxisPainter.setBackgroundPaint(null);
		} else
			this.yAxisPainter.setAxisLineAlignment(AxisLineAlignment.RIGHT);

		updateBounds();
	}

	/////////// X-AXIS
	@Override
	public void setXAxisMinValue(X xAxisMinValue) {
		xAxisPainter.setMinValue(xAxisMinValue);

		updateBounds();
	}

	@Override
	public void setXAxisMaxValue(X xAxisMaxValue) {
		xAxisPainter.setMaxValue(xAxisMaxValue);

		updateBounds();
	}

	@Override
	public void setDrawXAxis(boolean drawXAxis) {
		this.xyAxisChartRectangleLayout.setDrawXAxis(drawXAxis);

		updateBounds();
	}

	@Override
	public void setXAxisLegendHeight(double xAxisLegendHeight) {
		this.xyAxisChartRectangleLayout.setXAxisLegendHeight(xAxisLegendHeight);

		updateBounds();
	}

	@Override
	public void setXAxisMarkerDistanceInPixels(int markerDistanceInPixels) {
		this.xAxisPainter.setMarkerDistanceInPixels(markerDistanceInPixels);

		updateBounds();
	}

	@Override
	public void setXAxisPhysicalUnit(String physicalUnit) {

		this.xAxisPainter.setPhysicalUnit(physicalUnit);
	}

	public boolean isLogarithmicScale() {
		return this.xAxisPainter.isLogarithmicScale();
	}

	/////////// Y-AXIS

	@Override
	public void setYAxisMinValue(Y yAxisMinValue) {
		yAxisPainter.setMinValue(yAxisMinValue);

		updateBounds();
	}

	@Override
	public void setYAxisMaxValue(Y yAxisMaxValue) {
		yAxisPainter.setMaxValue(yAxisMaxValue);

		updateBounds();
	}

	@Override
	public void setDrawYAxis(boolean drawYAxis) {
		this.xyAxisChartRectangleLayout.setDrawYAxis(drawYAxis);

		updateBounds();
	}

	@Override
	public void setYAxisLegendWidth(double yAxisLegendWidth) {
		this.xyAxisChartRectangleLayout.setYAxisLegendWidth(yAxisLegendWidth);

		updateBounds();
	}

	@Override
	public void setYAxisMarkerDistanceInPixels(int markerDistanceInPixels) {
		this.yAxisPainter.setMarkerDistanceInPixels(markerDistanceInPixels);

		updateBounds();
	}

	@Override
	public void setYAxisPhysicalUnit(String physicalUnit) {
		this.yAxisPainter.setPhysicalUnit(physicalUnit);

		updateBounds();
	}

	public void setLogarithmicScale(boolean logarithmicScale) {
		this.xAxisPainter.setLogarithmicScale(logarithmicScale);
		this.yAxisPainter.setLogarithmicScale(logarithmicScale);

		updateBounds();
	}

	@Override
	public IPositionEncodingFunction getXPositionEncodingFunction() {
		return xAxisPainter.getPositionEncodingFunction();
	}

	@Override
	public IPositionEncodingFunction getYPositionEncodingFunction() {
		return yAxisPainter.getPositionEncodingFunction();
	}

}