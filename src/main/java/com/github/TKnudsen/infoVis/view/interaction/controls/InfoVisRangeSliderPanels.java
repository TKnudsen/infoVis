package com.github.TKnudsen.infoVis.view.interaction.controls;

public class InfoVisRangeSliderPanels {

	public static <T> boolean isShowingTooltips(InfoVisRangeSliderPanel panel) {
		return panel.getxAxisChartPanel().isShowingTooltips();
	}

	public static <T> void setShowingTooltips(InfoVisRangeSliderPanel panel, boolean showingTooltips) {
		panel.getxAxisChartPanel().setShowingTooltips(showingTooltips);
	}
}
