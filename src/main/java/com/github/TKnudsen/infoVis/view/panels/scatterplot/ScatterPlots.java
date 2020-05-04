package com.github.TKnudsen.infoVis.view.panels.scatterplot;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.handlers.LassoSelectionHandler;
import com.github.TKnudsen.infoVis.view.interaction.handlers.MouseButton;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;

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
 * Creates scatterplots
 * 
 * <p>
 * Copyright: (c) 2018-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 *
 */
public class ScatterPlots {

	public static ScatterPlot<Double[]> createForDoubles(List<Double[]> points, List<? extends Paint> colors) {
		ColorEncodingFunction<Double[]> colorMapping = new ColorEncodingFunction<Double[]>(points, colors);
		Function<Double[], Double> worldPositionMappingX = p -> p[0];
		Function<Double[], Double> worldPositionMappingY = p -> p[1];
		return new ScatterPlot<>(points, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

	public static ScatterPlot<Point2D> createForPoints(List<Point2D> points, List<? extends Paint> colors) {
		ColorEncodingFunction<Point2D> colorMapping = new ColorEncodingFunction<Point2D>(points, colors);
		Function<Point2D, Double> worldPositionMappingX = p -> p.getX();
		Function<Point2D, Double> worldPositionMappingY = p -> p.getY();
		return new ScatterPlot<>(points, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

	public static <T> SelectionModel<T> addInteraction(ScatterPlot<T> scatterPlot, boolean addClickSelection,
			boolean addRectangleSelection, boolean addLassoInteraction) {
		// SELECTION MODEL
		SelectionModel<T> selectionModel = SelectionModels.create();

		addInteraction(scatterPlot, selectionModel, addClickSelection, addRectangleSelection, addLassoInteraction);

		return selectionModel;
	}

	public static <T> void addInteraction(ScatterPlot<T> scatterPlot, SelectionModel<T> selectionModel,
			boolean addClickSelection, boolean addRectangleSelection, boolean addLassoInteraction) {

		// SELECTION HANDLER
		SelectionHandler<T> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(scatterPlot);

		if (addClickSelection)
			selectionHandler.setClickSelection(scatterPlot);

		if (addRectangleSelection)
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

		// LASSO SELECTION (RIGHT MOUSE BUTTON)
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
	 * applies a filter operation using a given dataset. and returns a new list,
	 * only containing those elements which can be applied by both given position
	 * mapping functions.
	 * 
	 * @param data
	 * @param worldPositionMappingX
	 * @param worldPositionMappingY
	 * @return
	 */
	public static <T> List<T> sanityCheckFilter(List<T> data, Function<? super T, Double> worldPositionMappingX,
			Function<? super T, Double> worldPositionMappingY) {
		return sanityCheckFilter(data, worldPositionMappingX, worldPositionMappingY, false);
	}

	/**
	 * applies a filter operation using a given dataset. returns a new list, only
	 * containing those elements which can be applied by both given position mapping
	 * functions.
	 * 
	 * @param data
	 * @param worldPositionMappingX
	 * @param worldPositionMappingY
	 * @param warnForQualityLeaks
	 * @return
	 */
	public static <T> List<T> sanityCheckFilter(List<T> data, Function<? super T, Double> worldPositionMappingX,
			Function<? super T, Double> worldPositionMappingY, boolean warnForQualityLeaks) {
		List<T> returnList = new ArrayList<T>();

		try {
			for (T t : data) {
				Double x = worldPositionMappingX.apply(t);
				Double y = worldPositionMappingY.apply(t);
				if (x != null && !Double.isNaN(x) && y != null && !Double.isNaN(y))
					returnList.add(t);
				else if (warnForQualityLeaks)
					System.err
							.println("ScatterPlots.sanityCheckFilter: object " + t + " did not pass the sanity check");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnList;
	}
}
