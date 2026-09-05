package com.github.TKnudsen.infoVis.view.panels.scatterplot;

import java.awt.Paint;
import java.awt.Point;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRange;
import com.github.TKnudsen.infoVis.view.interaction.IPanning;
import com.github.TKnudsen.infoVis.view.interaction.IZooming;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.XYNumericalChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctions;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingRangeTools;

/**
 * <p>
 * Shared state and behavior for the CPU ({@link ScatterPlot}) and GPU
 * ({@link ScatterPlotIndexedGPU}, {@link ScatterPlotSpriteGPU}) scatterplot
 * panels: the world-position mapping/color-mapping fields every panel needs
 * before it can construct its own painter, {@link #initializeData(List)}
 * (computes the full data range and sets up the axis painters), and
 * {@link #zoom}/{@link #resetZoom}/{@link #pan} -- confirmed byte-identical
 * across all three panels (none of the three reference any GPU-specific
 * state; they operate purely through the inherited {@code xAxisPainter}/
 * {@code yAxisPainter} and {@link #globalRangeX}/{@link #globalRangeY}).
 * </p>
 *
 * <p>
 * Deliberately NOT hoisted here: the {@code scatterPlotPainter} field itself
 * (each subclass's painter has its own concrete type -- {@code
 * ScatterPlotPainter}, {@code ScatterPlotIndexedGPUPainter}, {@code
 * ScatterPlotSpriteGPUPainter} -- with no shared supertype yet), {@code
 * initializePainter(List)} (already an overridable hook {@code
 * TrajectoryChartPanel} depends on -- its contract is untouched here), and
 * {@code setWorldPositionMappings} (the two GPU panels null-check the painter
 * and call {@code repaint()}; {@code ScatterPlot}'s version does neither --
 * a real divergence, left alone rather than silently flattened).
 * </p>
 */
public abstract class AbstractScatterPlotPanel<T> extends XYNumericalChartPanel<Double, Double> {

	private static final long serialVersionUID = 1L;

	// full data range per axis, for clamping zoom/pan (see IZooming/IPanning)
	protected NumericRange globalRangeX;
	protected NumericRange globalRangeY;

	/**
	 * world coordinates/position/values of the x dimension. Was final but can now
	 * be set, to avoid re-initialization overhead.
	 */
	protected Function<? super T, Double> worldPositionMappingX;

	/**
	 * world coordinates/position/values of the y dimension. Was final but can now
	 * be set, to avoid re-initialization overhead.
	 */
	protected Function<? super T, Double> worldPositionMappingY;

	/**
	 * colors of the objects
	 */
	protected final Function<? super T, ? extends Paint> colorMapping;

	protected AbstractScatterPlotPanel(Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		this.colorMapping = colorMapping;
		this.worldPositionMappingX = worldPositionMappingX;
		this.worldPositionMappingY = worldPositionMappingY;
	}

	/**
	 * Computes the x/y world ranges and initializes the axis painters
	 * accordingly. Called from the constructor, after {@code colorMapping},
	 * {@code worldPositionMappingX}, and {@code worldPositionMappingY} have been
	 * assigned but before {@code initializePainter(List)} -- so only those three
	 * fields are safe to read here. A subclass's own fields are not yet
	 * initialized at this point (standard Java construction order: this
	 * constructor's body runs before a subclass constructor's body), so do not
	 * read subclass-declared state from an override of this method.
	 * <p>
	 * If overridden, the override must still call
	 * {@link #initializeXAxisPainter(Double, Double)}/
	 * {@link #initializeYAxisPainter(Double, Double)} (directly or via
	 * {@code super}) with the correct world ranges, since those set up axis
	 * rendering.
	 *
	 * Tolerates a degenerate (single-value) dataset -- e.g. all elements sharing
	 * the same x or y value -- via
	 * {@link PositionEncodingFunctions#computeRangeTolerant(java.util.Collection,
	 * java.util.function.Function, String)} rather than throwing
	 * {@link com.github.TKnudsen.ComplexDataObject.model.tools.DegenerateRangeException
	 * DegenerateRangeException}: {@link #zoom}/{@link #pan} already no-op safely
	 * on a degenerate {@link #globalRangeX}/{@link #globalRangeY} (see
	 * {@code PositionEncodingRangeTools.computeZoomedRange}/{@code
	 * computePannedRange}, which return {@code null} whenever the current or
	 * global interval collapses to zero), and the axis painters constructed from
	 * a {@code [value, value]} range render fine (same tolerant math as
	 * {@link com.github.TKnudsen.infoVis.view.painters.scatterplot.AbstractScatterPlotPainter
	 * AbstractScatterPlotPainter}'s position-encoding setup). Without this, even
	 * the CPU-only {@code ScatterPlot} panel threw here -- before ever reaching
	 * {@code initializePainter(List)} -- for exactly the degenerate dataset the
	 * painter layer was already fixed to tolerate (code review finding #10/#28).
	 *
	 * @param data the data to plot
	 */
	protected void initializeData(List<T> data) {
		NumericRange rangeX = PositionEncodingFunctions.computeRangeTolerant(data, worldPositionMappingX,
				getClass().getSimpleName() + " (x-axis)");
		NumericRange rangeY = PositionEncodingFunctions.computeRangeTolerant(data, worldPositionMappingY,
				getClass().getSimpleName() + " (y-axis)");

		this.globalRangeX = rangeX;
		this.globalRangeY = rangeY;

		initializeXAxisPainter(rangeX.getMin(), rangeX.getMax());
		initializeYAxisPainter(rangeY.getMin(), rangeY.getMax());
	}

	/**
	 * Confirmed byte-identical across all three panels (none needs anything
	 * other than the default numerical axis painter) -- no subclass overrides
	 * these.
	 */
	@Override
	public void initializeXAxisPainter(Double min, Double max) {
		setXAxisPainter(new XAxisNumericalPainter<Double>(min, max));
	}

	@Override
	public void initializeYAxisPainter(Double min, Double max) {
		setYAxisPainter(new YAxisNumericalPainter<Double>(min, max));
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

	/**
	 * @see IZooming#zoom(Point, int, boolean, boolean)
	 */
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

	/**
	 * @see IZooming#resetZoom()
	 */
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
	 *
	 * @see IPanning#pan(int, int)
	 */
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
