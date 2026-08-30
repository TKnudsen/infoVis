package com.github.TKnudsen.infoVis.view.panels.axis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.TKnudsen.infoVis.view.chartLayouts.ChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.chartLayouts.XAxisChartsJuxtaposedRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.IAxisLogarithmicScale;
import com.github.TKnudsen.infoVis.view.painters.axis.IXAxis;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoder;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;

/**
 * @version 1.01
 * @since 2025
 */
public class XAxisNumericalChartsJuxtaposedPanel<X extends Number> extends InfoVisChartPanel
		implements IXAxis<X>, IXPositionEncoder, IAxisLogarithmicScale {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7285055233768600063L;

	protected XAxisChartsJuxtaposedRectangleLayout xAxisChartsJuxtaposedRectangleLayout;

	protected XAxisNumericalPainter<X> xAxisPainter;

	public XAxisNumericalChartsJuxtaposedPanel(X min, X max) {
		super();

		xAxisChartsJuxtaposedRectangleLayout = (XAxisChartsJuxtaposedRectangleLayout) getChartRectangleLayout();

		initializeXAxisPainter(min, max);
	}

	@Override
	/**
	 * Ensures that the layout or its inherited class matches the panel layout
	 * requirements.
	 * 
	 * @return
	 */
	protected ChartRectangleLayout createChartRectangleLayout() {
		return new XAxisChartsJuxtaposedRectangleLayout();
	}

	public void initializeXAxisPainter(X min, X max) {
		setXAxisPainter(new XAxisNumericalPainter<X>(min, max));
	}

	@Override
	protected void drawChart(Graphics2D g2) {
		// axes first
		if (this.xAxisChartsJuxtaposedRectangleLayout.isDrawXAxis())
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
		if (xAxisPainter != null)
			xAxisPainter.setRectangle(xAxisChartsJuxtaposedRectangleLayout.getXAxisRectangle());

		if (xAxisChartsJuxtaposedRectangleLayout != null) {
			List<Rectangle2D> chartRectangles = xAxisChartsJuxtaposedRectangleLayout.getChartRectangles();
			List<ChartPainter> chartPainters = getChartPainters();

			if (chartRectangles.size() != chartPainters.size())
				throw new IllegalArgumentException(
						"XAxisNumericalChartsJuxtaposedPanel.updatePainterRectangles: chart painters and rectangles of unequal size: "
								+ chartRectangles.size() + " and " + chartPainters.size());

			for (int i = 0; i < chartPainters.size(); i++) {
				ChartPainter chartPainter = chartPainters.get(i);
				chartPainter.setRectangle(chartRectangles.get(i));
			}
		}
	}

	@Override
	public void addChartPainter(ChartPainter chartPainter) {
		addChartPainter(getChartPainters().size(), chartPainter);
	}

	@Override
	public void addChartPainter(int index, ChartPainter chartPainter) {
		xAxisChartsJuxtaposedRectangleLayout.increaseChartCount();

		super.addChartPainter(index, chartPainter);

		if (chartPainter instanceof IXPositionEncoding)
			((IXPositionEncoding) chartPainter)
					.setXPositionEncodingFunction(xAxisPainter.getPositionEncodingFunction());

		updateBounds();
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
		this.xAxisChartsJuxtaposedRectangleLayout.setDrawXAxis(drawXAxis);

		updateBounds();
	}

	@Override
	public void setXAxisLegendHeight(double xAxisLegendHeight) {
		this.xAxisChartsJuxtaposedRectangleLayout.setXAxisLegendHeight(xAxisLegendHeight);

		updateBounds();
	}

	public void setMinHeightToDrawXAxis(double minHeightToDrawXAxis) {
		this.xAxisChartsJuxtaposedRectangleLayout.setMinHeightToDrawXAxis(minHeightToDrawXAxis);

		updateBounds();
	}

	/**
	 * Gets the absolute spacing between charts in pixels.
	 * 
	 * @return spacing in pixels, or calculated from relative spacing if set
	 */
	public int getSpacingBetweenCharts() {
		return this.xAxisChartsJuxtaposedRectangleLayout.getSpacingBetweenCharts();
	}

	/**
	 * Sets the absolute spacing between charts in pixels. This is overridden if
	 * relative spacing is set.
	 * 
	 * @param spacingBetweenCharts spacing in pixels (must be non-negative)
	 * @throws IllegalArgumentException if spacing is negative
	 */
	public void setSpacingBetweenCharts(int spacingBetweenCharts) {
		this.xAxisChartsJuxtaposedRectangleLayout.setSpacingBetweenCharts(spacingBetweenCharts);

		updateBounds();
	}

	/**
	 * Gets the relative spacing between charts as a fraction of total dimension.
	 * 
	 * @return relative spacing (0.0-1.0), or null if using absolute spacing
	 */
	public Double getRelativeSpaceBetweenCharts() {
		return this.xAxisChartsJuxtaposedRectangleLayout.getRelativeSpaceBetweenCharts();
	}

	/**
	 * Sets the relative spacing between charts as a fraction of total dimension.
	 * When set, this overrides absolute spacing.
	 * 
	 * @param relativeSpaceBetweenCharts spacing as fraction (0.0-1.0), or null for
	 *                                   absolute spacing
	 * @throws IllegalArgumentException if value is outside valid range
	 */
	public void setRelativeSpaceBetweenCharts(Double relativeSpaceBetweenCharts) {
		this.xAxisChartsJuxtaposedRectangleLayout.setRelativeSpaceBetweenCharts(relativeSpaceBetweenCharts);

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