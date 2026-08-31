package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.TKnudsen.ComplexDataObject.data.interval.NumberInterval;
import com.github.TKnudsen.infoVis.view.interaction.event.NumberIntervalChangeListener;
import com.github.TKnudsen.infoVis.view.interaction.event.NumberIntervalChangedEvent;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;

/**
 * <p>
 * Mouse-wheel/double-click zoom handler for a raw
 * {@link IPositionEncodingFunction}. Unlike
 * {@link com.github.TKnudsen.infoVis.view.interaction.handlers.ZoomInteractionHandler
 * ZoomInteractionHandler}, this handler does not target an
 * {@link com.github.TKnudsen.infoVis.view.interaction.IZooming IZooming} object
 * and does not itself apply the result to anything: it only <em>reads</em>
 * {@code positionEncodingFunction} for the zoom math (cursor position,
 * current/global range), then <em>broadcasts</em> the new interval to every
 * registered {@link NumberIntervalChangeListener} via
 * {@link #addNumberIntervalListener(NumberIntervalChangeListener)}. Each
 * listener is responsible for applying that interval itself, through whichever
 * axis setter its own view actually needs (e.g.
 * {@code panel.setXAxisMinValue(...)}/{@code setXAxisMaxValue(...)} -- never by
 * writing {@code positionEncodingFunction} directly, which would desync a
 * view's own cached tick/marker positions).
 * </p>
 *
 * <p>
 * <b>Choose this over {@code ZoomInteractionHandler} exactly when one zoom
 * gesture must drive more than one view</b> -- e.g. several panels sharing one
 * linked time/value axis. One handler, attached to a single "driving"
 * component, can have many listeners; {@code ZoomInteractionHandler} always
 * drives exactly one {@code IZooming} target and never broadcasts. If there is
 * only one target and it can implement {@code IZooming} itself, prefer
 * {@code ZoomInteractionHandler} -- it is the simpler contract.
 * </p>
 *
 * <p>
 * See
 * {@link com.github.TKnudsen.infoVis.view.panels.scatterPlot.ZoomingHandlerTester
 * ZoomingHandlerTester} for a self-contained, runnable demo (two panels, one
 * handler, linked x-axis zoom). Real production usage lives in
 * {@code stocksExplorer} (multiple views); the original reference usage -- one
 * handler synchronizing several time-series panels -- is
 * {@code TimeSeriesUnivariateChartTest} in {@code TimeSeriesLib}.
 * </p>
 *
 * @since 2017
 * @version 2.01 revised in August 2026
 */
public class ZoomingHandler extends InteractionHandler {

	private final MouseListener mouseListener;
	private final MouseWheelListener mouseWheelListener;

	private final Number worldGlobalMin;
	private final Number worldGlobalMax;
	private IPositionEncodingFunction positionEncodingFunction;

	private long lastScroll = 0L;

	private final List<NumberIntervalChangeListener> numberIntervalChangeListeners = new CopyOnWriteArrayList<>();

	/**
	 *
	 * @param worldGlobalMin           min
	 * @param worldGlobalMax           max
	 * @param positionEncodingFunction function
	 */
	public ZoomingHandler(Number worldGlobalMin, Number worldGlobalMax,
			IPositionEncodingFunction positionEncodingFunction) {
		this.worldGlobalMin = worldGlobalMin;
		this.worldGlobalMax = worldGlobalMax;
		this.positionEncodingFunction = positionEncodingFunction;

		this.mouseListener = createMouseListener();
		this.mouseWheelListener = createMouseWheelListener();
	}

	private MouseListener createMouseListener() {
		return new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() == 2) {
					resetZoom();

					component.repaint();
				}
			}
		};
	}

	private MouseWheelListener createMouseWheelListener() {
		return new MouseAdapter() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				handleZoom(e);

				component.repaint();
			}
		};
	}

	@Override
	public void attachTo(Component newComponent) {
		// Snapshot into a local instead of re-reading the volatile field across
		// this whole sequence -- if attachTo() were ever called concurrently from
		// two threads on the same handler, re-reading "component" between the
		// detach and attach halves could see the OTHER thread's newComponent
		// partway through, detaching/attaching listeners on the wrong object.
		Component oldComponent = this.component;
		if (oldComponent != null) {
			oldComponent.removeMouseListener(mouseListener);
			oldComponent.removeMouseWheelListener(mouseWheelListener);
		}
		this.component = newComponent;
		if (newComponent != null) {
			newComponent.addMouseListener(mouseListener);
			newComponent.addMouseWheelListener(mouseWheelListener);
		}
	}

	/**
	 * modifies the {@link IPositionEncodingFunction} and fires a
	 * {@link NumberIntervalChangedEvent} afterwards
	 *
	 * @param e mouse event
	 */
	public void handleZoom(MouseWheelEvent e) {
		NumberInterval oldNumberInterval = getCurrentXAxisMinMaxValues();

		if (e.getWhen() - lastScroll < 50) {
			return;
		}

		lastScroll = e.getWhen();

		int zoomCount = e.getWheelRotation() < 0 ? 1 : -1;

		Point location = e.getPoint();

		long center = positionEncodingFunction.inverseMapping(location.getX()).longValue();

		double zoomFact = zoomCount * 0.15;
		long xInterval = oldNumberInterval.getDuration().longValue();

		if (Math.abs((long) (xInterval * zoomFact)) < 1)
			return;

		double min = positionEncodingFunction.getMinWorldValue().longValue() + (long) (xInterval * zoomFact);
		double max = positionEncodingFunction.getMaxWorldValue().longValue() - (long) (xInterval * zoomFact);

		min = Math.max(min, worldGlobalMin.doubleValue());
		max = Math.min(max, worldGlobalMax.doubleValue());

		if (min >= max) {
			return;
		}

		positionEncodingFunction.setMinWorldValue(min);
		positionEncodingFunction.setMaxWorldValue(max);

		long newCenter = positionEncodingFunction.inverseMapping(location.getX()).longValue();
		min = positionEncodingFunction.getMinWorldValue().longValue() + center - newCenter;
		max = positionEncodingFunction.getMaxWorldValue().longValue() + center - newCenter;

		NumberInterval newNumberInterval = new NumberInterval(min, max);

		// if (!newNumberInterval.equals(oldNumberInterval))
		fireNumberIntervalChanged(new NumberIntervalChangedEvent(this, oldNumberInterval, newNumberInterval));
	}

	private final void resetZoom() {
		NumberInterval newNumberInterval = new NumberInterval(worldGlobalMin, worldGlobalMax);

		fireNumberIntervalChanged(new NumberIntervalChangedEvent(this, null, newNumberInterval));
	}

	public List<NumberIntervalChangeListener> getNumberIntervalListeners() {
		return numberIntervalChangeListeners;
	}

	public void addNumberIntervalListener(NumberIntervalChangeListener NumberIntervalListener) {
		if (this.numberIntervalChangeListeners.contains(NumberIntervalListener))
			this.numberIntervalChangeListeners.remove(NumberIntervalListener);

		this.numberIntervalChangeListeners.add(NumberIntervalListener);
	}

	/**
	 * Unregisters a previously added listener -- without this, a listener that
	 * synchronizes several panels off this one handler (see this class's own
	 * javadoc) has no way to stop receiving zoom broadcasts when its panel is
	 * closed/disposed, leaking it (and everything it closes over) for the
	 * handler's lifetime.
	 */
	public void removeNumberIntervalListener(NumberIntervalChangeListener NumberIntervalListener) {
		this.numberIntervalChangeListeners.remove(NumberIntervalListener);
	}

	/**
	 * This method has to be called when the start and end time of the display area
	 * have been changed and {@link NumberIntervalChangeListener} instances should
	 * be informed.
	 */
	private final void fireNumberIntervalChanged(NumberIntervalChangedEvent event) {
		if (!numberIntervalChangeListeners.isEmpty()) {
			for (NumberIntervalChangeListener NumberIntervalListener : numberIntervalChangeListeners)
				NumberIntervalListener.numberIntervalChanged(event);
		}
	}

	/**
	 * retrieves the current NumberInterval spanned by the xAxis. In contrast to the
	 * time interval of the data the time interval of the visual structure may
	 * differ due to interaction or linking purposes.
	 *
	 * @return
	 */
	private final NumberInterval getCurrentXAxisMinMaxValues() {
		return new NumberInterval(this.positionEncodingFunction.getMinWorldValue(),
				this.positionEncodingFunction.getMaxWorldValue());
	}

	public void setPositionEncodingFunction(IPositionEncodingFunction positionEncodingFunction) {
		this.positionEncodingFunction = positionEncodingFunction;
	}
}
