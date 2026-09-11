package com.github.TKnudsen.infoVis.view.painters.grid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;

/**
 * Base class for {@link Grid2DColorPainter}s that draw each cell as a single
 * size-scaled shape (driven by {@link #setRelativeSizes(double[][])}) rather
 * than a filled rectangle -- e.g. {@link Grid2DCircularPainter} (a circle) or
 * {@link Grid2DEllipsisPainter} (an ellipse fitted to the cell's aspect
 * ratio). Subclasses implement only {@link #drawShape(Graphics2D, Rectangle2D,
 * double)}; the per-cell iteration, alpha-graded outline, and relative-size
 * bookkeeping are shared here.
 *
 * @since 2026
 */
public abstract class Grid2DShapeSizePainter extends Grid2DColorPainter implements ITooltip {

	protected double[][] relativeSizes;

	protected Grid2DShapeSizePainter(double[][] relativeSizes, Color[][] gridColors) {
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

					g2.setColor(ColorTools.setAlpha(gridColors[i][j], 1.0f));
					drawShape(g2, grid[i][j], relativeSizes[i][j]);

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
	 * Draws (fills, if {@link #isFill()}) the shape for one cell, sized
	 * proportionally to {@code relativeSize} in {@code [0, 1]}. {@code g2}'s
	 * current paint is already set to this cell's color.
	 */
	protected abstract void drawShape(Graphics2D g2, Rectangle2D cell, double relativeSize);

	/**
	 * Derives per-cell relative sizes in [0, 1] from raw counts: values at or
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
