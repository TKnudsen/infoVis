package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartVerticalPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.YAxisNumericalChartPanel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Vertical bar chart including a bar chart painter.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.06
 */
public class BarChartVerticalPanel extends YAxisNumericalChartPanel<Double>
		implements IClickSelection<Integer>, ISelectionVisualizer<Integer>, IRectangleSelection<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4229660375270863154L;

	private final BarChartVerticalPainter barChartPainterVertical;

	// constructor attributes
	private List<Double> data;
	private List<Color> colors;
	private double maxGlobal = Double.NaN;

	public BarChartVerticalPanel(Double[] data, Color[] colors) {
		this(data, colors, Double.NaN);
	}

	public BarChartVerticalPanel(Double[] data, Color[] colors, Double maxGlobal) {
		this(DataConversion.arrayToList(data), DataConversion.arrayToList(colors), maxGlobal);
	}

	public BarChartVerticalPanel(List<Double> data, List<Color> colors) {
		this(data, colors, Double.NaN);
	}

	public BarChartVerticalPanel(List<Double> data, List<Color> colors, Double maxGlobal) {
		if (data == null)
			throw new IllegalArgumentException("BarChartVerticalPanel: data was null");
		else
			this.data = Collections.unmodifiableList(data);

		if (colors == null) {
			this.colors = new ArrayList<>();
			for (int i = 0; i < data.size(); i++)
				this.colors.add(null);
		} else
			this.colors = Collections.unmodifiableList(colors);

		this.maxGlobal = maxGlobal;

		initializeData();

		this.barChartPainterVertical = new BarChartVerticalPainter(this.data, this.colors);

		this.addChartPainter(barChartPainterVertical, true);
	}

	protected void initializeData() {
		if (data == null && Double.isNaN(maxGlobal))
			throw new IllegalArgumentException("BarChartVerticalPanel: no valid input given");

		double max = Double.NEGATIVE_INFINITY;
		if (!Double.isNaN(maxGlobal))
			max = maxGlobal;
		else
			max = MathFunctions.getMax(data);

		initializeYAxisPainter(0.0, max);
	}

	@Override
	public void initializeYAxisPainter(Double min, Double max) {
		YAxisNumericalPainter<Double> yAxisNumericalPainter = new YAxisNumericalPainter<Double>(min, max);
		yAxisNumericalPainter.setFlipAxisValues(true);

		setYAxisPainter(yAxisNumericalPainter);
	}

	@Override
	public List<Integer> getElementsAtPoint(Point p) {
		return barChartPainterVertical.getElementsAtPoint(p);
	}

	@Override
	public List<Integer> getElementsInRectangle(RectangularShape rectangle) {
		return barChartPainterVertical.getElementsInRectangle(rectangle);
	}

	@Override
	public void setSelectedFunction(Function<? super Integer, Boolean> selectedFunction) {
		this.barChartPainterVertical.setSelectedFunction(selectedFunction);
	}

}