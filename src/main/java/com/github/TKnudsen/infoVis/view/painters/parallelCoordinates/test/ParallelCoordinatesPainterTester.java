package com.github.TKnudsen.infoVis.view.painters.parallelCoordinates.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.panels.parallelCoordinates.ParallelCoordinates;
import com.github.TKnudsen.infoVis.view.panels.parallelCoordinates.ParallelCoordinatesFactory;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;

import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * Standalone demo of {@link ParallelCoordinates} /
 * {@link com.github.TKnudsen.infoVis.view.painters.parallelCoordinates.ParallelCoordinatesPainter}:
 * a synthetic dataset of 5-dimensional rows, colored by their first dimension,
 * with click, rectangle (left mouse button) and lasso (right mouse button)
 * selection wired up via {@link ParallelCoordinatesFactory}.
 *
 * @version 1.0
 * @since 2026
 */
public class ParallelCoordinatesPainterTester {

	public static void main(String[] args) {
		Random random = new Random(7);

		int dimensions = 5;
		int rows = 60;

		List<Double[]> data = new ArrayList<>();
		List<Color> colors = new ArrayList<>();
		for (int i = 0; i < rows; i++) {
			Double[] row = new Double[dimensions];
			for (int d = 0; d < dimensions; d++)
				row[d] = random.nextGaussian() * 0.15 + (d + 1) / (double) (dimensions + 1);
			data.add(row);
			colors.add(Color.getHSBColor((float) (double) row[0], 0.7f, 0.85f));
		}

		List<Function<? super Double[], Double>> worldPositionMappingsY = new ArrayList<>();
		for (int d = 0; d < dimensions; d++)
			worldPositionMappingsY.add(new DimensionMapping(d));

		ColorEncodingFunction<Double[]> colorMapping = new ColorEncodingFunction<>(data, colors);

		ParallelCoordinates<Double[]> parallelCoordinates = new ParallelCoordinates<>(data, colorMapping,
				worldPositionMappingsY);

		SelectionModel<Double[]> selectionModel = SelectionModels.create();
		ParallelCoordinatesFactory.attachInteractions(parallelCoordinates, selectionModel, true, true, true);

		SwingUtilities.invokeLater(
				() -> SVGFrameTools.dropSVGFrame(parallelCoordinates, "ParallelCoordinatesPainter Demo", 800, 500));
	}

	/**
	 * Named alternative to a plain lambda: YAxisNumericalPainters.create(...)
	 * labels each axis with {@code worldPositionMappingY.toString()}, which for an
	 * anonymous lambda is an unreadable identity string (e.g.
	 * {@code Tester$$Lambda$12@1a2b3c}) -- with several axes stacked at the top of
	 * the chart, those collide into an unreadable mess.
	 */
	private static class DimensionMapping implements Function<Double[], Double> {
		private final int dimension;

		DimensionMapping(int dimension) {
			this.dimension = dimension;
		}

		@Override
		public Double apply(Double[] row) {
			return row[dimension];
		}

		@Override
		public String toString() {
			return "dim " + dimension;
		}
	}

}
