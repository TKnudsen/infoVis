package com.github.TKnudsen.infoVis.view.panels.parallelCoordinates;

import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.chartLayouts.YYYAxisChartRectangleLayout;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainters;
import com.github.TKnudsen.infoVis.view.painters.parallelCoordinates.ParallelCoordinatesPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.YYYNumericalChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.SizeEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Chart panel for parallel coordinates / ParallelCoordinatesPainter.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class ParallelCoordinates<T> extends YYYNumericalChartPanel<Double> implements IRectangleSelection<T>,
		IShapeSelection<T>, IClickSelection<T>, ISelectionVisualizer<T>, IColorEncoding<T>, ISizeEncoding<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1954336336194404097L;

	private ParallelCoordinatesPainter<T> parallelCoordinatesPainter;

	private List<T> data;

	/**
	 * world coordinates/position/values of the individual y dimensions
	 */
	private final List<Function<? super T, Double>> worldPositionMappingsY;

	/**
	 * colors of the objects
	 */
	private final Function<? super T, ? extends Paint> colorMapping;

	public ParallelCoordinates(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			List<Function<? super T, Double>> worldPositionMappingsY) {
		super(worldPositionMappingsY.size());

		this.data = Collections.unmodifiableList(data);
		this.colorMapping = colorMapping;

		if (worldPositionMappingsY.isEmpty())
			throw new IllegalArgumentException("ParallelCoordinates: at least one worldPositionMappingsY is needed");
		this.worldPositionMappingsY = worldPositionMappingsY;

		// add spacing to chart layout to achieve a better chart alignment
		YYYAxisChartRectangleLayout layout = (YYYAxisChartRectangleLayout) getChartRectangleLayout();
		layout.setRelativeSpaceBetweenAxes(relativeSpacing(this.worldPositionMappingsY.size()));

		initializeYAxisPainters();

		initializePainter();

		addChartPainter(parallelCoordinatesPainter, true);

		setBackgroundColor(null);
	}

	private static double relativeSpacing(int yAxes) {
		return Math.min(0.4, Math.max(0, -0.0375 * yAxes + 0.375));
	}

	@Override
	protected void initializeYAxisPainters() {
		List<YAxisNumericalPainter<Double>> yAxisPainters = new ArrayList<>();

		for (int i = 0; i < worldPositionMappingsY.size(); i++) {
			YAxisNumericalPainter<Double> yAxis = YAxisNumericalPainters.create(data, worldPositionMappingsY.get(i));
			yAxis.setDrawPhysicalUnit(isDrawAxesNames());
			yAxisPainters.add(yAxis);
		}

		setyAxisPainters(yAxisPainters);
	}

	protected void initializePainter() {
		parallelCoordinatesPainter = new ParallelCoordinatesPainter<>(data, colorMapping, worldPositionMappingsY);

		// will be done a second time when the painter will be added and registered...
		this.parallelCoordinatesPainter.setXPositionEncodingFunction(getXPositionEncodingFunction());
		this.parallelCoordinatesPainter.setYPositionEncodingFunctions(getYPositionEncodingFunctions());

		this.parallelCoordinatesPainter.setSizeEncodingFunction(new SizeEncodingFunction<>(this, 0.66, 1.0));
	}

	@Override
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		return this.parallelCoordinatesPainter.getElementsInRectangle(rectangle);
	}

	@Override
	public List<T> getElementsInShape(Shape shape) {
		return parallelCoordinatesPainter.getElementsInShape(shape);
	}

	@Override
	public List<T> getElementsAtPoint(Point p) {
		return this.parallelCoordinatesPainter.getElementsAtPoint(p);
	}

	@Override
	public void setSelectedFunction(Function<? super T, Boolean> selectedFunction) {
		this.parallelCoordinatesPainter.setSelectedFunction(selectedFunction);
	}

	@Override
	public void setSizeEncodingFunction(Function<? super T, Double> sizeEncodingFunction) {
		this.parallelCoordinatesPainter.setSizeEncodingFunction(sizeEncodingFunction);
	}

//	public Function<? super T, String> getToolTipMapping() {
//		return parallelCoordinatesPainter.getToolTipMapping();
//	}
//
//	public void setToolTipMapping(Function<? super T, String> toolTipMapping) {
//		parallelCoordinatesPainter.setToolTipMapping(toolTipMapping);
//	}

	/**
	 * use for inheriting classes only
	 * 
	 * @return data
	 */
	protected List<T> getData() {
		return data;
	}

	/**
	 * use for inheriting classes only
	 * 
	 * @return function
	 */
	protected Function<? super T, ? extends Paint> getColorMapping() {
		return colorMapping;
	}

	@Override
	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction) {
		this.parallelCoordinatesPainter.setColorEncodingFunction(colorEncodingFunction);
	}

	public Paint getSelectionPaint() {
		return this.parallelCoordinatesPainter.getSelectionPaint();
	}

	public void setSelectionPaint(Paint selectionPaint) {
		this.parallelCoordinatesPainter.setSelectionPaint(selectionPaint);
	}

}