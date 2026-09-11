package com.github.TKnudsen.infoVis.view.painters.grid;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.painters.primitives.RectanglePainter;

/**
 * A grid of {@link RectanglePainter}s, one per cell, filled with a
 * corresponding {@link Color} from a {@code Color[][]}.
 *
 * @since 2015
 */
public class Grid2DColorPainter extends Grid2DPainterPainter<RectanglePainter> {

	protected Color[][] gridColors;

	/** Whether cells are filled or drawn as outlines only. Read by subclasses. */
	protected boolean fill = true;

	public Grid2DColorPainter(Color[][] gridColors) {
		super(createRectanglePainters(gridColors));

		this.gridColors = gridColors;
	}

	private static RectanglePainter[][] createRectanglePainters(Color[][] gridColors) {
		if (gridColors == null)
			return null;

		RectanglePainter[][] painters = new RectanglePainter[gridColors.length][gridColors[0].length];
		for (int x = 0; x < gridColors.length; x++)
			for (int y = 0; y < gridColors[0].length; y++) {
				painters[x][y] = new RectanglePainter();
				painters[x][y].setPaint(gridColors[x][y]);
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

	public Color[][] getGridColors() {
		return gridColors;
	}

	public void setGridColors(Color[][] gridColors) {
		this.gridColors = gridColors;

		painters = createRectanglePainters(gridColors);

		recalculateGrid();
	}

}
