package com.github.TKnudsen.infoVis.view.painters.parallelCoordinates.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.interaction.handlers.LassoSelectionHandler;
import com.github.TKnudsen.infoVis.view.interaction.handlers.MouseButton;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.panels.parallelCoordinates.ParallelCoordinatesAggregateChart;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * Standalone demo of {@link ParallelCoordinatesAggregateChart}: 4 synthetic
 * groups (as would come from e.g. a clustering result, though this painter is
 * agnostic of where groups come from), each drawn as one interquartile band, a
 * dashed 5th/95th-percentile outlier band, and one bold median line, instead
 * of plotting every individual row -- with click, rectangle (left mouse
 * button) and lasso (right mouse button) selection of whole groups.
 *
 * @version 1.0
 * @since 2026
 */
public class ParallelCoordinatesAggregatePainterTester {

	public static void main(String[] args) {
		Random random = new Random(11);

		int dimensions = 6;
		int groupCount = 4;
		int rowsPerGroup = 200;

		List<List<double[]>> groups = new ArrayList<>();
		for (int g = 0; g < groupCount; g++) {
			List<double[]> group = new ArrayList<>();
			double[] center = new double[dimensions];
			for (int d = 0; d < dimensions; d++)
				center[d] = random.nextDouble();

			for (int i = 0; i < rowsPerGroup; i++) {
				double[] row = new double[dimensions];
				for (int d = 0; d < dimensions; d++)
					row[d] = Math.max(0, Math.min(1, center[d] + random.nextGaussian() * 0.08));
				group.add(row);
			}
			groups.add(group);
		}

		List<Function<? super double[], Double>> worldPositionMappingsY = new ArrayList<>();
		for (int d = 0; d < dimensions; d++)
			worldPositionMappingsY.add(new DimensionMapping(d));

		Color[] groupColors = { Color.RED, Color.BLUE, Color.GREEN.darker(), Color.ORANGE.darker() };
		Function<? super Integer, ? extends Paint> groupColorMapping = groupIndex -> groupColors[groupIndex
				% groupColors.length];

		ParallelCoordinatesAggregateChart<double[]> chart = new ParallelCoordinatesAggregateChart<>(groups,
				worldPositionMappingsY, groupColorMapping);

		attachInteractions(chart);

		SwingUtilities.invokeLater(
				() -> SVGFrameTools.dropSVGFrame(chart, "ParallelCoordinatesAggregatePainter Demo", 800, 500));
	}

	/** Mirrors {@code ParallelCoordinatesFactory.attachInteractions}, keyed by group index (Integer) instead of by row. */
	private static void attachInteractions(ParallelCoordinatesAggregateChart<double[]> chart) {
		SelectionModel<Integer> selectionModel = SelectionModels.create();

		SelectionHandler<Integer> selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(chart);
		selectionHandler.setClickSelection(chart);
		selectionHandler.setRectangleSelection(chart);

		chart.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		chart.setSelectedFunction(groupIndex -> selectionModel.isSelected(groupIndex));

		LassoSelectionHandler<Integer> lassoSelectionHandler = new LassoSelectionHandler<>(selectionModel,
				MouseButton.RIGHT);
		lassoSelectionHandler.attachTo(chart);
		lassoSelectionHandler.setShapeSelection(chart);

		chart.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				lassoSelectionHandler.draw(g2);
			}
		});

		SelectionListener<Integer> selectionListener = (SelectionEvent<Integer> e) -> chart.repaint();
		selectionModel.addSelectionListener(selectionListener);
	}

	private static class DimensionMapping implements Function<double[], Double> {
		private final int dimension;

		DimensionMapping(int dimension) {
			this.dimension = dimension;
		}

		@Override
		public Double apply(double[] row) {
			return row[dimension];
		}

		@Override
		public String toString() {
			return "dim " + dimension;
		}
	}

}
