package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.CieCamColors;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative.tools.QualitativeColormapsTools;

/**
 * <p>
 * Qualitative color map sampling evenly-spaced colors from
 * {@link CieCamColors}' perceptually uniform CIECAM02 palette.
 * </p>
 *
 * @version 1.0
 */
public class CieCamColorsQualitativeColormap extends AbstractQualitativeColorMap {

	private List<Color> cieCamColors;

	@Override
	public Color[] getColors(int count) {
		// create colors
		List<Color> colors = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			int index = (int) (i / (float) count * this.colors.length);
			colors.add(this.colors[index]);
		}

		List<Color> cropSampledSubset = QualitativeColormapsTools.cropSampledSubset(colors, count);
		return cropSampledSubset.toArray(new Color[cropSampledSubset.size()]);
	}

	@Override
	protected void setColors() {
		cieCamColors = CieCamColors.L65C65;
		colors = new Color[cieCamColors.size()];
		colors = cieCamColors.toArray(colors);
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new CieCamColorsQualitativeColormap();
		}
		return instance;
	}
}
