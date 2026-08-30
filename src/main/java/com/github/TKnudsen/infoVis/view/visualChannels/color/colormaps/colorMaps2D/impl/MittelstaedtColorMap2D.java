package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces.ColorCIELab;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces.ColorConversions;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorSpaces.ColorXYZ;

/**
 * <p>
 * "Mittelstaedt et al.": a CIELab-based colormap with perceptually
 * (almost) equi-distant corners. Cataloged in Bernard et al., "A Survey and
 * Task-Based Quality Assessment of Static 2D Colormaps" (SPIE VDA, 2015).
 * That survey's own citation for it (superscript 2 in Table 1) points to
 * Steiger, M., Bernard, J., Mittelstaedt, S., Luecke-Tieke, H., Keim, D.,
 * May, T., and Kohlhammer, J., "Visual Analysis of Time-Series Similarities
 * for Anomaly Detection in Sensor Networks," Computer Graphics Forum
 * (EuroVis) 33(3) (2014), Section 4.1 / Figure 5, which spells out the
 * construction exactly: "We use four perceptually distant colors and
 * interpolate between these colors [...] namely yellow, cyan, red and blue
 * [...] The corner colors are equalized in intensity and saturation in the
 * HSI color space and then interpolated in the CIELAB color space." This
 * class follows that description precisely: the four hues are equalized to
 * full, equal saturation and brightness in HSB, converted to CIELAB, placed
 * at the four corners with complementary hues on each diagonal (Yellow
 * opposite Blue, Red opposite Cyan -- the placement itself is not spelled
 * out numerically in the source and is this class's own choice, consistent
 * with the paper's stated goal of separating complementary tones), and
 * bilinearly interpolated in CIELAB space before converting back to RGB.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class MittelstaedtColorMap2D extends AbstractColorMap2D {

	public MittelstaedtColorMap2D() {
		super((x, y) -> interpolate(x, y), ColorSpace.CIELAB);
	}

	private static final ColorCIELab YELLOW = toLab(Color.getHSBColor(60 / 360f, 1f, 1f));
	private static final ColorCIELab CYAN = toLab(Color.getHSBColor(180 / 360f, 1f, 1f));
	private static final ColorCIELab RED = toLab(Color.getHSBColor(0f, 1f, 1f));
	private static final ColorCIELab BLUE = toLab(Color.getHSBColor(240 / 360f, 1f, 1f));

	private static ColorCIELab toLab(Color c) {
		ColorXYZ xyz = ColorConversions.convertRGBtoXYZ(c.getRGB());
		return ColorConversions.convertXYZtoCIELab(xyz);
	}

	private static Color interpolate(double x, double y) {
		double w00 = (1 - x) * (1 - y);
		double w10 = x * (1 - y);
		double w01 = (1 - x) * y;
		double w11 = x * y;

		// (0,0)=Yellow, (1,0)=Red, (0,1)=Cyan, (1,1)=Blue -- complementary on both
		// diagonals: Yellow/Blue and Red/Cyan.
		double L = w00 * YELLOW.L + w10 * RED.L + w01 * CYAN.L + w11 * BLUE.L;
		double a = w00 * YELLOW.a + w10 * RED.a + w01 * CYAN.a + w11 * BLUE.a;
		double b = w00 * YELLOW.b + w10 * RED.b + w01 * CYAN.b + w11 * BLUE.b;

		ColorXYZ xyz = ColorConversions.convertCIELabtoXYZ(L, a, b);
		return new Color(ColorConversions.convertXYZtoRGB(xyz));
	}

	@Override
	public String getName() {
		return "Mittelstaedt et al.";
	}

	@Override
	public String getDescription() {
		return "CIELab-interpolated colormap with equalized Yellow/Cyan/Red/Blue corners "
				+ "(Steiger, Bernard, Mittelstaedt et al. 2014)";
	}

}
