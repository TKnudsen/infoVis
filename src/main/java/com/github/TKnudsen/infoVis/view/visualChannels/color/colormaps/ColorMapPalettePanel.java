package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.view.ColorMapPanel;

/**
 * <p>
 * {@link ColorMapPanel} variant that also draws min/max labels at the ends
 * of the color map.
 * </p>
 *
 * @version 1.0
 */
public class ColorMapPalettePanel extends ColorMapPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3374612626235119784L;

	private final String minLabel;
	private final String maxLabel;

	public ColorMapPalettePanel(AbstractColorMap colorMap) {
		this(colorMap, "MIN", "MAX");
	}

	public ColorMapPalettePanel(AbstractColorMap colorMap, String minLabel, String maxLabel) {
		super(colorMap);

		this.minLabel = minLabel;
		this.maxLabel = maxLabel;
	}

	public void drawColorMap(Graphics2D g) {
		Rectangle bounds = getBounds();

		AbstractColorMap colorMap = (AbstractColorMap) this.colorMap;
		// colorMap.drawColormap(bounds, g, 1f, false, "MIN", "MAX");
		colorMap.drawColormap(bounds, g, 1f, false, minLabel, maxLabel);
	}
}
