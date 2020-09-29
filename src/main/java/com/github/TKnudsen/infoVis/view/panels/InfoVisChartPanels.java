package com.github.TKnudsen.infoVis.view.panels;

import java.util.Objects;

import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.VerticalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.TitlePainter;

public class InfoVisChartPanels {

	public static void addTitle(InfoVisChartPanel panel, String title) {
		addTitle(panel, title, HorizontalStringAlignment.CENTER, VerticalStringAlignment.UP);
	}

	public static void addTitle(InfoVisChartPanel panel, String title,
			HorizontalStringAlignment horizontalStringAlignment, VerticalStringAlignment verticalStringAlignment) {
		Objects.requireNonNull(panel);

		TitlePainter titlePainter = new TitlePainter(title);
		titlePainter.setBackgroundPaint(null);
		titlePainter.setHorizontalStringAlignment(horizontalStringAlignment);
		titlePainter.setVerticalStringAlignment(verticalStringAlignment);
		panel.addChartPainter(titlePainter);
	}

}
