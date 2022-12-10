package com.github.TKnudsen.infoVis.view.panels.axis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import com.github.TKnudsen.infoVis.view.chartLayouts.ChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.chartLayouts.XYAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.painters.axis.IXAxisCategorical;
import com.github.TKnudsen.infoVis.view.painters.axis.IYAxis;
import com.github.TKnudsen.infoVis.view.painters.axis.categorical.XAxisCategoricalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoder;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Panel for charts with categorical x axes and numerical y
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.02
 */
public abstract class XCatYNumericalChartPanel<X extends List<String>, Y extends Number> extends InfoVisChartPanel
		implements IXAxisCategorical<X>, IYAxis<Y>, IYPositionEncoder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2971196700090892559L;

	protected XYAxisChartRectangleLayout xyAxisChartRectangleLayout = new XYAxisChartRectangleLayout();

	protected XAxisCategoricalPainter<X> xAxisPainter;
	protected YAxisNumericalPainter<Y> yAxisPainter;

	public XCatYNumericalChartPanel() {
		super();

		xyAxisChartRectangleLayout = (XYAxisChartRectangleLayout) getChartRectangleLayout();
	}

	@Override
	protected ChartRectangleLayout createChartRectangleLayout() {
		return new XYAxisChartRectangleLayout();
	}

	protected abstract void initializeXAxisPainter(X labels);

	protected abstract void initializeYAxisPainter(Y min, Y max);

	@Override
	protected void drawChart(Graphics2D g2) {
		super.drawChart(g2);

		// axes last
		if (this.xyAxisChartRectangleLayout.isDrawXAxis())
			if (xAxisPainter != null) {
				xAxisPainter.setFont(this.getFont());
				xAxisPainter.draw(g2);
			}

		if (this.xyAxisChartRectangleLayout.isDrawYAxis())
			if (yAxisPainter != null) {
				yAxisPainter.setFont(this.getFont());
				yAxisPainter.draw(g2);
			}
	}

	@Override
	/**
	 * uses the rectangle information provided with the layout and assigns it to the
	 * painters. The inherited variant FIRST assigns axis bounds, then the super.
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

		System.err.println(
				"XCatYNumericalChartPanel.addChartPainter: warning - registering xAxis (categorical) not supported yet - categorical position encoding needed first");

		if (chartPainter != null) {
			if (registerYAsis) {
				if (chartPainter instanceof IYPositionEncoding)
					((IYPositionEncoding) chartPainter)
							.setYPositionEncodingFunction(yAxisPainter.getPositionEncodingFunction());
			}
		}

		updateBounds();
	}

	public void setBackgroundColor(Color backgroundColor) {
		super.setBackground(backgroundColor);

		if (this.xAxisPainter != null)
			xAxisPainter.setBackgroundPaint(backgroundColor);

		if (this.yAxisPainter != null)
			yAxisPainter.setBackgroundPaint(backgroundColor);
	}

	public void setXAxisPainter(XAxisCategoricalPainter<X> xAxisPainter) {
		this.xAxisPainter = xAxisPainter;

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

	@Deprecated
	public boolean isOverlayOfXAxis() {
		return isXAxisOverlay();
	}

	@Deprecated
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
	 * @param overlayOfXAxis is overlay
	 */
	public void setXAxisOverlay(boolean overlayOfXAxis) {
		this.xyAxisChartRectangleLayout.setXAxisOverlay(overlayOfXAxis);
		// this.xAxisPainter.setAxisLineAlignment(AxisLineAlignment.BOTTOM);
		this.xAxisPainter.setBackgroundPaint(null);

		updateBounds();
	}

	@Deprecated
	public boolean isOverlayOfYAxis() {
		return isYAxisOverlay();
	}

	@Deprecated
	public void setOverlayOfYAxis(boolean overlayOfYAxis) {
		this.setYAxisOverlay(overlayOfYAxis);
	}

	public boolean isYAxisOverlay() {
		return xyAxisChartRectangleLayout.isYAxisOverlay();
	}

	/**
	 * lets the chart painter(s) begin on top of the less than axis, not (only) in
	 * the east. Automatically sets the AxisAlignment of the yAxisPainter to LEFT.
	 * Automatically removes background paint of axisPainter.
	 * 
	 * @param yAxisOverlay is overlay
	 */
	public void setYAxisOverlay(boolean yAxisOverlay) {
		this.xyAxisChartRectangleLayout.setYAxisOverlay(yAxisOverlay);
		this.yAxisPainter.setAxisLineAlignment(AxisLineAlignment.LEFT);
		this.yAxisPainter.setBackgroundPaint(null);

		updateBounds();
	}

	/////////// X-AXIS
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
	public void setXAxisLabels(X labels) {
		this.xAxisPainter.setMarkerLabels(labels);
	}

	public XAxisCategoricalPainter<X> getXAxisPainter() {
		return xAxisPainter;
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
		this.yAxisPainter.setLogarithmicScale(logarithmicScale);

		updateBounds();
	}

	@Override
	public IPositionEncodingFunction getYPositionEncodingFunction() {
		return yAxisPainter.getPositionEncodingFunction();
	}
}
