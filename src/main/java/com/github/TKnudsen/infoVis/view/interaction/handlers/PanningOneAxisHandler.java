package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.TKnudsen.ComplexDataObject.data.interval.NumberInterval;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.interaction.event.NumberIntervalChangeListener;
import com.github.TKnudsen.infoVis.view.interaction.event.NumberIntervalChangedEvent;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;

/**
 * @version 1.01
 * @since 2018
 */
public class PanningOneAxisHandler extends InteractionHandler {

	private final MouseListener mouseListener;
	private final MouseMotionListener mouseMotionListener;

	private final Number worldGlobalMin;
	private final Number worldGlobalMax;
	private IPositionEncodingFunction positionEncodingFunction;

	/**
	 * Which screen coordinate drives the pan math. {@code false} (default) reads
	 * mouse X, matching every existing caller of this class, which all target a
	 * horizontal axis; {@code true} reads mouse Y instead, for a handler built
	 * from a Y-axis position-encoding function -- see the
	 * {@code verticalAxis}-taking constructors. Without this, a "Y-axis" handler
	 * would still silently drive off horizontal mouse movement, since nothing
	 * about the position-encoding function itself tells this class which screen
	 * axis it corresponds to.
	 */
	private boolean verticalAxis = false;

	long lastDrag = 0L;
	Point lastPoint = null;

	private final List<NumberIntervalChangeListener> numberIntervalChangeListeners = new CopyOnWriteArrayList<>();

	/**
	 *
	 * @param worldGlobalMin           min
	 * @param worldGlobalMax           max
	 * @param positionEncodingFunction function
	 */
	public PanningOneAxisHandler(Number worldGlobalMin, Number worldGlobalMax,
			IPositionEncodingFunction positionEncodingFunction) {
		this.worldGlobalMin = worldGlobalMin;
		this.worldGlobalMax = worldGlobalMax;
		this.positionEncodingFunction = positionEncodingFunction;

		this.mouseListener = createMouseListener();
		this.mouseMotionListener = createMouseMotionListener();
	}

	/**
	 *
	 * @param worldGlobalMin           min
	 * @param worldGlobalMax           max
	 * @param positionEncodingFunction function
	 * @param mouseButton              button
	 */
	public PanningOneAxisHandler(Number worldGlobalMin, Number worldGlobalMax,
			IPositionEncodingFunction positionEncodingFunction, MouseButton mouseButton) {
		this(worldGlobalMin, worldGlobalMax, positionEncodingFunction);

		setMouseButton(mouseButton);
	}

	/**
	 * @param worldGlobalMin           min
	 * @param worldGlobalMax           max
	 * @param positionEncodingFunction function
	 * @param mouseButton              button
	 * @param verticalAxis             {@code true} if
	 *                                  {@code positionEncodingFunction} is a
	 *                                  Y-axis (drives off mouse Y instead of X)
	 */
	public PanningOneAxisHandler(Number worldGlobalMin, Number worldGlobalMax,
			IPositionEncodingFunction positionEncodingFunction, MouseButton mouseButton, boolean verticalAxis) {
		this(worldGlobalMin, worldGlobalMax, positionEncodingFunction, mouseButton);

		this.verticalAxis = verticalAxis;
	}

	private MouseListener createMouseListener() {
		return new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (!acceptMouseButton(e))
					return;

				lastPoint = null;
				component.repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (!acceptMouseButton(e))
					return;

				lastPoint = e.getPoint();
			}
		};
	}

	private MouseMotionListener createMouseMotionListener() {
		return new MouseMotionAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (!acceptMouseButton(e))
					return;

				if (e.getWhen() - lastDrag < 60)
					return;

				if (lastPoint != null)
					handlePanning(e);

				lastPoint = e.getPoint();
				lastDrag = e.getWhen();
				component.repaint();
			}
		};
	}

	public void attachTo(Component newComponent) {
		if (component != null) {
			component.removeMouseListener(mouseListener);
			component.removeMouseMotionListener(mouseMotionListener);
		}
		this.component = newComponent;
		if (component != null) {
			component.addMouseListener(mouseListener);
			component.addMouseMotionListener(mouseMotionListener);
		}
	}

	public void handlePanning(MouseEvent mouseEvent) {
		if (positionEncodingFunction == null)
			return;

		NumberInterval oldNumberInterval = getCurrentXAxisMinMaxValues();

		double currentCoord = verticalAxis ? mouseEvent.getPoint().getY() : mouseEvent.getPoint().getX();
		double lastCoord = verticalAxis ? lastPoint.getY() : lastPoint.getX();
		double delta = currentCoord - lastCoord;

		double d0 = positionEncodingFunction.inverseMapping(currentCoord).doubleValue();
		double d1 = positionEncodingFunction.inverseMapping(currentCoord + delta).doubleValue();
		double diff = MathFunctions.round(d1 - d0, 0);

		if (diff == 0)
			return;

		double max = positionEncodingFunction.getMaxWorldValue().doubleValue() - diff;
		double min = positionEncodingFunction.getMinWorldValue().doubleValue() - diff;

		min = Math.max(min, worldGlobalMin.doubleValue());
		max = Math.min(max, worldGlobalMax.doubleValue());

		positionEncodingFunction.setMinWorldValue(min);
		positionEncodingFunction.setMaxWorldValue(max);

		NumberInterval newNumberInterval = getCurrentXAxisMinMaxValues();

		// if (!newNumberInterval.equals(oldNumberInterval))
		fireNumberIntervalChanged(new NumberIntervalChangedEvent(this, oldNumberInterval, newNumberInterval));
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
