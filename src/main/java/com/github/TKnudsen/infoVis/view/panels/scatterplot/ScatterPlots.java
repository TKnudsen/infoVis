package com.github.TKnudsen.infoVis.view.panels.scatterplot;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.handlers.LassoSelectionHandler;
import com.github.TKnudsen.infoVis.view.interaction.handlers.MouseButton;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.VisualMappings;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.SizeEncodingFunction;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * 
 * <p>
 * InfoVis
 * </p>
 * 
 * Creates Scatterplots and/or adds interaction.
 * 
 * <p>
 * Copyright: (c) 2018-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.06
 *
 */
public class ScatterPlots {

	private static double SCALE = 0.95;
	private static double MIN_SIZE = 2.0;

	public static ScatterPlot<Double[]> createForDoubles(List<Double[]> points, List<? extends Paint> colors) {
		ColorEncodingFunction<Double[]> colorMapping = new ColorEncodingFunction<Double[]>(points, colors);
		Function<Double[], Double> worldToDoubleMappingX = p -> p[0];
		Function<Double[], Double> worldDoubleMappingY = p -> p[1];
		return new ScatterPlot<>(points, colorMapping, worldToDoubleMappingX, worldDoubleMappingY);
	}

	public static ScatterPlot<Point2D> createForPoints(List<Point2D> points, List<? extends Paint> colors) {
		ColorEncodingFunction<Point2D> colorMapping = new ColorEncodingFunction<Point2D>(points, colors);
		Function<Point2D, Double> worldToDoubleMappingX = p -> p.getX();
		Function<Point2D, Double> worldDoubleMappingY = p -> p.getY();
		return new ScatterPlot<>(points, colorMapping, worldToDoubleMappingX, worldDoubleMappingY);
	}

	/**
	 * most general form for creating a scatter plot.
	 * 
	 * @param <T>
	 * @param data
	 * @param worldPositionMappingX
	 * @param worldPositionMappingY
	 * @param colorMapping
	 * @param alarmWhenDataSanitiCheckFails
	 * @param selectionModel
	 * @return
	 */
	public static <T> ScatterPlot<T> create(List<T> data, Function<? super T, Double> worldPositionMappingX,
			Function<? super T, Double> worldPositionMappingY, Function<? super T, ? extends Paint> colorMapping,
			boolean alarmWhenDataSanitiCheckFails, SelectionModel<T> selectionModel) {

		List<T> filteredData = ScatterPlots.sanityCheckFilter(data, worldPositionMappingX, worldPositionMappingY,
				alarmWhenDataSanitiCheckFails);

		ScatterPlot<T> scatterPlot = new ScatterPlot<T>(filteredData, colorMapping, worldPositionMappingX,
				worldPositionMappingY);

		scatterPlot.setSizeEncodingFunction(new SizeEncodingFunction<>(scatterPlot, SCALE, MIN_SIZE));

		scatterPlot.setDrawXAxis(true);
		scatterPlot.setDrawYAxis(true);

		scatterPlot.setXAxisOverlay(false);
		scatterPlot.setYAxisOverlay(false);

		if (selectionModel != null)
			addInteraction(scatterPlot, selectionModel, true, true, true);
		else
			addInteraction(scatterPlot, true, true, true);

		return scatterPlot;
	}

	public static <T> SelectionModel<T> addInteraction(ScatterPlot<T> scatterPlot, boolean clickSelection,
			boolean rectangleSelection, boolean addLassoInteraction) {
		SelectionModel<T> selectionModel = SelectionModels.create();

		addInteraction(scatterPlot, selectionModel, clickSelection, rectangleSelection, addLassoInteraction);

		return selectionModel;
	}

	public static <T> void addInteraction(ScatterPlot<T> scatterPlot, SelectionModel<T> selectionModel,
			boolean clickSelection, boolean rectangleSelection, boolean addLassoInteraction) {
		if (!clickSelection && !rectangleSelection)
			return;

		SelectionHandler<T> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(scatterPlot);

		if (clickSelection)
			selectionHandler.setClickSelection(scatterPlot);

		if (rectangleSelection)
			selectionHandler.setRectangleSelection(scatterPlot);

		scatterPlot.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		scatterPlot.setSelectedFunction(new Function<T, Boolean>() {
			@Override
			public Boolean apply(T t) {
				return selectionHandler.getSelectionModel().isSelected(t);
			}
		});

		// lasso selection with the right mouse button
		if (addLassoInteraction) {
			LassoSelectionHandler<T> lassoSelectionHandler = new LassoSelectionHandler<>(selectionModel,
					MouseButton.RIGHT);
			lassoSelectionHandler.attachTo(scatterPlot);
			lassoSelectionHandler.setShapeSelection(scatterPlot);

			scatterPlot.addChartPainter(new ChartPainter() {
				@Override
				public void draw(Graphics2D g2) {
					lassoSelectionHandler.draw(g2);
				}
			});
		}

		selectionModel.addSelectionListener(new SelectionListener<T>() {

			@Override
			public void selectionChanged(SelectionEvent<T> selectionEvent) {
				scatterPlot.repaint();
				scatterPlot.revalidate();
			}
		});
	}

	/**
	 * applies a filter operation using a given data set. and returns a new list,
	 * only containing those elements which can be applied by both given position
	 * mapping functions.
	 * 
	 * @param data
	 * @param worldToDoubleMappingX
	 * @param worldDoubleMappingY
	 * @return
	 */
	public static <T> List<T> sanityCheckFilter(List<T> data, Function<? super T, Double> worldToDoubleMappingX,
			Function<? super T, Double> worldDoubleMappingY) {
		return sanityCheckFilter(data, worldToDoubleMappingX, worldDoubleMappingY, false);
	}

	/**
	 * applies a filter operation using a given data set. returns a new list, only
	 * containing those elements which can be applied by both given position mapping
	 * functions.
	 * 
	 * @param data
	 * @param worldToDoubleMappingX
	 * @param worldDoubleMappingY
	 * @param warnForQualityLeaks
	 * @return
	 */
	public static <T> List<T> sanityCheckFilter(List<T> data, Function<? super T, Double> worldToDoubleMappingX,
			Function<? super T, Double> worldDoubleMappingY, boolean warnForQualityLeaks) {

		return VisualMappings.sanityCheckFilter(data, worldToDoubleMappingX, worldDoubleMappingY, warnForQualityLeaks);
	}
}
