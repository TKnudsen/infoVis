package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.github.TKnudsen.infoVis.view.interaction.IZooming;

/**
 *
 * <p>
 * Mouse-wheel/double-click zoom handler for a single {@link IZooming} target.
 * Every wheel notch becomes one call to
 * {@link IZooming#zoom(java.awt.Point, int, boolean, boolean)}; a double-click
 * becomes one call to {@link IZooming#resetZoom()}. The target owns all zoom
 * math and state itself (typically via
 * {@link com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingRangeTools}
 * applied through its own axis setters) -- this handler is a thin, stateless
 * translator: it only debounces rapid wheel events and forwards the resulting
 * gesture to the target it was constructed with. It never reads or writes a
 * position-encoding function itself, and it never notifies any other object.
 * </p>
 *
 * <p>
 * <b>Choose this over {@link ZoomingHandler} whenever exactly one view needs to
 * zoom and that view can implement {@code IZooming} itself</b> -- which is true
 * for every scatterplot class in this library (see
 * {@link com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlots#addZoomInteraction
 * ScatterPlots.addZoomInteraction}). Reach for {@code ZoomingHandler} only when
 * one zoom gesture must be broadcast to <em>several</em> views at once (e.g.
 * linked axes across multiple panels) -- something this class cannot do, since
 * it always drives exactly one target and never fires an event anything else
 * could listen to.
 * </p>
 *
 * <p>
 * See
 * {@link com.github.TKnudsen.infoVis.view.panels.scatterPlot.ScatterPlotTester
 * ScatterPlotTester} for a self-contained, runnable demo (also
 * {@code CategoricalYAxisScatterplotTester} and
 * {@code ScatterPlotSideBySideTesters}, all wired via
 * {@code ScatterPlots.addZoomInteraction}); compare against
 * {@link com.github.TKnudsen.infoVis.view.panels.scatterPlot.ZoomingHandlerTester
 * ZoomingHandlerTester}, which demonstrates {@code ZoomingHandler}'s
 * multi-panel broadcast instead.
 * </p>
 *
 * @since 2018
 * @version 2.01 revised in August 2026
 */
public class ZoomInteractionHandler extends InteractionHandler {

	private final IZooming zoomable;
	private final boolean zoomX;
	private final boolean zoomY;

	private final MouseListener mouseListener;
	private final MouseWheelListener mouseWheelListener;

	private long lastScroll = 0L;

	/** Zooms both axes on every wheel notch. */
	public ZoomInteractionHandler(IZooming zoomable) {
		this(zoomable, true, true);
	}

	/**
	 * @param zoomable target invoked on every wheel notch / reset
	 * @param zoomX    whether wheel scrolling should zoom the x axis
	 * @param zoomY    whether wheel scrolling should zoom the y axis
	 */
	public ZoomInteractionHandler(IZooming zoomable, boolean zoomX, boolean zoomY) {
		this.zoomable = zoomable;
		this.zoomX = zoomX;
		this.zoomY = zoomY;

		this.mouseListener = createMouseListener();
		this.mouseWheelListener = createMouseWheelListener();
	}

	private MouseListener createMouseListener() {
		return new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() == 2) {
					zoomable.resetZoom();

					component.repaint();
				}
			}
		};
	}

	private MouseWheelListener createMouseWheelListener() {
		return new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWhen() - lastScroll < 50)
					return;
				lastScroll = e.getWhen();

				int zoomCount = e.getWheelRotation() < 0 ? 1 : -1;
				zoomable.zoom(e.getPoint(), zoomCount, zoomX, zoomY);

				component.repaint();
			}
		};
	}

	@Override
	public void attachTo(Component newComponent) {
		if (component != null) {
			component.removeMouseListener(mouseListener);
			component.removeMouseWheelListener(mouseWheelListener);
		}
		this.component = newComponent;
		if (component != null) {
			component.addMouseListener(mouseListener);
			component.addMouseWheelListener(mouseWheelListener);
		}
	}
}
