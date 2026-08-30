package com.github.TKnudsen.infoVis.view.panels.scatterplot.test;

import java.awt.GridLayout;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanels;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlot;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.AbstractColorMap1D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.quantitative.impl.RedBlueBipolarColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorMapEncodingFunction;

/**
 * <p>
 * Worked example: coloring scatterplot points by a quantitative attribute
 * using a palette from {@code visualChannels.color.colormaps}, via the
 * {@link ColorMapEncodingFunction} adapter. Shows both levels the same color
 * encoding plugs into: the ready-made {@link ScatterPlot} panel, and the
 * lower-level {@link ScatterPlotPainter} for embedding in a hand-built
 * composite chart.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ScatterPlotColorMapDemo {

	/**
	 * A stand-in for "some domain object with a numeric field you want to color
	 * by". Momentum is a raw value in the known, fixed range [-1, 1].
	 */
	private static final class Stock {
		final String name;
		final double marketCapBillion;
		final double peRatio;
		final double momentum;

		Stock(String name, double marketCapBillion, double peRatio, double momentum) {
			this.name = name;
			this.marketCapBillion = marketCapBillion;
			this.peRatio = peRatio;
			this.momentum = momentum;
		}
	}

	public static void main(String[] args) {

		List<Stock> stocks = createSampleData();

		// Step 1: pick a palette suited to the data. Momentum can be negative or
		// positive, so a diverging/bipolar colormap communicates that better than a
		// sequential one would.
		AbstractColorMap1D palette = (AbstractColorMap1D) RedBlueBipolarColorMap.getInstance();

		// Step 2: describe the rescale. Momentum lives in a known, fixed range
		// [-1, 1], so LinearNormalizationFunction -- the shared rescale-to-[0,1]
		// utility used throughout the ecosystem -- does it; use its
		// Collection<Number> constructor instead when the bounds are not known
		// upfront and should be derived from the actual data.
		LinearNormalizationFunction momentumNormalization = new LinearNormalizationFunction(-1.0, 1.0);

		// Step 3: compose extraction (s -> s.momentum) + rescale + palette into a
		// Function<Stock, Paint> in one call. From here on, no caller needs to
		// know a colormap is involved at all -- it is exactly what
		// setColorEncodingFunction(...)/the color-encoding constructor argument
		// expects.
		Function<? super Stock, ? extends Paint> colorEncoding = new ColorMapEncodingFunction<>(s -> s.momentum,
				momentumNormalization, palette);

		Function<? super Stock, Double> x = s -> s.marketCapBillion;
		Function<? super Stock, Double> y = s -> s.peRatio;

		// Panel-level use: pass the color encoding straight into the ScatterPlot
		// panel constructor.
		ScatterPlot<Stock> scatterPlot = new ScatterPlot<>(stocks, colorEncoding, x, y);
		scatterPlot.setToolTipMapping(s -> s.name + " (momentum " + s.momentum + ")");
		InfoVisChartPanels.addTitle(scatterPlot, "ScatterPlot panel + RedBlueBipolarColorMap");

		// Painter-level use: the identical color encoding, one level down, for
		// embedding in a custom composite chart instead of the ready-made panel.
		ScatterPlotPainter<Stock> painter = new ScatterPlotPainter<>(stocks, colorEncoding, x, y);
		InfoVisChartPanel painterPanel = new InfoVisChartPanel(painter);
		InfoVisChartPanels.addTitle(painterPanel, "ScatterPlotPainter (same color encoding)");

		JPanel combined = new JPanel(new GridLayout(1, 2));
		combined.add(scatterPlot);
		combined.add(painterPanel);

		SVGFrameTools.dropSVGFrame(combined, "Scatterplot color encoding via colormaps", 800, 400);
	}

	private static List<Stock> createSampleData() {
		List<Stock> stocks = new ArrayList<>();
		stocks.add(new Stock("Alpha Corp", 120, 18, -0.8));
		stocks.add(new Stock("Beta Industries", 45, 22, -0.3));
		stocks.add(new Stock("Gamma Holdings", 300, 15, 0.0));
		stocks.add(new Stock("Delta Systems", 80, 30, 0.4));
		stocks.add(new Stock("Epsilon Group", 210, 12, 0.9));
		stocks.add(new Stock("Zeta Networks", 15, 25, -0.5));
		stocks.add(new Stock("Eta Materials", 500, 9, 0.6));
		stocks.add(new Stock("Theta Robotics", 60, 40, 0.2));
		return stocks;
	}

}
