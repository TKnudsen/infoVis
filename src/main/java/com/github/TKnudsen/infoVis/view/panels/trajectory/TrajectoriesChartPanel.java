package com.github.TKnudsen.infoVis.view.panels.trajectory;

import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainter;
import com.github.TKnudsen.infoVis.view.painters.trajectory.TrajectoryPainter;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.SizeEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Chart panel for trajectories data mapped to numerical x and y axes.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class TrajectoriesChartPanel<T> extends TrajectoryChartPanel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5657437557932197054L;

	private final List<List<T>> data;

	private List<ScatterPlotPainter<T>> remainingTrajectorPainters = new ArrayList<>();

	public TrajectoriesChartPanel(List<List<T>> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		super(data.get(0), colorMapping, worldPositionMappingX, worldPositionMappingY);

		this.data = data;

		if (this.data.size() > 1)
			for (int i = 1; i < data.size(); i++) {
				TrajectoryPainter<T> painter = createPainter(data.get(i));
				addChartPainter(painter, true, true);
				remainingTrajectorPainters.add(painter);
			}

		rescaleAxisPositionEncodingFunction();
	}

	private void rescaleAxisPositionEncodingFunction() {
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (List<T> data : this.data)
			if (data != null) {
				for (T t : data) {

					double x = getWorldPositionMappingX().apply(t);
					double y = getWorldPositionMappingY().apply(t);

					minX = Math.min(minX, x);
					maxX = Math.max(maxX, x);

					minY = Math.min(minY, y);
					maxY = Math.max(maxY, y);
				}
			}

//		initializeXAxisPainter(minX, maxX);
		getXPositionEncodingFunction().setMinWorldValue(minX);
		getXPositionEncodingFunction().setMaxWorldValue(maxX);
//		why is that? because the axisPainter does not receive the new world coordinates
		xAxisPainter.setMinValue(minX);
		xAxisPainter.setMaxValue(maxX);

//		initializeYAxisPainter(minY, maxY);
		getYPositionEncodingFunction().setMinWorldValue(minY);
		getYPositionEncodingFunction().setMaxWorldValue(maxY);
//		why is that? because the axisPainter does not receive the new world coordinates
		yAxisPainter.setMinValue(minY);
		yAxisPainter.setMaxValue(maxY);
	}

	@Override
	protected void initializePainter() {
		this.scatterPlotPainter = createPainter(getData());
	}

	protected TrajectoryPainter<T> createPainter(List<T> data) {
		TrajectoryPainter<T> painter = new TrajectoryPainter<T>(data, this.getColorMapping(),
				getWorldPositionMappingX(), getWorldPositionMappingY());

		painter.setSizeEncodingFunction(new SizeEncodingFunction<>(this));

		return painter;
	}

	@Override
	public List<T> getElementsAtPoint(Point p) {
		List<T> elementsAtPoint = super.getElementsAtPoint(p);

		for (ScatterPlotPainter<T> painter : remainingTrajectorPainters)
			elementsAtPoint.addAll(painter.getElementsAtPoint(p));

		return elementsAtPoint;
	}

	@Override
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		List<T> elementsAtPoint = super.getElementsInRectangle(rectangle);

		for (ScatterPlotPainter<T> painter : remainingTrajectorPainters)
			elementsAtPoint.addAll(painter.getElementsInRectangle(rectangle));

		return elementsAtPoint;
	}

	@Override
	public List<T> getElementsInShape(Shape shape) {
		List<T> elementsAtPoint = super.getElementsInShape(shape);

		for (ScatterPlotPainter<T> painter : remainingTrajectorPainters)
			elementsAtPoint.addAll(painter.getElementsInShape(shape));

		return elementsAtPoint;
	}

	@Override
	public void setSizeEncodingFunction(Function<? super T, Double> sizeEncodingFunction) {
		super.setSizeEncodingFunction(sizeEncodingFunction);

		for (ScatterPlotPainter<T> painter : remainingTrajectorPainters)
			painter.setSizeEncodingFunction(sizeEncodingFunction);
	}

	@Override
	public void setSelectedFunction(Function<? super T, Boolean> selectedFunction) {
		super.setSelectedFunction(selectedFunction);

		for (ScatterPlotPainter<T> painter : remainingTrajectorPainters)
			painter.setSelectedFunction(selectedFunction);
	}

	@Override
	public void setToolTipMapping(Function<? super T, String> toolTipMapping) {
		super.setToolTipMapping(toolTipMapping);

		for (ScatterPlotPainter<T> painter : remainingTrajectorPainters)
			painter.setToolTipMapping(toolTipMapping);
	}
}
