package com.github.TKnudsen.infoVis.view.panels;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * An {@link InfoVisChartPanel} wrapping a single painter, clipped to the
 * panel's own bounds before each paint. Subclasses access the wrapped painter
 * directly via the strongly-typed {@link #painter} field (rather than through
 * {@link InfoVisChartPanel}'s generic chart-painter list), since they typically
 * need the painter's own subtype-specific API (e.g. tool-tip lookup by pixel
 * position).
 *
 * @param <T> the wrapped painter's concrete type
 *
 * @version 1.1
 * @since 2020
 */
public class ClippingPainterPanel<T extends ChartPainter> extends InfoVisChartPanel {

	private static final long serialVersionUID = 5251784318376930484L;

	protected final T painter;

	public ClippingPainterPanel(T painter) {
		super(painter);

		this.painter = painter;
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (getParent() == null)
			return;

		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1);
		Shape oldClip = g.getClip();
		g.setClip(rect);
		try {
			super.paintComponent(g);
		} finally {
			g.setClip(oldClip);
		}
	}

}
