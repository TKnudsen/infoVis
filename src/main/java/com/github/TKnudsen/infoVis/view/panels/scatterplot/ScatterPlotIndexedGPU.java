package com.github.TKnudsen.infoVis.view.panels.scatterplot;

import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.gpu.RenderMode;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IPanning;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.IZooming;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotIndexedGPUPainter;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainters;
import com.github.TKnudsen.infoVis.view.visualChannels.IOverplottingMitigation;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.SizeEncodingFunction;
import com.jogamp.opengl.GLAutoDrawable;

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
 * <p>
 * Everything shared with {@link ScatterPlotSpriteGPU} lives in
 * {@link AbstractGPUScatterPlotPanel}; this class only adds what depends on
 * the concretely-typed {@link #scatterPlotPainter} -- painter construction,
 * the four GL-invocation hooks, and {@link #updateWorldBounds()} (which
 * {@code ScatterPlotSpriteGPU} does not have).
 * </p>
 *
 * @version 2.01 revised in August 2026
 * @since 2025
 */
public class ScatterPlotIndexedGPU<T> extends AbstractGPUScatterPlotPanel<T>
		implements IRectangleSelection<T>, IShapeSelection<T>, IClickSelection<T>, ISelectionVisualizer<T>,
		IColorEncoding<T>, ISizeEncoding<T>, IOverplottingMitigation, IZooming, IPanning {

	private static final long serialVersionUID = 2949962927634263599L;

	protected ScatterPlotIndexedGPUPainter<T> scatterPlotPainter;

	public ScatterPlotIndexedGPU(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		super(colorMapping, worldPositionMappingX, worldPositionMappingY);

		initializeData(data);
		initializePainter(data);

		addChartPainter(scatterPlotPainter, true, true); // Add to chart painters

		setLayout(null); // Manual layout - we position GLCanvas in updatePainterRectangles

		glCanvas = createGLPanel();
		add(glCanvas);

		// Forward mouse events from GLCanvas to this panel
		forwardMouseEventsFromGLCanvas();
	}

	/**
	 * Constructs {@link #scatterPlotPainter}. Called from the constructor, after
	 * {@code colorMapping}, {@code worldPositionMappingX}, and
	 * {@code worldPositionMappingY} have been assigned -- so only those three
	 * fields (via their protected getters) are safe to read here; a subclass's
	 * own fields are not yet initialized at this point (see
	 * {@link #initializeData(List)} for why).
	 *
	 * @param data the data to plot
	 */
	protected void initializePainter(List<T> data) {
		this.scatterPlotPainter = new ScatterPlotIndexedGPUPainter<T>(data, getColorMapping(),
				getWorldPositionMappingX(), getWorldPositionMappingY());

		this.scatterPlotPainter.setSizeEncodingFunction(new SizeEncodingFunction<>(this));
		this.scatterPlotPainter.setRenderMode(RenderMode.GPU);
	}

	@Override
	protected ScatterPlotIndexedGPUPainter<T> getScatterPlotPainter() {
		return scatterPlotPainter;
	}

	// ==================== GL LIFECYCLE HOOKS ====================

	@Override
	protected void invokeInitGL(GLAutoDrawable drawable) {
		scatterPlotPainter.initGL(drawable);
	}

	@Override
	protected void invokeDisposeGL(GLAutoDrawable drawable) {
		scatterPlotPainter.disposeGL(drawable);
	}

	@Override
	protected void invokeReshapeGL(GLAutoDrawable drawable, int x, int y, int w, int h) {
		scatterPlotPainter.reshapeGL(w, h);
	}

	@Override
	protected void invokeDisplayGL(GLAutoDrawable drawable) {
		scatterPlotPainter.displayGL(drawable, currentChartRectangle);
	}

	@Override
	protected void onGLCanvasDragged(MouseEvent e) {
		// Render and show immediately during drag (JOGL's paintImmediately
		// fires from inside display(), which is safe here since we are NOT
		// inside a Swing paint cycle).
		doGLRender();
	}

	/**
	 * Recompute world bounds and update axes. Call this when layout positions have
	 * actually changed.
	 */
	public void updateWorldBounds() {
		// Recompute bounds from current data
		scatterPlotPainter.updateWorldBounds();

		if (scatterPlotPainter.getEffectiveRenderMode().equals(RenderMode.CPU)) {
			// CPU mode: reset axes to the full new range (existing behavior)
			initializeData(getData());
		} else {
			// GPU mode: only refresh the zoom-out/pan clamp, without resetting
			// whatever the user is currently zoomed/panned to -- without this,
			// globalRangeX/Y went stale the moment positions moved, permanently
			// capping zoom-out below what's needed to see all current points
			refreshGlobalRange(getData());
		}

		repaint();
	}

	// ==================== PROTECTED ACCESSORS ====================

	@Override
	protected List<T> getData() {
		return ScatterPlotPainters.getData(scatterPlotPainter);
	}
}
