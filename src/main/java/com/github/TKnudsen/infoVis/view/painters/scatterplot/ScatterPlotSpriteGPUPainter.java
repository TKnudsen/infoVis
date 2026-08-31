package com.github.TKnudsen.infoVis.view.painters.scatterplot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.TKnudsen.infoVis.view.gpu.GPURendererJOGLGLJPanel;
import com.github.TKnudsen.infoVis.view.gpu.RenderMode;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * <p>
 * Paints a scatter plot using visual mapping functions to map data (represented
 * as T) into the visual space. Supports both CPU-based (Java2D) and GPU-based
 * (OpenGL, via {@link GPURendererJOGLGLJPanel}) rendering modes for optimal
 * performance.
 * </p>
 *
 * <p>
 * Everything shared with {@link ScatterPlotIndexedGPUPainter} lives in
 * {@link AbstractGPUScatterPlotPainter} (CPU rendering shared further up, in
 * {@link AbstractScatterPlotPainter}); this class only adds what depends on
 * the concretely-typed {@link #gpuRenderer} -- initialization, teardown,
 * per-frame GL rendering, and per-point GPU batching.
 * </p>
 *
 * @version 3.01
 * @since 2018
 */
public class ScatterPlotSpriteGPUPainter<T> extends AbstractGPUScatterPlotPainter<T> {

	private static final Logger LOGGER = Logger.getLogger(ScatterPlotSpriteGPUPainter.class.getName());

	private GPURendererJOGLGLJPanel gpuRenderer;

	public ScatterPlotSpriteGPUPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		super(data, colorMapping, worldPositionMappingX, worldPositionMappingY);

		this.setRenderMode(RenderMode.GPU);

		LOGGER.info(String.format("ScatterPlotPainter initialized with %d data points, render mode: %s", data.size(),
				getRenderMode()));
	}

	@Override
	protected boolean isGpuRendererPresent() {
		return gpuRenderer != null;
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

	    double ps = Double.isNaN(getPointSize()) ? calculatePointSize(width, height) : getPointSize();

	    batchPointsForGPU(ps);

	    gpuRenderer.render(drawable, (float) ps);

	    // No explicit glFlush()/glFinish() here -- glReadPixels() below is itself
	    // a synchronous, blocking call that already waits for all prior rendering
	    // affecting the read region to complete (part of its own defined
	    // semantics), so an explicit full-pipeline stall first is pure redundancy,
	    // not a correctness requirement.
	    readbackToImage(gl, width, height);
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
		if (isDrawSelectedLast() && selectedFunction != null) {
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
		double worldX = getWorldPositionMappingX().apply(t);
		double worldY = getWorldPositionMappingY().apply(t);

		// Skip invalid coordinates
		if (Double.isNaN(worldX) || Double.isNaN(worldY)) {
			return;
		}

		// Get color
		Paint paint = getColorMapping() != null ? getColorMapping().apply(t) : getPaint();
		Color color = extractColor(paint);

		// Apply alpha for overplotting mitigation
		if (overplottingMitigation && color != null) {
			int alphaChannel = (int) (this.alpha * 255);
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaChannel);
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
		if (selected && getSelectionPaint() != null) {
			double haloDiameter = Math.max(size * 1.66, size + 2) * 2.0;
			Color selectionColor = extractColor(getSelectionPaint());
			gpuRenderer.addPointSprite((float) worldX, (float) worldY, (float) haloDiameter, selectionColor);
		}

		// Draw the actual point
		gpuRenderer.addPointSprite((float) worldX, (float) worldY, (float) coloredDiameter, color);
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

	// ==================== CLEANUP ====================

	public void dispose() {
		// GPU objects disposed in disposeGL(drawable)
		gpuRenderer = null;
		gpuRendererInitialized = false;
	}
}
