package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter;
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
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.07
 */
public class BarChart extends YAxisNumericalChartPanel<Number> implements IBarChart {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4229660375270863154L;

	protected final BarChartVerticalPainter barChartPainterVertical;

	// constructor attributes
	private List<? extends Number> data;
	private List<Color> colors;
	private Number maxGlobal = Double.NaN;

	public BarChart(Number[] data, Color[] colors) {
		this(data, colors, Double.NaN);
	}

	public BarChart(Number[] data, Color[] colors, Number maxGlobal) {
		this(DataConversion.arrayToList(data), DataConversion.arrayToList(colors), maxGlobal);
	}

	public BarChart(List<? extends Number> data, List<Color> colors) {
		this(data, colors, Double.NaN);
	}

	public BarChart(List<? extends Number> data, List<Color> colors, Number maxGlobal) {
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
		if (data == null && Double.isNaN(maxGlobal.doubleValue()))
			throw new IllegalArgumentException("BarChartVerticalPanel: no valid input given");

		Number max = Double.NEGATIVE_INFINITY;
		if (!Double.isNaN(maxGlobal.doubleValue()))
			max = maxGlobal;
		else
			max = MathFunctions.getMax(data);

		initializeYAxisPainter(0.0, max);
	}

	@Override
	public void initializeYAxisPainter(Number min, Number max) {
		YAxisNumericalPainter<Number> yAxisNumericalPainter = new YAxisNumericalPainter<Number>(min, max);
		yAxisNumericalPainter.setFlipAxisValues(true);

		setYAxisPainter(yAxisNumericalPainter);
	}

	@Override
	public BarChartPainter getBarChartPainter() {
		return barChartPainterVertical;
	}

}
