package com.github.TKnudsen.infoVis.view.panels.boxplot;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

import de.javagl.selection.LoggingSelectionListener;
import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

public class Boxplots {

	public static <T> BoxPlotHorizontalChartPanel createHorizontalBoxplot(Collection<T> data,
			Function<? super T, ? extends Number> worldToDoubleMapping, double minGlobal, double maxGlobal) {

		if (data == null || worldToDoubleMapping == null)
			throw new IllegalArgumentException("Boxplots: no valid input given");

		Collection<Number> d = new ArrayList<>();
		for (T t : data) {
			Number n = worldToDoubleMapping.apply(t);
			if (!Double.isNaN(n.doubleValue()))
				d.add(n);
		}

		StatisticsSupport statistics = new StatisticsSupport(d);

		BoxPlotHorizontalChartPanel panel = new BoxPlotHorizontalChartPanel(statistics, minGlobal, maxGlobal);

		return panel;
	}

	public static <T> BoxPlotVerticalChartPanel createVerticalBoxplot(Collection<T> data,
			Function<? super T, ? extends Number> worldToDoubleMapping, double minGlobal, double maxGlobal) {

		if (data == null || worldToDoubleMapping == null)
			throw new IllegalArgumentException("Boxplots: no valid input given");

		Collection<Number> d = new ArrayList<>();
		for (T t : data) {
			Number n = worldToDoubleMapping.apply(t);
			if (!Double.isNaN(n.doubleValue()))
				d.add(n);
		}

		StatisticsSupport statistics = new StatisticsSupport(d);

		BoxPlotVerticalChartPanel panel = new BoxPlotVerticalChartPanel(statistics, minGlobal, maxGlobal);

		return panel;
	}

	/**
	 * 
	 * @param <T>
	 * @param panel                the box plot that will receive rectangle
	 *                             selection interaction
	 * @param data                 the data that went into the box plot
	 * @param worldToDoubleMapping the mapping to double values
	 * @param selectionModel       the selection model of T if existing
	 * @return SelectionModel the in-going selection model or a newly created one
	 */
	public static <T> SelectionModel<T> addInteraction(BoxPlotHorizontalChartPanel panel, Collection<T> data,
			Function<? super T, ? extends Number> worldToDoubleMapping, SelectionModel<T> selectionModel) {

		if (data == null || worldToDoubleMapping == null)
			throw new IllegalArgumentException("Boxplots: no valid input given");

		// SELECTION MODEL
		SelectionModel<Double> sm = SelectionModels.create();

		// SELECTION HANDLER
		SelectionHandler<Double> selectionHandler = new SelectionHandler<>(sm);
		selectionHandler.attachTo(panel);
		selectionHandler.setRectangleSelection(panel);

		panel.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		sm.addSelectionListener(new LoggingSelectionListener<>());

		return addAbstractInteraction(data, worldToDoubleMapping, sm, selectionModel);
	}

	/**
	 * 
	 * @param <T>
	 * @param panel                the box plot that will receive rectangle
	 *                             selection interaction
	 * @param data                 the data that went into the box plot
	 * @param worldToDoubleMapping the mapping to double values
	 * @param selectionModel       the selection model of T if existing
	 * @return SelectionModel the in-going selection model or a newly created one
	 */
	public static <T> SelectionModel<T> addInteraction(BoxPlotVerticalChartPanel panel, Collection<T> data,
			Function<? super T, ? extends Number> worldToDoubleMapping, SelectionModel<T> selectionModel) {

		if (data == null || worldToDoubleMapping == null)
			throw new IllegalArgumentException("Boxplots: no valid input given");

		// SELECTION MODEL
		SelectionModel<Double> sm = SelectionModels.create();

		// SELECTION HANDLER
		SelectionHandler<Double> selectionHandler = new SelectionHandler<>(sm);
		selectionHandler.attachTo(panel);
		selectionHandler.setRectangleSelection(panel);

		panel.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		return addAbstractInteraction(data, worldToDoubleMapping, sm, selectionModel);
	}

	/**
	 * 
	 * @param <T>
	 * @param panel                the box plot that will receive rectangle
	 *                             selection interaction
	 * @param data                 the data that went into the box plot
	 * @param worldToDoubleMapping the mapping to double values
	 * @param selectionModel       the selection model of T if existing
	 * @return SelectionModel the in-going selection model or a newly created one
	 */
	private static <T> SelectionModel<T> addAbstractInteraction(Collection<T> data,
			Function<? super T, ? extends Number> worldToDoubleMapping, SelectionModel<Double> sm,
			SelectionModel<T> selectionModel) {

		if (data == null || worldToDoubleMapping == null)
			throw new IllegalArgumentException("Boxplots: no valid input given");

		SelectionModel<T> returnSM = selectionModel == null ? SelectionModels.create() : selectionModel;

		sm.addSelectionListener(new SelectionListener<Double>() {

			@Override
			public void selectionChanged(SelectionEvent<Double> selectionEvent) {
				double min = Double.POSITIVE_INFINITY;
				double max = Double.NEGATIVE_INFINITY;

				for (Double d : selectionEvent.getSelectionModel().getSelection()) {
					min = Math.min(min, d);
					max = Math.max(max, d);
				}

				Collection<T> d = new ArrayList<>();
				for (T t : data) {
					Number n = worldToDoubleMapping.apply(t);
					if (!Double.isNaN(n.doubleValue()))
						if (n.doubleValue() >= min && n.doubleValue() <= max)
							d.add(t);
				}

				returnSM.setSelection(d);
			}
		});

		// selectionModel.addSelectionListener(new LoggingSelectionListener<>());

		return returnSM;
	}
}
