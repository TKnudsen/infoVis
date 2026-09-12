package com.github.TKnudsen.infoVis.view.painters.scatterplot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.TKnudsen.infoVis.view.gpu.GPURendererJOGLIndexed;
import com.github.TKnudsen.infoVis.view.gpu.RenderMode;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * <p>
 * Paints a scatter plot using visual mapping functions to map data (represented
 * as T) into the visual space. Supports both CPU-based (Java2D) and GPU-based
 * (OpenGL, via {@link GPURendererJOGLIndexed}) rendering modes for optimal
 * performance.
 * </p>
 *
 * <p>
 * Everything shared with {@link ScatterPlotSpriteGPUPainter} lives in
 * {@link AbstractGPUScatterPlotPainter} (CPU rendering shared further up, in
 * {@link AbstractScatterPlotPainter}); this class only adds what depends on
 * the concretely-typed {@link #gpuRenderer} -- initialization, teardown,
 * per-frame GL rendering, and per-point GPU batching -- plus the two places
 * this painter's own rendering pipeline diverges from the sprite painter's
 * ({@link #onRectangleUpdated()}'s proactive GPU-projection push, and
 * {@link #updateWorldBounds()}, which the sprite painter does not have).
 * </p>
 *
 * @version 2.01 revised in August 2026
 * @since 2025
 */
public class ScatterPlotIndexedGPUPainter<T> extends AbstractGPUScatterPlotPainter<T> {

	private static final Logger LOGGER = Logger.getLogger(ScatterPlotIndexedGPUPainter.class.getName());

	private GPURendererJOGLIndexed gpuRenderer;

	public ScatterPlotIndexedGPUPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
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

	// ==================== GL LIFECYCLE METHODS ====================

	/**
	 * Initialize OpenGL resources via GLJPanel. Called once when the GLJPanel is
	 * created.
	 */
	public void initGL(GLAutoDrawable drawable) {
		try {
			if (gpuRenderer == null) {
				gpuRenderer = new GPURendererJOGLIndexed();
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

		// worldMinX/Max/Y start out as the static full-data range (computeWorldBounds),
		// but zoom/pan changes the *visible window* by mutating xPositionEncodingFunction
		// /yPositionEncodingFunction directly (via the panel's own axis setters, not via
		// setRectangle()) -- so the projection matrix (and every screen<->world helper that
		// reads these same fields: worldToScreenX/Y, screenToWorldX/Y, the world-radius
		// calc) must be rebuilt from the live functions on every frame, or zooming would
		// move the axes without ever moving the points. Matches ScatterPlotSpriteGPUPainter's
		// displayGL(), which already does this correctly.
		worldMinX = xPositionEncodingFunction.getMinWorldValue().doubleValue();
		worldMaxX = xPositionEncodingFunction.getMaxWorldValue().doubleValue();
		worldMinY = yPositionEncodingFunction.getMinWorldValue().doubleValue();
		worldMaxY = yPositionEncodingFunction.getMaxWorldValue().doubleValue();

		gpuRenderer.updateProjectionMatrix(w, h, worldMinX, worldMaxX, worldMinY, worldMaxY);
		gpuRenderer.clear();

		double defaultPointSize = Double.isNaN(getPointSize())
				? calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight())
				: getPointSize();

		// First pass: everything neither selected nor highlighted
		for (T t : data) {
			if (isDrawSelectedLast() && (isSelected(t) || isHighlighted(t)))
				continue;
			addPointToGPU(t, defaultPointSize, false, false);
		}

		// Second pass: selected/highlighted points on top
		if (isDrawSelectedLast()) {
			for (T t : data) {
				boolean selected = isSelected(t);
				boolean highlighted = isHighlighted(t);
				if (selected || highlighted)
					addPointToGPU(t, defaultPointSize, selected, highlighted);
			}
		}

		gpuRenderer.render((float) defaultPointSize);

		// No explicit glFlush()/glFinish() here -- glReadPixels() below is itself
		// a synchronous, blocking call that already waits for all prior rendering
		// affecting the read region to complete (part of its own defined
		// semantics), so an explicit full-pipeline stall first is pure redundancy,
		// not a correctness requirement.
		int sw = drawable.getSurfaceWidth();
		int sh = drawable.getSurfaceHeight();
		readbackToImage(gl, sw, sh);
	}

	/**
	 * Cleanup GL resources. Called by JOGL's own dispose(GLAutoDrawable)
	 * callback when the GLJPanel is destroyed -- the drawable it passes in
	 * guarantees the GL context is current, unlike {@link #dispose()}.
	 */
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

	// ==================== GPU RENDERING ====================

	private void addPointToGPU(T t, double defaultPointSize, boolean selected, boolean highlighted) {
		double worldX = getWorldPositionMappingX().apply(t);
		double worldY = getWorldPositionMappingY().apply(t);

		if (Double.isNaN(worldX) || Double.isNaN(worldY)) {
			return;
		}

		// Fixed (code review finding #28): was `colorMapping != null ?
		// colorMapping.apply(t) : null`, which fell all the way through to
		// extractColor(null) -> Color.GRAY -- unlike ScatterPlotSpriteGPUPainter
		// and the CPU painter, which both fall back to getPaint() instead. See
		// ScatterPlotPainterHierarchyCharacterizationTest's
		// nullColorMapping_indexedGpuPainterFallsBackToGray test, updated
		// alongside this fix.
		Paint paint = getColorMapping() != null ? getColorMapping().apply(t) : getPaint();
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

		// Highlight halo: drawn first (i.e. outermost/underneath), larger than the
		// selection halo, so a point that is both selected and highlighted still
		// shows the highlight as an outer ring -- mirrors
		// AbstractScatterPlotPainter.drawPointHighlighted's CPU-side layering
		if (highlighted) {
			double highlightHaloDiameter = Math.max(size * 2.0, size + 4) * 2.0;
			Color highlightColor = extractColor(getHighlightPaint());
			gpuRenderer.addPointSprite((float) worldX, (float) worldY, (float) highlightHaloDiameter, highlightColor);
		}

		// Selection halo: black outline larger than the colored point
		if (selected) {
			double haloDiameter = Math.max(size * 1.66, size + 2) * 2.0;
			Color selectionColor = extractColor(getSelectionPaint());
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

	/**
	 * Proactively pushes the new rectangle into the GPU projection matrix when
	 * GPU-active, so a resize is reflected immediately rather than waiting for
	 * the next frame. {@link ScatterPlotSpriteGPUPainter} does not override this
	 * (recomputes its projection unconditionally at the top of every
	 * {@code displayGL} call instead), which is why this hook is a no-op by
	 * default on {@link AbstractScatterPlotPainter}.
	 */
	@Override
	protected void onRectangleUpdated() {
		if (effectiveRenderMode == RenderMode.GPU && gpuRendererInitialized) {
			updateGPUProjection();
		}
	}

	/**
	 * Recompute world bounds and update axes. Call this when layout positions have
	 * actually changed.
	 */
	public void updateWorldBounds() {
		computeWorldBounds();

		if (gpuRendererInitialized)
			updateGPUProjection();
	}

	// ==================== CLEANUP ====================

	/**
	 * Call when the painter is no longer needed. Does not itself free GL
	 * resources -- ordinary application cleanup code (e.g. a panel's own
	 * dispose()) has no guarantee a GL context is current, and issuing GL
	 * calls without one crashes with "No OpenGL context current". Actual GPU
	 * cleanup happens in {@link #disposeGL(GLAutoDrawable)}, invoked by JOGL
	 * itself with a guaranteed-current context when the GLJPanel is destroyed.
	 */
	public void dispose() {
		gpuRenderer = null;
		gpuRendererInitialized = false;
	}
}
