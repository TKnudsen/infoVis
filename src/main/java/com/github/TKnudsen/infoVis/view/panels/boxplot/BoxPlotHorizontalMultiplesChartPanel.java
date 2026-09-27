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
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.boxplot.BoxPlotHorizontalPainter;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DPainterPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.panels.axis.XAxisNumericalChartPanel;

/**
 * <p>
 * Horizontal counterpart of {@link BoxPlotVerticalMultiplesChartPanel}: one
 * row per dimension, sharing one numerical X-axis scale, with a category
 * label to the left of each row.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class BoxPlotHorizontalMultiplesChartPanel extends XAxisNumericalChartPanel<Double>
		implements IRectangleSelection<Double> {

	private static final long serialVersionUID = 1L;

	// constructor attributes
	private List<StatisticsSupport> dataStatisticsPerDimension;
	List<Color> colors;
	List<String> labels;

	private double minGlobal = Double.NaN;
	private double maxGlobal = Double.NaN;

	Grid2DPainterPainter<BoxPlotHorizontalPainter> boxPlotPainterGrid;
	Grid2DPainterPainter<StringPainter> labelPainterGrid;

	public BoxPlotHorizontalMultiplesChartPanel(List<StatisticsSupport> dataStatisticsPerDimension,
			List<Color> colors, List<String> labels) {

		this(dataStatisticsPerDimension, colors, labels, Double.NaN, Double.NaN);
	}

	public BoxPlotHorizontalMultiplesChartPanel(List<StatisticsSupport> dataStatisticsPerDimension,
			List<Color> colors, List<String> labels, double minGlobal, double maxGlobal) {
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
				throw new IllegalArgumentException("BoxPlotHorizontalMultiplesChartPanel: no valid input given");

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

		initializeXAxisPainter(min, max);
	}

	private void initializeBoxPlotPainters() {
		if (dataStatisticsPerDimension == null)
			return;

		if (labels == null)
			labels = new ArrayList<>();

		while (labels.size() < dataStatisticsPerDimension.size())
			labels.add("");

		BoxPlotHorizontalPainter[][] painters = new BoxPlotHorizontalPainter[1][dataStatisticsPerDimension.size()];
		StringPainter[][] labelPainters = new StringPainter[1][dataStatisticsPerDimension.size()];

		for (int i = 0; i < dataStatisticsPerDimension.size(); i++) {
			StatisticsSupport dataStatistics = dataStatisticsPerDimension.get(i);

			BoxPlotHorizontalPainter painter = new BoxPlotHorizontalPainter(dataStatistics);
			painter.setXPositionEncodingFunction(xAxisPainter.getPositionEncodingFunction());
			painter.setBackgroundPaint(null);
			painter.setPaint(colors.get(i));

			painters[0][i] = painter;

			StringPainter labelPainter = new StringPainter(labels.get(i));
			labelPainter.setBackgroundPaint(null);
			labelPainter.setHorizontalStringAlignment(HorizontalStringAlignment.LEFT);

			labelPainters[0][i] = labelPainter;
		}

		boxPlotPainterGrid = new Grid2DPainterPainter<>(painters);
		boxPlotPainterGrid.setDrawOutline(false);

		addChartPainter(boxPlotPainterGrid);

		labelPainterGrid = new Grid2DPainterPainter<>(labelPainters);
		labelPainterGrid.setBackgroundPaint(null);
		labelPainterGrid.setDrawOutline(false);

		addChartPainter(labelPainterGrid);
	}

	@Override
	public void initializeXAxisPainter(Double min, Double max) {
		setXAxisPainter(new XAxisNumericalPainter<Double>(min, max));
	}

	@Override
	public List<Double> getElementsInRectangle(RectangularShape rectangle) {
		SortedSet<Double> elements = new TreeSet<>();

		Iterator<BoxPlotHorizontalPainter> iterator = boxPlotPainterGrid.iterator();

		while (iterator.hasNext())
			elements.addAll(iterator.next().getElementsInRectangle(rectangle));

		return new ArrayList<>(elements);
	}
}
