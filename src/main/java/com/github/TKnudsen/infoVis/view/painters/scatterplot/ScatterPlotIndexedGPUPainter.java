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
import com.github.TKnudsen.infoVis.view.gpu.GPURendererJOGLWorking;
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
 * @version 2.01 revised in August 2026
 * @since 2025
 */
public class ScatterPlotIndexedGPUPainter<T> extends ChartPainter
		implements IXPositionEncoding, IYPositionEncoding, ISizeEncoding<T>, IColorEncoding<T>, IRectangleSelection<T>,
		IShapeSelection<T>, IClickSelection<T>, ISelectionVisualizer<T>, ITooltip, IOverplottingMitigation {

	// ==================== LOGGING ====================

	private static final Logger LOGGER = Logger.getLogger(ScatterPlotIndexedGPUPainter.class.getName());

	// ==================== FIELDS ====================

	// Input data
	final List<T> data;

	// CPU rendering: screen coordinates cache
	protected final List<Point2D> screenPoints;
	private final ReadWriteLock screenPointsLock = new ReentrantReadWriteLock();

	// GPU rendering
	private GPURendererJOGLWorking gpuRenderer;
	private boolean gpuRendererInitialized = false;
	private RenderMode renderMode = RenderMode.GPU;
	private RenderMode effectiveRenderMode = RenderMode.GPU; // What's actually being used

	// Pixel readback: GL renders to FBO, we copy it here so Swing can paint it
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

	public ScatterPlotIndexedGPUPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		this.colorMapping = colorMapping;
		this.worldPositionMappingX = worldPositionMappingX;
		this.worldPositionMappingY = worldPositionMappingY;

		this.data = Collections
				.unmodifiableList(VisualMappingTools.sanityCheckFilter(data, worldPositionMappingX, true));
		this.screenPoints = new ArrayList<Point2D>(data.size());

		// Compute world bounds once
		computeWorldBounds();

		initializePositionEncodingFunctions();
		refreshDataPoints();

		// Initialize performance logger with descriptive name
		this.performanceLogger = new PerformanceLogger("ScatterPlot[" + data.size() + " points]");

		LOGGER.info(String.format("ScatterPlotPainter initialized with %d data points, render mode: %s", data.size(),
				renderMode));
	}

	// ==================== WORLD BOUNDS COMPUTATION ====================

	private void computeWorldBounds() {
		NumericRange rangeX = PositionEncodingFunctions.computeRange(data, worldPositionMappingX,
				getClass().getSimpleName() + " (world bounds, x)");
		NumericRange rangeY = PositionEncodingFunctions.computeRange(data, worldPositionMappingY,
				getClass().getSimpleName() + " (world bounds, y)");

		worldMinX = rangeX.getMin();
		worldMaxX = rangeX.getMax();
		worldMinY = rangeY.getMin();
		worldMaxY = rangeY.getMax();

		LOGGER.fine(String.format("World bounds: X[%.2f, %.2f], Y[%.2f, %.2f]", worldMinX, worldMaxX, worldMinY,
				worldMaxY));
	}

	// ==================== POSITION ENCODING ====================

	private void initializePositionEncodingFunctions() {
		this.xPositionEncodingFunction = PositionEncodingFunctions.createPositionEncodingFunction(data,
				worldPositionMappingX, 0d, 1d, false, getClass().getSimpleName() + " (x-axis)");
		this.xPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.yPositionEncodingFunction = PositionEncodingFunctions.createPositionEncodingFunction(data,
				worldPositionMappingY, 0d, 1d, true, getClass().getSimpleName() + " (y-axis)");
		this.yPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
	}

	protected void refreshDataPoints() {
		// Only needed for CPU rendering
		if (effectiveRenderMode == RenderMode.GPU) {
			LOGGER.fine("Skipping refreshDataPoints - using GPU rendering");
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

	// ==================== RENDERING MODE MANAGEMENT ====================

	/**
	 * Set the rendering mode (CPU, GPU, or AUTO). AUTO mode will choose GPU for
	 * datasets > 10,000 points if available.
	 */
	public void setRenderMode(RenderMode mode) {
		if (mode == null) {
			throw new IllegalArgumentException("Render mode cannot be null");
		}

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
			effectiveRenderMode = data.size() > 10_000 ? RenderMode.GPU : RenderMode.CPU;
			LOGGER.fine(String.format("AUTO mode selected %s rendering (%d points)", effectiveRenderMode, data.size()));
			break;
		}
	}

	private boolean ensureGPUInitialized() {
		// When using GLJPanel, initialization happens via initGL() callback
		return gpuRendererInitialized && gpuRenderer != null;
	}

	public boolean isGpuRendererInitialized() {
		return gpuRendererInitialized && gpuRenderer != null;
	}

	public java.awt.image.BufferedImage getLastRenderedFrame() {
		return lastRenderedFrame;
	}

	public void updateWorldBounds() {
		computeWorldBounds();

		if (gpuRendererInitialized)
			updateGPUProjection();
	}

	// ==================== GL LIFECYCLE METHODS ====================

	/**
	 * Initialize OpenGL resources via GLJPanel. Called once when the GLJPanel is
	 * created.
	 */
	public void initGL(GLAutoDrawable drawable) {
		try {
			if (gpuRenderer == null) {
				gpuRenderer = new GPURendererJOGLWorking();
			}

			gpuRenderer.init(drawable);
			gpuRendererInitialized = true;

			LOGGER.info("GPU renderer initialized via GLJPanel");

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to initialize GPU via GLJPanel", e);
			gpuRendererInitialized = false;
			effectiveRenderMode = RenderMode.CPU;
		}
	}

	/**
	 * Handle viewport changes (resize, etc.)
	 */
	public void reshapeGL(int width, int height) {
		if (gpuRenderer != null && gpuRendererInitialized && chartRectangle != null) {
			updateGPUProjection();
			LOGGER.fine(String.format("GPU viewport reshaped to %dx%d", width, height));
		}
	}

	/**
	 * Main OpenGL rendering method. Called by GLJPanel on every frame.
	 * 
	 * @deprecated replaced by a method with rectangle parameter.
	 */
	public void displayGL(GLAutoDrawable drawable) {
		System.out.println("=== displayGL START ===");
		System.out.println("  gpuRendererInitialized: " + gpuRendererInitialized);
		System.out.println("  gpuRenderer: " + (gpuRenderer != null ? "exists" : "NULL"));

		if (!gpuRendererInitialized || gpuRenderer == null) {
			System.out.println("  -> EARLY EXIT: GPU not initialized");
			return;
		}

		System.out.println("  chartRectangle: " + chartRectangle);
		System.out.println("  data size: " + (data != null ? data.size() : "NULL"));

		if (chartRectangle == null) {
			System.out.println("  -> EARLY EXIT: chartRectangle is null");
			return;
		}

		if (data == null || data.isEmpty()) {
			System.out.println("  -> EARLY EXIT: no data");
			return;
		}

		// Clear the screen first
		GL3 gl = drawable.getGL().getGL3();
		System.out.println("isGL3: " + drawable.getGL().isGL3());
		System.out.println("isGL2ES2: " + drawable.getGL().isGL2ES2());
		System.out.println("isGL2ES3: " + drawable.getGL().isGL2ES3());
		System.out.println("GLSL: " + drawable.getGL().glGetString(GL3.GL_SHADING_LANGUAGE_VERSION));

		gl.glClearColor(0.95f, 0.95f, 0.95f, 1.0f); // Very light gray
		gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
		System.out.println("  Cleared framebuffer");

		performanceLogger.logFrameStart();

		try {
			// Update projection
			System.out.println("  Updating projection:");
			System.out.println("    Viewport: " + (int) chartRectangle.getX() + "," + (int) chartRectangle.getY() + " "
					+ (int) chartRectangle.getWidth() + "x" + (int) chartRectangle.getHeight());
			System.out.println(
					"    World: X[" + worldMinX + ", " + worldMaxX + "], Y[" + worldMinY + ", " + worldMaxY + "]");

			updateGPUProjection();

			// Clear batch
			gpuRenderer.clear();
			System.out.println("  Cleared GPU batch");

			// Calculate point size
			double defaultPointSize = Double.isNaN(pointSize)
					? calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight())
					: pointSize;
			System.out.println("  Default point size: " + defaultPointSize);

			// Batch points
			int pointsAdded = 0;
			for (T t : data) {
				boolean selected = isSelected(t);

				if (drawSelectedLast && selected) {
					continue;
				}

				addPointToGPU(t, defaultPointSize, selected);
				pointsAdded++;
			}
			System.out.println("  Added " + pointsAdded + " unselected points");

			// Draw selected on top
			int selectedAdded = 0;
			if (drawSelectedLast) {
				for (T t : data) {
					if (isSelected(t)) {
						addPointToGPU(t, defaultPointSize, true);
						selectedAdded++;
					}
				}
			}
			System.out.println("  Added " + selectedAdded + " selected points");

			// Check what we batched
			System.out.println("  GPU batch size: " + gpuRenderer.getCurrentBatchSize() + " vertices");

			// Render
			System.out.println("  Calling gpuRenderer.render()...");
			gpuRenderer.render();
			System.out.println("  Render complete!");

			int vertexCount = data.size();
			int drawCalls = gpuRenderer.getDrawCallCount();
			System.out.println("  Draw calls: " + drawCalls);

			performanceLogger.logFrameEnd(vertexCount, drawCalls, RenderMode.GPU);

		} catch (Exception e) {
			System.err.println("  EXCEPTION in displayGL:");
			e.printStackTrace();
			LOGGER.log(Level.WARNING, "GPU rendering failed", e);
		}

		System.out.println("=== displayGL END ===\n");
	}

	/**
	 * Main OpenGL rendering method. Called by GLJPanel on every frame.
	 * 
	 * @param drawable
	 * @param chartRectangle
	 */
	public void displayGL(GLAutoDrawable drawable, Rectangle2D chartRectangle) {
		if (!gpuRendererInitialized || gpuRenderer == null)
			return;

		// Refresh GL context -- GLJPanel may rebuild its backing surface between frames
		gpuRenderer.beginFrame(drawable);

		if (chartRectangle == null) {
			int w = drawable.getSurfaceWidth();
			int h = drawable.getSurfaceHeight();
			chartRectangle = new Rectangle2D.Double(0, 0, w, h);
		}

		if (data == null || data.isEmpty())
			return;

		if (!drawable.getGL().isGL3())
			throw new IllegalStateException("Need GL3 but got: " + drawable.getGL().getClass());

		GL3 gl = drawable.getGL().getGL3();
		gl.glViewport(0, 0, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
		Color bg = getBackgroundPaint() != null ? (Color) getBackgroundPaint() : Color.WHITE;
		gl.glClearColor(bg.getRed() / 255f, bg.getGreen() / 255f, bg.getBlue() / 255f, 1f);
		gl.glClear(GL3.GL_COLOR_BUFFER_BIT);

		int w = drawable.getSurfaceWidth();
		int h = drawable.getSurfaceHeight();

		gpuRenderer.updateProjectionMatrix(w, h, worldMinX, worldMaxX, worldMinY, worldMaxY);
		gpuRenderer.clear();

		double defaultPointSize = Double.isNaN(pointSize)
				? calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight())
				: pointSize;

		// First pass: non-selected points
		for (T t : data) {
			if (drawSelectedLast && isSelected(t))
				continue;
			addPointToGPU(t, defaultPointSize, false);
		}

		// Second pass: selected points on top
		if (drawSelectedLast) {
			for (T t : data) {
				if (isSelected(t))
					addPointToGPU(t, defaultPointSize, true);
			}
		}

		gpuRenderer.render((float) defaultPointSize);

		gl.glFlush();
		gl.glFinish();

		int sw = drawable.getSurfaceWidth();
		int sh = drawable.getSurfaceHeight();
		readbackToImage(gl, sw, sh);
	}

	/**
	 * Reads the current GL FBO into a BufferedImage (TYPE_INT_ARGB, vertically
	 * flipped).
	 */
	private void readbackToImage(GL3 gl, int w, int h) {
		int size = w * h;
		if (readbackBuf == null || readbackBuf.length < size)
			readbackBuf = new int[size];

		// GL_BGRA + GL_UNSIGNED_BYTE on little-endian maps directly to Java
		// TYPE_INT_ARGB
		java.nio.IntBuffer buf = java.nio.ByteBuffer.allocateDirect(size * 4).order(java.nio.ByteOrder.nativeOrder())
				.asIntBuffer();
		gl.glReadPixels(0, 0, w, h, GL3.GL_BGRA, GL3.GL_UNSIGNED_BYTE, buf);
		buf.get(readbackBuf, 0, size);

		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h,
				java.awt.image.BufferedImage.TYPE_INT_ARGB);
		int[] data = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
		// Flip vertically: GL origin is bottom-left, Java top-left
		for (int y = 0; y < h; y++)
			System.arraycopy(readbackBuf, (h - 1 - y) * w, data, y * w, w);

		lastRenderedFrame = img;
	}

	private float calculateDynamicPointSize(int viewportWidth, int viewportHeight, int pointCount) {
		// Base size on viewport size
		float baseSize = Math.min(viewportWidth, viewportHeight) * 0.006f;

		// Adjust based on point density
		float density = (float) pointCount / (viewportWidth * viewportHeight);
		float densityFactor = 1.0f / (1.0f + density * 10000.0f);

		// Clamp between reasonable limits
		float finalSize = baseSize * densityFactor;
		return Math.max(2.0f, Math.min(20.0f, finalSize));
	}

	/**
	 * Cleanup GL resources. Called when GLJPanel is destroyed.
	 */
	public void disposeGL() {
		dispose();
	}

	// ==================== DRAW ====================

	@Override
	public void draw(Graphics2D g2) {
		if (chartRectangle == null || data == null || data.isEmpty())
			return;

		performanceLogger.logFrameStart();

		// For GPU mode, GLJPanel handles rendering via displayGL()
		// Only CPU rendering needs to be handled here
		if (effectiveRenderMode == RenderMode.CPU) {
			performanceLogger.logFrameStart();

			try {
				drawCPU(g2);
			} finally {
				int vertexCount = data.size();
				int drawCalls = data.size();
				performanceLogger.logFrameEnd(vertexCount, drawCalls, RenderMode.CPU);
			}
		}
		// GPU rendering is handled by displayGL() callback from GLJPanel
	}

	// ==================== CPU RENDERING ====================

	private void drawCPU(Graphics2D g2) {
		screenPointsLock.readLock().lock();
		try {
			final int n = data.size();
			final List<Point2D> points = screenPoints;

			// If screenPoints are currently being rebuilt, skip this frame
			if (points == null || points.size() != n)
				return;

			final Color oldColor = g2.getColor();

			// Get point size once
			double ps = this.pointSize;
			if (Double.isNaN(ps))
				ps = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());

			// Cache flags
			final boolean hasSelection = (selectedFunction != null);
			final boolean twoPhase = drawSelectedLast && hasSelection;

			// Collect selected indices for second pass
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

	// ==================== GPU RENDERING ====================

	/**
	 * @deprecated Since GPU rendering now happens in displayGL(), the drawGPU()
	 *             method is no longer called from draw(). Kept as a fallback
	 * @param g2
	 */
	private void drawGPU(Graphics2D g2) {
		// Try to initialize GPU if not already done
		if (!ensureGPUInitialized()) {
			LOGGER.fine("GPU not available, using CPU rendering");
			effectiveRenderMode = RenderMode.CPU;
			drawCPU(g2);
			return;
		}

		try {
			// Update GPU projection if rectangle changed
			updateGPUProjection();

			// Clear previous batch
			gpuRenderer.clear();

			// Calculate default point size
			double defaultPointSize = Double.isNaN(pointSize)
					? calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight())
					: pointSize;

			// First pass: draw unselected points
			for (T t : data) {
				boolean selected = isSelected(t);
				if (drawSelectedLast && selected) {
					continue;
				}
				addPointToGPU(t, defaultPointSize, selected);
			}

			// Second pass: draw selected points on top
			if (drawSelectedLast) {
				for (T t : data) {
					if (isSelected(t)) {
						addPointToGPU(t, defaultPointSize, true);
					}
				}
			}

			// Render all batched geometry
			gpuRenderer.render();

		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "GPU rendering failed, falling back to CPU", e);
			effectiveRenderMode = RenderMode.CPU;
			drawCPU(g2);
		}
	}

	private void addPointToGPU(T t, double defaultPointSize, boolean selected) {
		double worldX = worldPositionMappingX.apply(t);
		double worldY = worldPositionMappingY.apply(t);

		if (Double.isNaN(worldX) || Double.isNaN(worldY)) {
			return;
		}

		Paint paint = colorMapping != null ? colorMapping.apply(t) : null;
		Color color = extractColor(paint);

		if (overplottingMitigation && color != null) {
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255));
		}

		double size = sizeEncodingFunction != null ? sizeEncodingFunction.apply(t) : defaultPointSize;
		if (Double.isNaN(size)) {
			size = defaultPointSize;
		}

		// CPU painter treats 'size' as a radius and draws circles with:
		// colored diameter = 2 * size * 1.33
		// halo diameter = 2 * max(size * 1.66, size + 2)
		// gl_PointSize is a diameter, so convert accordingly.
		double coloredDiameter = size * 1.33 * 2.0;

		// Selection halo: black outline larger than the colored point
		if (selected) {
			double haloDiameter = Math.max(size * 1.66, size + 2) * 2.0;
			Color selectionColor = extractColor(selectionPaint);
			gpuRenderer.addPointSprite((float) worldX, (float) worldY, (float) haloDiameter, selectionColor);
		}

		// Main point
		gpuRenderer.addPointSprite((float) worldX, (float) worldY, (float) coloredDiameter, color);
	}

	private void updateGPUProjection() {
		if (chartRectangle == null)
			return;

		// worldMinX/Max/Y start out as the static full-data range (computeWorldBounds),
		// but zoom/pan changes the *visible window* by mutating xPositionEncodingFunction
		// /yPositionEncodingFunction directly (via the panel's own axis setters) -- so the
		// projection matrix (and every screen<->world helper below that reads these same
		// fields: worldToScreenX/Y, screenToWorldX/Y, the world-radius calc) must be
		// rebuilt from the live functions on every call, not from the stale construction-
		// time values, or zooming would move the axes without ever moving the points.
		worldMinX = xPositionEncodingFunction.getMinWorldValue().doubleValue();
		worldMaxX = xPositionEncodingFunction.getMaxWorldValue().doubleValue();
		worldMinY = yPositionEncodingFunction.getMinWorldValue().doubleValue();
		worldMaxY = yPositionEncodingFunction.getMaxWorldValue().doubleValue();

		gpuRenderer.updateProjectionMatrix((int) chartRectangle.getWidth(), (int) chartRectangle.getHeight(), worldMinX,
				worldMaxX, // X range
				worldMinY, worldMaxY // Y range - should be min first, max second
		);
	}

	private boolean isSelected(T t) {
		if (selectedFunction == null) {
			return false;
		}
		Boolean result = selectedFunction.apply(t);
		return result != null && result;
	}

	private Color extractColor(Paint paint) {
		if (paint instanceof Color) {
			return (Color) paint;
		}
		return Color.GRAY; // Default fallback
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

		// Stroke width
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

		// Update rendering backend
		if (effectiveRenderMode == RenderMode.GPU && gpuRendererInitialized) {
			updateGPUProjection();
		} else {
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
		if (!tooltipping || p == null) {
			return null;
		}

		final Rectangle2D cr = chartRectangle;
		if (cr == null) {
			return null;
		}

		if (refreshingDataPoints) {
			return null;
		}

		// For GPU rendering, we still use screen coordinates for hit testing
		if (effectiveRenderMode == RenderMode.GPU) {
			return getTooltipGPU(p);
		} else {
			return getTooltipCPU(p);
		}
	}

	private ChartPainter getTooltipCPU(Point p) {
		screenPointsLock.readLock().lock();
		try {
			if (data == null || data.isEmpty()) {
				return null;
			}
			if (screenPoints == null || screenPoints.isEmpty()) {
				return null;
			}

			final int n = data.size();
			if (screenPoints.size() != n) {
				return null;
			}

			final double maxRadius = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());
			final double px = p.getX();
			final double py = p.getY();

			double bestDist = Double.POSITIVE_INFINITY;
			T bestElement = null;

			for (int i = 0; i < n; i++) {
				final Point2D sp = screenPoints.get(i);
				if (sp == null) {
					continue;
				}

				final double dx = Math.abs(sp.getX() - px);
				if (dx >= maxRadius) {
					continue;
				}

				final double dy = Math.abs(sp.getY() - py);
				if (dy >= maxRadius) {
					continue;
				}

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
		// For GPU rendering, transform screen point back to world space
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
		if (element == null) {
			return null;
		}

		String toolTipString;
		if (toolTipMapping != null) {
			toolTipString = toolTipMapping.apply(element);
		} else {
			Double wx = worldPositionMappingX != null ? worldPositionMappingX.apply(element) : null;
			Double wy = worldPositionMappingY != null ? worldPositionMappingY.apply(element) : null;

			if (wx == null || wy == null || wx.isNaN() || wy.isNaN()) {
				return null;
			}

			toolTipString = MathFunctions.round(wx.doubleValue(), 2) + ", " + MathFunctions.round(wy.doubleValue(), 2);
		}

		if (toolTipString == null) {
			return null;
		}

		StringPainter stringPainter = new StringPainter(toolTipString);

		Rectangle2D rect = ToolTipTools.createToolTipRectangle(chartRectangle, p, toolTipWidth, toolTipHeight);
		stringPainter.setRectangle(rect);

		stringPainter.setBackgroundPaint(ColorTools.setAlpha(Color.DARK_GRAY, 0.5f));
		stringPainter.setFontColor(Color.WHITE);
		stringPainter.setFontSize(15);

		return stringPainter;
	}

	// ==================== COORDINATE TRANSFORMATION (for GPU mode)
	// ====================

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

	// ==================== SELECTION ====================

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
			// For GPU: use world coordinates directly
			for (T t : data) {
				double worldX = worldPositionMappingX.apply(t);
				double worldY = worldPositionMappingY.apply(t);

				double screenX = worldToScreenX(worldX);
				double screenY = worldToScreenY(worldY);

				if (shape.contains(screenX, screenY))
					elements.add(t);
			}
		} else {
			// For CPU: use screen coordinates
			for (T t : data) {
				double worldX = worldPositionMappingX.apply(t).doubleValue();
				double worldY = worldPositionMappingY.apply(t).doubleValue();

				double screenX = xPositionEncodingFunction.apply(worldX);
				double screenY = yPositionEncodingFunction.apply(worldY);

				if (shape.contains(screenX, screenY))
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
			// GPU mode
			for (T t : data) {
				double worldX = worldPositionMappingX.apply(t);
				double worldY = worldPositionMappingY.apply(t);

				double screenX = worldToScreenX(worldX);
				double screenY = worldToScreenY(worldY);

				if (circle.contains(screenX, screenY))
					elements.add(t);
			}
		} else {
			// CPU mode
			for (T t : data) {
				double worldX = worldPositionMappingX.apply(t);
				double worldY = worldPositionMappingY.apply(t);

				double screenX = xPositionEncodingFunction.apply(worldX);
				double screenY = yPositionEncodingFunction.apply(worldY);

				if (circle.contains(screenX, screenY))
					elements.add(t);
			}
		}

		return elements;
	}

	// ==================== PERFORMANCE LOGGING ====================

	/**
	 * Enable performance logging to track rendering metrics.
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
	 * Check if performance logging is enabled.
	 */
	public boolean isPerformanceLoggingEnabled() {
		return performanceLogger.isEnabled();
	}

	/**
	 * Reset performance statistics.
	 */
	public void resetPerformanceStatistics() {
		performanceLogger.reset();
	}

	/**
	 * Log current performance statistics.
	 */
	public void logPerformanceStatistics() {
		performanceLogger.logStatistics();
	}

	/**
	 * Get the performance logger for advanced control.
	 */
	public PerformanceLogger getPerformanceLogger() {
		return performanceLogger;
	}

	// ==================== CLEANUP ====================

	/**
	 * Dispose of GPU resources. Call when the painter is no longer needed.
	 */
	public void dispose() {
		if (gpuRenderer != null) {
			gpuRenderer.dispose();
			gpuRenderer = null;
			gpuRendererInitialized = false;
			LOGGER.info("GPU renderer disposed");
		}
	}

	// ==================== GETTERS/SETTERS ====================

	@Override
	public boolean isAlphaAdjustment() {
		return overplottingMitigation;
	}

	@Override
	public void setAlphaAdjustment(boolean dynamicAlphaAdjustment) {
		this.overplottingMitigation = dynamicAlphaAdjustment;
		if (effectiveRenderMode == RenderMode.CPU) {
			refreshDataPoints();
		}
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
