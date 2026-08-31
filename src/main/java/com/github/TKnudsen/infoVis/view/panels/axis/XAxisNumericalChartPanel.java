package com.github.TKnudsen.infoVis.view.panels.axis;

import java.awt.Color;
import java.awt.Graphics2D;

import com.github.TKnudsen.infoVis.view.chartLayouts.ChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.chartLayouts.XAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.painters.axis.IAxisLogarithmicScale;
import com.github.TKnudsen.infoVis.view.painters.axis.IXAxis;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoder;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;

/**
 * <p>
 * Panel for charts with numerical x-axes
 * </p>
 *
 * @version 2.07
 * @since 2016
 */
public abstract class XAxisNumericalChartPanel<X extends Number> extends InfoVisChartPanel
		implements IXAxis<X>, IXPositionEncoder, IAxisLogarithmicScale {

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
	/**
	 * Ensures that the layout or its inherited class matches the panel layout
	 * requirements.
	 * 
	 * @return
	 */
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
		addChartPainter(getChartPainters().size(), chartPainter, registerXAsis);
	}

	public void addChartPainter(int index, ChartPainter chartPainter, boolean registerXAsis) {
		// Axis registration only touches chartPainter itself, not the panel's
		// painter list, so it can safely happen before adding it -- doing so
		// lets super.addChartPainter()'s own single updateBounds() pass already
		// see the correct position-encoding function, instead of needing a
		// second, otherwise-redundant updateBounds() call after registering it.
		if (registerXAsis) {
			if (chartPainter instanceof IXPositionEncoding)
				((IXPositionEncoding) chartPainter)
						.setXPositionEncodingFunction(xAxisPainter.getPositionEncodingFunction());
		}

		super.addChartPainter(index, chartPainter);
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
	}

	public void setXAxisPainter(XAxisNumericalPainter<X> yAxisPainter) {
		if (this.xAxisPainter != null)
			if (this.xAxisPainter.isLogarithmicScale() != yAxisPainter.isLogarithmicScale())
				System.err.println(
						"InfoVisXAxisNumericalChartPanel: setting axis painter implicitly switched between log and linear scale(!)");

		this.xAxisPainter = yAxisPainter;

		for (ChartPainter chartPainter : getChartPainters())
			if (chartPainter instanceof IXPositionEncoding)
				((IXPositionEncoding) chartPainter)
						.setXPositionEncodingFunction(xAxisPainter.getPositionEncodingFunction());

		updateBounds();
	}

	/////////// X-AXIS
	public boolean isXAxisOverlay() {
		return xAxisChartRectangleLayout.isXAxisOverlay();
	}

	/**
	 * Lets the chart painter(s) begin superimposed with the x axis, not (only) in
	 * its north. Automatically sets the AxisAlignment of the xAxisPainter to
	 * BOTTOM. Automatically removes background paint of axisPainter.
	 * 
	 * @param overlayOfXAxis if overlay
	 */
	public void setXAxisOverlay(boolean overlayOfXAxis) {
		this.xAxisChartRectangleLayout.setXAxisOverlay(overlayOfXAxis);
		this.xAxisPainter.setAxisLineAlignment(AxisLineAlignment.BOTTOM);
		this.xAxisPainter.setBackgroundPaint(null);

		updateBounds();
	}

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

	public void setMinHeightToDrawXAxis(double minHeightToDrawXAxis) {
		this.xAxisChartRectangleLayout.setMinHeightToDrawXAxis(minHeightToDrawXAxis);

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

	@Override
	public boolean isLogarithmicScale() {
		return this.xAxisPainter.isLogarithmicScale();
	}

	@Override
	public void setLogarithmicScale(boolean logarithmicScale) {
		this.xAxisPainter.setLogarithmicScale(logarithmicScale);

		updateBounds();
	}

	@Override
	public IPositionEncodingFunction getXPositionEncodingFunction() {
		return xAxisPainter.getPositionEncodingFunction();
	}
	
	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);

		if (xAxisPainter != null)
			xAxisPainter.setFontColor(fg);
	}
}