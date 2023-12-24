package com.github.TKnudsen.infoVis.view.panels.grid;

import java.awt.Graphics2D;

import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;

public class Grid2DAggregationPanels {

	public static <T> void addInteraction(Grid2DAggregationPanel<T> grid2DAggregationPanel, SelectionModel<T> selectionModel) {

		SelectionHandler<T> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(grid2DAggregationPanel);

		selectionHandler.setClickSelection(grid2DAggregationPanel);

		grid2DAggregationPanel.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		selectionModel.addSelectionListener(new SelectionListener<T>() {

			@Override
			public void selectionChanged(SelectionEvent<T> selectionEvent) {
				grid2DAggregationPanel.repaint();
				grid2DAggregationPanel.revalidate();
			}
		});
	}
}
