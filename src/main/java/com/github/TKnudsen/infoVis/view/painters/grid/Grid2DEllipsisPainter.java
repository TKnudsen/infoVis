package com.github.TKnudsen.infoVis.view.painters.grid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * A {@link Grid2DShapeSizePainter} that draws each cell as a size-scaled
 * ellipse, fitted to the cell's own aspect ratio. Unlike
 * {@link Grid2DCircularPainter}, this fills non-square cells properly -- a
 * locked-aspect circle can only ever be as large as a cell's shorter side.
 *
 * <p>
 * Sized by actual filled area rather than bounding-box area: an ellipse
 * inscribed in a box covers only {@code pi/4 (~78.5%)} of that box, so at
 * {@code relativeSize == 1} its ink area matches a same-size
 * {@link Grid2DRectangleSizePainter} cell exactly -- which means the
 * bounding box itself overflows the cell by a fixed ~13% per side at that
 * point. If cells must never be overflown, the fraction is worth revisiting.
 * </p>
 *
 * @since 2026
 */
public class Grid2DEllipsisPainter extends Grid2DShapeSizePainter {

	/**
	 * An ellipse's area is {@code pi/4} of its bounding box's area; this is that
	 * ratio inverted, i.e. the bounding-box area needed so the ellipse's own
	 * filled area equals the target area.
	 */
	private static final double BOUNDING_BOX_AREA_CORRECTION = 4.0 / Math.PI;

	public Grid2DEllipsisPainter(double[][] relativeSizes, Color[][] gridColors) {
		super(relativeSizes, gridColors);
	}

	@Override
	protected void drawShape(Graphics2D g2, Rectangle2D cell, double relativeSize) {
		double targetArea = cell.getWidth() * cell.getHeight() * relativeSize;
		double boundingBoxArea = targetArea * BOUNDING_BOX_AREA_CORRECTION;

		double width = Math.sqrt(boundingBoxArea * (cell.getWidth() / cell.getHeight()));
		double height = Math.sqrt(boundingBoxArea * (cell.getHeight() / cell.getWidth()));

		Ellipse2D ellipse = new Ellipse2D.Double(cell.getCenterX() - 0.5 * width, cell.getCenterY() - 0.5 * height,
				width, height);

		if (fill)
			g2.fill(ellipse);
		else
			g2.draw(ellipse);
	}

}
