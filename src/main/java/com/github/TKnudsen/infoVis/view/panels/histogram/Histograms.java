package com.github.TKnudsen.infoVis.view.panels.histogram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

public class Histograms {

	/**
	 * 
	 * @param histogram
	 * @param selectionModel     note that the histogram will be added to the
	 *                           selection model as a selection listener. otherwise
	 *                           the histogram would not be able to re-create a
	 *                           selection bar chart every time when the selection
	 *                           has changed.
	 * @param clickSelection
	 * @param rectangleSelection
	 */
	public static <T> void addInteraction(Histogram<T> histogram, SelectionModel<T> selectionModel,
			boolean clickSelection, boolean rectangleSelection) {

		if (!clickSelection && !rectangleSelection)
			return;

		SelectionHandler<T> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(histogram);

		if (clickSelection)
			selectionHandler.setClickSelection(histogram);

		if (rectangleSelection)
			selectionHandler.setRectangleSelection(histogram);

		histogram.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		selectionModel.addSelectionListener(histogram);
	}

	public static <T> SelectionModel<T> addInteraction(Histogram<T> histogram, boolean clickSelection,
			boolean rectangleSelection) {

		SelectionModel<T> selectionModel = SelectionModels.create();

		addInteraction(histogram, selectionModel, clickSelection, rectangleSelection);

		return selectionModel;
	}

	public static Histogram<Number> create(Collection<? extends Number> data, int binCount, boolean vertical) {
		return create(data, n -> ((Number) n).doubleValue(), defaultAggregationFunction(data, binCount), null, vertical,
				null, null);
	}

	public static <T> Histogram<T> create(Collection<T> data, Function<? super T, Number> worldToNumberMapping,
			int binCount, boolean vertical) {

		return create(data, worldToNumberMapping, defaultAggregationFunction(data, worldToNumberMapping, binCount),
				null, vertical, null, null);
	}

	public static <T> Histogram<T> create(Collection<T> data, Function<? super T, Number> worldToNumberMapping,
			boolean vertical) {
		return create(data, worldToNumberMapping, null, null, vertical, null, null);
	}

	public static <T> Histogram<T> create(Collection<? extends T> data,
			Function<? super T, Number> worldToNumberMapping, Function<Number, Integer> aggregationFunction,
			Number maxGlobal, boolean vertical, Color defaultColor, Color filterColor) {
		if (vertical)
			return new HistogramVertical<T>(data, worldToNumberMapping, aggregationFunction, maxGlobal, defaultColor,
					filterColor);
		else
			return new HistogramHorizontal<T>(data, worldToNumberMapping, aggregationFunction, maxGlobal, defaultColor,
					filterColor);
	}

	public static <T> Function<Number, Integer> defaultAggregationFunction(Collection<? extends T> data,
			Function<? super T, Number> worldToNumberMapping, int binCount) {

		Collection<Number> numbers = new ArrayList<>();
		for (T t : data)
			numbers.add(worldToNumberMapping.apply(t));

		return defaultAggregationFunction(MathFunctions.getMin(numbers), MathFunctions.getMax(numbers), binCount);
	}

	public static Function<Number, Integer> defaultAggregationFunction(Collection<? extends Number> data,
			int binCount) {
		return defaultAggregationFunction(MathFunctions.getMin(data), MathFunctions.getMax(data), binCount);
	}

	/**
	 * 
	 * @param minValue min value of the target value domain
	 * @param maxValue max value of the target value domain
	 * @param binCount number of bins (e.g., bars in the Histogram)
	 * @return
	 */
	public static Function<Number, Integer> defaultAggregationFunction(Number minValue, Number maxValue, int binCount) {
		Objects.requireNonNull(minValue);
		Objects.requireNonNull(maxValue);

		if (maxValue.doubleValue() < minValue.doubleValue())
			System.err.println(
					"Histograms.defaultAggregationFunction: handled exception to cope with min value larger than max value");

		return new Function<Number, Integer>() {

			private double interval = Math.abs(maxValue.doubleValue() - minValue.doubleValue()) / (double) binCount;

			@Override
			public Integer apply(Number t) {
				if (t == null || Double.isNaN(t.doubleValue())) {
					System.err.println("Histograms.defaultAggregationFunction: value " + t
							+ " could not be binned. Returning null");
					return null;
				}
				if (t.doubleValue() < minValue.doubleValue()) {
					System.err.println("Histograms.defaultAggregationFunction: value " + t
							+ " smaller than minValue. Returning null");
					return null;
				}
				if (t.doubleValue() > maxValue.doubleValue()) {
					System.err.println("Histograms.defaultAggregationFunction: value " + t
							+ " greater than maxValue. Returning null");
					return null;
				}

				for (int i = 0; i < binCount; i++)
					if (t.doubleValue() <= minValue.doubleValue() + (i + 1) * interval)
						return i;

				// avoid exceptions due to double precision
				if (MathFunctions.round(t.doubleValue(), 7) <= MathFunctions
						.round(minValue.doubleValue() + binCount * interval, 7))
					return binCount;

				throw new IllegalArgumentException(
						"Histograms.defaultAggregationFunction: value " + t + " could not be binned");
			}
		};
	}

	public static <T> void setShowValueDomainAxis(Histogram<T> histogram, boolean show) {
		if (histogram.isVertical())
			histogram.setDrawXAxis(show);
		else
			histogram.setDrawYAxis(show);
	}
}
