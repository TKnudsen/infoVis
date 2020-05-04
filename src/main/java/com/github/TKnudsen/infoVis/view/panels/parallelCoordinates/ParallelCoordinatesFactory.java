package com.github.TKnudsen.infoVis.view.panels.parallelCoordinates;

import java.awt.Graphics2D;
import java.awt.Paint;
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

/**
 * 
 * <p>
 * InfoVis
 * </p>
 * 
 * Creates parallel coordinates panels
 * 
 * <p>
 * Copyright: (c) 2018-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 *
 */
public class ParallelCoordinatesFactory {

	public static ParallelCoordinates<Double[]> createForDoubles(List<Double[]> points, List<? extends Paint> colors) {
		ColorEncodingFunction<Double[]> colorMapping = new ColorEncodingFunction<Double[]>(points, colors);

		int dimensionality = Integer.MAX_VALUE;
		for (Double[] o : points)
			dimensionality = Math.min(dimensionality, o.length);

		if (dimensionality < 1)
			throw new IllegalArgumentException("ParallelCoordinatesFactory: too few dimensions: " + dimensionality);

		List<Function<? super Double[], Double>> worldPositionMappingYs = new ArrayList<>();
		for (int i = 0; i < dimensionality; i++) {
			final int index = i;
			worldPositionMappingYs.add(p -> p[index]);
		}

		return new ParallelCoordinates<Double[]>(points, colorMapping, worldPositionMappingYs);
	}

	public static <T> void attachInteractions(ParallelCoordinates<T> parallelCoordinates, SelectionModel<T> selectionModel,
			boolean addClickSelection, boolean addRectangleSelection, boolean addLassoInteraction) {

		// SELECTION HANDLER
		SelectionHandler<T> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(parallelCoordinates);

		if (addClickSelection)
			selectionHandler.setClickSelection(parallelCoordinates);

		if (addRectangleSelection)
			selectionHandler.setRectangleSelection(parallelCoordinates);

		parallelCoordinates.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		parallelCoordinates.setSelectedFunction(new Function<T, Boolean>() {
			@Override
			public Boolean apply(T t) {
				return selectionHandler.getSelectionModel().isSelected(t);
			}
		});

		// LASSO SELECTION (RIGHT MOUSE BUTTON)
		if (addLassoInteraction) {
			LassoSelectionHandler<T> lassoSelectionHandler = new LassoSelectionHandler<>(selectionModel,
					MouseButton.RIGHT);
			lassoSelectionHandler.attachTo(parallelCoordinates);
			lassoSelectionHandler.setShapeSelection(parallelCoordinates);

			parallelCoordinates.addChartPainter(new ChartPainter() {
				@Override
				public void draw(Graphics2D g2) {
					lassoSelectionHandler.draw(g2);
				}
			});
		}

		selectionModel.addSelectionListener(new SelectionListener<T>() {

			@Override
			public void selectionChanged(SelectionEvent<T> selectionEvent) {
				parallelCoordinates.repaint();
			}
		});
	}
}
