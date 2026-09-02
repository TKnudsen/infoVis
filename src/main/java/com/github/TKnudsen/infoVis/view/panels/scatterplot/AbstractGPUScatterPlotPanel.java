package com.github.TKnudsen.infoVis.view.panels.scatterplot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.gpu.PerformanceLogger;
import com.github.TKnudsen.infoVis.view.gpu.RenderMode;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.AbstractGPUScatterPlotPainter;
import com.github.TKnudsen.infoVis.view.tools.OverplottingMitigationTools;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;

/**
 * <p>
 * Shared {@code GLJPanel} scaffolding for {@link ScatterPlotIndexedGPU} and
 * {@link ScatterPlotSpriteGPU}: GL panel construction, the readback-frame
 * paint pipeline, GL-render scheduling/coalescing, mouse-event forwarding
 * from the GL surface to this panel, and the full delegate-to-painter getter
 * /setter surface -- all confirmed byte-identical between the two panels by
 * direct comparison (once both are read through the single {@link #glCanvas}
 * field name; {@link ScatterPlotSpriteGPU} additionally keeps a {@code
 * glPanel} field as a pure alias, for compatibility with anything that
 * referred to it by that name before this extraction).
 * </p>
 *
 * <p>
 * Four one-line hooks cover the only real divergences, all of which trace
 * back to the concrete GPU painters' own diverging {@code reshapeGL}/
 * {@code displayGL} signatures (kept subclass-owned deliberately, see
 * {@link AbstractGPUScatterPlotPainter}'s class-level javadoc) plus the one
 * genuinely different interaction behavior:
 * </p>
 * <ul>
 * <li>{@link #invokeInitGL(GLAutoDrawable)}, {@link #invokeDisposeGL(GLAutoDrawable)},
 * {@link #invokeReshapeGL(GLAutoDrawable, int, int, int, int)},
 * {@link #invokeDisplayGL(GLAutoDrawable)} -- forward to the concretely-typed
 * painter's own GL lifecycle methods.</li>
 * <li>{@link #onGLCanvasDragged(MouseEvent)} -- {@code ScatterPlotIndexedGPU}
 * schedules an async GL render; {@code ScatterPlotSpriteGPU} forces an
 * immediate synchronous repaint instead (commented "CRITICAL" in the
 * pre-extraction source) -- a real, deliberate difference, not flattened
 * here.</li>
 * </ul>
 */
public abstract class AbstractGPUScatterPlotPanel<T> extends AbstractScatterPlotPanel<T> {

	private static final long serialVersionUID = 1L;

	// ==================== CORE COMPONENTS ====================

	public GLJPanel glCanvas;
	protected Rectangle2D currentChartRectangle = null;

	private boolean glRenderScheduled = false;

	// Re-entrancy guard for GLJPanel.paintComponent.
	// JOGL's super.paintComponent() internally calls paintImmediately(parent) which
	// re-enters glCanvas.paintComponent via paintChildren. The inner call must skip
	// the super call (and thus skip another GL render) but still draw the frame.
	private boolean inGlPaintComponent = false;

	// Track painters that need to render on top of GLCanvas
	private List<ChartPainter> overlayPainters = new ArrayList<>();

	protected AbstractGPUScatterPlotPanel(Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		super(colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

	/**
	 * @return the underlying GPU scatterplot painter, typed to the shared
	 *         ancestor; each concrete subclass's existing (narrower-typed)
	 *         accessor already satisfies this via a covariant return type
	 */
	protected abstract AbstractGPUScatterPlotPainter<T> getScatterPlotPainter();

	// ==================== GL LIFECYCLE HOOKS ====================

	protected abstract void invokeInitGL(GLAutoDrawable drawable);

	protected abstract void invokeDisposeGL(GLAutoDrawable drawable);

	protected abstract void invokeReshapeGL(GLAutoDrawable drawable, int x, int y, int w, int h);

	protected abstract void invokeDisplayGL(GLAutoDrawable drawable);

	/**
	 * Called when the mouse is dragged over {@link #glCanvas}. Deliberately
	 * different per subclass -- see the class-level javadoc.
	 */
	protected abstract void onGLCanvasDragged(MouseEvent e);

	// ==================== GL PANEL CONSTRUCTION ====================

	protected GLJPanel createGLPanel() {
		GLProfile profile = GLProfile.getMaxProgrammableCore(true);
		GLCapabilities caps = new GLCapabilities(profile);

		// Override paintComponent so that after GL initialization, any Swing-initiated
		// paint of the GLJPanel is a no-op. This prevents an unreliable FBO->Swing
		// blit (observed to render solid black on at least one GPU/driver
		// combination) from painting over the glReadPixels image drawn below.
		GLJPanel panel = new GLJPanel(caps) {
			@Override
			protected void paintComponent(Graphics g) {
				if (!getScatterPlotPainter().isGpuRendererInitialized()) {
					super.paintComponent(g); // first paint: initializes GL context
					return;
				}
				// Re-entrancy guard: JOGL's super.paintComponent internally calls
				// paintImmediately on this component, which would re-enter here.
				if (inGlPaintComponent)
					return;
				inGlPaintComponent = true;
				try {
					// Pass the REAL screen Graphics so JOGL's pipeline fires:
					// super.paintComponent(g) -> displayGL() -> readbackToImage() ->
					// lastRenderedFrame
					// JOGL's own blit to g may draw black (broken on this system), but we
					// immediately overdraw it with the correct readback image.
					super.paintComponent(g);
				} finally {
					inGlPaintComponent = false;
				}
				// Overdraw JOGL's broken blit with the pixel-correct readback frame.
				java.awt.image.BufferedImage frame = getScatterPlotPainter().getLastRenderedFrame();
				if (frame != null) {
					g.drawImage(frame, 0, 0, getWidth(), getHeight(), null);
				}
			}
		};
		panel.setOpaque(false);
		panel.setIgnoreRepaint(true);

		panel.addGLEventListener(new GLEventListener() {
			@Override
			public void init(GLAutoDrawable d) {
				invokeInitGL(d);
			}

			@Override
			public void dispose(GLAutoDrawable d) {
				invokeDisposeGL(d);
			}

			@Override
			public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
				invokeReshapeGL(d, x, y, w, h);
				// GL surface has been resized -- schedule a fresh render at the new size.
				javax.swing.SwingUtilities.invokeLater(() -> AbstractGPUScatterPlotPanel.this.repaint());
			}

			@Override
			public void display(GLAutoDrawable d) {
				Color panelBg = getBackground();
				if (panelBg != null) {
					getScatterPlotPainter().setBackgroundPaint(panelBg);
				}
				invokeDisplayGL(d);
			}
		});

		return panel;
	}

	@Override
	public void addChartPainter(ChartPainter chartPainter) {
		// Selection handlers and other overlays go to overlay list
		// (they have no rectangle and just draw interaction feedback)
		if (chartPainter.getClass().getName().contains("$")) { // Anonymous inner class from ScatterPlots
			overlayPainters.add(chartPainter);
		} else {
			super.addChartPainter(chartPainter);
		}
	}

	@Override
	public void addChartPainter(ChartPainter chartPainter, boolean allowsXAxisOverlap, boolean allowsYAxisOverlap) {
		// Same logic
		if (chartPainter.getClass().getName().contains("$")) {
			overlayPainters.add(chartPainter);
		} else {
			super.addChartPainter(chartPainter, allowsXAxisOverlap, allowsYAxisOverlap);
		}
	}

	// ==================== RENDERING MODE MANAGEMENT ====================

	@Override
	protected void updatePainterRectangles() {
		super.updatePainterRectangles();

		Rectangle2D cr = getChartRectangleLayout().getChartRectangle();

		if (cr != null) {
			this.currentChartRectangle = cr;

			// Set rectangle for painter (used for CPU fallback and coordinate transforms)
			getScatterPlotPainter().setRectangle(cr);

			// Position GLCanvas EXACTLY over chart area (not axes)
			int x = (int) Math.round(cr.getX() + 1);
			int y = (int) Math.round(cr.getY());
			int w = (int) Math.round(cr.getWidth());
			int h = (int) Math.round(cr.getHeight());

			glCanvas.setBounds(x, y, w, h);
			// Schedule a GL re-render so the frame is produced at the new size.
			// paintComponent will scale the stale frame to fit until the new frame arrives.
			scheduleGLRender();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		if (getBackground() != null) {
			g2.setColor(getBackground());
			g2.fillRect(0, 0, getWidth(), getHeight());
		}

		for (ChartPainter painter : getChartPainters()) {
			if (painter != getScatterPlotPainter()) {
				painter.draw(g2);
			}
		}

		// Draw the last GL-rendered frame. Rendering is decoupled from painting:
		// display() is only called from doGLRender(), never from here.
		// The frame is drawn scaled to the current chart rectangle so that even a
		// stale frame (from before a resize) shows points at proportionally correct
		// positions instead of at fixed old pixel coordinates.
		if (glCanvas != null && getScatterPlotPainter().isGpuRendererInitialized()) {
			java.awt.image.BufferedImage frame = getScatterPlotPainter().getLastRenderedFrame();
			if (frame != null && currentChartRectangle != null) {
				g2.drawImage(frame, (int) currentChartRectangle.getX(), (int) currentChartRectangle.getY(),
						(int) currentChartRectangle.getWidth(), (int) currentChartRectangle.getHeight(), null);
			}
		}
	}

	@Override
	protected void paintChildren(Graphics g) {
		// Always paint children -- this is what triggers glCanvas.paintComponent,
		// which is where GL rendering actually happens. Skipping it after init was
		// the bug: doGLRender/display() caused a parent repaint, but paintChildren
		// was a no-op so displayGL never fired again after the first frame.
		super.paintChildren(g);
		if (getScatterPlotPainter().isGpuRendererInitialized()) {
			// After the glCanvas has painted (and updated lastRenderedFrame),
			// draw the correct frame over whatever glCanvas put on screen.
			java.awt.image.BufferedImage frame = getScatterPlotPainter().getLastRenderedFrame();
			if (frame != null && currentChartRectangle != null) {
				Graphics2D g2 = (Graphics2D) g;
				g2.drawImage(frame, (int) currentChartRectangle.getX(), (int) currentChartRectangle.getY(),
						(int) currentChartRectangle.getWidth(), (int) currentChartRectangle.getHeight(), null);
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g); // This calls paintComponent and paints children (including GLCanvas)

		// Now paint axes aside of the GLCanvas
		Graphics2D g2 = (Graphics2D) g;

		// Paint axes
		if (xAxisPainter != null) {
			xAxisPainter.draw(g2);
		}
		if (yAxisPainter != null) {
			yAxisPainter.draw(g2);
		}

		for (ChartPainter overlayPainter : overlayPainters) {
			overlayPainter.draw(g2);
		}
	}

	@Override
	public void repaint() {
		scheduleGLRender();
		super.repaint();
	}

	/**
	 * Schedules a single GL render via invokeLater if one is not already pending.
	 * Coalesces rapid repaint() calls into one render.
	 */
	private void scheduleGLRender() {
		if (!glRenderScheduled) {
			glRenderScheduled = true;
			javax.swing.SwingUtilities.invokeLater(this::doGLRender);
		}
	}

	/**
	 * Renders the GL scene and shows the result. Called from the EDT but NOT from
	 * within a Swing paint cycle, so JOGL's internal paintImmediately call works
	 * cleanly without re-entrancy or double-buffer corruption.
	 */
	protected void doGLRender() {
		glRenderScheduled = false;
		if (glCanvas != null && getScatterPlotPainter().isGpuRendererInitialized()) {
			// display() -> glCanvas.paintComponent() -> super.paintComponent() ->
			// displayGL() -> readbackToImage() -> lastRenderedFrame updated.
			glCanvas.display();
			// Now draw the freshly updated lastRenderedFrame to screen.
			super.repaint();
		}
	}

	/**
	 * Set the rendering mode (CPU, GPU, or AUTO).
	 *
	 * <ul>
	 * <li>CPU: Traditional Java2D rendering (best for &lt; 10,000 points)</li>
	 * <li>GPU: OpenGL-accelerated rendering (best for &gt; 10,000 points)</li>
	 * <li>AUTO: Automatically selects based on dataset size</li>
	 * </ul>
	 *
	 * @param mode the rendering mode to use
	 */
	public void setRenderMode(RenderMode mode) {
		getScatterPlotPainter().setRenderMode(mode);
		repaint();
	}

	/**
	 * Get the current rendering mode setting.
	 *
	 * @return the configured render mode (may differ from effective mode)
	 */
	public RenderMode getRenderMode() {
		return getScatterPlotPainter().getRenderMode();
	}

	/**
	 * Get the effective rendering mode currently being used. This may differ from
	 * getRenderMode() if AUTO mode is selected or if GPU initialization failed.
	 *
	 * @return the actual render mode being used
	 */
	public RenderMode getEffectiveRenderMode() {
		return getScatterPlotPainter().getEffectiveRenderMode();
	}

	// ==================== PERFORMANCE LOGGING ====================

	/**
	 * Enable performance logging to track rendering metrics (FPS, draw calls,
	 * etc.). Useful for benchmarking and identifying performance bottlenecks.
	 */
	public void enablePerformanceLogging() {
		getScatterPlotPainter().enablePerformanceLogging();
	}

	/**
	 * Disable performance logging.
	 */
	public void disablePerformanceLogging() {
		getScatterPlotPainter().disablePerformanceLogging();
	}

	/**
	 * Check if performance logging is currently enabled.
	 *
	 * @return true if performance logging is active
	 */
	public boolean isPerformanceLoggingEnabled() {
		return getScatterPlotPainter().isPerformanceLoggingEnabled();
	}

	/**
	 * Reset accumulated performance statistics.
	 */
	public void resetPerformanceStatistics() {
		getScatterPlotPainter().resetPerformanceStatistics();
	}

	/**
	 * Log current performance statistics to the console.
	 */
	public void logPerformanceStatistics() {
		getScatterPlotPainter().logPerformanceStatistics();
	}

	/**
	 * Get direct access to the performance logger for advanced control.
	 *
	 * @return the performance logger instance
	 */
	public PerformanceLogger getPerformanceLogger() {
		return getScatterPlotPainter().getPerformanceLogger();
	}

	/**
	 * Get a formatted performance report.
	 *
	 * @return multi-line performance statistics report
	 */
	public String getPerformanceReport() {
		PerformanceLogger logger = getScatterPlotPainter().getPerformanceLogger();
		return logger != null ? logger.getStatisticsReport() : "No statistics available";
	}

	// ==================== SELECTION INTERFACE ====================

	protected void forwardMouseEventsFromGLCanvas() {
		MouseAdapter forwarder = new MouseAdapter() {
			private MouseEvent translateEvent(MouseEvent e) {
				Point glCanvasPos = glCanvas.getLocation();
				return new MouseEvent(AbstractGPUScatterPlotPanel.this, e.getID(), e.getWhen(), e.getModifiersEx(),
						e.getX() + glCanvasPos.x, e.getY() + glCanvasPos.y, e.getClickCount(), e.isPopupTrigger(),
						e.getButton());
			}

			@Override
			public void mousePressed(MouseEvent e) {
				for (MouseListener ml : getMouseListeners()) {
					ml.mousePressed(translateEvent(e));
				}
				AbstractGPUScatterPlotPanel.this.repaint(); // Force repaint
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				for (MouseListener ml : getMouseListeners()) {
					ml.mouseReleased(translateEvent(e));
				}
				AbstractGPUScatterPlotPanel.this.repaint(); // Force repaint
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				for (MouseMotionListener mml : getMouseMotionListeners()) {
					mml.mouseDragged(translateEvent(e));
				}
				onGLCanvasDragged(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				for (MouseMotionListener mml : getMouseMotionListeners()) {
					mml.mouseMoved(translateEvent(e));
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				for (MouseListener ml : getMouseListeners()) {
					ml.mouseClicked(translateEvent(e));
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				for (MouseListener ml : getMouseListeners()) {
					ml.mouseEntered(translateEvent(e));
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				for (MouseListener ml : getMouseListeners()) {
					ml.mouseExited(translateEvent(e));
				}
			}

			@Override
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
				Point glCanvasPos = glCanvas.getLocation();
				java.awt.event.MouseWheelEvent translated = new java.awt.event.MouseWheelEvent(
						AbstractGPUScatterPlotPanel.this, e.getID(), e.getWhen(), e.getModifiersEx(),
						e.getX() + glCanvasPos.x, e.getY() + glCanvasPos.y, e.getClickCount(), e.isPopupTrigger(),
						e.getScrollType(), e.getScrollAmount(), e.getWheelRotation());
				for (java.awt.event.MouseWheelListener mwl : getMouseWheelListeners()) {
					mwl.mouseWheelMoved(translated);
				}
			}
		};

		glCanvas.addMouseListener(forwarder);
		glCanvas.addMouseMotionListener(forwarder);
		glCanvas.addMouseWheelListener(forwarder);
	}

	// note: no @Override on these -- like AbstractScatterPlotPanel's zoom/resetZoom
	// /pan, they satisfy interfaces (IRectangleSelection, IShapeSelection, etc.)
	// declared on each concrete subclass, not on this abstract class itself.

	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		return getScatterPlotPainter().getElementsInRectangle(rectangle);
	}

	public List<T> getElementsInShape(Shape shape) {
		return getScatterPlotPainter().getElementsInShape(shape);
	}

	public List<T> getElementsAtPoint(Point p) {
		return getScatterPlotPainter().getElementsAtPoint(p);
	}

	public void setSelectedFunction(Function<? super T, Boolean> selectedFunction) {
		getScatterPlotPainter().setSelectedFunction(selectedFunction);
	}

	// ==================== VISUAL ENCODING ====================

	public void setSizeEncodingFunction(Function<? super T, Double> sizeEncodingFunction) {
		getScatterPlotPainter().setSizeEncodingFunction(sizeEncodingFunction);
	}

	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction) {
		getScatterPlotPainter().setColorEncodingFunction(colorEncodingFunction);
	}

	// ==================== TOOLTIP CONFIGURATION ====================

	public Function<? super T, String> getToolTipMapping() {
		return getScatterPlotPainter().getToolTipMapping();
	}

	public void setToolTipMapping(Function<? super T, String> toolTipMapping) {
		getScatterPlotPainter().setToolTipMapping(toolTipMapping);
	}

	public int getToolTipWidth() {
		return getScatterPlotPainter().getToolTipWidth();
	}

	public void setToolTipWidth(int toolTipWidth) {
		getScatterPlotPainter().setToolTipWidth(toolTipWidth);
	}

	public int getToolTipHeight() {
		return getScatterPlotPainter().getToolTipHeight();
	}

	public void setToolTipHeight(int toolTipHeight) {
		getScatterPlotPainter().setToolTipHeight(toolTipHeight);
	}

	// ==================== VISUAL PROPERTIES ====================

	public boolean isAlphaAdjustment() {
		return getScatterPlotPainter().isAlphaAdjustment();
	}

	public void setAlphaAdjustment(boolean dynamicAlphaAdjustment) {
		getScatterPlotPainter().setAlphaAdjustment(dynamicAlphaAdjustment);
	}

	public Paint getSelectionPaint() {
		return getScatterPlotPainter().getSelectionPaint();
	}

	public void setSelectionPaint(Paint selectionPaint) {
		getScatterPlotPainter().setSelectionPaint(selectionPaint);
	}

	// ==================== POSITION MAPPING ====================

	/**
	 * Update the world position mappings. This allows changing the data projection
	 * without recreating the entire scatter plot.
	 *
	 * @param worldPositionMappingX new X coordinate mapping
	 * @param worldPositionMappingY new Y coordinate mapping
	 */
	public void setWorldPositionMappings(Function<? super T, Double> worldPositionMappingX,
			Function<? super T, Double> worldPositionMappingY) {

		this.worldPositionMappingX = worldPositionMappingX;
		this.worldPositionMappingY = worldPositionMappingY;

		// the painter computes each point's position from its own copy of these
		// mappings, not from the panel's -- without pushing the update here, the
		// axis would rescale to the new mapping's range while every plotted point
		// kept using the old (now stale) mapping. Null-guarded like both concrete
		// panels' pre-extraction versions were, even though scatterPlotPainter is
		// in practice always assigned before this method becomes reachable.
		AbstractGPUScatterPlotPainter<T> painter = getScatterPlotPainter();
		if (painter != null) {
			painter.setWorldPositionMappingX(worldPositionMappingX);
			painter.setWorldPositionMappingY(worldPositionMappingY);
		}

		initializeData(getData());

		if (painter != null) {
			painter.setXPositionEncodingFunction(getXPositionEncodingFunction());
			painter.setYPositionEncodingFunction(getYPositionEncodingFunction());
		}

		repaint();
	}

	// ==================== PROTECTED ACCESSORS ====================

	/**
	 * Get the underlying data list. For use in inheriting classes only.
	 *
	 * @return the data points
	 */
	protected abstract List<T> getData();

	// ==================== CLEANUP ====================

	/**
	 * Dispose of GPU resources when the panel is no longer needed. Call this method
	 * before discarding the scatter plot to prevent memory leaks.
	 */
	public void dispose() {
		getScatterPlotPainter().dispose();
	}

	@Override
	public void setBackground(Color backgroundColor) {
		super.setBackground(backgroundColor);

		if (glCanvas != null)
			glCanvas.setBackground(backgroundColor);
	}

	// ==================== CONVENIENCE METHODS ====================

	/**
	 * Get the number of data points in this scatter plot.
	 *
	 * @return the count of data points
	 */
	public int getDataSize() {
		List<T> data = getData();
		return data != null ? data.size() : 0;
	}

	/**
	 * Check if this scatter plot is using GPU rendering.
	 *
	 * @return true if currently rendering with GPU
	 */
	public boolean isUsingGPU() {
		return getEffectiveRenderMode() == RenderMode.GPU;
	}

	/**
	 * Configure optimal settings for the current dataset size. This method
	 * automatically:
	 * <ul>
	 * <li>Enables GPU for large datasets (&gt; 10,000 points)</li>
	 * <li>Enables alpha adjustment for medium/large datasets</li>
	 * <li>Adjusts point sizes appropriately</li>
	 * </ul>
	 */
	public void autoConfigureForDataSize() {
		int size = getDataSize();

		if (OverplottingMitigationTools.shouldUseGPURendering(size))
			setRenderMode(RenderMode.GPU);

		if (OverplottingMitigationTools.shouldEnableAlphaAdjustment(size))
			setAlphaAdjustment(true);

		repaint();
	}

	/**
	 * Print a summary of the scatter plot configuration to console. Useful for
	 * debugging and verification.
	 */
	public void printConfiguration() {
		System.out.println("=== ScatterPlot Configuration ===");
		System.out.println("Data points: " + getDataSize());
		System.out.println("Render mode: " + getRenderMode());
		System.out.println("Effective mode: " + getEffectiveRenderMode());
		System.out.println("Alpha adjustment: " + isAlphaAdjustment());
		System.out.println("Performance logging: " + isPerformanceLoggingEnabled());

		if (isPerformanceLoggingEnabled()) {
			System.out.println("\n" + getPerformanceReport());
		}

		System.out.println("================================");
	}
}
