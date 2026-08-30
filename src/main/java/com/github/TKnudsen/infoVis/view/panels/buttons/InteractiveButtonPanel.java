package com.github.TKnudsen.infoVis.view.panels.buttons;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;

import com.github.TKnudsen.infoVis.view.painters.buttons.ButtonPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * <p>
 * A single-{@link ButtonPainter} panel that fires a property-change event on
 * click and toggles the painter's focused state on hover. Built on
 * {@link InfoVisChartPanel} rather than the legacy, single-painter {@code
 * ChartPanel} -- the painter's rectangle is kept inset from the panel's chart
 * rectangle (see {@link #updatePainterRectangles()}) so the button reads as a
 * button rather than filling its whole container.
 * </p>
 *
 * @version 1.0
 * @since 2018
 */
public abstract class InteractiveButtonPanel<P extends ButtonPainter> extends InfoVisChartPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Fraction of the chart rectangle's width/height left as empty space on each
	 * side of the button painter -- matches the legacy {@code ChartPanel}-based
	 * implementation's fixed 15% inset.
	 */
	private static final double BUTTON_INSET_RATIO = 0.15;

	private final P painter;

	private String eventProperty;

	private final List<PropertyChangeListener> listeners = new ArrayList<>();

	public InteractiveButtonPanel(P painter, String eventProperty) {
		super(painter);

		this.painter = painter;
		this.eventProperty = eventProperty;

		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				notifyListeners(InteractiveButtonPanel.this.eventProperty);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				painter.setFocused(true);
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				painter.setFocused(false);
				repaint();
			}
		});
	}

	@Override
	protected void updatePainterRectangles() {
		Rectangle2D chartRectangle = getChartRectangleLayout().getChartRectangle();
		if (chartRectangle == null)
			return;

		double insetX = chartRectangle.getWidth() * BUTTON_INSET_RATIO;
		double insetY = chartRectangle.getHeight() * BUTTON_INSET_RATIO;

		painter.setRectangle(new Rectangle2D.Double(chartRectangle.getX() + insetX, chartRectangle.getY() + insetY,
				chartRectangle.getWidth() - 2 * insetX, chartRectangle.getHeight() - 2 * insetY));
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		// This shares its name/signature with java.awt.Component's own
		// addPropertyChangeListener, which it unintentionally overrides. JPanel's
		// constructor (via updateUI -> setUI -> SynthPanelUI.installUI ->
		// installListeners) calls this, through the base class, BEFORE this
		// subclass's own field initializers run -- so `listeners` is still null at
		// that point, every time, under any Look-and-Feel whose UI delegate
		// installs a property-change listener during installUI. That call was
		// never meant for this custom click-listener list in the first place, so
		// silently ignoring it here is correct, not just a crash workaround.
		if (listeners == null)
			return;

		listeners.add(listener);
	}

	private void notifyListeners(String property) {
		for (PropertyChangeListener listener : listeners)
			listener.propertyChange(new PropertyChangeEvent(this, property, 0, 0));
	}

	public P getButtonPainter() {
		return painter;
	}

	public String getEventProperty() {
		return eventProperty;
	}

	public void setEventProperty(String eventProperty) {
		this.eventProperty = eventProperty;
	}

}
