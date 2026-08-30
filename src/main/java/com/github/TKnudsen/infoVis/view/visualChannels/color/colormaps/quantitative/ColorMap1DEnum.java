package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative;

/**
 * Identifies the kind of gradient a concrete {@link AbstractColorMap1D}
 * implements - bipolar (diverging around a neutral midpoint) or unipolar
 * (a single monotonic gradient) - as returned by its
 * {@link AbstractColorMap1D#getColorMapType()}.
 *
 * @since 2011
 */
public enum ColorMap1DEnum {
	BIPOLAR_BlueRed, BIPOLAR_RedBlue, BIPOLAR_RedGreen, BIPOLAR_BlueOrange, BIPOLAR_BlueYellow_EvenBrightness,
	UNIPOLAR_BlueToYellow, BIPOLAR_GreenPurple, BIPOLAR_PurpleGreen, BIPOLAR_GreenYellowRed, GreenYellowRedColorMap,
	GreenYellowRedLowSaturationColorMap, UNIPOLAR_BoraBora, UNIPOLAR_Gray, UNIPOLAR_WhiteBlue, UNIPOLAR_GrayRed,
	UNIPOLAR_GrayGreen, UNIPOLAR_GrayOrange, UNIPOLAR_GrayPurple, UNIPOLAR_GrayGrassGreen, UNIPOLAR_WhiteRed,
	UNIPOLAR_LightGrayBlack, UNIPOLAR_WhiteDarkGray, UNIPOLAR_WhiteBlack, UNIPOLAR_BlueWhite,
	UNIPOLAR_DarkBlueGreenLightYellowHueLightness, UNIPOLAR_LightYellowLightGreenTealNavy, UNIPOLAR_GrayBlue, UniColor,
	UNIPOLAR_DarkGrayAlpha, UNIPOLAR_WhiteAlpha, UNIPOLAR_WhiteSaturationReducedAlpha, UNIPOLAR_BlackAlpha,
	UNIPOLAR_BlackSaturationReducedAlpha, UNIPOLAR_GrayYellow, UNIPOLAR_GrayLighterBlue, UNIPOLAR_GrayWhite,
	UNIPOLAR_Rainbow, UNIPOLAR_GrayUserD, UNIPOLAR_GrayUserDefined, UserDefined
}
