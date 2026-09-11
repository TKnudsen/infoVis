package com.github.TKnudsen.infoVis.view.painters.grid;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.painters.primitives.RectanglePainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;

/**
 * A {@link Grid2DColorPainter} where each cell additionally carries its own
 * alpha value.
 *
 * @since 2016
 */
public class Grid2DColorTransparencyPainter extends Grid2DColorPainter {

	private float[][] alpha;

	public Grid2DColorTransparencyPainter(Color[][] gridColors, float[][] alpha) {
		super(gridColors);

		setAlpha(alpha);
	}

	public float[][] getAlpha() {
		return alpha;
	}

	/**
	 * Applies alpha to the internal cell colors without modifying the original
	 * {@link #getGridColors()}.
	 */
	public void setAlpha(float[][] alpha) {
		this.alpha = alpha;

		Color[][] gridColors = getGridColors();
		RectanglePainter[][] painters = getPainters();

		for (int i = 0; i < gridColors.length; i++)
			for (int j = 0; j < gridColors[i].length; j++)
				painters[i][j]
						.setPaint(ColorTools.setAlpha(gridColors[i][j], Math.min(1.0f, Math.max(0.0f, alpha[i][j]))));
	}

}
