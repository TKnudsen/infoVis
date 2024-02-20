package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartVerticalPainter;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DPainterPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.VerticalStringAlignment;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Factory for BarCharts. Also provides tools and bar chart modification
 * capability.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public class BarCharts {

	/**
	 * creates a bar chart with all bars having the same color
	 * 
	 * @param bars  bars
	 * @param color color
	 * @return bar chart
	 */
	public static BarChart createBarChart(List<? extends Number> bars, Color color) {
		List<Color> colors = new ArrayList<Color>();

		for (@SuppressWarnings("unused")
		Number d : bars)
			colors.add(color);

		return new BarChart(bars, colors);
	}

	public static BarChart createBarChart(List<? extends Number> bars, List<Color> colors) {
		return new BarChart(bars, colors);
	}

	public static BarChartHorizontal createBarChartHorizontal(List<? extends Number> bars, Color color) {
		List<Color> colors = new ArrayList<Color>();

		for (@SuppressWarnings("unused")
		Number d : bars)
			colors.add(color);

		return createBarChartHorizontal(bars, colors);
	}

	public static BarChartHorizontal createBarChartHorizontal(List<? extends Number> bars, List<Color> colors) {
		return new BarChartHorizontal(bars, colors);
	}

	public static <T> SortedMap<String, Integer> createData(Collection<? extends T> data,
			Function<? super T, String> worldToCategoryMapping) {

		if (data.isEmpty())
			return null;

		SortedMap<String, Integer> counts = new TreeMap<>();
		for (T t : data) {
			if (t == null)
				continue;

			String s = worldToCategoryMapping.apply(t);
			if (s == null)
				continue;

			if (!counts.containsKey(s))
				counts.put(s, 0);

			counts.put(s, counts.get(s) + 1);
		}

		return counts;
	}

	/**
	 * 
	 * @param data           list of bar chart data
	 * @param barchartColors one color for each bar chart layer
	 * @return bar chart
	 */
	public static BarChart createLayeredBarChart(List<List<? extends Number>> data, List<Color> barchartColors) {
		Objects.requireNonNull(data);

		if (data.isEmpty())
			return null;

		// create bar chart
		Color color = Color.GRAY;
		if (barchartColors != null && !barchartColors.isEmpty())
			color = barchartColors.get(0);
		BarChart barChart = createBarChart(data.get(0), color);

		// add additional bar chart layers
		for (int i = 1; i < data.size(); i++) {
			List<? extends Number> bars = data.get(i);

			List<Color> colors = new ArrayList<Color>();
			Color c = barchartColors.size() > i ? barchartColors.get(i) : Color.BLACK;
			for (@SuppressWarnings("unused")
			Number d : bars)
				colors.add(c);

			BarChartVerticalPainter barChartVerticalPainter = new BarChartVerticalPainter(bars, colors);
			barChart.addChartPainter(barChartVerticalPainter, true);
		}

		return barChart;
	}

	/**
	 * values is the collection of numbers (not bins) which will be binned here. The
	 * result is represented with a bar chart.
	 * 
	 * @param values   values
	 * @param bins     bins
	 * @param barColor colors
	 * @return bar chart
	 */
	public static BarChart createHistogramBarchart(Collection<? extends Number> values, int bins, Color barColor) {

		NormalizationFunction normalization = new LinearNormalizationFunction(values);

		Double[] counts = new Double[bins];
		for (Number value : values)
			for (int i = 0; i < bins; i++) {
				if (value == null || Double.isNaN(value.doubleValue()))
					continue;
				Number n = normalization.apply(value);
				if (1 / (int) bins * i < n.doubleValue())
					counts[i]++;
			}

		List<Color> colors = new ArrayList<Color>();
		for (int i = 0; i < bins; i++)
			colors.add(barColor);

		BarChart barChart = BarCharts.createBarChart(DataConversion.arrayToList(counts), colors);
		barChart.setBackground(null);

		return barChart;
	}

	public static void addLegend(BarChart barChart, List<String> labels) {

		StringPainter[][] painters = new StringPainter[labels.size()][1];

		for (int i = 0; i < labels.size(); i++) {
			StringPainter stringPainter = new StringPainter(labels.get(i));
			stringPainter.setBackgroundPaint(null);

			stringPainter.setVerticalOrientation(true);
			stringPainter.setVerticalStringAlignment(VerticalStringAlignment.UP);
			painters[i][0] = stringPainter;
		}

		Grid2DPainterPainter<StringPainter> gridPainter = new Grid2DPainterPainter<>(painters);
		gridPainter.setBackgroundPaint(null);

		barChart.addChartPainter(gridPainter);
	}

	public static void addLegend(BarChartHorizontal barChart, List<String> labels) {
		addLegend(barChart, labels, HorizontalStringAlignment.RIGHT);
	}

	public static void addLegend(BarChartHorizontal barChart, List<String> labels,
			HorizontalStringAlignment alignment) {

		StringPainter[][] painters = new StringPainter[1][labels.size()];

		for (int i = 0; i < labels.size(); i++) {
			StringPainter stringPainter = new StringPainter(labels.get(i));
			stringPainter.setBackgroundPaint(null);

			stringPainter.setHorizontalStringAlignment(alignment);
			painters[0][i] = stringPainter;
		}

		Grid2DPainterPainter<StringPainter> gridPainter = new Grid2DPainterPainter<>(painters);
		gridPainter.setBackgroundPaint(null);

		barChart.addChartPainter(gridPainter);
	}

	public static SelectionModel<Integer> addInteraction(IBarChart barChart) {
		return addInteraction(barChart, true, true, null);
	}

	public static SelectionModel<Integer> addInteraction(IBarChart barChart, boolean clickInteraction,
			boolean rectangleSelection, SelectionModel<Integer> selectionModel) {
		if (selectionModel == null)
			selectionModel = SelectionModels.create();

		SelectionHandler<Integer> selectionHandler = new SelectionHandler<>(selectionModel);

		if (barChart instanceof Component)
			selectionHandler.attachTo((Component) barChart);

		if (clickInteraction)
			selectionHandler.setClickSelection(barChart);

		if (rectangleSelection)
			selectionHandler.setRectangleSelection(barChart);

		if (barChart instanceof InfoVisChartPanel)
			((InfoVisChartPanel) barChart).addChartPainter(new ChartPainter() {
				@Override
				public void draw(Graphics2D g2) {
					selectionHandler.draw(g2);
				}
			});

		barChart.setSelectedFunction(new Function<Integer, Boolean>() {

			@Override
			public Boolean apply(Integer t) {
				return selectionHandler.getSelectionModel().isSelected(t);
			}
		});

		return selectionModel;
	}

	public static double getGridSpacing(IBarChart barChart) {
		return barChart.getBarChartPainter().getGridSpacing();
	}

	public static void setGridSpacing(IBarChart barChart, double gridSpacing) {
		barChart.getBarChartPainter().setGridSpacing(gridSpacing);
	}

	public static boolean isToolTipping(IBarChart barChart) {
		if (barChart instanceof InfoVisChartPanel)
			return ((InfoVisChartPanel) barChart).isShowingTooltips();
		else
			return barChart.getBarChartPainter().isToolTipping();
	}

	public static void setToolTipping(IBarChart barChart, boolean toolTipping) {
		if (barChart instanceof InfoVisChartPanel)
			((InfoVisChartPanel) barChart).setShowingTooltips(toolTipping);
		else
			barChart.getBarChartPainter().setToolTipping(toolTipping);
	}

	public static Paint getBorderPaint(IBarChart barChart) {
		return barChart.getBarChartPainter().getBorderPaint();
	}

	public static void setBorderPaint(IBarChart barChart, Paint borderPaint) {
		barChart.getBarChartPainter().setBorderPaint(borderPaint);
	}

	/**
	 * avoid instantiation
	 */
	private BarCharts() {
	};
}
