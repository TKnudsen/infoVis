package com.github.TKnudsen.infoVis.view.panels;

import java.util.Objects;

import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.VerticalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.TitlePainter;

/**
 * <p>
 * Static helper methods for {@link InfoVisChartPanel}, e.g. adding a
 * {@link TitlePainter} with a given alignment.
 * </p>
 *
 * @version 1.0
 */
public class InfoVisChartPanels {

	public static TitlePainter addTitle(InfoVisChartPanel panel, String title) {
		return addTitle(panel, title, HorizontalStringAlignment.CENTER, VerticalStringAlignment.UP);
	}

	public static TitlePainter addTitle(InfoVisChartPanel panel, String title,
			HorizontalStringAlignment horizontalStringAlignment, VerticalStringAlignment verticalStringAlignment) {
		Objects.requireNonNull(panel);

		TitlePainter titlePainter = new TitlePainter(title);
		titlePainter.setBackgroundPaint(null);
		titlePainter.setHorizontalStringAlignment(horizontalStringAlignment);
		titlePainter.setVerticalStringAlignment(verticalStringAlignment);
		panel.addChartPainter(titlePainter);

		return titlePainter;
	}

}
