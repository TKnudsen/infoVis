package com.github.TKnudsen.infoVis.view.panels.axis;

import java.awt.Color;
import java.awt.Graphics2D;

import com.github.TKnudsen.infoVis.view.chartLayouts.ChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.chartLayouts.XAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.painters.axis.IXAxis;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoder;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Panel for charts with numerical x-axes
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.06
 */
public abstract class XAxisNumericalChartPanel<X extends Number> extends InfoVisChartPanel
		implements IXAxis<X>, IXPositionEncoder {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7285055233768600063L;

	protected XAxisChartRectangleLayout xAxisChartRectangleLayout;

	protected XAxisNumericalPainter<X> xAxisPainter;

	public XAxisNumericalChartPanel() {
		super();

		xAxisChartRectangleLayout = (XAxisChartRectangleLayout) getChartRectangleLayout();
	}

	@Override
	protected ChartRectangleLayout createChartRectangleLayout() {
		return new XAxisChartRectangleLayout();
	}

	protected abstract void initializeXAxisPainter(X min, X max);

	@Override
	protected void drawChart(Graphics2D g2) {
		// axes first
		if (this.xAxisChartRectangleLayout.isDrawXAxis())
			if (xAxisPainter != null) {
				xAxisPainter.setFont(this.getFont());
				xAxisPainter.draw(g2);
			}

		// super
		for (ChartPainter chartPainter : getChartPainters())
			if (chartPainter != null) {
				chartPainter.setFont(this.getFont());
				chartPainter.draw(g2);
			}
	}

	@Override
	/**
	 * uses the rectangle information provided with the layout and assigns it to the
	 * painters. The inherited variant FIRST assigns axis bounds, then the super.
	 * stuff. Reason: the position mapping is maintained in the axis painters.
	 */
	protected void updatePainterRectangles() {
		// needs to be done before super calls the painters
		if (xAxisPainter != null)
			xAxisPainter.setRectangle(xAxisChartRectangleLayout.getXAxisRectangle());

		super.updatePainterRectangles();
	}

	public void addChartPainter(ChartPainter chartPainter, boolean registerXAsis) {
		addChartPainter(chartPainter);

		if (registerXAsis) {
			if (chartPainter instanceof IXPositionEncoding)
				((IXPositionEncoding) chartPainter)
						.setXPositionEncodingFunction(xAxisPainter.getPositionEncodingFunction());
		}

		updateBounds();
	}

	public void setBackgroundColor(Color backgroundColor) {
		super.setBackground(backgroundColor);

		if (this.xAxisPainter != null)
			xAxisPainter.setBackgroundPaint(backgroundColor);
	}

	public void setXAxisPainter(XAxisNumericalPainter<X> yAxisPainter) {
		if (this.xAxisPainter != null)
			if (this.xAxisPainter.isLogarithmicScale() != yAxisPainter.isLogarithmicScale())
				System.err.println(
						"InfoVisXAxisNumericalChartPanel: setting axis painter implicitly switched between log and linear scale(!)");

		this.xAxisPainter = yAxisPainter;

		setBackgroundColor(getBackgroundColor());

		updateBounds();
	}

	public boolean isOverlayOfXAxis() {
		return xAxisChartRectangleLayout.isOverlayOfXAxis();
	}

	/**
	 * lets the chart painter(s) begin on top of the x axis, not (only) in the
	 * north. Automatically sets the AxisAlignment of the xAxisPainter to BOTTOM.
	 * Automatically removes background paint of axisPainter.
	 * 
	 * @param overlayOfXAxis
	 */
	public void setOverlayOfXAxis(boolean overlayOfXAxis) {
		this.xAxisChartRectangleLayout.setOverlayOfXAxis(overlayOfXAxis);
		this.xAxisPainter.setAxisLineAlignment(AxisLineAlignment.BOTTOM);
		this.xAxisPainter.setBackgroundPaint(null);

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
		this.xAxisChartRectangleLayout.setDrawXAxis(drawXAxis);

		updateBounds();
	}

	@Override
	public void setXAxisLegendHeight(double xAxisLegendHeight) {
		this.xAxisChartRectangleLayout.setXAxisLegendHeight(xAxisLegendHeight);

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

		updateBounds();
	}

	public boolean isLogarithmicScale() {
		return this.xAxisPainter.isLogarithmicScale();
	}

	public void setLogarithmicScale(boolean logarithmicScale) {
		this.xAxisPainter.setLogarithmicScale(logarithmicScale);

		updateBounds();
	}

	@Override
	public IPositionEncodingFunction getXPositionEncodingFunction() {
		return xAxisPainter.getPositionEncodingFunction();
	}
}