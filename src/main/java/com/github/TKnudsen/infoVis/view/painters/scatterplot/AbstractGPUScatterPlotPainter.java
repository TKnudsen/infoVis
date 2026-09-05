package com.github.TKnudsen.infoVis.view.painters.scatterplot;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DegenerateRangeException;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRange;
import com.github.TKnudsen.infoVis.view.gpu.PerformanceLogger;
import com.github.TKnudsen.infoVis.view.gpu.RenderMode;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctions;

/**
 * <p>
 * Shared surface for {@link ScatterPlotIndexedGPUPainter} and
 * {@link ScatterPlotSpriteGPUPainter}: render-mode bookkeeping, the readback
 * frame cache, world-bounds tracking, the CPU/GPU coordinate-transform helpers,
 * the CPU/GPU tooltip dispatch, and the CPU/GPU selection-hit-testing dispatch
 * -- all confirmed byte-identical between the two classes by direct comparison
 * ahead of this extraction (code review finding #27).
 * </p>
 *
 * <p>
 * Deliberately NOT hoisted here (each concrete subclass keeps its own): the
 * {@code gpuRenderer} field itself -- {@code GPURendererJOGLIndexed} (used by
 * the indexed painter) and {@code GPURendererJOGLSprite} (used by the sprite
 * painter) share no common supertype and have diverging method signatures --
 * and everything that touches it directly ({@code initGL}, {@code disposeGL},
 * {@code displayGL}, {@code reshapeGL}, {@code addPointToGPU}, {@code dispose},
 * {@code draw}). {@link #isGpuRendererPresent()} is the one seam those methods
 * need to expose to this class (for {@link #isGpuRendererInitialized()}).
 * </p>
 *
 * <p>
 * Two intentional behavior fixes ride along with this extraction (see the
 * code review's finding #28 and the characterization tests in
 * {@code ScatterPlotPainterHierarchyCharacterizationTest}):
 * </p>
 * <ul>
 * <li>{@link #computeWorldBounds()} now tolerates a degenerate (single-value)
 * dataset the same way {@link AbstractScatterPlotPainter}'s position-encoding
 * setup already does, instead of throwing {@link DegenerateRangeException} --
 * previously both GPU painters had their own separate, strict copy of this
 * range computation.</li>
 * <li>{@code ScatterPlotIndexedGPUPainter.addPointToGPU}'s null-colorMapping
 * fallback changes from gray to {@code getPaint()}, matching the sprite/CPU
 * painters (that fix lives in the concrete subclass, since it's inside the
 * method that also calls the concretely-typed {@code gpuRenderer}).</li>
 * </ul>
 */
public abstract class AbstractGPUScatterPlotPainter<T> extends AbstractScatterPlotPainter<T> {

	// GPU rendering
	protected boolean gpuRendererInitialized = false;
	private RenderMode renderMode = RenderMode.GPU;
	protected RenderMode effectiveRenderMode = RenderMode.GPU; // What's actually being used

	// Pixel readback: GL renders to FBO, we copy it here so Swing can paint it
	protected volatile java.awt.image.BufferedImage lastRenderedFrame = null;
	protected int[] readbackBuf = null; // reused across frames
	// Off-heap staging buffer for glReadPixels -- purely an internal scratch
	// buffer for this method (unlike lastRenderedFrame, never exposed to or
	// read from another thread), so it's safe to grow-and-reuse the same way
	// readbackBuf already is, instead of allocating a fresh multi-megabyte
	// direct buffer every single frame.
	protected java.nio.ByteBuffer readbackByteBuffer = null;

	// Performance tracking
	protected final PerformanceLogger performanceLogger;

	// World coordinate bounds (computed once, used by GPU); re-derived from the
	// live position-encoding functions on every frame by displayGL()/
	// updateGPUProjection() as zoom/pan change the visible window, so their
	// construction-time value here only matters until the first repaint.
	protected double worldMinX = Double.POSITIVE_INFINITY;
	protected double worldMaxX = Double.NEGATIVE_INFINITY;
	protected double worldMinY = Double.POSITIVE_INFINITY;
	protected double worldMaxY = Double.NEGATIVE_INFINITY;

	protected AbstractGPUScatterPlotPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		super(data, colorMapping, worldPositionMappingX, worldPositionMappingY);

		// Compute world bounds once. Runs after super() returns (Java requires the
		// super() call first), i.e. after data/worldPositionMappingX/Y are already
		// assigned -- computeWorldBounds() only reads those, so this reordering
		// relative to the pre-extraction constructors (which computed bounds before
		// setting up position encoding) has no functional effect.
		computeWorldBounds();

		this.performanceLogger = new PerformanceLogger("ScatterPlot[" + data.size() + " points]");
	}

	/**
	 * Recomputes {@link #worldMinX}/{@link #worldMaxX}/{@link #worldMinY}/
	 * {@link #worldMaxY} from the full current dataset. Tolerates a degenerate
	 * (single-value) range the same way {@link AbstractScatterPlotPainter}'s
	 * position-encoding setup does -- previously this used the strict
	 * {@link PositionEncodingFunctions#computeRange}, so a single-value dataset
	 * threw {@link DegenerateRangeException} here before ever reaching the
	 * (already-tolerant) position-encoding setup in the super constructor.
	 */
	protected void computeWorldBounds() {
		NumericRange rangeX = PositionEncodingFunctions.computeRangeTolerant(data, getWorldPositionMappingX(),
				getClass().getSimpleName() + " (world bounds, x)");
		NumericRange rangeY = PositionEncodingFunctions.computeRangeTolerant(data, getWorldPositionMappingY(),
				getClass().getSimpleName() + " (world bounds, y)");

		worldMinX = rangeX.getMin();
		worldMaxX = rangeX.getMax();
		worldMinY = rangeY.getMin();
		worldMaxY = rangeY.getMax();
	}

	/**
	 * Only needed for CPU rendering -- when GPU-effective, screen points are
	 * never read (GPU rendering goes through world coordinates directly via
	 * {@link #worldToScreenX(double)}/{@link #worldToScreenY(double)}), so
	 * recomputing them here would be pure waste.
	 * <p>
	 * Guards on {@code effectiveRenderMode != RenderMode.CPU} rather than
	 * {@code == RenderMode.GPU}: this method is first reached from
	 * {@link AbstractScatterPlotPainter}'s own constructor (via the {@code this::
	 * refreshDataPoints} listener wiring and the constructor's own call), i.e.
	 * while still inside this class's {@code super(...)} call -- at that point
	 * this class's own field initializers (including {@code effectiveRenderMode
	 * = RenderMode.GPU}) have not run yet, so the field reads as {@code null}.
	 * {@code null != CPU} still correctly skips (matching the eventual GPU
	 * default), whereas {@code null == GPU} would not, and screen points would
	 * be uselessly computed once at construction time.
	 */
	@Override
	protected void refreshDataPoints() {
		if (effectiveRenderMode != RenderMode.CPU)
			return;

		super.refreshDataPoints();
	}

	// ==================== RENDERING MODE MANAGEMENT ====================

	/**
	 * Set the rendering mode (CPU, GPU, or AUTO). AUTO mode will choose GPU for
	 * datasets > 10,000 points if available.
	 */
	public void setRenderMode(RenderMode mode) {
		if (mode == null)
			throw new IllegalArgumentException("Render mode cannot be null");

		this.renderMode = mode;
		updateEffectiveRenderMode();
	}

	public RenderMode getRenderMode() {
		return renderMode;
	}

	public RenderMode getEffectiveRenderMode() {
		return effectiveRenderMode;
	}

	private void updateEffectiveRenderMode() {
		switch (renderMode) {
		case CPU:
			effectiveRenderMode = RenderMode.CPU;
			break;

		case GPU:
			effectiveRenderMode = RenderMode.GPU;
			break;

		case AUTO:
			effectiveRenderMode = data.size() > 10_000 ? RenderMode.GPU : RenderMode.CPU;
			break;

		default:
			effectiveRenderMode = RenderMode.CPU;
			break;
		}
	}

	/** @return whether the GPU renderer has been successfully initialized */
	public boolean isGpuRendererInitialized() {
		return gpuRendererInitialized && isGpuRendererPresent();
	}

	/**
	 * @return whether the concretely-typed {@code gpuRenderer} field is non-null;
	 *         implemented per concrete subclass since the renderer type itself is
	 *         not shared (see the class-level note)
	 */
	protected abstract boolean isGpuRendererPresent();

	/**
	 * Call when the painter is no longer needed. Does not itself free GL
	 * resources -- ordinary application cleanup code (e.g. a panel's own
	 * dispose()) has no guarantee a GL context is current, and issuing GL
	 * calls without one crashes with "No OpenGL context current". Actual GPU
	 * cleanup happens in each subclass's own {@code disposeGL(GLAutoDrawable)},
	 * invoked by JOGL itself with a guaranteed-current context when the GL
	 * surface is destroyed.
	 * <p>
	 * Declared abstract rather than shared: its body is just nulling out the
	 * concretely-typed {@code gpuRenderer} field, which (like the field itself)
	 * is not shared -- see the class-level note. Declaring it here rather than
	 * leaving it an implicit per-subclass method lets panel-level code (see
	 * {@code AbstractGPUScatterPlotPanel#dispose()}) call it through this
	 * shared ancestor type.
	 */
	public abstract void dispose();

	// ==================== PERFORMANCE LOGGING ====================

	/**
	 * Enable performance logging to track rendering metrics (FPS, draw calls,
	 * etc.). Useful for benchmarking and identifying performance bottlenecks.
	 */
	public void enablePerformanceLogging() {
		performanceLogger.enable();
	}

	/**
	 * Disable performance logging.
	 */
	public void disablePerformanceLogging() {
		performanceLogger.disable();
	}

	/**
	 * Check if performance logging is currently enabled.
	 *
	 * @return true if performance logging is active
	 */
	public boolean isPerformanceLoggingEnabled() {
		return performanceLogger.isEnabled();
	}

	/**
	 * Reset accumulated performance statistics.
	 */
	public void resetPerformanceStatistics() {
		performanceLogger.reset();
	}

	/**
	 * Log current performance statistics to the console.
	 */
	public void logPerformanceStatistics() {
		performanceLogger.logStatistics();
	}

	/**
	 * Get direct access to the performance logger for advanced control.
	 *
	 * @return the performance logger instance
	 */
	public PerformanceLogger getPerformanceLogger() {
		return performanceLogger;
	}

	/**
	 * @return the most recently rendered frame, read back from the GPU via
	 *         {@link #readbackToImage}, or null before the first frame
	 */
	public java.awt.image.BufferedImage getLastRenderedFrame() {
		return lastRenderedFrame;
	}

	/**
	 * Reads the current GL framebuffer into a {@code BufferedImage}
	 * (TYPE_INT_ARGB, vertically flipped since GL's origin is bottom-left and
	 * Java's is top-left) and stores it in {@link #lastRenderedFrame}, so the
	 * Swing panel can paint it directly instead of relying on the GL surface's
	 * own (unreliable, on at least one GPU/driver combination) FBO-to-Swing
	 * blit.
	 */
	protected void readbackToImage(com.jogamp.opengl.GL3 gl, int w, int h) {
		int size = w * h;
		if (readbackBuf == null || readbackBuf.length < size)
			readbackBuf = new int[size];

		if (readbackByteBuffer == null || readbackByteBuffer.capacity() < size * 4)
			readbackByteBuffer = java.nio.ByteBuffer.allocateDirect(size * 4)
					.order(java.nio.ByteOrder.nativeOrder());
		readbackByteBuffer.clear();
		java.nio.IntBuffer buf = readbackByteBuffer.asIntBuffer();

		// GL_BGRA + GL_UNSIGNED_BYTE on little-endian maps directly to Java TYPE_INT_ARGB
		gl.glReadPixels(0, 0, w, h, com.jogamp.opengl.GL3.GL_BGRA, com.jogamp.opengl.GL3.GL_UNSIGNED_BYTE, buf);
		buf.get(readbackBuf, 0, size);

		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h,
				java.awt.image.BufferedImage.TYPE_INT_ARGB);
		int[] pixels = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
		// Flip vertically: GL origin is bottom-left, Java top-left
		for (int y = 0; y < h; y++)
			System.arraycopy(readbackBuf, (h - 1 - y) * w, pixels, y * w, w);

		lastRenderedFrame = img;
	}

	protected boolean isSelected(T t) {
		if (selectedFunction == null)
			return false;
		Boolean result = selectedFunction.apply(t);
		return result != null && result;
	}

	protected Color extractColor(Paint paint) {
		if (paint instanceof Color)
			return (Color) paint;
		return Color.GRAY;
	}

	// ==================== COORDINATE TRANSFORMATION (for GPU mode) ====================

	protected double worldToScreenX(double worldX) {
		double spanX = worldMaxX - worldMinX;
		if (spanX == 0.0)
			spanX = 1.0;
		return chartRectangle.getMinX() + (worldX - worldMinX) / spanX * chartRectangle.getWidth();
	}

	protected double worldToScreenY(double worldY) {
		double spanY = worldMaxY - worldMinY;
		if (spanY == 0.0)
			spanY = 1.0;
		return chartRectangle.getMaxY() - (worldY - worldMinY) / spanY * chartRectangle.getHeight();
	}

	protected double screenToWorldX(double screenX) {
		return worldMinX + (screenX - chartRectangle.getMinX()) / chartRectangle.getWidth() * (worldMaxX - worldMinX);
	}

	protected double screenToWorldY(double screenY) {
		return worldMinY + (chartRectangle.getMaxY() - screenY) / chartRectangle.getHeight() * (worldMaxY - worldMinY);
	}

	// ==================== TOOLTIP ====================

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!isToolTipping() || p == null)
			return null;

		final Rectangle2D cr = chartRectangle;
		if (cr == null)
			return null;

		if (refreshingDataPoints)
			return null;

		if (effectiveRenderMode == RenderMode.GPU) {
			return getTooltipGPU(p);
		} else {
			return getTooltipCPU(p);
		}
	}

	private ChartPainter getTooltipCPU(Point p) {
		screenPointsLock.readLock().lock();
		try {
			if (data == null || data.isEmpty())
				return null;
			if (screenPoints == null || screenPoints.isEmpty())
				return null;

			final int n = data.size();
			if (screenPoints.size() != n)
				return null;

			final double maxRadius = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());
			final double px = p.getX();
			final double py = p.getY();

			double bestDist = Double.POSITIVE_INFINITY;
			T bestElement = null;

			for (int i = 0; i < n; i++) {
				final java.awt.geom.Point2D sp = screenPoints.get(i);
				if (sp == null)
					continue;

				final double dx = Math.abs(sp.getX() - px);
				if (dx >= maxRadius)
					continue;

				final double dy = Math.abs(sp.getY() - py);
				if (dy >= maxRadius)
					continue;

				final double dist = dx + dy;
				if (dist < bestDist) {
					final T candidate = data.get(i);
					if (candidate != null) {
						bestDist = dist;
						bestElement = candidate;
					}
				}
			}

			return createTooltipPainter(p, bestElement);

		} finally {
			screenPointsLock.readLock().unlock();
		}
	}

	private ChartPainter getTooltipGPU(Point p) {
		double worldX = screenToWorldX(p.getX());
		double worldY = screenToWorldY(p.getY());

		double maxRadius = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());
		double worldRadius = maxRadius * (worldMaxX - worldMinX) / chartRectangle.getWidth();

		double bestDist = Double.POSITIVE_INFINITY;
		T bestElement = null;

		for (T t : data) {
			double tx = getWorldPositionMappingX().apply(t);
			double ty = getWorldPositionMappingY().apply(t);

			double dx = tx - worldX;
			double dy = ty - worldY;
			double dist = Math.sqrt(dx * dx + dy * dy);

			if (dist < worldRadius && dist < bestDist) {
				bestDist = dist;
				bestElement = t;
			}
		}

		return createTooltipPainter(p, bestElement);
	}

	private ChartPainter createTooltipPainter(Point p, T element) {
		if (element == null)
			return null;

		String toolTipString;
		if (getToolTipMapping() != null) {
			toolTipString = getToolTipMapping().apply(element);
		} else {
			Double wx = getWorldPositionMappingX() != null ? getWorldPositionMappingX().apply(element) : null;
			Double wy = getWorldPositionMappingY() != null ? getWorldPositionMappingY().apply(element) : null;
			if (wx == null || wy == null || wx.isNaN() || wy.isNaN())
				return null;
			toolTipString = MathFunctions.round(wx, 2) + ", " + MathFunctions.round(wy, 2);
		}

		if (toolTipString == null)
			return null;

		StringPainter stringPainter = new StringPainter(toolTipString);
		Rectangle2D rect = ToolTipTools.createToolTipRectangle(chartRectangle, p, getToolTipWidth(), getToolTipHeight());
		stringPainter.setRectangle(rect);

		stringPainter.setBackgroundPaint(ColorTools.setAlpha(Color.DARK_GRAY, 0.5f));
		stringPainter.setFontColor(Color.WHITE);
		stringPainter.setFontSize(15);

		return stringPainter;
	}

	// ==================== SELECTION (CPU/GPU dispatch) ====================

	@Override
	public List<T> getElementsInShape(Shape shape) {
		if (shape == null || data == null)
			return null;

		List<T> elements = new ArrayList<>();

		if (effectiveRenderMode == RenderMode.GPU) {
			for (T t : data) {
				double wx = getWorldPositionMappingX().apply(t);
				double wy = getWorldPositionMappingY().apply(t);
				double sx = worldToScreenX(wx);
				double sy = worldToScreenY(wy);
				if (shape.contains(sx, sy))
					elements.add(t);
			}
		} else {
			for (T t : data) {
				double wx = getWorldPositionMappingX().apply(t);
				double wy = getWorldPositionMappingY().apply(t);
				double sx = xPositionEncodingFunction.apply(wx);
				double sy = yPositionEncodingFunction.apply(wy);
				if (shape.contains(sx, sy))
					elements.add(t);
			}
		}

		return elements;
	}

	@Override
	public List<T> getElementsAtPoint(Point p) {
		if (p == null)
			return null;

		double radius = getPointSize();
		if (Double.isNaN(radius))
			radius = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());

		Ellipse2D circle = new Ellipse2D.Double();
		circle.setFrameFromCenter(p.getX(), p.getY(), p.getX() + radius, p.getY() + radius);

		if (data == null)
			return null;

		List<T> elements = new ArrayList<>();

		if (effectiveRenderMode == RenderMode.GPU) {
			for (T t : data) {
				double wx = getWorldPositionMappingX().apply(t);
				double wy = getWorldPositionMappingY().apply(t);
				double sx = worldToScreenX(wx);
				double sy = worldToScreenY(wy);
				if (circle.contains(sx, sy))
					elements.add(t);
			}
		} else {
			for (T t : data) {
				double wx = getWorldPositionMappingX().apply(t);
				double wy = getWorldPositionMappingY().apply(t);
				double sx = xPositionEncodingFunction.apply(wx);
				double sy = yPositionEncodingFunction.apply(wy);
				if (circle.contains(sx, sy))
					elements.add(t);
			}
		}

		return elements;
	}
}
