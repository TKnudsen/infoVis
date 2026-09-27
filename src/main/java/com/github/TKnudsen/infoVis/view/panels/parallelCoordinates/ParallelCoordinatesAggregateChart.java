package com.github.TKnudsen.infoVis.view.panels.parallelCoordinates;

import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.chartLayouts.YYYAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainters;
import com.github.TKnudsen.infoVis.view.painters.parallelCoordinates.ParallelCoordinatesAggregatePainter;
import com.github.TKnudsen.infoVis.view.panels.axis.YYYAxesNumericalChartPanel;

/**
 * <p>
 * Chart panel for {@link ParallelCoordinatesAggregatePainter}: adds the same
 * Y-axes-with-name-labels frame {@link ParallelCoordinates} gives the
 * row-level painter, so aggregate bands/center lines aren't drawn axis-less.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ParallelCoordinatesAggregateChart<T> extends YYYAxesNumericalChartPanel<Double>
		implements IRectangleSelection<Integer>, IShapeSelection<Integer>, IClickSelection<Integer>,
		ISelectionVisualizer<Integer> {

	private static final long serialVersionUID = 1L;

	private final ParallelCoordinatesAggregatePainter<T> aggregatePainter;

	public ParallelCoordinatesAggregateChart(List<List<T>> groups,
			List<Function<? super T, Double>> worldPositionMappingsY,
			Function<? super Integer, ? extends Paint> groupColorMapping) {
		super(worldPositionMappingsY.size());

		YYYAxisChartRectangleLayout layout = (YYYAxisChartRectangleLayout) getChartRectangleLayout();
		layout.setRelativeSpaceBetweenAxes(Math.min(0.4, Math.max(0, -0.0375 * worldPositionMappingsY.size() + 0.375)));

		List<T> flattened = new ArrayList<>();
		for (List<T> group : groups)
			flattened.addAll(group);

		List<YAxisNumericalPainter<Double>> yAxisPainters = new ArrayList<>();
		for (Function<? super T, Double> mapping : worldPositionMappingsY) {
			YAxisNumericalPainter<Double> yAxis = YAxisNumericalPainters.create(flattened, mapping);
			yAxis.setDrawPhysicalUnit(isDrawAxesNames());
			yAxisPainters.add(yAxis);
		}
		setYAxisPainters(yAxisPainters);

		aggregatePainter = new ParallelCoordinatesAggregatePainter<>(groups, worldPositionMappingsY,
				groupColorMapping);
		aggregatePainter.setXPositionEncodingFunction(getXPositionEncodingFunction());
		aggregatePainter.setYPositionEncodingFunctions(getYPositionEncodingFunctions());

		addChartPainter(aggregatePainter, true);

		setBackground(null);
	}

	public ParallelCoordinatesAggregatePainter<T> getAggregatePainter() {
		return aggregatePainter;
	}

	@Override
	public List<Integer> getElementsInRectangle(RectangularShape rectangle) {
		return aggregatePainter.getElementsInRectangle(rectangle);
	}

	@Override
	public List<Integer> getElementsInShape(Shape shape) {
		return aggregatePainter.getElementsInShape(shape);
	}

	@Override
	public List<Integer> getElementsAtPoint(Point p) {
		return aggregatePainter.getElementsAtPoint(p);
	}

	@Override
	public void setSelectedFunction(Function<? super Integer, Boolean> selectedFunction) {
		aggregatePainter.setSelectedFunction(selectedFunction);
	}

}
