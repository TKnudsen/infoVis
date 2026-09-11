package com.github.TKnudsen.infoVis.view.painters.grid;

import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.primitives.RectanglePainter;

/**
 * A grid of {@link RectanglePainter}s whose sizes vary within their cell,
 * proportional to a per-cell {@code relativeSizes} value -- each rectangle is
 * centered in its cell and scaled so its area is
 * {@code cellArea * relativeSizes[x][y]}, keeping the cell's aspect ratio.
 *
 * @since 2016
 */
public class Grid2DRectangleSizePainter extends Grid2DPainterPainter<RectanglePainter> {

	protected double[][] relativeSizes;

	protected boolean fill;

	public Grid2DRectangleSizePainter(double[][] relativeSizes) {
		super(createRectanglePainters(relativeSizes));

		this.relativeSizes = relativeSizes;

		setFill(true);
	}

	@Override
	protected void initializePainters() {
		if (painters == null)
			return;

		for (int i = 0; i < painters.length; i++)
			for (int j = 0; j < painters[i].length; j++)
				if (painters[i][j] != null) {
					painters[i][j].setFill(fill);
					painters[i][j].setBackgroundPaint(null);
				}
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null || grid == null)
			return;

		for (int x = 0; x < grid.length; x++)
			for (int y = 0; y < grid[0].length; y++) {
				Rectangle2D cell = grid[x][y];

				double area = cell.getWidth() * cell.getHeight() * relativeSizes[x][y];
				double width = Math.sqrt(area * (cell.getWidth() / cell.getHeight()));
				double height = Math.sqrt(area * (cell.getHeight() / cell.getWidth()));

				Rectangle2D scaled = new Rectangle2D.Double(cell.getCenterX() - 0.5 * width,
						cell.getCenterY() - 0.5 * height, width, height);
				painters[x][y].setRectangle(scaled);
			}
	}

	private static RectanglePainter[][] createRectanglePainters(double[][] relativeSizes) {
		RectanglePainter[][] painters = new RectanglePainter[relativeSizes.length][relativeSizes[0].length];
		for (int x = 0; x < relativeSizes.length; x++)
			for (int y = 0; y < relativeSizes[0].length; y++) {
				painters[x][y] = new RectanglePainter();
				painters[x][y].setDrawOutline(false);
			}
		return painters;
	}

	public boolean isFill() {
		return fill;
	}

	public void setFill(boolean fill) {
		this.fill = fill;

		if (painters != null)
			for (int i = 0; i < painters.length; i++)
				for (int j = 0; j < painters[i].length; j++)
					if (painters[i][j] != null)
						painters[i][j].setFill(fill);
	}

	public double[][] getRelativeSizes() {
		return relativeSizes;
	}

	public void setRelativeSizes(double[][] relativeSizes) {
		this.relativeSizes = relativeSizes;

		painters = createRectanglePainters(relativeSizes);

		recalculateGrid();
	}

}
