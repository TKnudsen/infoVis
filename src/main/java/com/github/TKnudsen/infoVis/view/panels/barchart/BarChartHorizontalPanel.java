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
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartHorizontalPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.XAxisNumericalChartPanel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Horizontal bar chart including a bar chart painter.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.06
 */
public class BarChartHorizontalPanel extends XAxisNumericalChartPanel<Double>
		implements IClickSelection<Integer>, IRectangleSelection<Integer>, ISelectionVisualizer<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4980260395011582723L;

	private final BarChartHorizontalPainter barChartPainterHorizontal;

	private List<Double> data;
	private List<Color> colors;
	private double maxGlobal = Double.NaN;

	public BarChartHorizontalPanel(Double[] data, Color[] colors) {
		this(data, colors, Double.NaN);
	}

	public BarChartHorizontalPanel(Double[] data, Color[] colors, Double maxGlobal) {
		this(DataConversion.arrayToList(data), DataConversion.arrayToList(colors), maxGlobal);
	}

	public BarChartHorizontalPanel(List<Double> data, List<Color> colors) {
		this(data, colors, Double.NaN);
	}

	public BarChartHorizontalPanel(List<Double> data, List<Color> colors, Double maxGlobal) {
		if (data == null || data.size() == 0)
			throw new IllegalArgumentException("BarChartHorizontalPanel: no data");
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

		this.barChartPainterHorizontal = new BarChartHorizontalPainter(data, colors);

		this.addChartPainter(barChartPainterHorizontal, true);
	}

	protected void initializeData() {
		if (data == null && Double.isNaN(maxGlobal))
			throw new IllegalArgumentException("BarChartHorizontalPanel: no valid input given");

		double max = Double.NEGATIVE_INFINITY;
		if (!Double.isNaN(maxGlobal))
			max = maxGlobal;
		else
			max = MathFunctions.getMax(data);

		initializeXAxisPainter(0.0, max);
	}

	@Override
	public void initializeXAxisPainter(Double min, Double max) {
		XAxisNumericalPainter<Double> xAxisNumericalPainter = new XAxisNumericalPainter<Double>(min, max);
		xAxisNumericalPainter.setFlipAxisValues(false);

		setXAxisPainter(xAxisNumericalPainter);
	}

	@Override
	public List<Integer> getElementsAtPoint(Point p) {
		return barChartPainterHorizontal.getElementsAtPoint(p);
	}

	@Override
	public List<Integer> getElementsInRectangle(RectangularShape rectangle) {
		return barChartPainterHorizontal.getElementsInRectangle(rectangle);
	}

	@Override
	public void setSelectedFunction(Function<? super Integer, Boolean> selectedFunction) {
		this.barChartPainterHorizontal.setSelectedFunction(selectedFunction);
	}

}
