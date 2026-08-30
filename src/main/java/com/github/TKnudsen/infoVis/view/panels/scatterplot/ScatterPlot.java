package com.github.TKnudsen.infoVis.view.panels.scatterplot;

import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.RectangularShape;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRange;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IPanning;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.IZooming;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainters;
import com.github.TKnudsen.infoVis.view.panels.axis.XYNumericalChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.IOverplottingMitigation;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctions;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingRangeTools;
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
public class ScatterPlot<T> extends XYNumericalChartPanel<Double, Double> implements IRectangleSelection<T>,
		IShapeSelection<T>, IClickSelection<T>, ISelectionVisualizer<T>, IColorEncoding<T>, ISizeEncoding<T>,
		IOverplottingMitigation, IZooming, IPanning {

	/**
	 *
	 */
	private static final long serialVersionUID = 2949962927634263599L;

	protected ScatterPlotPainter<T> scatterPlotPainter;

	// full data range per axis, for clamping zoom/pan (see IZooming/IPanning)
	private NumericRange globalRangeX;
	private NumericRange globalRangeY;

	// decision: no need to store data also here, plus in the painter
	// private List<T> data;

	/**
	 * world coordinates/position/values of the x dimension. Was final but can now
	 * be set, to avoid re-initialization overhead.
	 */
	private Function<? super T, Double> worldPositionMappingX;

	/**
	 * world coordinates/position/values of the y dimension. Was final but can now
	 * be set, to avoid re-initialization overhead.
	 */
	private Function<? super T, Double> worldPositionMappingY;

	/**
	 * colors of the objects
	 */
	private final Function<? super T, ? extends Paint> colorMapping;

	public ScatterPlot(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		// this.data = Collections.unmodifiableList(data);
		this.colorMapping = colorMapping;
		this.worldPositionMappingX = worldPositionMappingX;
		this.worldPositionMappingY = worldPositionMappingY;

		initializeData(data);

		initializePainter(data);

		addChartPainter(scatterPlotPainter, true, true);

		setBackground(null);
	}

	/**
	 * Computes the x/y world ranges and initializes the axis painters
	 * accordingly. Called from the constructor, after {@code colorMapping},
	 * {@code worldPositionMappingX}, and {@code worldPositionMappingY} have been
	 * assigned but before {@link #initializePainter(List)} -- so only those three
	 * fields (via their protected getters) are safe to read here. A subclass's
	 * own fields are not yet initialized at this point (standard Java
	 * construction order: this constructor's body runs before a subclass
	 * constructor's body), so do not read subclass-declared state from an
	 * override of this method.
	 * <p>
	 * If overridden, the override must still call
	 * {@link #initializeXAxisPainter(Double, Double)}/
	 * {@link #initializeYAxisPainter(Double, Double)} (directly or via
	 * {@code super}) with the correct world ranges, since those set up axis
	 * rendering.
	 *
	 * @param data the data to plot
	 */
	protected void initializeData(List<T> data) {
		NumericRange rangeX = PositionEncodingFunctions.computeRange(data, worldPositionMappingX,
				getClass().getSimpleName() + " (x-axis)");
		NumericRange rangeY = PositionEncodingFunctions.computeRange(data, worldPositionMappingY,
				getClass().getSimpleName() + " (y-axis)");

		this.globalRangeX = rangeX;
		this.globalRangeY = rangeY;

		initializeXAxisPainter(rangeX.getMin(), rangeX.getMax());

		initializeYAxisPainter(rangeY.getMin(), rangeY.getMax());
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
	 * @return data
	 */
	protected List<T> getData() {
		return ScatterPlotPainters.getData(scatterPlotPainter);
	}

	/**
	 * use for inheriting classes only
	 * 
	 * @return function
	 */
	protected Function<? super T, ? extends Paint> getColorMapping() {
		return colorMapping;
	}

	/**
	 * use for inheriting classes only
	 * 
	 * @return function
	 */
	protected Function<? super T, Double> getWorldPositionMappingX() {
		return worldPositionMappingX;
	}

	/**
	 * use for inheriting classes only
	 * 
	 * @return function
	 */
	protected Function<? super T, Double> getWorldPositionMappingY() {
		return worldPositionMappingY;
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

	@Override
	public void zoom(Point location, int zoomCount, boolean zoomX, boolean zoomY) {
		if (zoomX) {
			double worldX = getXPositionEncodingFunction().inverseMapping(location.getX()).doubleValue();
			double[] newRangeX = PositionEncodingRangeTools.computeZoomedRange(xAxisPainter.getMinValue(),
					xAxisPainter.getMaxValue(), globalRangeX.getMin(), globalRangeX.getMax(), worldX, zoomCount);
			if (newRangeX != null) {
				xAxisPainter.setMinValue(newRangeX[0]);
				xAxisPainter.setMaxValue(newRangeX[1]);
			}
		}

		if (zoomY) {
			double worldY = getYPositionEncodingFunction().inverseMapping(location.getY()).doubleValue();
			double[] newRangeY = PositionEncodingRangeTools.computeZoomedRange(yAxisPainter.getMinValue(),
					yAxisPainter.getMaxValue(), globalRangeY.getMin(), globalRangeY.getMax(), worldY, zoomCount);
			if (newRangeY != null) {
				yAxisPainter.setMinValue(newRangeY[0]);
				yAxisPainter.setMaxValue(newRangeY[1]);
			}
		}

		updateBounds();
	}

	@Override
	public void resetZoom() {
		xAxisPainter.setMinValue(globalRangeX.getMin());
		xAxisPainter.setMaxValue(globalRangeX.getMax());
		yAxisPainter.setMinValue(globalRangeY.getMin());
		yAxisPainter.setMaxValue(globalRangeY.getMax());

		updateBounds();
	}

	/**
	 * Not wired by default on this panel: {@link ScatterPlots#addPanInteraction}
	 * drives it via the left mouse button, same as rectangle selection, so the
	 * two would fight over the same drag if both were attached. The capability
	 * still works if a caller wires it deliberately (e.g. with selection
	 * disabled, or on a different mouse button).
	 */
	@Override
	public void pan(int deltaX, int deltaY) {
		if (deltaX != 0) {
			double fromPixel = getXPositionEncodingFunction().getMinPixel().doubleValue();
			double worldDeltaX = getXPositionEncodingFunction().inverseMapping(fromPixel + deltaX).doubleValue()
					- getXPositionEncodingFunction().inverseMapping(fromPixel).doubleValue();

			double[] newRangeX = PositionEncodingRangeTools.computePannedRange(xAxisPainter.getMinValue(),
					xAxisPainter.getMaxValue(), globalRangeX.getMin(), globalRangeX.getMax(), worldDeltaX);
			if (newRangeX != null) {
				xAxisPainter.setMinValue(newRangeX[0]);
				xAxisPainter.setMaxValue(newRangeX[1]);
			}
		}

		if (deltaY != 0) {
			double fromPixel = getYPositionEncodingFunction().getMinPixel().doubleValue();
			double worldDeltaY = getYPositionEncodingFunction().inverseMapping(fromPixel + deltaY).doubleValue()
					- getYPositionEncodingFunction().inverseMapping(fromPixel).doubleValue();

			double[] newRangeY = PositionEncodingRangeTools.computePannedRange(yAxisPainter.getMinValue(),
					yAxisPainter.getMaxValue(), globalRangeY.getMin(), globalRangeY.getMax(), worldDeltaY);
			if (newRangeY != null) {
				yAxisPainter.setMinValue(newRangeY[0]);
				yAxisPainter.setMaxValue(newRangeY[1]);
			}
		}

		updateBounds();
	}
}