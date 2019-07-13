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
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2017-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 *
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
	 * @param worldGlobalMin
	 * @param worldGlobalMax
	 * @param positionEncodingFunction
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

	/**
	 * modifies the {@link IPositionEncodingFunction} and fires a
	 * {@link NumberIntervalChangedEvent} afterwards
	 * 
	 * @param e
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
