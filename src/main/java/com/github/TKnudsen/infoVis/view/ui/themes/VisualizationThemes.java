package com.github.TKnudsen.infoVis.view.ui.themes;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.ui.NimbusUITools;
import com.github.TKnudsen.infoVis.view.ui.themes.VisualizationTheme.Builder;

/**
 * <p>
 * Static factory for the built-in {@link VisualizationTheme} presets:
 * Nimbus-light, classic, dark, and one derived from
 * {@link NimbusUITools}.
 * </p>
 *
 * @version 1.0
 */
public class VisualizationThemes {

	public static VisualizationTheme nimbusLight() {
		Color bg = new Color(237, 237, 237);
		Color border = new Color(170, 170, 170);
		Color font = Color.BLACK;

		return new Builder(bg, border, font).neutralColor(new Color(64, 64, 64)).binColor(new Color(100, 100, 100))
				.darkTheme(false).build(); // Hatching colors auto-generated
	}

	public static VisualizationTheme classic() {
		return new Builder(Color.WHITE, new Color(170, 170, 170), Color.BLACK).neutralColor(new Color(207, 207, 207))
				.binColor(new Color(207, 207, 207)).darkTheme(false).build(); // Hatching colors auto-generated
	}

	public static VisualizationTheme dark() {
		Color bg = new Color(30, 30, 30);
		Color font = new Color(220, 220, 220);
		Color border = new Color(80, 80, 80);

		return new Builder(bg, border, font).neutralColor(new Color(180, 180, 180)).binColor(new Color(150, 150, 150))
				.highlightColor(new Color(100, 100, 100)).darkTheme(true).build(); // Hatching colors auto-generated
	}

	public static VisualizationTheme fromNimbusUITools() {
		return new Builder(NimbusUITools.getBackgroundColor(), NimbusUITools.getBorderColor(),
				NimbusUITools.getFonColor()).darkTheme(true).build(); // Hatching colors auto-generated
	}
}
