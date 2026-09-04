package com.github.TKnudsen.infoVis.view.panels.axis;

import java.awt.Color;
import java.awt.Graphics2D;

import com.github.TKnudsen.infoVis.view.chartLayouts.ChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.chartLayouts.XYAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.painters.axis.IAxisLogarithmicScale;
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
 * Panel for charts with numerical x and y axes
 * </p>
 *
 * @version 2.07
 * @since 2016
 */
public abstract class XYNumericalChartPanel<X extends Number, Y extends Number> extends InfoVisChartPanel
		implements IXAxis<X>, IYAxis<Y>, IXPositionEncoder, IYPositionEncoder, IAxisLogarithmicScale {

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
		// Axis registration only touches chartPainter itself, not the panel's
		// painter list, so it can safely happen before adding it -- doing so
		// lets super.addChartPainter()'s own single updateBounds() pass already
		// see the correct position-encoding functions, instead of needing a
		// second, otherwise-redundant updateBounds() call after registering them.
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

		super.addChartPainter(index, chartPainter);
	}

	@Override
	/**
	 * Sets the background color of this panel and manages chart painter
	 * backgrounds.
	 * <p>
	 * <b>Non-null color:</b> Sets a unified panel background and clears all chart
	 * painter backgrounds (making them transparent).
	 * <p>
	 * <b>Null:</b> Clears the panel background and preserves individual chart
	 * painter backgrounds.
	 * <p>
	 * <b>Note:</b> Painter backgrounds cleared by a non-null color are not restored
	 * when switching back to null. Manage externally if restoration is needed.
	 *
	 * @param backgroundColor the background color for the panel, or null to allow
	 *                        individual chart painter backgrounds to be visible
	 */
	public void setBackground(Color backgroundColor) {
		super.setBackground(backgroundColor);

		if (this.xAxisPainter != null && backgroundColor != null)
			xAxisPainter.setBackgroundPaint(null);

		if (this.yAxisPainter != null && backgroundColor != null)
			yAxisPainter.setBackgroundPaint(null);
	}

	/**
	 * @deprecated use setBackground for panels and setBackgroundColor for single
	 *             painters. Panels overwrite painter's behavior, but not the other
	 *             way around.
	 * @param backgroundColor
	 */
	public void setBackgroundColor(Color backgroundColor) {
		// super.setBackground(backgroundColor);

		if (this.xAxisPainter != null)
			xAxisPainter.setBackgroundPaint(backgroundColor);

		if (this.yAxisPainter != null)
			yAxisPainter.setBackgroundPaint(backgroundColor);
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

		// distribute background color
		//setBackground(getBackground());
		
		this.xAxisPainter.setBackgroundPaint(null);

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

		// setBackgroundColor(getBackgroundColor());
		// setBackgroundColor(getBackground());
		// refresh newly added components
		//setBackground(getBackground());
		this.yAxisPainter.setBackgroundPaint(null);

		updateBounds();
	}

	/**
	 * Sets both axis painters' line/marker paint and font (label) color in one
	 * call -- e.g. white, for readability on a dark look-and-feel background
	 * where the default black axis label text is illegible. Mirrors
	 * {@code TimeSeriesBundleChart.setAxisPaintersColor}
	 * but lives here so every {@code XYNumericalChartPanel} subclass (including
	 * {@code ScatterPlot}) gets it, not just time series bundle charts.
	 */
	public void setAxisPaintersColor(Color color) {
		if (xAxisPainter != null) {
			xAxisPainter.setPaint(color);
			xAxisPainter.setFontColor(color);
		}
		if (yAxisPainter != null) {
			yAxisPainter.setPaint(color);
			yAxisPainter.setFontColor(color);
		}
	}

//	/**
//	 *
//	 * @return
//	 * @deprecated naming convention. method now called isXAxisOverlay
//	 */
//	public boolean isOverlayOfXAxis() {
//		return isXAxisOverlay();
//	}

//	/**
//	 * lets the chart painter(s) begin on top of the x axis, not (only) in the
//	 * north. Automatically sets the AxisAlignment of the xAxisPainter to BOTTOM.
//	 * Automatically removes background paint of axisPainter.
//	 * 
//	 * @param overlayOfXAxis
//	 * @deprecated naming convention. method now called setXAxisOverlay
//	 */
//	public void setOverlayOfXAxis(boolean overlayOfXAxis) {
//		this.setXAxisOverlay(overlayOfXAxis);
//	}

	public boolean isXAxisOverlay() {
		return xyAxisChartRectangleLayout.isXAxisOverlay();
	}

	/**
	 * lets the chart painter(s) begin on top of the x axis, not (only) in the
	 * north. Automatically sets the AxisAlignment of the xAxisPainter to BOTTOM.
	 * Automatically removes background paint of axisPainter.
	 * 
	 * @param overlayOfXAxis if overlay
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

//	@Deprecated
//	public boolean isOverlayOfYAxis() {
//		return this.isYAxisOverlay();
//	}

//	@Deprecated
//	public void setOverlayOfYAxis(boolean overlayOfYAxis) {
//		this.setYAxisOverlay(overlayOfYAxis);
//	}

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

	@Override
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

	@Override
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

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);

		if (xAxisPainter != null)
			xAxisPainter.setFontColor(fg);
		if (yAxisPainter != null)
			yAxisPainter.setFontColor(fg);
	}

}