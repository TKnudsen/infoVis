package com.github.TKnudsen.infoVis.view.panels.axis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.TKnudsen.infoVis.view.chartLayouts.ChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.chartLayouts.YYYAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoder;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;

/**
 * <p>
 * Panel for charts with multiple Y axes aligned horizontally, side by side,
 * like in Parallel Coordinates plots.
 * 
 * The "YYY" naming indicates multiple Y axes arranged along the X dimension.
 * Each axis can display different numerical data ranges and scales.
 * </p>
 *
 * @version 1.1
 * @since 2016
 */
public abstract class YYYAxesNumericalChartPanel<Y extends Number> extends InfoVisChartPanel
		implements IXPositionEncoder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4409164863624533833L;

	/**
	 * reference to the YYYAxisChartRectangleLayout
	 */
	protected YYYAxisChartRectangleLayout axisChartRectangleLayout;

	private List<YAxisNumericalPainter<Y>> yAxisPainters;

	private boolean drawAxesNames = true;

	public YYYAxesNumericalChartPanel(int axes) {
		super();

		if (axes < 1)
			throw new IllegalArgumentException(
					"YYYNumericalChartPanel: Number of axes must be at least 1, got: " + axes);

		axisChartRectangleLayout = (YYYAxisChartRectangleLayout) getChartRectangleLayout();

		for (int i = 0; i < axes; i++)
			axisChartRectangleLayout.addAxis();
	}

	@Override
	protected ChartRectangleLayout createChartRectangleLayout() {
		return new YYYAxisChartRectangleLayout();
	}

	@Override
	protected void drawChart(Graphics2D g2) {
		super.drawChart(g2);

		if (!this.axisChartRectangleLayout.isDrawAxis())
			return;

		forEachPainter(painter -> {
			painter.setFont(this.getFont());
			painter.draw(g2);
		});
	}

	@Override
	/**
	 * uses the rectangle information provided with the layout and assigns it to the
	 * painters. The inherited variant FIRST assigns axis bounds, then the super
	 * stuff. Reason: the position mapping is maintained in the axis painters.
	 */
	protected void updatePainterRectangles() {
		if (yAxisPainters != null)
			for (int i = 0; i < yAxisPainters.size(); i++) {
				YAxisNumericalPainter<Y> painter = yAxisPainters.get(i);
				if (painter != null)
					painter.setRectangle(axisChartRectangleLayout.getYAxisRectangle(i));
			}

		super.updatePainterRectangles();
	}

	/**
	 * Applies an operation to all non-null axis painters.
	 */
	private void forEachPainter(Consumer<YAxisNumericalPainter<Y>> action) {
		if (yAxisPainters == null)
			return;

		for (YAxisNumericalPainter<Y> painter : yAxisPainters)
			if (painter != null)
				action.accept(painter);
	}

	public void addChartPainter(ChartPainter chartPainter, boolean registerAxesPositionEncoding) {
		addChartPainter(getChartPainters().size(), chartPainter, registerAxesPositionEncoding);
	}

	public void addChartPainter(int index, ChartPainter chartPainter, boolean registerAxesPositionEncoding) {
		super.addChartPainter(index, chartPainter);

		if (registerAxesPositionEncoding) {
			if (chartPainter instanceof IXPositionEncoding)
				((IXPositionEncoding) chartPainter)
						.setXPositionEncodingFunction(axisChartRectangleLayout.getAxesPositionEncodingFunction());
		}

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

		if (yAxisPainters != null && backgroundColor != null)
			forEachPainter(painter -> painter.setBackgroundPaint(null));
	}

	/**
	 * @deprecated Use {@link #setBackground(Color)} instead. Use setBackground for
	 *             panels and setBackgroundColor for single painters. Panels
	 *             overwrite painter's behavior, but not the other way around.
	 * 
	 * @param backgroundColor
	 */
	public void setBackgroundColor(Color backgroundColor) {
		forEachPainter(painter -> painter.setBackgroundPaint(backgroundColor));
	}

	/////////// Y-AXES

	public void setDrawYAxes(boolean drawYAxis) {
		this.axisChartRectangleLayout.setDrawAxis(drawYAxis);

		updateBounds();
	}

	public void setYAxesLegendOffset(double yAxisLegendWidth) {
		this.axisChartRectangleLayout.setAxisLegendOffset(yAxisLegendWidth);

		updateBounds();
	}

	public void setYAxesMarkerDistanceInPixels(int markerDistanceInPixels) {
		forEachPainter(painter -> painter.setMarkerDistanceInPixels(markerDistanceInPixels));

		updateBounds();
	}

	public void setYAxesPhysicalUnit(String physicalUnit) {
		forEachPainter(painter -> painter.setPhysicalUnit(physicalUnit));

		updateBounds();
	}

	public void setLogarithmicScale(boolean logarithmicScale) {
		forEachPainter(painter -> painter.setLogarithmicScale(logarithmicScale));

		updateBounds();
	}

	@Override
	public IPositionEncodingFunction getXPositionEncodingFunction() {
		return axisChartRectangleLayout.getAxesPositionEncodingFunction();
	}

	public List<IPositionEncodingFunction> getYPositionEncodingFunctions() {
		if (yAxisPainters == null)
			return new ArrayList<>();

		return yAxisPainters.stream().filter(painter -> painter != null)
				.map(YAxisNumericalPainter::getPositionEncodingFunction).collect(Collectors.toList());
	}

	public List<YAxisNumericalPainter<Y>> getYAxisPainters() {
		return yAxisPainters == null ? null : Collections.unmodifiableList(yAxisPainters);
	}

	/**
	 * sets axis painters. axis alignment will be set to CENTER
	 * 
	 * @param yAxisPainters painters
	 */
	public void setYAxisPainters(List<YAxisNumericalPainter<Y>> yAxisPainters) {
		if (yAxisPainters == null)
			throw new IllegalArgumentException(
					"YYYNumericalChartPanel.setYAxisPainters: yAxisPainters shall not be null");

		int axisCount = axisChartRectangleLayout.getAxisCount();
		if (yAxisPainters.size() != axisCount)
			throw new IllegalArgumentException("YYYNumericalChartPanel.setYAxisPainters: Expected " + axisCount
					+ " painters, got " + yAxisPainters.size());

		// Defensive copy
		List<YAxisNumericalPainter<Y>> paintersCopy = new ArrayList<>(yAxisPainters);

		for (YAxisNumericalPainter<Y> painter : paintersCopy) {
			painter.setAxisLineAlignment(AxisLineAlignment.CENTER);
			painter.setDrawAxisBetweenAxeMarkersOnly(false);
		}

		this.yAxisPainters = paintersCopy;

		updateBounds();
	}

	public boolean isDrawAxesNames() {
		return drawAxesNames;
	}

	public void setDrawAxesNames(boolean drawAxesNames) {
		this.drawAxesNames = drawAxesNames;

		forEachPainter(painter -> painter.setDrawPhysicalUnit(drawAxesNames));

		updateBounds();
	}

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);

		if (yAxisPainters != null)
			for (YAxisNumericalPainter<Y> painter : yAxisPainters)
				painter.setFontColor(fg);
	}

}