package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.List;

import com.github.TKnudsen.infoVis.view.interaction.IToolTipPaintable;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 *
 */
public class TooltipHandler extends InteractionHandler {

	private final MouseListener mouseListener;
	private final MouseMotionListener mouseMotionListener;

	public TooltipHandler() {
		this.mouseListener = createMouseListener();
		this.mouseMotionListener = createMouseMotionListener();
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

	private MouseListener createMouseListener() {
		return new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				if (component instanceof IToolTipPaintable)
					((IToolTipPaintable) component).setToolTipPainter(null);
			}
		};
	}

	private MouseMotionListener createMouseMotionListener() {
		return new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				ChartPainter toolTipPainter = null;
				if (component instanceof InfoVisChartPanel)
					if (((InfoVisChartPanel) component).isShowingTooltips()) {
						List<ChartPainter> chartPainters = ((InfoVisChartPanel) component).getChartPainters();
						for (ChartPainter chartPainter : chartPainters) {
							if (chartPainter instanceof ITooltip) {
								toolTipPainter = ((ITooltip) chartPainter).getTooltip(e.getPoint());
								break;
							}
						}
					}

				if (component instanceof IToolTipPaintable)
					if (((InfoVisChartPanel) component).isShowingTooltips())
						((IToolTipPaintable) component).setToolTipPainter(toolTipPainter);
			}
		};
	}

}
