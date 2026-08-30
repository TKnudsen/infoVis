package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import com.github.TKnudsen.infoVis.view.interaction.IPanning;

/**
 * <p>
 * InfoVis
 * </p>
 *
 * <p>
 * Copyright: (c) 2018-2026 Juergen Bernard,
 * https://github.com/TKnudsen/InfoVis<br>
 * </p>
 *
 * <p>
 * Translates mouse-drag events on an attached {@link Component} into
 * {@link IPanning#pan(int, int)} calls against a target. The target owns all
 * pan math and state (typically via
 * {@link com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingRangeTools}
 * applied through its own axis setters) -- this handler only debounces and
 * forwards raw AWT events.
 * </p>
 *
 * <p>
 * Distinct from {@link PanningOneAxisHandler}, which drives a raw
 * {@link com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction}
 * for a single axis and is kept unchanged for its existing (TimeSeriesLib,
 * linked multi-panel) usages.
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.00
 */
public class PanInteractionHandler extends InteractionHandler {

	private final IPanning pannable;

	private final MouseListener mouseListener;
	private final MouseMotionListener mouseMotionListener;

	private long lastDrag = 0L;
	private Point lastPoint = null;

	public PanInteractionHandler(IPanning pannable) {
		this.pannable = pannable;

		this.mouseListener = createMouseListener();
		this.mouseMotionListener = createMouseMotionListener();
	}

	public PanInteractionHandler(IPanning pannable, MouseButton mouseButton) {
		this(pannable);

		setMouseButton(mouseButton);
	}

	private MouseListener createMouseListener() {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (!acceptMouseButton(e))
					return;

				lastPoint = e.getPoint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (!acceptMouseButton(e))
					return;

				lastPoint = null;
				component.repaint();
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

				if (lastPoint != null) {
					int deltaX = e.getPoint().x - lastPoint.x;
					int deltaY = e.getPoint().y - lastPoint.y;
					pannable.pan(deltaX, deltaY);
				}

				lastPoint = e.getPoint();
				lastDrag = e.getWhen();
				component.repaint();
			}
		};
	}

	@Override
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
}
