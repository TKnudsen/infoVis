package com.github.TKnudsen.infoVis.view.painters.spline;

import java.awt.Graphics2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.visualPrimitives.Bspline;

/**
 * Renders a {@link Bspline}.
 *
 * @version 1.1 -- data is now immutable, supplied through the constructor
 *          (matching the other painters in this package) instead of via a
 *          setData mutator.
 * @since 2019
 */
public class BsplinePainter extends ChartPainter {

	private final Bspline data;
	private int translationX = 0;
	private int translationY = 0;

	public BsplinePainter(Bspline data) {
		if (data == null)
			throw new IllegalArgumentException("data must not be null");

		this.data = data;
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		g2.translate(translationX, translationY);
		data.paint(g2);
		g2.translate(-translationX, -translationY);
	}

	public Bspline getData() {
		return data;
	}

	public void setTranslation(int x, int y) {
		translationX = x;
		translationY = y;
	}

}
