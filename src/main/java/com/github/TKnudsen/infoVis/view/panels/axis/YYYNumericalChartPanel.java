package com.github.TKnudsen.infoVis.view.panels.axis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

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
 * InfoVis
 * </p>
 * 
 * <p>
 * Panel for charts with multiple y axes aligned side by side, like the Parallel
 * Coordinates plot
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public abstract class YYYNumericalChartPanel<Y extends Number> extends InfoVisChartPanel implements IXPositionEncoder {

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

	public YYYNumericalChartPanel(int axes) {
		super();

		axisChartRectangleLayout = (YYYAxisChartRectangleLayout) getChartRectangleLayout();

		for (int i = 0; i < axes; i++)
			axisChartRectangleLayout.addAxis();
	}

	@Override
	protected ChartRectangleLayout createChartRectangleLayout() {
		return new YYYAxisChartRectangleLayout();
	}

	/**
	 * necessary to be able to use the axis painter functionality. add into
	 * constructor of inheriting class.
	 */
	protected abstract void initializeYAxisPainters();

	@Override
	protected void drawChart(Graphics2D g2) {
		super.drawChart(g2);

		for (YAxisNumericalPainter<?> yAxisPainter : yAxisPainters)
			// axes last
			if (this.axisChartRectangleLayout.isDrawAxis())
				if (yAxisPainter != null) {
					yAxisPainter.setFont(this.getFont());
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
		if (yAxisPainters != null)
			for (int i = 0; i < yAxisPainters.size(); i++)
				yAxisPainters.get(i).setRectangle(axisChartRectangleLayout.getYAxisRectangle(i));

		super.updatePainterRectangles();
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

	public void setBackground(Color backgroundColor) {
		super.setBackground(backgroundColor);

		if (yAxisPainters != null)
			for (YAxisNumericalPainter<?> yAxisPainter : yAxisPainters)
				if (yAxisPainter != null)
					yAxisPainter.setBackgroundPaint(null);
	}

	public void setBackgroundColor(Color backgroundColor) {
		super.setBackground(backgroundColor);

		if (yAxisPainters != null)
			for (YAxisNumericalPainter<?> yAxisPainter : yAxisPainters)
				if (yAxisPainter != null)
					yAxisPainter.setBackgroundPaint(null);
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
		if (yAxisPainters != null)
			for (YAxisNumericalPainter<?> yAxisPainter : yAxisPainters)
				yAxisPainter.setMarkerDistanceInPixels(markerDistanceInPixels);

		updateBounds();
	}

	public void setYAxesPhysicalUnit(String physicalUnit) {
		if (yAxisPainters != null)
			for (YAxisNumericalPainter<?> yAxisPainter : yAxisPainters)
				yAxisPainter.setPhysicalUnit(physicalUnit);

		updateBounds();
	}

	public void setLogarithmicScale(boolean logarithmicScale) {
		if (yAxisPainters != null)
			for (YAxisNumericalPainter<?> yAxisPainter : yAxisPainters)
				yAxisPainter.setLogarithmicScale(logarithmicScale);

		updateBounds();
	}

	@Override
	public IPositionEncodingFunction getXPositionEncodingFunction() {
		return axisChartRectangleLayout.getAxesPositionEncodingFunction();
	}

	public List<IPositionEncodingFunction> getYPositionEncodingFunctions() {
		List<IPositionEncodingFunction> yYPositionEncodingFunction = new ArrayList<IPositionEncodingFunction>();

		if (yAxisPainters != null)
			for (YAxisNumericalPainter<?> yAxisPainter : yAxisPainters)
				yYPositionEncodingFunction.add(yAxisPainter.getPositionEncodingFunction());

		return yYPositionEncodingFunction;
	}

	public List<YAxisNumericalPainter<Y>> getyAxisPainters() {
		return yAxisPainters;
	}

	/**
	 * sets axis painters. axis alignment will be set to CENTER
	 * 
	 * @param yAxisPainters painters
	 */
	public void setyAxisPainters(List<YAxisNumericalPainter<Y>> yAxisPainters) {
		for (YAxisNumericalPainter<Y> a : yAxisPainters)
			a.setAxisLineAlignment(AxisLineAlignment.CENTER);

		for (YAxisNumericalPainter<Y> a : yAxisPainters)
			a.setDrawAxisBetweenAxeMarkersOnly(false);

		this.yAxisPainters = yAxisPainters;

		setBackgroundColor(getBackgroundColor());

		updateBounds();
	}

	public boolean isDrawAxesNames() {
		return drawAxesNames;
	}

	public void setDrawAxesNames(boolean drawAxesNames) {
		this.drawAxesNames = drawAxesNames;

		if (yAxisPainters != null)
			for (YAxisNumericalPainter<?> yAxisPainter : yAxisPainters)
				yAxisPainter.setDrawPhysicalUnit(drawAxesNames);
	}

}