package com.github.TKnudsen.infoVis.view.painters.barchart;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.painters.barchart.bar.BarHorizontalPainter;
import com.github.TKnudsen.infoVis.view.tools.Rectangle2DTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Basic horizontal bar chart painter
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.04
 */
public class BarChartHorizontalPainter extends BarChartPainter implements IXPositionEncoding {

	public BarChartHorizontalPainter(double[] data, Color[] colors) {
		super(data, colors);
	}

	public BarChartHorizontalPainter(Number[] data, Color[] colors) {
		super(data, colors);
	}

	public BarChartHorizontalPainter(List<? extends Number> data, List<Color> colors) {
		super(data, colors);
	}

	@Override
	protected void initializeBarPainters() {
		this.barPainters = new ArrayList<>();

		if (data != null)
			for (int i = 0; i < data.size(); i++) {
				BarHorizontalPainter barPainter = new BarHorizontalPainter(data.get(i), getMinValue(), colors.get(i));

				barPainter.setXPositionEncodingFunction(getPositionEncodingFunction());
				barPainter.setFillBar(isFillBars());
				barPainter.setDrawBarOutlines(isDrawBarOutlines());
				barPainter.setLineStroke(getLineStroke());
				barPainter.setBackgroundPaint(null);

				barPainters.add(barPainter);
			}
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		double gridSpacing = getGridSpacing();
		if (Double.isNaN(gridSpacing))
			gridSpacing = Rectangle2DTools.calculateSpacingValue(chartRectangle.getWidth(), chartRectangle.getHeight(),
					barPainters.size(), barPainters.size());
		Rectangle2D[][] rectangleMatrix = Rectangle2DTools.createRectangleMatrix(chartRectangle, 1, barPainters.size(),
				gridSpacing);

		if (rectangleMatrix != null)
			for (int i = 0; i < barPainters.size(); i++)
				barPainters.get(i).setRectangle(rectangleMatrix[0][i]);
	}

	@Override
	protected void updatePositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		this.getPositionEncodingFunction().setMinPixel(rectangle.getMinX());
		this.getPositionEncodingFunction().setMaxPixel(rectangle.getMaxX());
	}

	@Override
	public void setXPositionEncodingFunction(IPositionEncodingFunction xPositionEncodingFunction) {
		this.setPositionEncodingFunction(xPositionEncodingFunction);
	}

	@Override
	protected boolean isInvertedAxis() {
		return false;
	}
}
