package com.github.TKnudsen.infoVis.view.ui.themes;

/**
 * <p>
 * Contract for components that react to {@link VisualizationTheme} changes.
 * </p>
 *
 * @version 1.0
 */
public interface VisualizationThemeHandler {

	public void handleThemeChange(VisualizationTheme oldTheme, VisualizationTheme newTheme);

	public void applyTheme(VisualizationTheme theme);
}
