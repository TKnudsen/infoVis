package com.github.TKnudsen.infoVis.view.chartLayouts;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.infoVis.view.panels.axis.sizeCharacteristics.AxisSizeCharacteristics;
import com.github.TKnudsen.infoVis.view.tools.Rectangle2DTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.FixedPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * layouts a pre-given number of Y-Axes across the available x-space.
 * 
 * Example usage: Parallel Coordinates
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class YYYAxisChartRectangleLayout extends ChartRectangleLayout {

	private List<AxisSizeCharacteristics> axisSizeCharacteristics = new ArrayList<AxisSizeCharacteristics>();

	private FixedPositionEncodingFunction axesPositionEncodingFunction;

	private int spacingBetweenAxes = 0;

	public YYYAxisChartRectangleLayout() {
		this(0);
	}

	public YYYAxisChartRectangleLayout(int axes) {
		for (int i = 0; i < axes; i++)
			addAxis();

		refreshAxesPositionEncodingFunction();
	}

	public YYYAxisChartRectangleLayout(List<AxisSizeCharacteristics> axisSizeCharacteristics) {
		this.axisSizeCharacteristics = axisSizeCharacteristics;

		refreshAxesPositionEncodingFunction();
	}

	private void refreshAxesPositionEncodingFunction() {
		if (axesPositionEncodingFunction == null && axisSizeCharacteristics.size() > 0) {
			Map<Number, Double> mapping = new LinkedHashMap<Number, Double>();
			for (int i = 0; i < axisSizeCharacteristics.size(); i++)
				if (axisSizeCharacteristics.get(i).getAxisRectangle() != null)
					mapping.put(i, axisSizeCharacteristics.get(i).getAxisRectangle().getCenterX());
				else
					mapping.put(i, 1.0);

			if (mapping.size() > 0)
				axesPositionEncodingFunction = new FixedPositionEncodingFunction(mapping);
		} else
			for (int i = 0; i < axisSizeCharacteristics.size(); i++)
				if (axisSizeCharacteristics.get(i).getAxisRectangle() != null)
					axesPositionEncodingFunction.addFixedPosition(i,
							axisSizeCharacteristics.get(i).getAxisRectangle().getCenterX());
				else
					axesPositionEncodingFunction.addFixedPosition(i, 1.0);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		if (axisSizeCharacteristics.size() == 0)
			return;

		Rectangle2D[][] createRectangleMatrix = Rectangle2DTools.createRectangleMatrix(chartRectangle,
				axisSizeCharacteristics.size(), 1, spacingBetweenAxes);

		if (createRectangleMatrix == null)
			return;

		for (int i = 0; i < createRectangleMatrix.length; i++) {
			Rectangle2D rect = createRectangleMatrix[i][0];
			axisSizeCharacteristics.get(i).setAxisRectangle(rect);
		}

		refreshAxesPositionEncodingFunction();
	}

	public Rectangle2D getYAxisRectangle(int axeIndex) {
		return axisSizeCharacteristics.get(axeIndex).getAxisRectangle();
	}

	public void addAxis() {
		axisSizeCharacteristics.add(new AxisSizeCharacteristics());

		refreshAxesPositionEncodingFunction();
	}

	public int getSpacingBetweenAxes() {
		return spacingBetweenAxes;
	}

	public void setSpacingBetweenAxes(int spacingBetweenAxes) {
		this.spacingBetweenAxes = spacingBetweenAxes;

		setRectangle(rectangle);
	}

	public double getAxisLegendOffset() {
		if (axisSizeCharacteristics.size() > 0)
			return axisSizeCharacteristics.get(0).getAxisLegendOffset();

		return new AxisSizeCharacteristics().getAxisLegendOffset();
	}

	public void setAxisLegendOffset(double axisLegendOffset) {
		if (axisSizeCharacteristics.size() > 0)
			for (AxisSizeCharacteristics axisSizeCharacteristics : axisSizeCharacteristics)
				axisSizeCharacteristics.setAxisLegendOffset(axisLegendOffset);
		else
			throw new IllegalArgumentException(getClass().getSimpleName() + ": add axes first");

		setRectangle(rectangle);
	}

	public boolean isDrawAxis() {
		if (axisSizeCharacteristics.size() > 0)
			return axisSizeCharacteristics.get(0).isDrawAxis();

		return new AxisSizeCharacteristics().isDrawAxis();
	}

	public void setDrawAxis(boolean drawAxis) {
		if (axisSizeCharacteristics.size() > 0)
			for (AxisSizeCharacteristics axisSizeCharacteristics : axisSizeCharacteristics)
				axisSizeCharacteristics.setDrawAxis(drawAxis);
		else
			throw new IllegalArgumentException(getClass().getSimpleName() + ": add axes first");

		setRectangle(rectangle);
	}

	public double getMinSizeToDrawAxis() {
		if (axisSizeCharacteristics.size() > 0)
			return axisSizeCharacteristics.get(0).getMinSizeToDrawAxis();

		return new AxisSizeCharacteristics().getMinSizeToDrawAxis();
	}

	public void setMinSizeToDrawAxis(double minSizeToDrawAxis) {
		if (axisSizeCharacteristics.size() > 0)
			for (AxisSizeCharacteristics axisSizeCharacteristics : axisSizeCharacteristics)
				axisSizeCharacteristics.setMinSizeToDrawAxis(minSizeToDrawAxis);
		else
			throw new IllegalArgumentException(getClass().getSimpleName() + ": add axes first");

		setRectangle(rectangle);
	}

	public IPositionEncodingFunction getAxesPositionEncodingFunction() {
		if (axesPositionEncodingFunction == null)
			refreshAxesPositionEncodingFunction();

		return axesPositionEncodingFunction;
	}

}
