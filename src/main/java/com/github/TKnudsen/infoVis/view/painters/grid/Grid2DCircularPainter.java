package com.github.TKnudsen.infoVis.view.painters.grid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * A {@link Grid2DShapeSizePainter} that draws each cell as a size-scaled
 * circle. Since a circle's width and height are locked together, its
 * diameter is capped by the cell's shorter side -- for non-square cells,
 * {@link Grid2DEllipsisPainter} fills the cell better.
 *
 * @since 2013
 */
public class Grid2DCircularPainter extends Grid2DShapeSizePainter {

	private static final double MAX_RADIUS_FRACTION = 0.5;

	public Grid2DCircularPainter(double[][] relativeSizes, Color[][] gridColors) {
		super(relativeSizes, gridColors);
	}

	@Override
	protected void drawShape(Graphics2D g2, Rectangle2D cell, double relativeSize) {
		double cellPixels = Math.min(cell.getWidth(), cell.getHeight());
		double maxRadius = (cellPixels - 2 * stroke.getLineWidth()) * MAX_RADIUS_FRACTION;
		double radius = maxRadius * Math.sqrt(relativeSize);

		DisplayTools.drawPoint(g2, cell.getCenterX(), cell.getCenterY(), radius, fill);
	}

}
