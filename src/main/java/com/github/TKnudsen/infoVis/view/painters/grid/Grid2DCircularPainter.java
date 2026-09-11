package com.github.TKnudsen.infoVis.view.painters.grid;

import java.awt.Color;
import java.awt.Graphics2D;

import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * A {@link Grid2DColorPainter} that draws each cell as a size-scaled dot
 * (radius driven by {@link #setRelativeSizes(double[][])}) instead of a
 * filled rectangle.
 *
 * @since 2013
 */
public class Grid2DCircularPainter extends Grid2DColorPainter implements ITooltip {

	private static final double MAX_RADIUS_FRACTION = 0.30;

	private double[][] relativeSizes;

	public Grid2DCircularPainter(double[][] relativeSizes, Color[][] gridColors) {
		super(gridColors);

		this.relativeSizes = relativeSizes;
	}

	@Override
	public void drawChart(Graphics2D g2) {
		Color previousColor = g2.getColor();

		if (relativeSizes != null && gridColors != null && grid != null)
			for (int i = 0; i < gridColors.length; i++)
				for (int j = 0; j < gridColors[i].length; j++) {
					if (gridColors[i][j] == null)
						continue;

					g2.setColor(gridColors[i][j]);

					double cellPixels = Math.min(grid[i][j].getWidth(), grid[i][j].getHeight());
					double maxRadius = (cellPixels - 2 * stroke.getLineWidth()) * MAX_RADIUS_FRACTION;
					double radius = maxRadius / Math.sqrt(1 / relativeSizes[i][j]);

					DisplayTools.drawPoint(g2, grid[i][j].getCenterX(), grid[i][j].getCenterY(), radius,
							ColorTools.setAlpha(gridColors[i][j], 1.0f), fill);

					if (isDrawOutline() && getBorderPaint() != null) {
						// level of detail: if the grid is too small, the outline transparency is
						// reduced so a dense grid doesn't turn into a solid mesh of borders
						float cellSize = (float) Math.min(rectangle.getWidth() / grid.length,
								rectangle.getHeight() / grid[0].length);
						float alpha;
						if (cellSize > 10)
							alpha = 0.66f;
						else if (cellSize > 5)
							alpha = 0.33f;
						else if (cellSize > 2)
							alpha = 0.16f;
						else if (cellSize > 1)
							alpha = 0.1f;
						else
							alpha = 0.05f;

						g2.setColor(ColorTools.setAlpha(getBorderPaint(), alpha));
						g2.draw(grid[i][j]);
					}
				}

		g2.setColor(previousColor);
	}

	/**
	 * Derives per-cell relative dot sizes in [0, 1] from raw counts: values at or
	 * above {@code breakEvenPoint} are scaled against the maximum count, values
	 * below it (only meaningful for negative counts) against the minimum.
	 *
	 * @param maxCountSetFromExternal use this instead of the data maximum, unless
	 *                                {@link Float#NaN}
	 * @param minCountSetFromExternal use this instead of the data minimum, unless
	 *                                {@link Float#NaN}
	 */
	public static double[][] calculateRelativeSizes(float[][] gridCounts, float maxCountSetFromExternal,
			float minCountSetFromExternal, float breakEvenPoint) {
		if (gridCounts == null)
			return null;

		double[][] sizes = new double[gridCounts.length][gridCounts[0].length];

		float maxCount = Float.isNaN(maxCountSetFromExternal) ? Float.NEGATIVE_INFINITY : maxCountSetFromExternal;
		if (Float.isNaN(maxCountSetFromExternal))
			for (float[] row : gridCounts)
				for (float count : row)
					maxCount = Math.max(maxCount, count);

		for (int i = 0; i < sizes.length; i++)
			for (int j = 0; j < sizes[i].length; j++)
				if (gridCounts[i][j] >= breakEvenPoint && maxCount > 0) {
					float v = Math.max(0, Math.min(1, gridCounts[i][j] / maxCount));
					if (v > 0)
						sizes[i][j] = v;
				}

		float minCount = Float.isNaN(minCountSetFromExternal) ? Float.POSITIVE_INFINITY : minCountSetFromExternal;
		if (Float.isNaN(minCountSetFromExternal))
			for (float[] row : gridCounts)
				for (float count : row)
					minCount = Math.min(minCount, count);

		if (minCount < 0)
			for (int i = 0; i < sizes.length; i++)
				for (int j = 0; j < sizes[i].length; j++)
					if (gridCounts[i][j] < breakEvenPoint)
						sizes[i][j] = Math.max(0, Math.min(1, gridCounts[i][j] / minCount));

		return sizes;
	}

	public double[][] getRelativeSizes() {
		return relativeSizes;
	}

	public void setRelativeSizes(double[][] relativeSizes) {
		this.relativeSizes = relativeSizes;
	}

}
