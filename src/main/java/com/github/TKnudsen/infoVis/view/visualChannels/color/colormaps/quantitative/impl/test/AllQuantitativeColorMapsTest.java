package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.ui.NimbusUITools;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorMapPalettePanel;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.BlackAlphaColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.BlackSaturationReduced_AlphaColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.BlueHSBBrightnessGradientColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.BlueOrangeBipolarColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.BlueRedColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.BlueToOrangePocoetalColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.BlueWhiteColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.BlueYellowEvenBrightnessBipolarColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.BoraBoraColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.DarkBlueGreenLightYellowHueLightnessColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.DarkGrayAlphaColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.DarkGrayToBlueColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.DarkGrayToGreenColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.DarkGrayToMagentaColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.DarkGrayToOrangeColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.DarkGrayToPurpleColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.DarkGrayToUserDefinedColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.DarkGrayToWhiteColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.GrayBlueColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.GrayGrassGreenColormap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.GrayGreenColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.GrayLighterBlueColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.GrayPurpleColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.GrayRedColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.GrayUnipolarColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.GrayYellowColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.GreenPurpleBipolarColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.GreenYellowRedColormap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.GreenYellowRedLowSaturationColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.LightGrayBlackColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.LightYellowLightGreenTealNavy;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.OrangeHSBBrightnessGradientColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.PurpleGreenBipolarColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.RainbowColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.RedBlueBipolarColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.UniColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.UniColorMapCroppedAndInverted;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.UserDefinedToBlueColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.UserDefinedToRedColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.WhiteAlphaColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.WhiteBlackColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.WhiteBlueColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.WhiteDarkGrayColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.WhiteRedColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.WhiteSaturationReduced_AlphaColorMap1D;

/**
 * <p>
 * One row per migrated quantitative colormap -- label on the left,
 * {@link ColorMapPalettePanel}'s gradient on the right -- stacked into a
 * single scrollable grid inside one {@code SVGFrame}, rather than one popup
 * window per colormap (see {@code ColorMapTester} in the legacy {@code
 * ColorMapLib} for that older style). Meant as a quick visual survey of the
 * whole quantitative family at once: run it after adding a new colormap to
 * {@code quantitative.impl} to see where it lands relative to the others.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class AllQuantitativeColorMapsTest {

	private static final int ROW_HEIGHT = 36;

	public static void main(String[] args) {
		
		NimbusUITools.switchToNimbus();

		AbstractColorMap[] colorMaps = { BlackAlphaColorMap.getInstance(),
				BlackSaturationReduced_AlphaColorMap.getInstance(), BlueHSBBrightnessGradientColorMap.getInstance(),
				BlueOrangeBipolarColorMap.getInstance(), BlueRedColorMap.getInstance(),
				BlueToOrangePocoetalColorMap.getInstance(), BlueWhiteColorMap.getInstance(),
				BlueYellowEvenBrightnessBipolarColorMap.getInstance(), BoraBoraColorMap.getInstance(),
				DarkBlueGreenLightYellowHueLightnessColorMap.getInstance(), DarkGrayAlphaColorMap1D.getInstance(),
				DarkGrayToBlueColorMap.getInstance(), DarkGrayToGreenColorMap.getInstance(),
				DarkGrayToMagentaColorMap.getInstance(), DarkGrayToOrangeColorMap.getInstance(),
				DarkGrayToPurpleColorMap.getInstance(), DarkGrayToWhiteColorMap.getInstance(),
				// parameterized (no fixed target color), unlike the rest of this family --
				// pick a representative one just for this survey
				DarkGrayToUserDefinedColorMap.getInstance(Color.CYAN),
				GrayBlueColorMap.getInstance(), GrayGrassGreenColormap.getInstance(), GrayGreenColorMap.getInstance(),
				GrayLighterBlueColorMap.getInstance(), GrayPurpleColorMap.getInstance(), GrayRedColorMap.getInstance(),
				GrayUnipolarColorMap.getInstance(), GrayYellowColorMap.getInstance(),
				GreenPurpleBipolarColorMap.getInstance(), GreenYellowRedColormap.getInstance(),
				GreenYellowRedLowSaturationColorMap.getInstance(), LightGrayBlackColorMap.getInstance(),
				LightYellowLightGreenTealNavy.getInstance(), OrangeHSBBrightnessGradientColorMap.getInstance(),
				PurpleGreenBipolarColorMap.getInstance(), RainbowColorMap.getInstance(),
				RedBlueBipolarColorMap.getInstance(), UniColorMap.getInstance(),
				UniColorMapCroppedAndInverted.getInstance(),
				// also parameterized -- see DarkGrayToUserDefinedColorMap above
				UserDefinedToBlueColorMap.getInstance(Color.CYAN), UserDefinedToRedColorMap.getInstance(Color.CYAN),
				WhiteAlphaColorMap1D.getInstance(),
				WhiteBlackColorMap1D.getInstance(), WhiteBlueColorMap.getInstance(),
				WhiteDarkGrayColorMap.getInstance(), WhiteRedColorMap.getInstance(),
				WhiteSaturationReduced_AlphaColorMap1D.getInstance() };

		JPanel grid = new JPanel(new GridLayout(colorMaps.length, 1));
		for (AbstractColorMap colorMap : colorMaps)
			grid.add(row(colorMap));

		JScrollPane scrollPane = new JScrollPane(grid);
		scrollPane.getVerticalScrollBar().setUnitIncrement(ROW_HEIGHT);

		SVGFrameTools.dropSVGFrame(scrollPane, "All quantitative colormaps (" + colorMaps.length + ")", 520, 900);
	}

	private static JPanel row(AbstractColorMap colorMap) {
		JLabel label = new JLabel(" " + colorMap.getClass().getSimpleName());
		label.setPreferredSize(new Dimension(220, ROW_HEIGHT));

		ColorMapPalettePanel gradient = new ColorMapPalettePanel(colorMap);
		gradient.setPreferredSize(new Dimension(260, ROW_HEIGHT));

		JPanel row = new JPanel(new BorderLayout());
		row.add(label, BorderLayout.WEST);
		row.add(gradient, BorderLayout.CENTER);
		row.setPreferredSize(new Dimension(480, ROW_HEIGHT));
		return row;
	}

}
