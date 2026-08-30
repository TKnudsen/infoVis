package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative.tools.QualitativeColormapsTools;

/**
 * <p>
 * Qualitative color map sampling evenly-spaced hues around the HSB color
 * circle at a fixed saturation and brightness.
 * </p>
 *
 * @version 1.0
 */
public class HueQualitativeColormap extends AbstractQualitativeColorMap {

	private int brightness;
	private int saturation;

	public HueQualitativeColormap() {
		this(128, 128);
	}

	public HueQualitativeColormap(int brightness, int saturation) {
		this.brightness = brightness;
		this.saturation = saturation;
	}

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
		this.colors = getColorArray(355, saturation, brightness);
	}

	public static Color[] getColorArray(int number, int bright, int sat) {
		Color[] color = new Color[number];

		double saturation = sat;
		double brightness = bright;
		double hue = 0;

		for (int i = 0; i < number; i++) {
			hue = i * (255 / (double) number);
			color[i] = new Color(
					Color.HSBtoRGB((float) (1 - (hue / 255)), (float) (saturation / 255), (float) (brightness / 255)));
		}
		return color;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.HSB;
	}

	@Override
	public String getName() {
		return "HueQualitativeColormap";
	}

	@Override
	public String getDescription() {
		return "Hues of the color circle";
	}

}
