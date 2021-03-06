package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartHorizontalPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter;
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
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.08
 */
public class BarChartHorizontal extends XAxisNumericalChartPanel<Number> implements IBarChart {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4980260395011582723L;

	protected final BarChartHorizontalPainter barChartPainterHorizontal;

	private List<Number> data;
	private List<Color> colors;
	private Number maxGlobal = Double.NaN;

	public BarChartHorizontal(Number[] data, Color[] colors) {
		this(data, colors, Double.NaN);
	}

	public BarChartHorizontal(Number[] data, Color[] colors, Number maxGlobal) {
		this(DataConversion.arrayToList(data), DataConversion.arrayToList(colors), maxGlobal);
	}

	public BarChartHorizontal(List<? extends Number> data, List<Color> colors) {
		this(data, colors, Double.NaN);
	}

	public BarChartHorizontal(List<? extends Number> data, List<Color> colors, Number maxGlobal) {
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
		if (data == null && Double.isNaN(maxGlobal.doubleValue()))
			throw new IllegalArgumentException("BarChartHorizontalPanel: no valid input given");

		Number max = Double.NEGATIVE_INFINITY;
		if (!Double.isNaN(maxGlobal.doubleValue()))
			max = maxGlobal;
		else
			max = MathFunctions.getMax(data);

		initializeXAxisPainter(0.0, max);
	}

	@Override
	public void initializeXAxisPainter(Number min, Number max) {
		XAxisNumericalPainter<Number> xAxisNumericalPainter = new XAxisNumericalPainter<Number>(min, max);
		xAxisNumericalPainter.setFlipAxisValues(false);

		setXAxisPainter(xAxisNumericalPainter);
	}

	@Override
	public BarChartPainter getBarChartPainter() {
		return barChartPainterHorizontal;
	}

}
