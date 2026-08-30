package com.github.TKnudsen.infoVis.view.painters.barchart;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.TKnudsen.infoVis.view.painters.barchart.bar.BarVerticalPainter;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChart;
import com.github.TKnudsen.infoVis.view.tools.Rectangle2DTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoding;

/**
 * <p>
 * Basic vertical bar chart painter
 * </p>
 *
 * @version 2.04
 * @since 2016
 */
public class BarChartVerticalPainter extends BarChartPainter implements IYPositionEncoding {

	public BarChartVerticalPainter(double[] data, Color[] colors) {
		super(data, colors);
	}

	public BarChartVerticalPainter(Number[] data, Color[] colors) {
		super(data, colors);
	}

	public BarChartVerticalPainter(Collection<? extends Number> data, List<Color> colors) {
		super(data, colors);
	}

	@Override
	protected void initializeBarPainters() {
		this.barPainters = new ArrayList<>();

		if (data != null)
			for (int i = 0; i < data.size(); i++) {
				BarVerticalPainter barPainter = new BarVerticalPainter(data.get(i), getMinValue(), colors.get(i));

				barPainter.setYPositionEncodingFunction(getPositionEncodingFunction());
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
		if (Double.isNaN(gridSpacing)) {
			gridSpacing = Rectangle2DTools.calculateSpacingValue(chartRectangle.getWidth(), chartRectangle.getHeight(),
					barPainters.size(), barPainters.size(), BarChart.BARCHART_GRID_SPACING_RATIO);
			gridSpacing = Math.max(1.0, gridSpacing);
		}

		Rectangle2D[][] rectangleMatrix = Rectangle2DTools.createRectangleMatrix(chartRectangle, barPainters.size(), 1,
				(int)gridSpacing);

		if (rectangleMatrix != null)
			for (int i = 0; i < barPainters.size(); i++)
				barPainters.get(i).setRectangle(rectangleMatrix[i][0]);
	}

	@Override
	protected void updatePositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		this.getPositionEncodingFunction().setMinPixel(rectangle.getMinY());
		this.getPositionEncodingFunction().setMaxPixel(rectangle.getMaxY());
	}

	@Override
	public void setYPositionEncodingFunction(IPositionEncodingFunction yPositionEncodingFunction) {
		this.setPositionEncodingFunction(yPositionEncodingFunction);
	}

	@Override
	protected boolean isInvertedAxis() {
		return true;
	}
}
