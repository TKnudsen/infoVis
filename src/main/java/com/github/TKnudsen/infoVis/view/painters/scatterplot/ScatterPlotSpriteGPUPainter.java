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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRange;
import com.github.TKnudsen.infoVis.view.gpu.GPURendererJOGLGLJPanel;
import com.github.TKnudsen.infoVis.view.gpu.PerformanceLogger;
import com.github.TKnudsen.infoVis.view.gpu.RenderMode;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.tools.BasicStrokeTools;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.tools.OverplottingMitigationTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.tools.VisualMappingTools;
import com.github.TKnudsen.infoVis.view.visualChannels.IOverplottingMitigation;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctions;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.ConstantSizeEncodingFunction;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * <p>
 * Paints a scatter plot using visual mapping functions to map data (represented
 * as T) into the visual space. Supports both CPU-based (Java2D) and GPU-based
 * (OpenGL) rendering modes for optimal performance.
 * </p>
 *
 * @version 3.01
 * @since 2018
 */
public class ScatterPlotSpriteGPUPainter<T> extends ChartPainter
		implements IXPositionEncoding, IYPositionEncoding, ISizeEncoding<T>, IColorEncoding<T>, IRectangleSelection<T>,
		IShapeSelection<T>, IClickSelection<T>, ISelectionVisualizer<T>, ITooltip, IOverplottingMitigation {

	// ==================== LOGGING ====================

	private static final Logger LOGGER = Logger.getLogger(ScatterPlotSpriteGPUPainter.class.getName());

	// ==================== FIELDS ====================

	// Input data
	final List<T> data;

	// CPU rendering: screen coordinates cache
	protected final List<Point2D> screenPoints;
	private final ReadWriteLock screenPointsLock = new ReentrantReadWriteLock();

	// GPU rendering
	private GPURendererJOGLGLJPanel gpuRenderer;
	private boolean gpuRendererInitialized = false;
	private RenderMode renderMode = RenderMode.GPU;
	private RenderMode effectiveRenderMode = RenderMode.GPU; // What's actually being used

	// Pixel readback: GLJPanel's own FBO->Swing blit is unreliable on some
	// systems, so the panel overdraws it with this readback image instead --
	// see ScatterPlotSpriteGPU's GLJPanel subclass.
	private volatile java.awt.image.BufferedImage lastRenderedFrame = null;
	private int[] readbackBuf = null; // reused across frames

	// Performance tracking
	private final PerformanceLogger performanceLogger;

	// Overplotting mitigation
	protected boolean overplottingMitigation = false;
	protected float alpha = 1.0f;

	// Settable size of dots
	private double pointSize = Double.NaN;

	private boolean tooltipping = true;
	private int toolTipWidth = 150;
	private int toolTipHeight = 30;

	// Position mapping of data
	private IPositionEncodingFunction xPositionEncodingFunction;
	private IPositionEncodingFunction yPositionEncodingFunction;
	protected boolean externalXPositionEncodingFunction = false;
	protected boolean externalYPositionEncodingFunction = false;

	// Listening to position encoding functions
	private final PositionEncodingFunctionListener myPositionEncodingFunctionListener = this::refreshDataPoints;

	// Color coding of data
	private Function<? super T, ? extends Paint> colorMapping;

	// Maps a T to individual double values which can be mapped to x and y position
	private Function<? super T, Double> worldPositionMappingX;
	private Function<? super T, Double> worldPositionMappingY;

	// World coordinate bounds (computed once, used by GPU)
	private double worldMinX = Double.POSITIVE_INFINITY;
	private double worldMaxX = Double.NEGATIVE_INFINITY;
	private double worldMinY = Double.POSITIVE_INFINITY;
	private double worldMaxY = Double.NEGATIVE_INFINITY;

	private Function<? super T, Double> sizeEncodingFunction = new ConstantSizeEncodingFunction<>(3);

	private Function<? super T, Boolean> selectedFunction;
	private boolean drawSelectedLast = true;
	private Paint selectionPaint = Color.BLACK;

	private Function<? super T, String> toolTipMapping;

	private boolean refreshingDataPoints;

	// ==================== CONSTRUCTOR ====================

	public ScatterPlotSpriteGPUPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {

		this.colorMapping = colorMapping;
		this.worldPositionMappingX = worldPositionMappingX;
		this.worldPositionMappingY = worldPositionMappingY;

		this.data = Collections.unmodifiableList(VisualMappingTools.sanityCheckFilter(data, worldPositionMappingX, true));
		this.screenPoints = new ArrayList<Point2D>(data.size());

		computeWorldBounds();
		initializePositionEncodingFunctions();
		refreshDataPoints();

		this.performanceLogger = new PerformanceLogger("ScatterPlot[" + data.size() + " points]");

		LOGGER.info(String.format("ScatterPlotPainter initialized with %d data points, render mode: %s", data.size(),
				renderMode));
	}

	private void computeWorldBounds() {
		NumericRange rangeX = PositionEncodingFunctions.computeRange(data, worldPositionMappingX,
				getClass().getSimpleName() + " (world bounds, x)");
		NumericRange rangeY = PositionEncodingFunctions.computeRange(data, worldPositionMappingY,
				getClass().getSimpleName() + " (world bounds, y)");

		worldMinX = rangeX.getMin();
		worldMaxX = rangeX.getMax();
		worldMinY = rangeY.getMin();
		worldMaxY = rangeY.getMax();
	}

	private void initializePositionEncodingFunctions() {
		this.xPositionEncodingFunction = PositionEncodingFunctions.createPositionEncodingFunction(data,
				worldPositionMappingX, 0d, 1d, false, getClass().getSimpleName() + " (x-axis)");
		this.xPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.yPositionEncodingFunction = PositionEncodingFunctions.createPositionEncodingFunction(data,
				worldPositionMappingY, 0d, 1d, true, getClass().getSimpleName() + " (y-axis)");
		this.yPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
	}

	protected void refreshDataPoints() {
		if (effectiveRenderMode == RenderMode.GPU) {
			return;
		}

		screenPointsLock.writeLock().lock();
		try {
			refreshingDataPoints = true;
			screenPoints.clear();

			if (data == null || chartRectangle == null)
				return;

			for (int i = 0; i < data.size(); i++) {
				T t = data.get(i);
				double worldX = worldPositionMappingX.apply(t);
				double worldY = worldPositionMappingY.apply(t);

				double x = xPositionEncodingFunction.apply(worldX);
				double y = yPositionEncodingFunction.apply(worldY);
				screenPoints.add(new Point2D.Double(x, y));
			}

			if (overplottingMitigation) {
				alpha = OverplottingMitigationTools.computeAlpha(screenPoints.size());
			}
		} finally {
			refreshingDataPoints = false;
			screenPointsLock.writeLock().unlock();
		}
	}

	public void setRenderMode(RenderMode mode) {
		if (mode == null)
			throw new IllegalArgumentException("Render mode cannot be null");
		this.renderMode = mode;
		updateEffectiveRenderMode();
		LOGGER.info(String.format("Render mode set to %s (effective: %s)", mode, effectiveRenderMode));
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
			LOGGER.info("GPU mode set - will initialize via GLJPanel");
			break;

		case AUTO:
			effectiveRenderMode = (data.size() > 10_000) ? RenderMode.GPU : RenderMode.CPU;
			break;

		default:
			// Defensive fallback
			effectiveRenderMode = RenderMode.CPU;
			break;
		}
	}

	// ==================== GL LIFECYCLE (REDESIGNED) ====================

	public void initGL(GLAutoDrawable drawable) {
		try {
			GL3 gl = drawable.getGL().getGL3();

			if (gpuRenderer == null) {
				gpuRenderer = new GPURendererJOGLGLJPanel();
			}
			// Pre-size for the worst case (every point selected -> a halo + a point
			// vertex each) so the first few frames of a large dataset don't pay for
			// repeated buffer growth.
			gpuRenderer.setMaxPoints(Math.max(1, data.size()) * 2);
			gpuRenderer.init(drawable);
			gpuRendererInitialized = true;

			// OpenGL state setup
			gl.glEnable(GL3.GL_BLEND);
			gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDisable(GL3.GL_DEPTH_TEST); // 2D rendering

			LOGGER.info("GPU renderer initialized successfully");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to initialize GPU", e);
			gpuRendererInitialized = false;
			effectiveRenderMode = RenderMode.CPU;
		}
	}

	public void reshapeGL(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL3 gl = drawable.getGL().getGL3();

		// Set viewport to match GLJPanel surface. Note: chartRectangle is
		// intentionally NOT touched here -- it must stay in Swing/outer-panel
		// coordinate space (set via setRectangle(Rectangle2D), which accounts for
		// the axis offset), since it's used for CPU-mode drawing and for
		// GPU-mode selection/tooltip screen<->world conversions. Overwriting it
		// with a (0,0)-origin rectangle here previously desynced selection
		// hit-testing from the actually-rendered pixel positions by exactly the
		// axis width. Actual GL rendering (displayGL) already gets its own
		// width/height directly from the drawable, so this method has no need
		// to touch chartRectangle at all.
		gl.glViewport(0, 0, width, height);

		LOGGER.fine(String.format("Viewport reshaped to %dx%d", width, height));
	}

	public void displayGL(GLAutoDrawable drawable) {
	    if (!gpuRendererInitialized || gpuRenderer == null) {
	        return;
	    }
	    if (data == null || data.isEmpty()) {
	        return;
	    }

	    GL3 gl = drawable.getGL().getGL3();

	    int width = drawable.getSurfaceWidth();
	    int height = drawable.getSurfaceHeight();

	    gl.glViewport(0, 0, width, height);

	    // Clear background
	    Color bg = getBackgroundPaint() instanceof Color ? (Color) getBackgroundPaint() : Color.WHITE;
	    gl.glClearColor(bg.getRed() / 255f, bg.getGreen() / 255f, bg.getBlue() / 255f, 1.0f);
	    gl.glClear(GL3.GL_COLOR_BUFFER_BIT);

	    // worldMinX/Max/Y start out as the static full-data range (computeWorldBounds),
	    // but zoom/pan changes the *visible window* by mutating xPositionEncodingFunction
	    // /yPositionEncodingFunction directly (via the panel's own axis setters) -- so the
	    // projection matrix (and every screen<->world helper that reads these same fields:
	    // worldToScreenX/Y, screenToWorldX/Y, the world-radius calc) must be rebuilt from
	    // the live functions on every frame, not from the stale construction-time values,
	    // or zooming would move the axes without ever moving the points.
	    worldMinX = xPositionEncodingFunction.getMinWorldValue().doubleValue();
	    worldMaxX = xPositionEncodingFunction.getMaxWorldValue().doubleValue();
	    worldMinY = yPositionEncodingFunction.getMinWorldValue().doubleValue();
	    worldMaxY = yPositionEncodingFunction.getMaxWorldValue().doubleValue();

	    // Update projection to match current surface
	    gpuRenderer.updateProjectionMatrix(0, 0, width, height, worldMinX, worldMaxX, worldMinY, worldMaxY);

	    gpuRenderer.clear();

	    double ps = Double.isNaN(this.pointSize) ? calculatePointSize(width, height) : this.pointSize;

	    batchPointsForGPU(ps);

	    gpuRenderer.render(drawable, (float) ps);

	    gl.glFlush();
	    gl.glFinish();

	    readbackToImage(gl, width, height);
	}

	/** @return whether the GPU renderer has been successfully initialized */
	public boolean isGpuRendererInitialized() {
		return gpuRendererInitialized && gpuRenderer != null;
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
	 * Swing panel can paint it directly instead of relying on GLJPanel's own
	 * FBO-to-Swing blit.
	 */
	private void readbackToImage(GL3 gl, int w, int h) {
		int size = w * h;
		if (readbackBuf == null || readbackBuf.length < size)
			readbackBuf = new int[size];

		// GL_BGRA + GL_UNSIGNED_BYTE on little-endian maps directly to Java TYPE_INT_ARGB
		java.nio.IntBuffer buf = java.nio.ByteBuffer.allocateDirect(size * 4).order(java.nio.ByteOrder.nativeOrder())
				.asIntBuffer();
		gl.glReadPixels(0, 0, w, h, GL3.GL_BGRA, GL3.GL_UNSIGNED_BYTE, buf);
		buf.get(readbackBuf, 0, size);

		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h,
				java.awt.image.BufferedImage.TYPE_INT_ARGB);
		int[] pixels = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
		for (int y = 0; y < h; y++)
			System.arraycopy(readbackBuf, (h - 1 - y) * w, pixels, y * w, w);

		lastRenderedFrame = img;
	}

	public void disposeGL(GLAutoDrawable drawable) {
		try {
			if (gpuRenderer != null) {
				gpuRenderer.dispose(drawable);
				LOGGER.info("GPU renderer disposed");
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error during GPU disposal", e);
		} finally {
			gpuRenderer = null;
			gpuRendererInitialized = false;
		}
	}

	// ==================== GPU BATCHING (OPTIMIZED) ====================

	private void batchPointsForGPU(double defaultPointSize) {
		// Separate selected/unselected if needed
		if (drawSelectedLast && selectedFunction != null) {
			// First pass: non-selected points
			for (T t : data) {
				if (!isSelected(t)) {
					addPointToGPU(t, defaultPointSize, false);
				}
			}
			// Second pass: selected points (drawn on top)
			for (T t : data) {
				if (isSelected(t)) {
					addPointToGPU(t, defaultPointSize, true);
				}
			}
		} else {
			// Single pass
			for (T t : data) {
				addPointToGPU(t, defaultPointSize, isSelected(t));
			}
		}
	}

	private void addPointToGPU(T t, double defaultPointSize, boolean selected) {
		double worldX = worldPositionMappingX.apply(t);
		double worldY = worldPositionMappingY.apply(t);

		// Skip invalid coordinates
		if (Double.isNaN(worldX) || Double.isNaN(worldY)) {
			return;
		}

		// Get color
		Paint paint = colorMapping != null ? colorMapping.apply(t) : getPaint();
		Color color = extractColor(paint);

		// Apply alpha for overplotting mitigation
		if (overplottingMitigation && color != null) {
			int alpha = (int) (this.alpha * 255);
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
		}

		// Get size
		double size = sizeEncodingFunction != null ? sizeEncodingFunction.apply(t) : defaultPointSize;
		if (Double.isNaN(size)) {
			size = defaultPointSize;
		}

		// CPU painter treats 'size' as a radius and draws circles with:
		// colored diameter = 2 * size * 1.33
		// halo diameter = 2 * max(size * 1.66, size + 2)
		// gl_PointSize is a diameter, so convert accordingly (matching
		// ScatterPlotIndexedGPUPainter -- without this, points here render at
		// roughly 1/2.66 the CPU/Working size).
		double coloredDiameter = size * 1.33 * 2.0;

		// Draw selection halo first (if selected)
		if (selected && selectionPaint != null) {
			double haloDiameter = Math.max(size * 1.66, size + 2) * 2.0;
			Color selectionColor = extractColor(selectionPaint);
			gpuRenderer.addPointSprite((float) worldX, (float) worldY, (float) haloDiameter, selectionColor);
		}

		// Draw the actual point
		gpuRenderer.addPointSprite((float) worldX, (float) worldY, (float) coloredDiameter, color);
	}

	private Color extractColor(Paint paint) {
		if (paint instanceof Color) {
			return (Color) paint;
		}
		return Color.GRAY;
	}

	private boolean isSelected(T t) {
		if (selectedFunction == null) {
			return false;
		}
		Boolean result = selectedFunction.apply(t);
		return result != null && result;
	}

// ==================== DRAW ====================

	@Override
	public void draw(Graphics2D g2) {
		if (chartRectangle == null || data == null || data.isEmpty())
			return;

		performanceLogger.logFrameStart();

		if (effectiveRenderMode == RenderMode.CPU) {
			try {
				drawCPU(g2);
			} finally {
				performanceLogger.logFrameEnd(data.size(), data.size(), RenderMode.CPU);
			}
		}
// GPU handled by JOGL callback
	}

	private void drawCPU(Graphics2D g2) {
		screenPointsLock.readLock().lock();
		try {
			final int n = data.size();
			final List<Point2D> points = screenPoints;
			if (points == null || points.size() != n)
				return;

			final Color oldColor = g2.getColor();

			double ps = this.pointSize;
			if (Double.isNaN(ps))
				ps = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());

			final boolean hasSelection = (selectedFunction != null);
			final boolean twoPhase = drawSelectedLast && hasSelection;

			List<Integer> selectedIndices = twoPhase ? new ArrayList<>(Math.min(128, n / 10)) : Collections.emptyList();

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

// ==================== SIZING ====================

	public static double calculatePointSize(double viewWidth, double viewHeight) {
		return Math.max(3, Math.min(viewWidth, viewHeight) * 0.006);
	}

// ==================== RECTANGLE UPDATE ====================

	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);
		if (rectangle == null)
			return;
		if (chartRectangle == null)
			return;

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

		if (effectiveRenderMode != RenderMode.GPU) {
			refreshDataPoints();
		}
	}

	private final void updateXPositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;
		this.xPositionEncodingFunction.setMinPixel(rectangle.getMinX());
		this.xPositionEncodingFunction.setMaxPixel(rectangle.getMaxX());
	}

	private final void updateYPositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;
		this.yPositionEncodingFunction.setMinPixel(rectangle.getMinY());
		this.yPositionEncodingFunction.setMaxPixel(rectangle.getMaxY());
	}

// ==================== TOOLTIP ====================

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!tooltipping || p == null)
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
				final Point2D sp = screenPoints.get(i);
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
			double tx = worldPositionMappingX.apply(t);
			double ty = worldPositionMappingY.apply(t);

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
		if (toolTipMapping != null) {
			toolTipString = toolTipMapping.apply(element);
		} else {
			Double wx = worldPositionMappingX != null ? worldPositionMappingX.apply(element) : null;
			Double wy = worldPositionMappingY != null ? worldPositionMappingY.apply(element) : null;
			if (wx == null || wy == null || wx.isNaN() || wy.isNaN())
				return null;
			toolTipString = MathFunctions.round(wx, 2) + ", " + MathFunctions.round(wy, 2);
		}

		if (toolTipString == null)
			return null;

		StringPainter stringPainter = new StringPainter(toolTipString);
		Rectangle2D rect = ToolTipTools.createToolTipRectangle(chartRectangle, p, toolTipWidth, toolTipHeight);
		stringPainter.setRectangle(rect);

		stringPainter.setBackgroundPaint(ColorTools.setAlpha(Color.DARK_GRAY, 0.5f));
		stringPainter.setFontColor(Color.WHITE);
		stringPainter.setFontSize(15);

		return stringPainter;
	}

	private double worldToScreenX(double worldX) {
		return chartRectangle.getMinX() + (worldX - worldMinX) / (worldMaxX - worldMinX) * chartRectangle.getWidth();
	}

	private double worldToScreenY(double worldY) {
		return chartRectangle.getMaxY() - (worldY - worldMinY) / (worldMaxY - worldMinY) * chartRectangle.getHeight();
	}

	private double screenToWorldX(double screenX) {
		return worldMinX + (screenX - chartRectangle.getMinX()) / chartRectangle.getWidth() * (worldMaxX - worldMinX);
	}

	private double screenToWorldY(double screenY) {
		return worldMinY + (chartRectangle.getMaxY() - screenY) / chartRectangle.getHeight() * (worldMaxY - worldMinY);
	}

	@Override
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		return getElementsInShape(rectangle);
	}

	@Override
	public List<T> getElementsInShape(Shape shape) {
		if (shape == null || data == null)
			return null;

		List<T> elements = new ArrayList<>();

		if (effectiveRenderMode == RenderMode.GPU) {
			for (T t : data) {
				double wx = worldPositionMappingX.apply(t);
				double wy = worldPositionMappingY.apply(t);
				double sx = worldToScreenX(wx);
				double sy = worldToScreenY(wy);
				if (shape.contains(sx, sy))
					elements.add(t);
			}
		} else {
			for (T t : data) {
				double wx = worldPositionMappingX.apply(t);
				double wy = worldPositionMappingY.apply(t);
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

		double radius = this.pointSize;
		if (Double.isNaN(pointSize))
			radius = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());

		Ellipse2D circle = new Ellipse2D.Double();
		circle.setFrameFromCenter(p.getX(), p.getY(), p.getX() + radius, p.getY() + radius);

		if (data == null)
			return null;

		List<T> elements = new ArrayList<>();

		if (effectiveRenderMode == RenderMode.GPU) {
			for (T t : data) {
				double wx = worldPositionMappingX.apply(t);
				double wy = worldPositionMappingY.apply(t);
				double sx = worldToScreenX(wx);
				double sy = worldToScreenY(wy);
				if (circle.contains(sx, sy))
					elements.add(t);
			}
		} else {
			for (T t : data) {
				double wx = worldPositionMappingX.apply(t);
				double wy = worldPositionMappingY.apply(t);
				double sx = xPositionEncodingFunction.apply(wx);
				double sy = yPositionEncodingFunction.apply(wy);
				if (circle.contains(sx, sy))
					elements.add(t);
			}
		}
		return elements;
	}

// ==================== Performance ====================

	public void enablePerformanceLogging() {
		performanceLogger.enable();
	}

	public void disablePerformanceLogging() {
		performanceLogger.disable();
	}

	public boolean isPerformanceLoggingEnabled() {
		return performanceLogger.isEnabled();
	}

	public void resetPerformanceStatistics() {
		performanceLogger.reset();
	}

	public void logPerformanceStatistics() {
		performanceLogger.logStatistics();
	}

	public PerformanceLogger getPerformanceLogger() {
		return performanceLogger;
	}

// ==================== Cleanup ====================

	public void dispose() {
// GPU objects disposed in disposeGL(drawable)
		gpuRenderer = null;
		gpuRendererInitialized = false;
	}

// ==================== Getters/Setters ====================

	@Override
	public boolean isAlphaAdjustment() {
		return overplottingMitigation;
	}

	@Override
	public void setAlphaAdjustment(boolean dynamicAlphaAdjustment) {
		this.overplottingMitigation = dynamicAlphaAdjustment;
		if (effectiveRenderMode == RenderMode.CPU)
			refreshDataPoints();
	}

	public double getPointSize() {
		return pointSize;
	}

	public void setPointSize(double pointSize) {
		this.pointSize = pointSize;
	}

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
	 *                              immediately (CPU-mode fallback; GPU mode
	 *                              re-derives fresh every frame regardless)
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
	 *                              immediately (CPU-mode fallback; GPU mode
	 *                              re-derives fresh every frame regardless)
	 */
	public void setWorldPositionMappingY(Function<? super T, Double> worldPositionMappingY) {
		this.worldPositionMappingY = worldPositionMappingY;

		refreshDataPoints();
	}

	@Override
	public boolean isToolTipping() {
		return tooltipping;
	}

	@Override
	public void setToolTipping(boolean tooltipping) {
		this.tooltipping = tooltipping;
	}

	@Override
	public void setSizeEncodingFunction(Function<? super T, Double> sizeEncodingFunction) {
		this.sizeEncodingFunction = sizeEncodingFunction;
	}

	@Override
	public void setSelectedFunction(Function<? super T, Boolean> selectedFunction) {
		this.selectedFunction = selectedFunction;
	}

	@Override
	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction) {
		this.colorMapping = colorEncodingFunction;
	}

	@Override
	public void setXPositionEncodingFunction(IPositionEncodingFunction xPositionEncodingFunction) {
		this.xPositionEncodingFunction.removePositionEncodingFunctionListener(myPositionEncodingFunctionListener);
		this.xPositionEncodingFunction = xPositionEncodingFunction;
		this.xPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
		this.externalXPositionEncodingFunction = true;
	}

	@Override
	public void setYPositionEncodingFunction(IPositionEncodingFunction yPositionEncodingFunction) {
		this.yPositionEncodingFunction.removePositionEncodingFunctionListener(myPositionEncodingFunctionListener);
		this.yPositionEncodingFunction = yPositionEncodingFunction;
		this.yPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
		this.externalYPositionEncodingFunction = true;
	}

	public Function<? super T, String> getToolTipMapping() {
		return toolTipMapping;
	}

	public void setToolTipMapping(Function<? super T, String> toolTipMapping) {
		this.toolTipMapping = toolTipMapping;
	}

	public boolean isDrawSelectedLast() {
		return drawSelectedLast;
	}

	public void setDrawSelectedLast(boolean drawSelectedLast) {
		this.drawSelectedLast = drawSelectedLast;
	}

	public Paint getSelectionPaint() {
		return selectionPaint;
	}

	public void setSelectionPaint(Paint selectionPaint) {
		this.selectionPaint = selectionPaint;
	}

	public int getToolTipWidth() {
		return toolTipWidth;
	}

	public void setToolTipWidth(int toolTipWidth) {
		this.toolTipWidth = toolTipWidth;
	}

	public int getToolTipHeight() {
		return toolTipHeight;
	}

	public void setToolTipHeight(int toolTipHeight) {
		this.toolTipHeight = toolTipHeight;
	}
}
