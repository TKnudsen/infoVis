package com.github.TKnudsen.infoVis.view.painters.scatterplot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.BasicStrokeTools;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.tools.OverplottingMitigationTools;
import com.github.TKnudsen.infoVis.view.tools.VisualMappingTools;
import com.github.TKnudsen.infoVis.view.visualChannels.IOverplottingMitigation;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctions;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.ConstantSizeEncodingFunction;

/**
 * <p>
 * Shared CPU-rendering surface for {@link ScatterPlotPainter} and (from
 * {@link AbstractGPUScatterPlotPainter} onward) the GPU-based scatterplot
 * painters: the position/color/size mapping fields, the position-encoding
 * setup (including the degenerate-range tolerance), {@code refreshDataPoints},
 * the two-pass CPU render loop ({@link #drawCPU(Graphics2D)}), and the full
 * getter/setter surface -- all confirmed byte-identical across
 * {@code ScatterPlotPainter}, {@code ScatterPlotIndexedGPUPainter}, and
 * {@code ScatterPlotSpriteGPUPainter} by direct comparison ahead of this
 * extraction (code review finding #27).
 * </p>
 *
 * <p>
 * {@link #screenPoints} is deliberately kept {@code protected} rather than
 * made private (unlike most other fields here): {@link LabeledScatterplotPainter}
 * reads it directly in its own {@code setRectangle} override, so narrowing its
 * visibility would break that subclass.
 * </p>
 *
 * <p>
 * {@link #drawPoint(Graphics2D, Point2D, float, Paint, boolean)} is
 * deliberately kept {@code protected} and non-final: {@code TrajectoryPainter}
 * overrides it to draw connecting line segments between points.
 * </p>
 *
 * <p>
 * {@link ITooltip#getTooltip(Point)} is deliberately NOT implemented here --
 * unlike everything else in this class, it is expected to diverge once
 * {@link AbstractGPUScatterPlotPainter} is introduced (a GPU painter needs a
 * CPU/GPU dispatch that a CPU-only painter does not), so each concrete leaf
 * painter provides its own.
 * </p>
 */
public abstract class AbstractScatterPlotPainter<T> extends ChartPainter
		implements IXPositionEncoding, IYPositionEncoding, ISizeEncoding<T>, IColorEncoding<T>, IRectangleSelection<T>,
		IShapeSelection<T>, IClickSelection<T>, ISelectionVisualizer<T>, ITooltip, IOverplottingMitigation {

	// input data
	final List<T> data;

	// screen coordinates of input data
	protected final List<Point2D> screenPoints;
	// package-visible (not private): ScatterPlotPainter.getTooltip() reads this
	// directly, matching the field's pre-extraction visibility characteristics
	protected final ReadWriteLock screenPointsLock = new ReentrantReadWriteLock();

	// overplotting mitigation
	protected boolean overplottingMitigation = false;
	protected float alpha = 1.0f;

	// settable size of dots
	private double pointSize = Double.NaN;

	private boolean tooltipping = true;
	private int toolTipWidth = 150;
	private int toolTipHeight = 30;

	// position mapping of data
	// package-visible (not private): AbstractGPUScatterPlotPainter's CPU-mode
	// selection hit-testing reads these directly, matching their pre-extraction
	// visibility characteristics
	protected IPositionEncodingFunction xPositionEncodingFunction;
	protected IPositionEncodingFunction yPositionEncodingFunction;
	protected boolean externalXPositionEncodingFunction = false;
	protected boolean externalYPositionEncodingFunction = false;

	// listening to yPositionEncodingFunctions
	private final PositionEncodingFunctionListener myPositionEncodingFunctionListener = this::refreshDataPoints;

	// color coding of data
	private Function<? super T, ? extends Paint> colorMapping;

	// maps a T to individual double values which can be mapped to x and y position
	private Function<? super T, Double> worldPositionMappingX;
	private Function<? super T, Double> worldPositionMappingY;

	// package-visible (not private): AbstractGPUScatterPlotPainter's concrete
	// subclasses' addPointToGPU read this directly, matching its pre-extraction
	// visibility characteristics
	protected Function<? super T, Double> sizeEncodingFunction = new ConstantSizeEncodingFunction<>(3);

	// package-visible (not private): AbstractGPUScatterPlotPainter.isSelected()
	// reads this directly, matching its pre-extraction visibility characteristics
	protected Function<? super T, Boolean> selectedFunction;
	private boolean drawSelectedLast = true;
	private Paint selectionPaint = Color.BLACK;

	private Function<? super T, String> toolTipMapping;

	// package-visible (not private): ScatterPlotPainter.getTooltip() reads this
	// directly, matching the field's pre-extraction visibility characteristics
	protected boolean refreshingDataPoints;

	/**
	 * @param data                  the data elements to plot
	 * @param colorMapping          maps each element to its point color; a null
	 *                              result falls back to the painter's own paint
	 * @param worldPositionMappingX maps each element to its x value in data (world)
	 *                              space
	 * @param worldPositionMappingY maps each element to its y value in data (world)
	 *                              space
	 */
	protected AbstractScatterPlotPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		this.colorMapping = colorMapping;
		this.worldPositionMappingX = worldPositionMappingX;
		this.worldPositionMappingY = worldPositionMappingY;

		this.data = Collections.unmodifiableList(VisualMappingTools.sanityCheckFilter(data, worldPositionMappingX, true));

		this.screenPoints = new ArrayList<Point2D>(data.size());

		initializePositionEncodingFunctions();

		refreshDataPoints();
	}

	/**
	 * Builds the default x/y position encoding functions from the world position
	 * mappings and registers this painter to refresh its screen points whenever
	 * either one changes.
	 */
	private void initializePositionEncodingFunctions() {
		this.xPositionEncodingFunction = createPositionEncodingFunctionTolerant(worldPositionMappingX, 0d, 1d, false,
				getClass().getSimpleName() + " (x-axis)");
		this.xPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.yPositionEncodingFunction = createPositionEncodingFunctionTolerant(worldPositionMappingY, 0d, 1d, true,
				getClass().getSimpleName() + " (y-axis)");
		this.yPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
	}

	/**
	 * Thin wrapper around {@link PositionEncodingFunctions#createPositionEncodingFunctionTolerant},
	 * fixing {@code data} to this painter's own field.
	 */
	private PositionEncodingFunction createPositionEncodingFunctionTolerant(Function<? super T, Double> mapping,
			Double minPixel, Double maxPixel, boolean flipAxisValues, String context) {
		return PositionEncodingFunctions.createPositionEncodingFunctionTolerant(data, mapping, minPixel, maxPixel,
				flipAxisValues, context);
	}

	/**
	 * Recomputes {@link #screenPoints} from the current data and position encoding
	 * functions under the write lock, and updates the overplotting-mitigation alpha
	 * if enabled. Safe to call while {@link #draw(Graphics2D)} is concurrently
	 * reading a prior snapshot.
	 */
	protected void refreshDataPoints() {
		screenPointsLock.writeLock().lock();

		try {
			refreshingDataPoints = true;
			screenPoints.clear();

			if (data == null || chartRectangle == null)
				return;

			for (int i = 0; i < data.size(); i++) {

				T t = data.get(i);
				double worldX = worldPositionMappingX.apply(t).doubleValue();
				double worldY = worldPositionMappingY.apply(t).doubleValue();
				double x = xPositionEncodingFunction.apply(worldX);
				double y = yPositionEncodingFunction.apply(worldY);

				screenPoints.add(new Point2D.Double(x, y));
			}

			if (overplottingMitigation)
				alpha = OverplottingMitigationTools.computeAlpha(screenPoints.size());

		} finally {
			refreshingDataPoints = false;
			screenPointsLock.writeLock().unlock();
		}
	}

	/**
	 * Draws all points in two passes when a selection exists and
	 * {@link #drawSelectedLast} is set: unselected points first, then selected
	 * points on top, so a selection is never visually obscured by unselected points
	 * drawn after it.
	 */
	@Override
	public void draw(Graphics2D g2) {
		drawCPU(g2);
	}

	/**
	 * The CPU two-pass render loop, extracted so a GPU-based subclass can call it
	 * directly for its own CPU-fallback rendering path without duplicating it.
	 */
	protected final void drawCPU(Graphics2D g2) {
		if (chartRectangle == null || data == null || data.isEmpty())
			return;

		screenPointsLock.readLock().lock();
		try {

			final int n = data.size();
			final List<Point2D> points = screenPoints;

			// if screenPoints are currently being rebuilt, just skip this frame.
			if (points == null || points.size() != n)
				return;

			final Color oldColor = g2.getColor();

			// get point size once
			double ps = this.pointSize;
			if (Double.isNaN(ps))
				ps = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());

			// cache flags
			final boolean hasSelection = (selectedFunction != null);
			final boolean twoPhase = drawSelectedLast && hasSelection;

			// collect selected indices for second pass
			List<Integer> selectedIndices = twoPhase ? new ArrayList<>(Math.min(128, n / 10)) : Collections.emptyList();

			// ---- FIRST PASS: draw non-selected ----
			for (int i = 0; i < n; i++) {
				final Point2D p = points.get(i);
				if (p == null || Double.isNaN(p.getX()) || Double.isNaN(p.getY()))
					continue;

				boolean selected = false;
				if (hasSelection) {
					Boolean b = selectedFunction.apply(data.get(i));
					selected = (b != null && b.booleanValue());
				}

				if (twoPhase && selected) {
					selectedIndices.add(i);
					continue;
				}

				Paint paint = colorMapping != null ? colorMapping.apply(data.get(i)) : null;
				if (paint == null)
					paint = ColorTools.setAlpha(getPaint(), alpha);

				double size = ps;
				double sEnc = sizeEncodingFunction != null ? sizeEncodingFunction.apply(data.get(i)) : Double.NaN;
				if (!Double.isNaN(sEnc))
					size = sEnc;

				drawPoint(g2, p, (float) size, paint, selected);
			}

			// ---- SECOND PASS: draw selected last ----
			if (twoPhase && !selectedIndices.isEmpty()) {
				for (int idx : selectedIndices) {
					final Point2D p = points.get(idx);
					if (p == null || Double.isNaN(p.getX()) || Double.isNaN(p.getY()))
						continue;

					Paint paint = colorMapping != null ? colorMapping.apply(data.get(idx)) : null;
					if (paint == null)
						paint = ColorTools.setAlpha(getPaint(), alpha);

					double size = ps;
					double sEnc = sizeEncodingFunction != null ? sizeEncodingFunction.apply(data.get(idx)) : Double.NaN;
					if (!Double.isNaN(sEnc))
						size = sEnc;

					drawPoint(g2, p, (float) size, paint, true);
				}
			}

			g2.setColor(oldColor);

		} finally {
			screenPointsLock.readLock().unlock();
		}
	}

	/**
	 * Draws a single point at {@code point}; if {@code selected}, first draws a
	 * larger point in {@link #selectionPaint} behind it as a selection outline.
	 */
	protected void drawPoint(Graphics2D g2, Point2D point, float pointSize, Paint pointPaint, boolean selected) {

		float size = pointSize * 1.33f;
		if (selected) {
			double pointSizeBig = Math.max(pointSize * 1.66f, pointSize + 2);
			g2.setPaint(selectionPaint);
			DisplayTools.drawPoint(g2, point.getX(), point.getY(), pointSizeBig, true);
			g2.setPaint(pointPaint);
			DisplayTools.drawPoint(g2, point.getX(), point.getY(), size, true);
		} else {
			g2.setPaint(pointPaint);
			DisplayTools.drawPoint(g2, point.getX(), point.getY(), size, true);
		}
	}

	/**
	 * @return the default point radius used whenever {@link #pointSize} has not
	 *         been set explicitly: 0.6% of the shorter of
	 *         {@code viewWidth}/{@code viewHeight}, at least 3 pixels
	 */
	public static double calculatePointSize(double viewWidth, double viewHeight) {
		return Math.max(3, Math.min(viewWidth, viewHeight) * 0.006);
	}

	/**
	 * Derives the outline stroke width from the new rectangle size, updates the
	 * internal x/y position encoding functions' pixel range (unless an external one
	 * was supplied via
	 * {@link #setXPositionEncodingFunction(IPositionEncodingFunction)} /
	 * {@link #setYPositionEncodingFunction(IPositionEncodingFunction)}), and
	 * refreshes the screen points.
	 */
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		if (chartRectangle == null)
			return;

		// stroke width...
		double size = Math.floor(Math.min(this.chartRectangle.getWidth(), this.chartRectangle.getHeight()) / 150);
		if (size < 0)
			size = 0;
		if (size % 2 == 1)
			size -= 1;
		size += 1;
		this.stroke = BasicStrokeTools.get((float) size);

		if (!externalXPositionEncodingFunction)
			updateXPositionEncoding(rectangle);
		if (!externalYPositionEncodingFunction)
			updateYPositionEncoding(rectangle);

		refreshDataPoints();
		onRectangleUpdated();
	}

	/**
	 * No-op hook called at the end of {@link #setRectangle(Rectangle2D)}.
	 * {@link ScatterPlotIndexedGPUPainter} overrides this to proactively push the
	 * new rectangle into its GPU projection matrix when GPU-active;
	 * {@link ScatterPlotSpriteGPUPainter} does not (it recomputes its own
	 * projection unconditionally on every {@code displayGL} call instead), so it
	 * keeps this default no-op -- a real, pre-existing asymmetry between the two,
	 * not something to force into false symmetry.
	 */
	protected void onRectangleUpdated() {
	}

	/**
	 * Updates the x position encoding function's pixel range to match
	 * {@code rectangle}.
	 */
	private final void updateXPositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		this.xPositionEncodingFunction.setMinPixel(rectangle.getMinX());
		this.xPositionEncodingFunction.setMaxPixel(rectangle.getMaxX());
	}

	/**
	 * Updates the y position encoding function's pixel range to match
	 * {@code rectangle}.
	 */
	private final void updateYPositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		this.yPositionEncodingFunction.setMinPixel(rectangle.getMinY());
		this.yPositionEncodingFunction.setMaxPixel(rectangle.getMaxY());
	}

	/**
	 * @return whether overplotting mitigation (data-density-based point alpha) is
	 *         enabled
	 */
	@Override
	public boolean isAlphaAdjustment() {
		return overplottingMitigation;
	}

	/**
	 * Enables/disables overplotting mitigation and refreshes the screen points so
	 * the change takes effect immediately.
	 */
	@Override
	public void setAlphaAdjustment(boolean dynamicAlphaAdjustment) {
		this.overplottingMitigation = dynamicAlphaAdjustment;

		refreshDataPoints();
	}

	/**
	 * @return the explicitly set point radius, or {@link Double#NaN} if none was
	 *         set (in which case {@link #calculatePointSize} is used)
	 */
	public double getPointSize() {
		return pointSize;
	}

	/**
	 * @param pointSize fixed point radius to use instead of the value computed by
	 *                  {@link #calculatePointSize}; pass {@link Double#NaN} to
	 *                  clear it
	 */
	public void setPointSize(double pointSize) {
		this.pointSize = pointSize;
	}

	/** @return the current per-element color mapping function */
	public Function<? super T, ? extends Paint> getColorMapping() {
		return colorMapping;
	}

	/** @return the current per-element x (world) value mapping function */
	public Function<? super T, Double> getWorldPositionMappingX() {
		return worldPositionMappingX;
	}

	/**
	 * @param worldPositionMappingX new per-element x (world) value mapping
	 *                              function; screen points are recomputed
	 *                              immediately
	 */
	public void setWorldPositionMappingX(Function<? super T, Double> worldPositionMappingX) {
		this.worldPositionMappingX = worldPositionMappingX;

		refreshDataPoints();
	}

	/** @return the current per-element y (world) value mapping function */
	public Function<? super T, Double> getWorldPositionMappingY() {
		return worldPositionMappingY;
	}

	/**
	 * @param worldPositionMappingY new per-element y (world) value mapping
	 *                              function; screen points are recomputed
	 *                              immediately
	 */
	public void setWorldPositionMappingY(Function<? super T, Double> worldPositionMappingY) {
		this.worldPositionMappingY = worldPositionMappingY;

		refreshDataPoints();
	}

	/** @return whether this painter responds to tooltip requests */
	@Override
	public boolean isToolTipping() {
		return tooltipping;
	}

	/**
	 * @param tooltipping whether this painter should respond to tooltip requests
	 */
	@Override
	public void setToolTipping(boolean tooltipping) {
		this.tooltipping = tooltipping;
	}

	/** Delegates to {@link #getElementsInShape(Shape)}. */
	@Override
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		return getElementsInShape(rectangle);
	}

	/**
	 * @return the data elements whose current screen position falls inside
	 *         {@code shape}
	 */
	@Override
	public List<T> getElementsInShape(Shape shape) {
		if (shape == null)
			return null;

		if (data == null)
			return null;

		List<T> elements = new ArrayList<>();
		for (T d : data) {
			double worldX = worldPositionMappingX.apply(d).doubleValue();
			double worldY = worldPositionMappingY.apply(d).doubleValue();

			double screenX = xPositionEncodingFunction.apply(worldX);
			double screenY = yPositionEncodingFunction.apply(worldY);
			if (shape.contains(screenX, screenY))
				elements.add(d);
		}

		return elements;
	}

	/**
	 * @return the data elements whose current screen position falls inside a circle
	 *         of radius {@link #getPointSize()} (or {@link #calculatePointSize} if
	 *         unset) centered on {@code p}
	 */
	@Override
	public List<T> getElementsAtPoint(Point p) {
		if (p == null)
			return null;

		final Rectangle2D cr = chartRectangle;
		if (cr == null)
			return null;

		double radius = this.pointSize;
		if (Double.isNaN(pointSize))
			radius = calculatePointSize(cr.getWidth(), cr.getHeight());

		Ellipse2D circle = new Ellipse2D.Double();
		circle.setFrameFromCenter(p.getX(), p.getY(), p.getX() + radius, p.getY() + radius);

		if (data == null)
			return null;

		List<T> elements = new ArrayList<>();
		for (T d : data) {
			double worldX = worldPositionMappingX.apply(d);
			double worldY = worldPositionMappingY.apply(d);

			double screenX = xPositionEncodingFunction.apply(worldX);
			double screenY = yPositionEncodingFunction.apply(worldY);
			if (circle.contains(screenX, screenY))
				elements.add(d);
		}

		return elements;
	}

	/**
	 * @param sizeEncodingFunction maps each element to its point size; a
	 *                             {@link Double#NaN} result falls back to the
	 *                             default point size
	 */
	@Override
	public void setSizeEncodingFunction(Function<? super T, Double> sizeEncodingFunction) {
		this.sizeEncodingFunction = sizeEncodingFunction;
	}

	/**
	 * @param selectedFunction reports whether a given element is currently selected
	 */
	@Override
	public void setSelectedFunction(Function<? super T, Boolean> selectedFunction) {
		this.selectedFunction = selectedFunction;
	}

	/** @param colorEncodingFunction maps each element to its point color */
	@Override
	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction) {
		this.colorMapping = colorEncodingFunction;
	}

	/**
	 * Replaces the x position encoding function with an externally supplied one and
	 * marks it as external, so {@link #setRectangle(Rectangle2D)} no longer
	 * overwrites its pixel range automatically.
	 */
	@Override
	public void setXPositionEncodingFunction(IPositionEncodingFunction xPositionEncodingFunction) {
		this.xPositionEncodingFunction.removePositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.xPositionEncodingFunction = xPositionEncodingFunction;
		this.xPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.externalXPositionEncodingFunction = true;
	}

	/**
	 * Replaces the y position encoding function with an externally supplied one and
	 * marks it as external, so {@link #setRectangle(Rectangle2D)} no longer
	 * overwrites its pixel range automatically.
	 */
	@Override
	public void setYPositionEncodingFunction(IPositionEncodingFunction yPositionEncodingFunction) {
		this.yPositionEncodingFunction.removePositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.yPositionEncodingFunction = yPositionEncodingFunction;
		this.yPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.externalYPositionEncodingFunction = true;
	}

	/**
	 * @return the function used to render a tooltip label for an element, or null
	 *         to fall back to the raw x/y world coordinates
	 */
	public Function<? super T, String> getToolTipMapping() {
		return toolTipMapping;
	}

	/**
	 * @param toolTipMapping maps an element to its tooltip label; null falls back
	 *                       to the raw x/y world coordinates
	 */
	public void setToolTipMapping(Function<? super T, String> toolTipMapping) {
		this.toolTipMapping = toolTipMapping;
	}

	/**
	 * @return whether selected points are drawn in a second pass on top of
	 *         unselected ones
	 */
	public boolean isDrawSelectedLast() {
		return drawSelectedLast;
	}

	/**
	 * @param drawSelectedLast whether selected points should be drawn in a second
	 *                         pass on top of unselected ones
	 */
	public void setDrawSelectedLast(boolean drawSelectedLast) {
		this.drawSelectedLast = drawSelectedLast;
	}

	/**
	 * @return the paint used for the selection outline drawn behind a selected
	 *         point
	 */
	public Paint getSelectionPaint() {
		return selectionPaint;
	}

	/**
	 * @param selectionPaint paint used for the selection outline drawn behind a
	 *                       selected point
	 */
	public void setSelectionPaint(Paint selectionPaint) {
		this.selectionPaint = selectionPaint;
	}

	/** @return the width of the tooltip label rectangle, in pixels */
	public int getToolTipWidth() {
		return toolTipWidth;
	}

	/** @param toolTipWidth width of the tooltip label rectangle, in pixels */
	public void setToolTipWidth(int toolTipWidth) {
		this.toolTipWidth = toolTipWidth;
	}

	/** @return the height of the tooltip label rectangle, in pixels */
	public int getToolTipHeight() {
		return toolTipHeight;
	}

	/** @param toolTipHeight height of the tooltip label rectangle, in pixels */
	public void setToolTipHeight(int toolTipHeight) {
		this.toolTipHeight = toolTipHeight;
	}
}
