package com.github.TKnudsen.infoVis.view.panels.boxplot;

import java.awt.Color;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.boxplot.BoxPlotVerticalPainter;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DPainterPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.YAxisNumericalChartPanel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.04
 */
public class BoxPlotVerticalMultiplesChartPanel extends YAxisNumericalChartPanel<Double>
		implements IRectangleSelection<Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7833259569761438713L;

	// constructor attributes
	private List<StatisticsSupport> dataStatisticsPerDimension;
	List<Color> colors;
	List<String> labels;

	private double minGlobal = Double.NaN;
	private double maxGlobal = Double.NaN;

	Grid2DPainterPainter<BoxPlotVerticalPainter> boxPlotPainterGrid;

	public BoxPlotVerticalMultiplesChartPanel(List<StatisticsSupport> dataStatisticsPerDimension, List<Color> colors,
			List<String> labels) {

		this(dataStatisticsPerDimension, colors, labels, Double.NaN, Double.NaN);
	}

	public BoxPlotVerticalMultiplesChartPanel(List<StatisticsSupport> dataStatisticsPerDimension, List<Color> colors,
			List<String> labels, double minGlobal, double maxGlobal) {
		this.dataStatisticsPerDimension = dataStatisticsPerDimension;
		this.colors = colors;
		this.labels = labels;

		this.minGlobal = minGlobal;
		this.maxGlobal = maxGlobal;

		initializeDataAndAxis();

		initializeBoxPlotPainters();
	}

	protected void initializeDataAndAxis() {
		if (dataStatisticsPerDimension == null)
			if (Double.isNaN(minGlobal) || Double.isNaN(maxGlobal))
				throw new IllegalArgumentException("InfoVisBoxPlotVerticalMultiplesPanel: no valid input given");

		double min = Double.POSITIVE_INFINITY;
		if (!Double.isNaN(minGlobal))
			min = minGlobal;
		else
			for (StatisticsSupport dataStatistics : dataStatisticsPerDimension)
				min = Math.min(min, dataStatistics.getMin());

		double max = Double.NEGATIVE_INFINITY;
		if (!Double.isNaN(maxGlobal))
			max = maxGlobal;
		else
			for (StatisticsSupport dataStatistics : dataStatisticsPerDimension)
				max = Math.max(max, dataStatistics.getMax());

		initializeYAxisPainter(min, max);
	}

	private void initializeBoxPlotPainters() {
		if (dataStatisticsPerDimension == null)
			return;

		if (labels == null)
			labels = new ArrayList<>();

		while (labels.size() < dataStatisticsPerDimension.size())
			labels.add("");

		BoxPlotVerticalPainter[][] painters = new BoxPlotVerticalPainter[dataStatisticsPerDimension.size()][1];

		for (int i = 0; i < dataStatisticsPerDimension.size(); i++) {
			StatisticsSupport dataStatistics = dataStatisticsPerDimension.get(i);

			BoxPlotVerticalPainter painter = new BoxPlotVerticalPainter(dataStatistics);
			painter.setYPositionEncodingFunction(yAxisPainter.getPositionEncodingFunction());
			painter.setBackgroundPaint(null);
			painter.setPaint(colors.get(i));

			painters[i][0] = painter;
		}

		boxPlotPainterGrid = new Grid2DPainterPainter<>(painters);
		boxPlotPainterGrid.setDrawOutline(false);

		addChartPainter(boxPlotPainterGrid);
	}

	@Override
	public void initializeYAxisPainter(Double min, Double max) {
		setYAxisPainter(new YAxisNumericalPainter<Double>(min, max));
	}

	@Override
	public List<Double> getElementsInRectangle(RectangularShape rectangle) {
		SortedSet<Double> elements = new TreeSet<>();

		Iterator<BoxPlotVerticalPainter> iterator = boxPlotPainterGrid.iterator();

		while (iterator.hasNext())
			elements.addAll(iterator.next().getElementsInRectangle(rectangle));

		return new ArrayList<>(elements);
	}
}
