package com.github.TKnudsen.infoVis.view.interaction.controls.rangeSlider;

/**
 * <p>
 * Static helper methods for {@link InfoVisRangeSliderPanel}.
 * </p>
 *
 * @version 1.0
 */
public class InfoVisRangeSliderPanels {

	public static <T> boolean isShowingTooltips(InfoVisRangeSliderPanel panel) {
		return panel.getXAxisChartPanel().isShowingTooltips();
	}

	public static <T> void setShowingTooltips(InfoVisRangeSliderPanel panel, boolean showingTooltips) {
		panel.getXAxisChartPanel().setShowingTooltips(showingTooltips);
	}
}
