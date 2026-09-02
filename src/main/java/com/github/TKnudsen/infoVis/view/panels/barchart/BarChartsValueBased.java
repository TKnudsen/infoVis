package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;
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
import com.github.TKnudsen.infoVis.view.ui.InfoVisColors;

import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * <p>
 * Factory for BarChartsValueBased. Also provides tools and bar chart modification
 * capability.
 * </p>
 *
 * @version 1.04
 * @since 2016
 */
public class BarChartsValueBased {

	public static final Color DEFAULT_COLOR = Color.GRAY;

	public static final Color DEFAULT_FILTER_COLOR = Color.DARK_GRAY;

	/**
	 * creates a bar chart with all bars having the same color
	 * 
	 * @param counts counts
	 * @param color  color
	 * @return bar chart
	 */
	public static BarChartValueBased createBarChart(Map<String, Integer> counts, Color color) {
		List<Number> data = new ArrayList<Number>();
		List<Color> colors = new ArrayList<Color>();

		for (@SuppressWarnings("unused")
		String s : counts.keySet()) {
			data.add(counts.get(s));
			colors.add(color);
		}

		return new BarChartValueBased(data, colors);
	}

	/**
	 * creates a bar chart with all bars having the same color
	 * 
	 * @param bars  bars
	 * @param color color
	 * @return bar chart
	 */
	public static BarChartValueBased createBarChart(List<? extends Number> bars, Color color) {
		List<Color> colors = new ArrayList<Color>();

		for (@SuppressWarnings("unused")
		Number d : bars)
			colors.add(color);

		return new BarChartValueBased(bars, colors);
	}

	public static BarChartValueBased createBarChart(List<? extends Number> bars, List<Color> colors) {
		return new BarChartValueBased(bars, colors);
	}

	public static BarChartHorizontalValueBased createBarChartHorizontal(List<? extends Number> bars, Color color) {
		List<Color> colors = new ArrayList<Color>();

		for (@SuppressWarnings("unused")
		Number d : bars)
			colors.add(color);

		return createBarChartHorizontal(bars, colors);
	}

	public static BarChartHorizontalValueBased createBarChartHorizontal(List<? extends Number> bars, List<Color> colors) {
		return new BarChartHorizontalValueBased(bars, colors);
	}

	/**
	 * creates a bar chart with labels
	 * 
	 * @param <T>
	 * @param data                   the raw (categorical) data
	 * @param worldToCategoryMapping the mapping of raw (categorical) data to an
	 *                               alphabet of categories. can be null or s->s.
	 * @return
	 */
	public static <T> BarChartValueBased createBarChartWithLabels(Collection<? extends T> data,
			Function<? super T, String> worldToCategoryMapping) {

		Function<? super T, String> f = worldToCategoryMapping != null ? worldToCategoryMapping
				: c -> Parsers.parseString(c);

		SortedMap<String, Integer> barChartData = createData(data, f);

		List<String> labels = new ArrayList<>(barChartData.keySet());
		List<Integer> bars = new ArrayList<>();
		for (String label : labels)
			bars.add(barChartData.get(label));

		BarChartValueBased barChart = BarChartsValueBased.createBarChart(bars, Color.GRAY);
		barChart.setBackground(null);
		BarChartsValueBased.setSelectionPaint(barChart, InfoVisColors.SELECTION_COLOR);
		BarChartsValueBased.setBorderPaint(barChart, null);
		BarChartsValueBased.addLegend(barChart, labels);

		return barChart;
	}

	/**
	 * creates a bar chart with labels
	 * 
	 * @param <T>
	 * @param data                   the raw (categorical) data
	 * @param worldToCategoryMapping the mapping of raw (categorical) data to an
	 *                               alphabet of categories. can be null or s->s.
	 * @return
	 */
	public static <T> BarChartHorizontalValueBased createBarChartHorizontalWithLabels(Collection<? extends T> data,
			Function<? super T, String> worldToCategoryMapping) {

		Function<? super T, String> f = worldToCategoryMapping != null ? worldToCategoryMapping
				: c -> Parsers.parseString(c);

		SortedMap<String, Integer> barChartData = createData(data, f);

		List<String> labels = new ArrayList<>(barChartData.keySet());
		List<Integer> bars = new ArrayList<>();
		for (String label : labels)
			bars.add(barChartData.get(label));

		BarChartHorizontalValueBased barChart = BarChartsValueBased.createBarChartHorizontal(bars, Color.GRAY);
		barChart.setBackground(null);
		barChart.getBarChartPainter().setSelectionPaint(InfoVisColors.SELECTION_COLOR);
		barChart.getBarChartPainter().setBorderPaint(null);
		BarChartsValueBased.addLegend(barChart, labels, HorizontalStringAlignment.LEFT);

		return barChart;
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
	public static BarChartValueBased createLayeredBarChart(List<List<? extends Number>> data, List<Color> barchartColors) {
		Objects.requireNonNull(data);

		if (data.isEmpty())
			return null;

		// create bar chart
		Color color = Color.GRAY;
		if (barchartColors != null && !barchartColors.isEmpty())
			color = barchartColors.get(0);
		BarChartValueBased barChart = createBarChart(data.get(0), color);

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
	public static BarChartValueBased createHistogramBarchart(Collection<? extends Number> values, int bins, Color barColor) {

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

		BarChartValueBased barChart = BarChartsValueBased.createBarChart(DataConversion.arrayToList(counts), colors);
		barChart.setBackground(null);

		return barChart;
	}

	public static void addLegend(BarChartValueBased barChart, List<String> labels) {

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

	public static void addLegend(BarChartHorizontalValueBased barChart, List<String> labels) {
		addLegend(barChart, labels, HorizontalStringAlignment.RIGHT);
	}

	public static void addLegend(BarChartHorizontalValueBased barChart, List<String> labels,
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

	public static SelectionModel<Integer> addInteraction(IBarChartValueBased barChart) {
		return addInteraction(barChart, true, true, null);
	}

	public static SelectionModel<Integer> addInteraction(IBarChartValueBased barChart, boolean clickInteraction,
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

	public static double getGridSpacing(IBarChartValueBased barChart) {
		return BarChartStylingSupport.getGridSpacing(barChart.getBarChartPainter());
	}

	public static void setGridSpacing(IBarChartValueBased barChart, double gridSpacing) {
		barChart.getBarChartPainter().setGridSpacing(gridSpacing);
	}

	public static boolean isToolTipping(IBarChartValueBased barChart) {
		return BarChartStylingSupport.isToolTipping(barChart, barChart.getBarChartPainter());
	}

	public static void setToolTipping(IBarChartValueBased barChart, boolean toolTipping) {
		BarChartStylingSupport.setToolTipping(barChart, toolTipping, barChart.getBarChartPainter());
	}

	public static Paint getBorderPaint(IBarChartValueBased barChart) {
		return BarChartStylingSupport.getBorderPaint(barChart.getBarChartPainter());
	}

	public static void setBorderPaint(IBarChartValueBased barChart, Paint borderPaint) {
		barChart.getBarChartPainter().setBorderPaint(borderPaint);
	}

	public static void setColors(IBarChartValueBased barChart, Color[] colors) {
		barChart.getBarChartPainter().setColors(colors);
	}

	public static void setColors(IBarChartValueBased barChart, List<Color> colors) {
		barChart.getBarChartPainter().setColors(colors);
	}

	public static void setSelectionPaint(IBarChartValueBased barChart, Color color) {
		barChart.getBarChartPainter().setSelectionPaint(color);
	}

	/**
	 * avoid instantiation
	 */
	private BarChartsValueBased() {
	};
}
