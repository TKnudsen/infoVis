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

import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRange;
import com.github.TKnudsen.infoVis.view.gpu.PerformanceLogger;
import com.github.TKnudsen.infoVis.view.gpu.RenderMode;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IPanning;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.IZooming;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainters;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotSpriteGPUPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.XYNumericalChartPanel;
import com.github.TKnudsen.infoVis.view.tools.OverplottingMitigationTools;
import com.github.TKnudsen.infoVis.view.visualChannels.IOverplottingMitigation;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctions;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingRangeTools;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.SizeEncodingFunction;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;

/**
 * <p>
 * Chart panel for scatterplots with support for both CPU-based (Java2D) and
 * GPU-based (OpenGL) rendering modes. Automatically selects optimal rendering
 * mode based on dataset size, with manual override capability.
 * 
 * Features:
 * <ul>
 * <li>Traditional CPU rendering for small datasets</li>
 * <li>GPU-accelerated rendering for large datasets</li>
 * <li>Automatic mode selection based on point count</li>
 * <li>Performance logging and monitoring</li>
 * <li>Full selection support (rectangle, lasso, click)</li>
 * </ul>
 * </p>
 *
 * @version 3.01
 * @since 2018
 */
public class ScatterPlotSpriteGPU<T> extends XYNumericalChartPanel<Double, Double> implements IRectangleSelection<T>,
		IShapeSelection<T>, IClickSelection<T>, ISelectionVisualizer<T>, IColorEncoding<T>, ISizeEncoding<T>,
		IOverplottingMitigation, IZooming, IPanning {

	private static final long serialVersionUID = 2949962927634263599L;

	// ==================== CORE COMPONENTS ====================

	public GLJPanel glPanel;
	/** Alias for glPanel -- kept for test compatibility. */
	public GLJPanel glCanvas;
	protected ScatterPlotSpriteGPUPainter<T> scatterPlotPainter;
	private Rectangle2D currentChartRectangle = null;

	// full data range per axis, for clamping zoom (see IZooming)
	private NumericRange globalRangeX;
	private NumericRange globalRangeY;

	private boolean glRenderScheduled = false;

	// Re-entrancy guard for GLJPanel.paintComponent.
	// JOGL's super.paintComponent() internally calls paintImmediately(parent) which
	// re-enters glPanel.paintComponent via paintChildren. The inner call must skip
	// the super call (and thus skip another GL render) but still draw the frame.
	private boolean inGlPaintComponent = false;

	/**
	 * World coordinates/position/values of the x dimension. Can be updated to avoid
	 * re-initialization overhead.
	 */
	private Function<? super T, Double> worldPositionMappingX;

	/**
	 * World coordinates/position/values of the y dimension. Can be updated to avoid
	 * re-initialization overhead.
	 */
	private Function<? super T, Double> worldPositionMappingY;

	/**
	 * Colors of the objects
	 */
	private final Function<? super T, ? extends Paint> colorMapping;

	// Track painters that need to render on top of GLCanvas
	private List<ChartPainter> overlayPainters = new ArrayList<>();

	// ==================== CONSTRUCTOR ====================

	public ScatterPlotSpriteGPU(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {

		this.colorMapping = colorMapping;
		this.worldPositionMappingX = worldPositionMappingX;
		this.worldPositionMappingY = worldPositionMappingY;

		initializeData(data);
		initializePainter(data);

		addChartPainter(scatterPlotPainter, true, true); // Add to chart painters

		setLayout(null); // Manual layout - we position GLCanvas in updatePainterRectangles

		glPanel = createGLPanel(scatterPlotPainter);
		glCanvas = glPanel;
		setOpaque(false); // Don't paint background in parent
		add(glPanel);

		// Forward mouse events from GLCanvas to this panel
		forwardMouseEventsFromGLCanvas();
	}

	// ==================== INITIALIZATION ====================

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
	 * A subclass overriding this method to install its own painter subtype must
	 * assign {@link #scatterPlotPainter} itself and should <b>not</b> call
	 * {@code super.initializePainter(data)} -- doing so would construct a default
	 * {@link ScatterPlotSpriteGPUPainter} that is immediately discarded when this
	 * method's own assignment overwrites it.
	 *
	 * @param data the data to plot
	 */
	protected void initializePainter(List<T> data) {
		this.scatterPlotPainter = new ScatterPlotSpriteGPUPainter<T>(data, colorMapping, worldPositionMappingX,
				worldPositionMappingY);

		this.scatterPlotPainter.setSizeEncodingFunction(new SizeEncodingFunction<>(this));
		this.scatterPlotPainter.setRenderMode(RenderMode.GPU);
	}

	@Override
	public void initializeXAxisPainter(Double min, Double max) {
		setXAxisPainter(new XAxisNumericalPainter<Double>(min, max));
	}

	@Override
	public void initializeYAxisPainter(Double min, Double max) {
		setYAxisPainter(new YAxisNumericalPainter<Double>(min, max));
	}

	private GLJPanel createGLPanel(ScatterPlotSpriteGPUPainter<T> painter) {
		GLProfile profile = GLProfile.getMaxProgrammableCore(true);
		GLCapabilities caps = new GLCapabilities(profile);

		// Override paintComponent so that after GL initialization, any Swing-initiated
		// paint of the GLJPanel is a no-op. This prevents an unreliable FBO->Swing
		// blit (observed to render solid black on at least one GPU/driver
		// combination) from painting over the glReadPixels image drawn below.
		GLJPanel panel = new GLJPanel(caps) {
			@Override
			protected void paintComponent(java.awt.Graphics g) {
				if (!scatterPlotPainter.isGpuRendererInitialized()) {
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
					// super.paintComponent(g) -> displayGL() -> readbackToImage() -> lastRenderedFrame
					super.paintComponent(g);
				} finally {
					inGlPaintComponent = false;
				}
				// Overdraw whatever JOGL's own blit put on screen with the pixel-correct
				// readback frame.
				java.awt.image.BufferedImage frame = scatterPlotPainter.getLastRenderedFrame();
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
				painter.initGL(d);
			}

			@Override
			public void dispose(GLAutoDrawable d) {
				painter.disposeGL(d);
			}

			@Override
			public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
				painter.reshapeGL(d, x, y, w, h);
				// GL surface has been resized -- schedule a fresh render at the new size.
				javax.swing.SwingUtilities.invokeLater(() -> ScatterPlotSpriteGPU.this.repaint());
			}

			@Override
			public void display(GLAutoDrawable d) {
				Color panelBg = getBackground();
				if (panelBg != null) {
					scatterPlotPainter.setBackgroundPaint(panelBg);
				}
				painter.displayGL(d);
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
			scatterPlotPainter.setRectangle(cr);

			// Position GLCanvas EXACTLY over chart area (not axes)
			int x = (int) Math.round(cr.getX() + 1);
			int y = (int) Math.round(cr.getY());
			int w = (int) Math.round(cr.getWidth());
			int h = (int) Math.round(cr.getHeight());

			glPanel.setBounds(x, y, w, h);
			// Schedule a GL re-render so the frame is produced at the new size.
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
			if (painter != scatterPlotPainter) {
				painter.draw(g2);
			}
		}

		// Draw the last GL-rendered frame. Rendering is decoupled from painting:
		// display() is only called from doGLRender(), never from here. The frame is
		// drawn scaled to the current chart rectangle so that even a stale frame
		// (from before a resize) shows points at proportionally correct positions.
		if (glPanel != null && scatterPlotPainter.isGpuRendererInitialized()) {
			java.awt.image.BufferedImage frame = scatterPlotPainter.getLastRenderedFrame();
			if (frame != null && currentChartRectangle != null) {
				g2.drawImage(frame, (int) currentChartRectangle.getX(), (int) currentChartRectangle.getY(),
						(int) currentChartRectangle.getWidth(), (int) currentChartRectangle.getHeight(), null);
			}
		}
	}

	@Override
	protected void paintChildren(Graphics g) {
		// Always paint children -- this is what triggers glPanel.paintComponent,
		// which is where GL rendering actually happens.
		super.paintChildren(g);
		if (scatterPlotPainter.isGpuRendererInitialized()) {
			// After glPanel has painted (and updated lastRenderedFrame), draw the
			// correct frame over whatever glPanel put on screen.
			java.awt.image.BufferedImage frame = scatterPlotPainter.getLastRenderedFrame();
			if (frame != null && currentChartRectangle != null) {
				Graphics2D g2 = (Graphics2D) g;
				g2.drawImage(frame, (int) currentChartRectangle.getX(), (int) currentChartRectangle.getY(),
						(int) currentChartRectangle.getWidth(), (int) currentChartRectangle.getHeight(), null);
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g); // Paints component + children (GLJPanel)

		// Now paint axes and overlays ON TOP
		Graphics2D g2 = (Graphics2D) g;

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
	void doGLRender() {
		glRenderScheduled = false;
		if (glPanel != null && scatterPlotPainter.isGpuRendererInitialized()) {
			glPanel.display();
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
		if (scatterPlotPainter != null) {
			scatterPlotPainter.setRenderMode(mode);
			repaint();
		}
	}

	/**
	 * Get the current rendering mode setting.
	 * 
	 * @return the configured render mode (may differ from effective mode)
	 */
	public RenderMode getRenderMode() {
		return scatterPlotPainter != null ? scatterPlotPainter.getRenderMode() : RenderMode.CPU;
	}

	/**
	 * Get the effective rendering mode currently being used. This may differ from
	 * getRenderMode() if AUTO mode is selected or if GPU initialization failed.
	 * 
	 * @return the actual render mode being used
	 */
	public RenderMode getEffectiveRenderMode() {
		return scatterPlotPainter != null ? scatterPlotPainter.getEffectiveRenderMode() : RenderMode.CPU;
	}

	// ==================== PERFORMANCE LOGGING ====================

	/**
	 * Enable performance logging to track rendering metrics (FPS, draw calls,
	 * etc.). Useful for benchmarking and identifying performance bottlenecks.
	 */
	public void enablePerformanceLogging() {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.enablePerformanceLogging();
		}
	}

	/**
	 * Disable performance logging.
	 */
	public void disablePerformanceLogging() {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.disablePerformanceLogging();
		}
	}

	/**
	 * Check if performance logging is currently enabled.
	 * 
	 * @return true if performance logging is active
	 */
	public boolean isPerformanceLoggingEnabled() {
		return scatterPlotPainter != null && scatterPlotPainter.isPerformanceLoggingEnabled();
	}

	/**
	 * Reset accumulated performance statistics.
	 */
	public void resetPerformanceStatistics() {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.resetPerformanceStatistics();
		}
	}

	/**
	 * Log current performance statistics to the console.
	 */
	public void logPerformanceStatistics() {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.logPerformanceStatistics();
		}
	}

	/**
	 * Get direct access to the performance logger for advanced control.
	 * 
	 * @return the performance logger instance, or null if painter not initialized
	 */
	public PerformanceLogger getPerformanceLogger() {
		return scatterPlotPainter != null ? scatterPlotPainter.getPerformanceLogger() : null;
	}

	/**
	 * Get a formatted performance report.
	 * 
	 * @return multi-line performance statistics report
	 */
	public String getPerformanceReport() {
		if (scatterPlotPainter == null) {
			return "Performance logging not available";
		}

		PerformanceLogger logger = scatterPlotPainter.getPerformanceLogger();
		return logger != null ? logger.getStatisticsReport() : "No statistics available";
	}

	// ==================== SELECTION INTERFACE ====================

	private void forwardMouseEventsFromGLCanvas() {
		MouseAdapter forwarder = new MouseAdapter() {
			private MouseEvent translateEvent(MouseEvent e) {
				Point glCanvasPos = glPanel.getLocation();
				return new MouseEvent(ScatterPlotSpriteGPU.this, e.getID(), e.getWhen(), e.getModifiersEx(),
						e.getX() + glCanvasPos.x, e.getY() + glCanvasPos.y, e.getClickCount(), e.isPopupTrigger(),
						e.getButton());
			}

			@Override
			public void mousePressed(MouseEvent e) {
				for (MouseListener ml : getMouseListeners()) {
					ml.mousePressed(translateEvent(e));
				}
				ScatterPlotSpriteGPU.this.repaint(); // Force repaint
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				for (MouseListener ml : getMouseListeners()) {
					ml.mouseReleased(translateEvent(e));
				}
				ScatterPlotSpriteGPU.this.repaint(); // Force repaint
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				for (MouseMotionListener mml : getMouseMotionListeners()) {
					mml.mouseDragged(translateEvent(e));
				}
				// CRITICAL: Force immediate repaint during drag
				ScatterPlotSpriteGPU.this.paintImmediately(0, 0, getWidth(), getHeight());
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
				Point glCanvasPos = glPanel.getLocation();
				java.awt.event.MouseWheelEvent translated = new java.awt.event.MouseWheelEvent(
						ScatterPlotSpriteGPU.this, e.getID(), e.getWhen(), e.getModifiersEx(),
						e.getX() + glCanvasPos.x, e.getY() + glCanvasPos.y, e.getClickCount(), e.isPopupTrigger(),
						e.getScrollType(), e.getScrollAmount(), e.getWheelRotation());
				for (java.awt.event.MouseWheelListener mwl : getMouseWheelListeners()) {
					mwl.mouseWheelMoved(translated);
				}
			}
		};

		glPanel.addMouseListener(forwarder);
		glPanel.addMouseMotionListener(forwarder);
		glPanel.addMouseWheelListener(forwarder);
	}

	@Override
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		return scatterPlotPainter != null ? scatterPlotPainter.getElementsInRectangle(rectangle) : null;
	}

	@Override
	public List<T> getElementsInShape(Shape shape) {
		return scatterPlotPainter != null ? scatterPlotPainter.getElementsInShape(shape) : null;
	}

	@Override
	public List<T> getElementsAtPoint(Point p) {
		return scatterPlotPainter != null ? scatterPlotPainter.getElementsAtPoint(p) : null;
	}

	@Override
	public void setSelectedFunction(Function<? super T, Boolean> selectedFunction) {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.setSelectedFunction(selectedFunction);
		}
	}

	// ==================== VISUAL ENCODING ====================

	@Override
	public void setSizeEncodingFunction(Function<? super T, Double> sizeEncodingFunction) {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.setSizeEncodingFunction(sizeEncodingFunction);
		}
	}

	@Override
	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction) {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.setColorEncodingFunction(colorEncodingFunction);
		}
	}

	// ==================== TOOLTIP CONFIGURATION ====================

	public Function<? super T, String> getToolTipMapping() {
		return scatterPlotPainter != null ? scatterPlotPainter.getToolTipMapping() : null;
	}

	public void setToolTipMapping(Function<? super T, String> toolTipMapping) {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.setToolTipMapping(toolTipMapping);
		}
	}

	public int getToolTipWidth() {
		return scatterPlotPainter != null ? scatterPlotPainter.getToolTipWidth() : 150;
	}

	public void setToolTipWidth(int toolTipWidth) {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.setToolTipWidth(toolTipWidth);
		}
	}

	public int getToolTipHeight() {
		return scatterPlotPainter != null ? scatterPlotPainter.getToolTipHeight() : 30;
	}

	public void setToolTipHeight(int toolTipHeight) {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.setToolTipHeight(toolTipHeight);
		}
	}

	// ==================== VISUAL PROPERTIES ====================

	@Override
	public boolean isAlphaAdjustment() {
		return scatterPlotPainter != null && scatterPlotPainter.isAlphaAdjustment();
	}

	@Override
	public void setAlphaAdjustment(boolean dynamicAlphaAdjustment) {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.setAlphaAdjustment(dynamicAlphaAdjustment);
		}
	}

	public Paint getSelectionPaint() {
		return scatterPlotPainter != null ? scatterPlotPainter.getSelectionPaint() : null;
	}

	public void setSelectionPaint(Paint selectionPaint) {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.setSelectionPaint(selectionPaint);
		}
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
		// kept using the old (now stale) mapping.
		if (scatterPlotPainter != null) {
			scatterPlotPainter.setWorldPositionMappingX(worldPositionMappingX);
			scatterPlotPainter.setWorldPositionMappingY(worldPositionMappingY);
		}

		initializeData(ScatterPlotPainters.getData(scatterPlotPainter));

		if (scatterPlotPainter != null) {
			scatterPlotPainter.setXPositionEncodingFunction(getXPositionEncodingFunction());
			scatterPlotPainter.setYPositionEncodingFunction(getYPositionEncodingFunction());
		}

		repaint();
	}

	// ==================== PROTECTED ACCESSORS ====================

	/**
	 * Get the underlying data list. For use in inheriting classes only.
	 * 
	 * @return the data points
	 */
	protected List<T> getData() {
		return ScatterPlotPainters.getData(scatterPlotPainter);
	}

	/**
	 * Get the color mapping function. For use in inheriting classes only.
	 * 
	 * @return the color mapping function
	 */
	protected Function<? super T, ? extends Paint> getColorMapping() {
		return colorMapping;
	}

	/**
	 * Get the X position mapping function. For use in inheriting classes only.
	 * 
	 * @return the X world position mapping
	 */
	protected Function<? super T, Double> getWorldPositionMappingX() {
		return worldPositionMappingX;
	}

	/**
	 * Get the Y position mapping function. For use in inheriting classes only.
	 * 
	 * @return the Y world position mapping
	 */
	protected Function<? super T, Double> getWorldPositionMappingY() {
		return worldPositionMappingY;
	}

	/**
	 * Get direct access to the scatter plot painter. For use in inheriting classes
	 * or advanced configuration only.
	 * 
	 * @return the underlying ScatterPlotPainter instance
	 */
	protected ScatterPlotSpriteGPUPainter<T> getScatterPlotPainter() {
		return scatterPlotPainter;
	}

	// ==================== CLEANUP ====================

	/**
	 * Dispose of GPU resources when the panel is no longer needed. Call this method
	 * before discarding the scatter plot to prevent memory leaks.
	 */
	public void dispose() {
		if (scatterPlotPainter != null) {
			scatterPlotPainter.dispose();
		}
	}

	@Override
	public void setBackground(Color backgroundColor) {
		super.setBackground(backgroundColor);

		if (glPanel != null)
			glPanel.setBackground(backgroundColor);
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

	// ==================== PAN / ZOOM ====================

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