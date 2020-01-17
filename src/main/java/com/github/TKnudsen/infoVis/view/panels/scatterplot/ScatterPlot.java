package com.github.TKnudsen.infoVis.view.panels.scatterplot;

import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.RectangularShape;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.XYNumericalChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.SizeEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Chart panel for scatterplots.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.06
 */
public class ScatterPlot<T> extends XYNumericalChartPanel<Double, Double> implements IRectangleSelection<T>,
		IShapeSelection<T>, IClickSelection<T>, ISelectionVisualizer<T>, IColorEncoding<T>, ISizeEncoding<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2949962927634263599L;

	protected ScatterPlotPainter<T> scatterPlotPainter;

	private List<T> data;

	/**
	 * world coordinates/position/values of the x dimension
	 */
	private final Function<? super T, Double> worldPositionMappingX;

	/**
	 * world coordinates/position/values of the y dimension
	 */
	private final Function<? super T, Double> worldPositionMappingY;

	/**
	 * colors of the objects
	 */
	private final Function<? super T, ? extends Paint> colorMapping;

	public ScatterPlot(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		this.data = Collections.unmodifiableList(data);
		this.colorMapping = colorMapping;
		this.worldPositionMappingX = worldPositionMappingX;
		this.worldPositionMappingY = worldPositionMappingY;

		initializeData();

		initializePainter();

		addChartPainter(scatterPlotPainter, true, true);

		setBackgroundColor(null);
	}

	protected void initializeData() {
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		if (data != null) {
			for (T t : data) {

				double x = worldPositionMappingX.apply(t);
				double y = worldPositionMappingY.apply(t);

				minX = Math.min(minX, x);
				maxX = Math.max(maxX, x);

				minY = Math.min(minY, y);
				maxY = Math.max(maxY, y);
			}
		}

		initializeXAxisPainter(minX, maxX);

		initializeYAxisPainter(minY, maxY);
	}

	protected void initializePainter() {
		this.scatterPlotPainter = new ScatterPlotPainter<T>(data, colorMapping, worldPositionMappingX,
				worldPositionMappingY);

		this.scatterPlotPainter.setSizeEncodingFunction(new SizeEncodingFunction<>(this));
	}

	@Override
	public void initializeXAxisPainter(Double min, Double max) {
		setXAxisPainter(new XAxisNumericalPainter<Double>(min, max));
	}

	@Override
	public void initializeYAxisPainter(Double min, Double max) {
		setYAxisPainter(new YAxisNumericalPainter<Double>(min, max));
	}

	@Override
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		return this.scatterPlotPainter.getElementsInRectangle(rectangle);
	}

	@Override
	public List<T> getElementsInShape(Shape shape) {
		return scatterPlotPainter.getElementsInShape(shape);
	}

	@Override
	public List<T> getElementsAtPoint(Point p) {
		return this.scatterPlotPainter.getElementsAtPoint(p);
	}

	@Override
	public void setSelectedFunction(Function<? super T, Boolean> selectedFunction) {
		this.scatterPlotPainter.setSelectedFunction(selectedFunction);
	}

	@Override
	public void setSizeEncodingFunction(Function<? super T, Double> sizeEncodingFunction) {
		this.scatterPlotPainter.setSizeEncodingFunction(sizeEncodingFunction);
	}

	public Function<? super T, String> getToolTipMapping() {
		return scatterPlotPainter.getToolTipMapping();
	}

	public void setToolTipMapping(Function<? super T, String> toolTipMapping) {
		scatterPlotPainter.setToolTipMapping(toolTipMapping);
	}

	/**
	 * use for inheriting classes only
	 * 
	 * @return
	 */
	protected List<T> getData() {
		return data;
	}

	/**
	 * use for inheriting classes only
	 * 
	 * @return
	 */
	protected Function<? super T, ? extends Paint> getColorMapping() {
		return colorMapping;
	}

	/**
	 * use for inheriting classes only
	 * 
	 * @return
	 */
	protected Function<? super T, Double> getWorldPositionMappingX() {
		return worldPositionMappingX;
	}

	/**
	 * use for inheriting classes only
	 * 
	 * @return
	 */
	protected Function<? super T, Double> getWorldPositionMappingY() {
		return worldPositionMappingY;
	}

	@Override
	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction) {
		this.scatterPlotPainter.setColorEncodingFunction(colorEncodingFunction);
	}

}