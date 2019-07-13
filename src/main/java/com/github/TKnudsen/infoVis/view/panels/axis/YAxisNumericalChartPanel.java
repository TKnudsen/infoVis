package com.github.TKnudsen.infoVis.view.panels.axis;

import java.awt.Color;
import java.awt.Graphics2D;

import com.github.TKnudsen.infoVis.view.chartLayouts.ChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.chartLayouts.YAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.painters.axis.IYAxis;
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
 * Panel for charts with numerical y axes
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.05
 */
public abstract class YAxisNumericalChartPanel<Y extends Number> extends InfoVisChartPanel
		implements IYAxis<Y>, IYPositionEncoder {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7285055233768600063L;

	protected YAxisChartRectangleLayout yAxisChartRectangleLayout;

	protected YAxisNumericalPainter<Y> yAxisPainter;

	public YAxisNumericalChartPanel() {
		super();

		yAxisChartRectangleLayout = (YAxisChartRectangleLayout) getChartRectangleLayout();
	}

	@Override
	protected ChartRectangleLayout createChartRectangleLayout() {
		return new YAxisChartRectangleLayout();
	}

	protected abstract void initializeYAxisPainter(Y min, Y max);

	@Override
	protected void drawChart(Graphics2D g2) {
		// axes first
		if (this.yAxisChartRectangleLayout.isDrawYAxis())
			if (yAxisPainter != null) {
				yAxisPainter.setFont(this.getFont());
				yAxisPainter.draw(g2);
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
		if (yAxisPainter != null)
			yAxisPainter.setRectangle(yAxisChartRectangleLayout.getYAxisRectangle());

		super.updatePainterRectangles();
	}

	public void addChartPainter(ChartPainter chartPainter, boolean registerYAsis) {
		addChartPainter(chartPainter);

		if (registerYAsis) {
			if (chartPainter instanceof IYPositionEncoding)
				((IYPositionEncoding) chartPainter)
						.setYPositionEncodingFunction(yAxisPainter.getPositionEncodingFunction());
		}

		updateBounds();
	}

	public void setBackgroundColor(Color backgroundColor) {
		super.setBackground(backgroundColor);

		if (this.yAxisPainter != null)
			yAxisPainter.setBackgroundPaint(backgroundColor);
	}

	public void setYAxisPainter(YAxisNumericalPainter<Y> yAxisPainter) {
		if (this.yAxisPainter != null)
			if (this.yAxisPainter.isLogarithmicScale() != yAxisPainter.isLogarithmicScale())
				System.err.println(
						"InfoVisYAxisNumericalChartPanel: setting axis painter implicitly switched between log and linear scale(!)");

		this.yAxisPainter = yAxisPainter;

		setBackgroundColor(getBackgroundColor());

		updateBounds();
	}

	public boolean isOverlayOfYAxis() {
		return yAxisChartRectangleLayout.isOverlayOfYAxis();
	}

	/**
	 * lets the chart painter(s) begin on top of the < axis, not (only) in the east.
	 * Automatically sets the AxisAlignment of the yAxisPainter to LEFT.
	 * Automatically removes background paint of axisPainter.
	 * 
	 * @param overlayOfYAxis
	 */
	public void setOverlayOfYAxis(boolean overlayOfYAxis) {
		this.yAxisChartRectangleLayout.setOverlayOfYAxis(overlayOfYAxis);
		this.yAxisPainter.setAxisLineAlignment(AxisLineAlignment.LEFT);
		this.yAxisPainter.setBackgroundPaint(null);

		updateBounds();
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
		this.yAxisChartRectangleLayout.setDrawYAxis(drawYAxis);

		updateBounds();
	}

	@Override
	public void setYAxisLegendWidth(double yAxisLegendWidth) {
		this.yAxisChartRectangleLayout.setYAxisLegendWidth(yAxisLegendWidth);

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

	public boolean isLogarithmicScale() {
		return this.yAxisPainter.isLogarithmicScale();
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