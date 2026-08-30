package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @version 1.01
 * @since 2012
 */
public class UniColorMapCroppedAndInverted extends AbstractColorMap1D {

	protected static UniColorMapCroppedAndInverted instance;

	@Override
	protected void setColors() {
		Color[] cs = new Color[] { new Color(224, 239, 173), new Color(219, 238, 171), new Color(215, 238, 169),
				new Color(210, 237, 167), new Color(206, 237, 165), new Color(201, 236, 163), new Color(196, 236, 161),
				new Color(191, 235, 160), new Color(186, 234, 159), new Color(182, 233, 158), new Color(177, 232, 157),
				new Color(172, 231, 156), new Color(167, 229, 156), new Color(162, 228, 155), new Color(157, 226, 155),
				new Color(152, 224, 155), new Color(148, 222, 155), new Color(143, 220, 155), new Color(139, 218, 155),
				new Color(134, 216, 156), new Color(130, 213, 156), new Color(126, 210, 157), new Color(121, 207, 157),
				new Color(118, 204, 158), new Color(114, 201, 159), new Color(110, 197, 160), new Color(107, 194, 160),
				new Color(103, 190, 161), new Color(100, 186, 162), new Color(97, 182, 163), new Color(94, 178, 164),
				new Color(92, 174, 165), new Color(89, 170, 165), new Color(87, 165, 166), new Color(85, 161, 167),
				new Color(83, 156, 167), new Color(81, 151, 168), new Color(80, 146, 168), new Color(79, 142, 169),
				new Color(78, 137, 169), new Color(77, 132, 169), new Color(76, 127, 169), new Color(76, 122, 168),
				new Color(75, 117, 168), new Color(75, 112, 168), new Color(75, 107, 167), new Color(75, 102, 166),
				new Color(76, 98, 165), new Color(76, 93, 164) };
		// , new Color(77, 88, 163), new Color(77, 84, 161), new Color(78, 79, 159), new
		// Color(79, 75, 158) };
		// new Color(80, 71, 156), new Color(81, 67, 153), new Color(83, 63,
		// 151)};
		// , new Color(84, 59, 148), new Color(85, 55, 146), new Color(87, 52,
		// 143), new Color(88, 48, 140), new Color(89, 45, 137), new Color(91,
		// 42, 133),
		// new Color(92, 39, 130), new Color(93, 37, 126), new Color(95, 34,
		// 123), new Color(96, 32, 119), new Color(97, 30, 115), new Color(98,
		// 28, 111), new Color(99, 27, 107), new Color(100, 25, 103), new
		// Color(101, 24, 99)};
		// ,
		// new Color(102, 23, 94), new Color(102, 22, 90), new Color(103, 21,
		// 86), new Color(103, 21, 82), new Color(104, 20, 77), new Color(104,
		// 20, 73), new Color(104, 20, 69) };

		List<Color> colors_ = Arrays.asList(cs);
		Collections.reverse(colors_);

		colors = new Color[colors_.size()];
		colors = colors_.toArray(colors);
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UniColor;
	}

	public String toString() {
		return "Uni Color";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new UniColorMapCroppedAndInverted();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
