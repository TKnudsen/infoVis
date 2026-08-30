package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl;

import java.awt.Color;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.ColorMap1DEnum;

/**
 * @version 1.02 BoraBoraColorMap() Constructor added (2012-01-10)
 * @since 2011-11-03
 */
public class BoraBoraColorMap extends AbstractColorMap1D {

	protected static AbstractColorMap instance;

	protected BoraBoraColorMap() {
		super();
	}

	@Override
	public ColorMap1DEnum getColorMapType() {
		return ColorMap1DEnum.UNIPOLAR_BoraBora;
	}

	@Override
	protected void setColors() {
		colors = new Color[] { new Color(0, 77, 102), new Color(1, 77, 102), new Color(2, 78, 103),
				new Color(3, 79, 103), new Color(4, 79, 104), new Color(5, 80, 104), new Color(6, 81, 104),
				new Color(7, 81, 105), new Color(8, 82, 105), new Color(9, 83, 106), new Color(10, 84, 106),
				new Color(11, 84, 106), new Color(12, 85, 107), new Color(13, 86, 107), new Color(14, 86, 108),
				new Color(15, 87, 108), new Color(16, 88, 108), new Color(17, 88, 109), new Color(18, 89, 109),
				new Color(19, 90, 110), new Color(20, 90, 110), new Color(21, 91, 110), new Color(22, 92, 111),
				new Color(23, 93, 111), new Color(24, 93, 112), new Color(25, 94, 112), new Color(26, 95, 112),
				new Color(27, 95, 113), new Color(28, 96, 113), new Color(29, 97, 114), new Color(30, 98, 114),
				new Color(31, 98, 114), new Color(32, 99, 115), new Color(33, 100, 115), new Color(34, 100, 116),
				new Color(35, 101, 116), new Color(36, 102, 116), new Color(37, 102, 117), new Color(38, 103, 117),
				new Color(39, 104, 118), new Color(40, 105, 118), new Color(41, 105, 118), new Color(42, 106, 119),
				new Color(43, 107, 119), new Color(44, 107, 120), new Color(45, 108, 120), new Color(46, 109, 120),
				new Color(47, 109, 121), new Color(48, 110, 121), new Color(49, 111, 122), new Color(50, 112, 122),
				new Color(51, 112, 122), new Color(52, 113, 123), new Color(53, 114, 123), new Color(54, 114, 124),
				new Color(55, 115, 124), new Color(56, 116, 124), new Color(57, 116, 125), new Color(58, 117, 125),
				new Color(59, 118, 126), new Color(60, 119, 126), new Color(61, 119, 126), new Color(62, 120, 127),
				new Color(63, 121, 127), new Color(64, 121, 128), new Color(65, 122, 128), new Color(66, 123, 128),
				new Color(67, 123, 129), new Color(68, 124, 129), new Color(69, 125, 130), new Color(70, 126, 130),
				new Color(71, 126, 130), new Color(72, 127, 131), new Color(73, 128, 131), new Color(74, 128, 132),
				new Color(75, 129, 132), new Color(76, 130, 132), new Color(77, 130, 133), new Color(78, 131, 133),
				new Color(79, 132, 134), new Color(80, 132, 134), new Color(81, 133, 134), new Color(82, 134, 135),
				new Color(83, 135, 135), new Color(84, 135, 136), new Color(85, 136, 136), new Color(86, 137, 136),
				new Color(87, 137, 137), new Color(88, 138, 137), new Color(89, 139, 138), new Color(90, 140, 138),
				new Color(91, 140, 138), new Color(92, 141, 139), new Color(93, 142, 139), new Color(94, 142, 140),
				new Color(95, 143, 140), new Color(96, 144, 140), new Color(97, 144, 141), new Color(98, 145, 141),
				new Color(99, 146, 142), new Color(100, 147, 142), new Color(101, 147, 142), new Color(102, 148, 143),
				new Color(103, 149, 143), new Color(104, 149, 144), new Color(105, 150, 144), new Color(106, 151, 144),
				new Color(107, 151, 145), new Color(108, 152, 145), new Color(109, 153, 146), new Color(110, 154, 146),
				new Color(111, 154, 146), new Color(112, 155, 147), new Color(113, 156, 147), new Color(114, 156, 148),
				new Color(115, 157, 148), new Color(116, 158, 148), new Color(117, 158, 149), new Color(118, 159, 149),
				new Color(119, 160, 150), new Color(120, 161, 150), new Color(121, 161, 150), new Color(122, 162, 151),
				new Color(123, 163, 151), new Color(124, 163, 152), new Color(125, 164, 152), new Color(126, 165, 152),
				new Color(127, 165, 153), new Color(128, 166, 153), new Color(129, 167, 154), new Color(130, 167, 154),
				new Color(131, 168, 154), new Color(132, 169, 155), new Color(133, 170, 155), new Color(134, 170, 156),
				new Color(135, 171, 156), new Color(136, 172, 156), new Color(137, 172, 157), new Color(138, 173, 157),
				new Color(139, 174, 158), new Color(140, 175, 158), new Color(141, 175, 158), new Color(142, 176, 159),
				new Color(143, 177, 159), new Color(144, 177, 160), new Color(145, 178, 160), new Color(146, 179, 160),
				new Color(147, 179, 161), new Color(148, 180, 161), new Color(149, 181, 162), new Color(150, 181, 162),
				new Color(151, 182, 162), new Color(152, 183, 163), new Color(153, 184, 163), new Color(154, 184, 164),
				new Color(155, 185, 164), new Color(156, 186, 164), new Color(157, 186, 165), new Color(158, 187, 165),
				new Color(159, 188, 166), new Color(160, 188, 166), new Color(161, 189, 166), new Color(162, 190, 167),
				new Color(163, 191, 167), new Color(164, 191, 168), new Color(165, 192, 168), new Color(166, 193, 168),
				new Color(167, 193, 169), new Color(168, 194, 169), new Color(169, 195, 170), new Color(170, 195, 170),
				new Color(171, 196, 170), new Color(172, 197, 171), new Color(173, 198, 171), new Color(174, 198, 172),
				new Color(175, 199, 172), new Color(176, 200, 172), new Color(177, 200, 173), new Color(178, 201, 173),
				new Color(179, 202, 174), new Color(180, 203, 174), new Color(181, 203, 174), new Color(182, 204, 175),
				new Color(183, 205, 175), new Color(184, 205, 176), new Color(185, 206, 176), new Color(186, 207, 176),
				new Color(187, 207, 177), new Color(188, 208, 177), new Color(189, 209, 178), new Color(190, 210, 178),
				new Color(191, 210, 178), new Color(192, 211, 179), new Color(193, 212, 179), new Color(194, 212, 180),
				new Color(195, 213, 180), new Color(196, 214, 180), new Color(197, 214, 181), new Color(198, 215, 181),
				new Color(199, 216, 182), new Color(200, 216, 182), new Color(201, 217, 182), new Color(202, 218, 183),
				new Color(203, 219, 183), new Color(204, 219, 184), new Color(205, 220, 184), new Color(206, 221, 184),
				new Color(207, 221, 185), new Color(208, 222, 185), new Color(209, 223, 186), new Color(210, 224, 186),
				new Color(211, 224, 186), new Color(212, 225, 187), new Color(213, 226, 187), new Color(214, 226, 188),
				new Color(215, 227, 188), new Color(216, 228, 188), new Color(217, 228, 189), new Color(218, 229, 189),
				new Color(219, 230, 190), new Color(220, 230, 190), new Color(221, 231, 190), new Color(222, 232, 191),
				new Color(223, 233, 191), new Color(224, 233, 192), new Color(225, 234, 192), new Color(226, 235, 192),
				new Color(227, 235, 193), new Color(228, 236, 193), new Color(229, 237, 194), new Color(230, 238, 194),
				new Color(231, 238, 194), new Color(232, 239, 195), new Color(233, 240, 195), new Color(234, 240, 196),
				new Color(235, 241, 196), new Color(236, 242, 196), new Color(237, 242, 197), new Color(238, 243, 197),
				new Color(239, 244, 198), new Color(240, 244, 198), new Color(241, 245, 198), new Color(242, 246, 199),
				new Color(243, 247, 199), new Color(244, 247, 200), new Color(245, 248, 200), new Color(246, 249, 200),
				new Color(247, 249, 201), new Color(248, 250, 201), new Color(249, 251, 202), new Color(250, 251, 202),
				new Color(251, 252, 202), new Color(252, 253, 203), new Color(253, 254, 203), new Color(254, 254, 204),
				new Color(255, 255, 204) };
	}

	public String toString() {
		return "Bora Bora";
	}

	/**
	 * @return the shared singleton instance of this color map
	 */
	public static synchronized AbstractColorMap getInstance() {
		if (instance == null) {
			instance = new BoraBoraColorMap();
		}
		return instance;
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}
}
