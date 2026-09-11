package com.github.TKnudsen.infoVis.view.painters.grid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * A {@link Grid2DShapeSizePainter} that draws each cell as a size-scaled
 * ellipse, fitted to the cell's own aspect ratio (the same area-and-aspect
 * -ratio formula {@link Grid2DRectangleSizePainter} uses for its
 * rectangles). Unlike {@link Grid2DCircularPainter}, this fills non-square
 * cells properly -- a locked-aspect circle can only ever be as large as a
 * cell's shorter side.
 *
 * @since 2026
 */
public class Grid2DEllipsisPainter extends Grid2DShapeSizePainter {

	public Grid2DEllipsisPainter(double[][] relativeSizes, Color[][] gridColors) {
		super(relativeSizes, gridColors);
	}

	@Override
	protected void drawShape(Graphics2D g2, Rectangle2D cell, double relativeSize) {
		double area = cell.getWidth() * cell.getHeight() * relativeSize;
		double width = Math.sqrt(area * (cell.getWidth() / cell.getHeight()));
		double height = Math.sqrt(area * (cell.getHeight() / cell.getWidth()));

		Ellipse2D ellipse = new Ellipse2D.Double(cell.getCenterX() - 0.5 * width, cell.getCenterY() - 0.5 * height,
				width, height);

		if (fill)
			g2.fill(ellipse);
		else
			g2.draw(ellipse);
	}

}
