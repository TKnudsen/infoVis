package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.panels.QuadraticPanel;
import com.github.TKnudsen.infoVis.view.ui.NimbusUITools;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.Colormap2DPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.IColorMap2D;
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
 * One row per re-implemented static 2D colormap from Bernard et al., "A
 * Survey and Task-Based Quality Assessment of Static 2D Colormaps" (SPIE
 * VDA, 2015) -- label on the left, {@link Colormap2DPanel}'s gradient on the
 * right -- stacked into a single scrollable grid, mirroring
 * {@code AllQuantitativeColorMapsTest} for the 1D colormaps. Combines two
 * sources: colormaps fully specified by Table 1 alone (exact anchor colors
 * and axis assignment), and colormaps ported pixel-for-pixel from Color2D
 * (https://github.com/dominikjaeckle/Color2D), which supplies exact
 * reference data for Bremm et al. (regular), TeulingFig2, and Ziegler et
 * al., plus three colormaps (Steiger, Schumann, Cube Diagonal) from the
 * same author group's sibling WSCG 2015 publication that are not part of
 * the survey's 22, plus a third source: {@link MittelstaedtColorMap2D},
 * implemented directly from the exact construction described in Steiger,
 * Bernard, Mittelstaedt et al., "Visual Analysis of Time-Series Similarities
 * for Anomaly Detection in Sensor Networks," EuroVis (2014) -- the survey's
 * own citation for that colormap. A fourth source is {@link
 * TeulingFig3ColorMap2D}: the original Teuling et al. 2011 paper's own
 * Figure 3 legend is printed as a flat, undistorted 5x5 swatch (at the exact
 * "n=5, a=0.37" the paper reports using), so its 25 cells were sampled
 * directly from the paper's PDF and bilinearly upsampled -- exact at those
 * 25 points, interpolated in between. Still missing: TeulingFig3NoWhitening
 * and TeulingFig4a (the paper's Figure 4 also shows these constructions, but
 * only in an oblique 3D diagram; corner colors extracted from it via
 * perspective-corrected pixel sampling did not reproduce a consistent
 * corner-to-axis orientation across panels, so nothing was implemented from
 * it rather than risk a mislabeled reconstruction) and Guo et al. (whose
 * paper gives no formula or constants at all).
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class AllStatic2DColorMapsTest {

	private static final int ROW_HEIGHT = 60;

	public static void main(String[] args) {

		NimbusUITools.switchToNimbus();

		IColorMap2D[] colorMaps = { new ConstantBlueColorMap2D(), new ConstantGreenColorMap2D(),
				new ConstantRedColorMap2D(), new FourCornersColorMap2D(), new WainerFrancoliniColorMap2D(),
				new BaumColorMap2D(), new YeoColorMap2D(), new CubeDiagonalCutBCYRColorMap2D(),
				new CubeDiagonalCutBMYGColorMap2D(), new SimulaAlhoniemiColorMap2D(), new RamirezColorMap2D(),
				new BremmColorMap2D(), new TeulingFig2ColorMap2D(), new TeulingFig3ColorMap2D(),
				new ZieglerColorMap2D(), new SteigerColorMap2D(), new SchumannColorMap2D(),
				new CubeDiagonalColorMap2D(), new MittelstaedtColorMap2D() };

		JPanel grid = new JPanel(new GridLayout(colorMaps.length, 1));
		for (IColorMap2D colorMap : colorMaps)
			grid.add(row(colorMap));

		JScrollPane scrollPane = new JScrollPane(grid);
		scrollPane.getVerticalScrollBar().setUnitIncrement(ROW_HEIGHT);

		SVGFrameTools.dropSVGFrame(scrollPane, "Re-implemented static 2D colormaps (" + colorMaps.length + ")", 420,
				700);
	}

	private static JPanel row(IColorMap2D colorMap) {
		JLabel label = new JLabel(" " + colorMap.getName());
		label.setPreferredSize(new Dimension(220, ROW_HEIGHT));

		// A 2D colormap's domain is [0,1]x[0,1] -- a square -- so its swatch should
		// be one too, regardless of how much width the row happens to have.
		Colormap2DPanel swatch = new Colormap2DPanel(colorMap);
		QuadraticPanel quadraticSwatch = new QuadraticPanel(swatch);

		JPanel row = new JPanel(new BorderLayout());
		row.add(label, BorderLayout.WEST);
		row.add(quadraticSwatch, BorderLayout.CENTER);
		row.setPreferredSize(new Dimension(380, ROW_HEIGHT));
		return row;
	}

}
