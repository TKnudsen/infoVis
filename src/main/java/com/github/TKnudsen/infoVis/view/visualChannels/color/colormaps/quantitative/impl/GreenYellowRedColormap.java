package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @version 1.01
 * @since 2012-03-02
 */
public class GreenYellowRedColormap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(255, 0, 0), new Color(255, 5, 1), new Color(255, 10, 2), new Color(255, 15, 3),
				new Color(255, 21, 5), new Color(255, 32, 7), new Color(255, 37, 8), new Color(255, 42, 10),
				new Color(255, 47, 11), new Color(255, 53, 12), new Color(255, 63, 15), new Color(255, 68, 16),
				new Color(255, 73, 17), new Color(255, 77, 18), new Color(255, 82, 20), new Color(255, 92, 22),
				new Color(255, 97, 23), new Color(255, 102, 25), new Color(255, 106, 26), new Color(255, 111, 28),
				new Color(255, 120, 30), new Color(255, 124, 31), new Color(255, 129, 33), new Color(255, 133, 34),
				new Color(255, 142, 36), new Color(255, 146, 38), new Color(255, 150, 39), new Color(255, 155, 40),
				new Color(255, 159, 41), new Color(255, 167, 44), new Color(255, 171, 45), new Color(255, 175, 46),
				new Color(255, 179, 48), new Color(255, 182, 49), new Color(255, 190, 51), new Color(255, 194, 53),
				new Color(255, 197, 54), new Color(255, 201, 56), new Color(255, 205, 57), new Color(255, 212, 59),
				new Color(255, 216, 61), new Color(255, 219, 62), new Color(255, 223, 63), new Color(255, 229, 66),
				new Color(255, 232, 67), new Color(255, 236, 68), new Color(255, 240, 69), new Color(255, 242, 71),
				new Color(255, 248, 73), new Color(255, 251, 74), new Color(255, 255, 76), new Color(252, 255, 77),
				new Color(249, 255, 79), new Color(243, 255, 81), new Color(240, 255, 82), new Color(237, 255, 84),
				new Color(234, 255, 85), new Color(232, 255, 86), new Color(227, 255, 89), new Color(224, 255, 90),
				new Color(222, 255, 91), new Color(219, 255, 92), new Color(214, 255, 95), new Color(212, 255, 96),
				new Color(210, 255, 97), new Color(208, 255, 99), new Color(205, 255, 100), new Color(201, 255, 102),
				new Color(199, 255, 104), new Color(197, 255, 105), new Color(195, 255, 107), new Color(193, 255, 108),
				new Color(190, 255, 110), new Color(188, 255, 112), new Color(186, 255, 113), new Color(184, 255, 114),
				new Color(182, 255, 115), new Color(179, 255, 118), new Color(178, 255, 119), new Color(176, 255, 120),
				new Color(175, 255, 122), new Color(172, 255, 124), new Color(171, 255, 125), new Color(170, 255, 127),
				new Color(168, 255, 128), new Color(167, 255, 130), new Color(165, 255, 132), new Color(164, 255, 133),
				new Color(163, 255, 135), new Color(162, 255, 136), new Color(161, 255, 137), new Color(159, 255, 140),
				new Color(158, 255, 141), new Color(157, 255, 142), new Color(156, 255, 143), new Color(156, 255, 145),
				new Color(155, 255, 147), new Color(154, 255, 148), new Color(153, 255, 150), new Color(152, 255, 151),
				new Color(152, 255, 152)

		};
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.BIPOLAR_GreenYellowRed;
	}

	public String toString() {
		return "Green Yellow Red";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new GreenYellowRedColormap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
