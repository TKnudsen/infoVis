package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.BaumColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.BremmColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.ConstantBlueColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.ConstantGreenColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.ConstantRedColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.CubeDiagonalColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.CubeDiagonalCutBCYRColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.CubeDiagonalCutBMYGColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.FourCornersColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.MittelstaedtColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.RamirezColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.SchumannColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.SimulaAlhoniemiColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.SteigerColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.TeulingFig2ColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.TeulingFig3ColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.WainerFrancoliniColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.YeoColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.ZieglerColorMap2D;

/**
 * <p>
 * Registry of every concrete, directly instantiable {@link IColorMap2D}
 * implementation in this library's {@code impl} package -- a fixed set of
 * bivariate colormaps (a color per {@code (x, y)} pair) suitable for e.g.
 * populating a "choose a 2D colormap" menu without listing classes by hand.
 * </p>
 * <p>
 * Excludes two kinds of classes that live in {@code impl} but are not
 * themselves usable colormaps: {@code abstract} bases (e.g.
 * {@code LookupImageColorMap2D}) and package-private helpers (e.g.
 * {@code AnchorColorInterpolation}) -- every {@code public}, concrete
 * {@code impl} class is included.
 * </p>
 * <p>
 * <b>Thread safety:</b> {@link #colorMaps2D()} is not synchronized. Two
 * threads racing the first call could each build and assign their own array
 * (each internally equal, just distinct instances) before either check
 * observes the other's write -- harmless for typical single-threaded/EDT
 * usage, but not a guarantee of a single shared instance under concurrent
 * first access.
 * </p>
 *
 * @version 1.0
 * @since 2019
 */
public class Colormap2D {

	private static IColorMap2D[] colorMaps;

	/**
	 * @return every registered 2D colormap, lazily constructed on first call and
	 *         cached thereafter. The returned array is the shared cached
	 *         instance, not a defensive copy -- callers must not mutate it.
	 */
	public static IColorMap2D[] colorMaps2D() {
		if (colorMaps == null) {
			IColorMap2D[] cm = { new ConstantBlueColorMap2D(), new ConstantGreenColorMap2D(),
					new ConstantRedColorMap2D(), new FourCornersColorMap2D(), new WainerFrancoliniColorMap2D(),
					new BaumColorMap2D(), new YeoColorMap2D(), new CubeDiagonalCutBCYRColorMap2D(),
					new CubeDiagonalCutBMYGColorMap2D(), new SimulaAlhoniemiColorMap2D(), new RamirezColorMap2D(),
					new BremmColorMap2D(), new TeulingFig2ColorMap2D(), new TeulingFig3ColorMap2D(),
					new ZieglerColorMap2D(), new SteigerColorMap2D(), new SchumannColorMap2D(),
					new CubeDiagonalColorMap2D(), new MittelstaedtColorMap2D() };

			colorMaps = cm;
		}

		return colorMaps;
	}
}
