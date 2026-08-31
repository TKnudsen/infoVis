package com.github.TKnudsen.infoVis.view.panels.scatterplot;

import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.RectangularShape;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IPanning;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.IZooming;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainters;
import com.github.TKnudsen.infoVis.view.visualChannels.IOverplottingMitigation;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.SizeEncodingFunction;

/**
 * <p>
 * Chart panel for scatterplots.
 * </p>
 *
 * @version 2.07
 * @since 2018
 */
public class ScatterPlot<T> extends AbstractScatterPlotPanel<T> implements IRectangleSelection<T>,
		IShapeSelection<T>, IClickSelection<T>, ISelectionVisualizer<T>, IColorEncoding<T>, ISizeEncoding<T>,
		IOverplottingMitigation, IZooming, IPanning {

	/**
	 *
	 */
	private static final long serialVersionUID = 2949962927634263599L;

	protected ScatterPlotPainter<T> scatterPlotPainter;

	public ScatterPlot(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		super(colorMapping, worldPositionMappingX, worldPositionMappingY);

		initializeData(data);

		initializePainter(data);

		addChartPainter(scatterPlotPainter, true, true);

		setBackground(null);
	}

	/**
	 * Constructs {@link #scatterPlotPainter}. Called from the constructor, after
	 * {@code colorMapping}, {@code worldPositionMappingX}, and
	 * {@code worldPositionMappingY} have been assigned -- so only those three
	 * fields (via their protected getters) are safe to read here; a subclass's
	 * own fields are not yet initialized at this point (see
	 * {@link #initializeData(List)} for why).
	 * <p>
	 * A subclass overriding this method to install its own painter subtype (e.g.
	 * {@code TrajectoryChartPanel}) must assign {@link #scatterPlotPainter}
	 * itself and should <b>not</b> call {@code super.initializePainter(data)} --
	 * doing so would construct a default {@link ScatterPlotPainter} that is
	 * immediately discarded when this method's own assignment overwrites it.
	 *
	 * @param data the data to plot
	 */
	protected void initializePainter(List<T> data) {
		this.scatterPlotPainter = new ScatterPlotPainter<T>(data, colorMapping, worldPositionMappingX,
				worldPositionMappingY);

		this.scatterPlotPainter.setSizeEncodingFunction(new SizeEncodingFunction<>(this));
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
	 * @return data
	 */
	protected List<T> getData() {
		return ScatterPlotPainters.getData(scatterPlotPainter);
	}

	public void setWorldPositionMappings(Function<? super T, Double> worldPositionMappingX,
			Function<? super T, Double> worldPositionMappingY) {

		this.worldPositionMappingX = worldPositionMappingX;
		this.worldPositionMappingY = worldPositionMappingY;

		// the painter computes each point's screen position from its own copy of
		// these mappings, not from the panel's -- without pushing the update here,
		// the axis would rescale to the new mapping's range while every plotted
		// point kept using the old (now stale) mapping.
		this.scatterPlotPainter.setWorldPositionMappingX(worldPositionMappingX);
		this.scatterPlotPainter.setWorldPositionMappingY(worldPositionMappingY);

		initializeData(ScatterPlotPainters.getData(scatterPlotPainter));

		this.scatterPlotPainter.setXPositionEncodingFunction(getXPositionEncodingFunction());
		this.scatterPlotPainter.setYPositionEncodingFunction(getYPositionEncodingFunction());
	}

	@Override
	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction) {
		this.scatterPlotPainter.setColorEncodingFunction(colorEncodingFunction);
	}

	@Override
	public boolean isAlphaAdjustment() {
		return scatterPlotPainter.isAlphaAdjustment();
	}

	@Override
	public void setAlphaAdjustment(boolean dynamicAlphaAdjustment) {
		this.scatterPlotPainter.setAlphaAdjustment(dynamicAlphaAdjustment);
	}

	public Paint getSelectionPaint() {
		return this.scatterPlotPainter.getSelectionPaint();
	}

	public void setSelectionPaint(Paint selectionPaint) {
		this.scatterPlotPainter.setSelectionPaint(selectionPaint);
	}

	public int getToolTipWidth() {
		return this.scatterPlotPainter.getToolTipWidth();
	}

	public void setToolTipWidth(int toolTipWidth) {
		this.scatterPlotPainter.setToolTipWidth(toolTipWidth);
	}

	public int getToolTipHeight() {
		return this.scatterPlotPainter.getToolTipHeight();
	}

	public void setToolTipHeight(int toolTipHeight) {
		this.scatterPlotPainter.setToolTipHeight(toolTipHeight);
	}

}