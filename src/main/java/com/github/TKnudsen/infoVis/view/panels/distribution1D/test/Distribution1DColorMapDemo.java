package com.github.TKnudsen.infoVis.view.panels.distribution1D.test;

import java.awt.GridLayout;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DHorizontalPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanels;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DPanels;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative.AbstractQualitativeColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative.ColorBrewerSet3ColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;

/**
 * <p>
 * Worked example: coloring a 1D distribution by a categorical attribute using
 * a qualitative palette from {@code visualChannels.color.colormaps}. Unlike
 * the quantitative case ({@code ColorMapEncodingFunction}, see
 * {@code ScatterPlotColorMapDemo}), a qualitative colormap hands out a fixed
 * {@code Color[]} for N categories rather than mapping a normalized number --
 * so the adapter here is the existing, general-purpose
 * {@link ColorEncodingFunction} instead: build it once over the distinct
 * category labels, then reuse it as a lookup for every data point. Shows both
 * levels the resulting color encoding plugs into: the {@link Distribution1DPanels}
 * panel factory, and the lower-level {@link Distribution1DHorizontalPainter}
 * for embedding in a custom composite chart.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class Distribution1DColorMapDemo {

	/**
	 * A stand-in for "some domain object with a categorical field you want to
	 * color by, and a numeric field to position by".
	 */
	private static final class DailyReturn {
		final String sector;
		final double returnPercent;

		DailyReturn(String sector, double returnPercent) {
			this.sector = sector;
			this.returnPercent = returnPercent;
		}
	}

	public static void main(String[] args) {

		List<DailyReturn> readings = createSampleData();

		// Step 1: collect the distinct categories, in a stable order.
		Set<String> sectors = new LinkedHashSet<>();
		for (DailyReturn r : readings)
			sectors.add(r.sector);
		List<String> sectorList = new ArrayList<>(sectors);

		// Step 2: pick a qualitative palette and ask it for exactly as many colors as
		// there are categories.
		AbstractQualitativeColorMap palette = (AbstractQualitativeColorMap) ColorBrewerSet3ColorMap.getInstance();
		List<Paint> sectorColors = new ArrayList<>();
		for (java.awt.Color c : palette.getColors(sectorList.size()))
			sectorColors.add(c);

		// Step 3: compose into a category-label -> Paint lookup ...
		Function<String, Paint> categoryColor = new ColorEncodingFunction<>(sectorList, sectorColors);

		// ... then adapt it to the actual domain type. This one-line indirection is
		// the only project-specific glue -- everything else is reusable machinery.
		Function<? super DailyReturn, ? extends Paint> colorEncoding = r -> categoryColor.apply(r.sector);

		Function<? super DailyReturn, ? extends Number> worldToDoubleMapping = r -> r.returnPercent;

		// Panel-level use: the ready-made factory for Distribution1D panels.
		Distribution1DPanel<DailyReturn> panel = Distribution1DPanels.create(readings, worldToDoubleMapping,
				colorEncoding, -5.0, 5.0, false);
		InfoVisChartPanel distributionPanel = (InfoVisChartPanel) panel;
		InfoVisChartPanels.addTitle(distributionPanel, "Distribution1D panel + ColorBrewerSet3ColorMap");

		// Painter-level use: the identical color encoding, one level down, for
		// embedding in a custom composite chart instead of the ready-made panel.
		Distribution1DHorizontalPainter<DailyReturn> painter = new Distribution1DHorizontalPainter<>(readings,
				worldToDoubleMapping, colorEncoding);
		InfoVisChartPanel painterPanel = new InfoVisChartPanel(painter);
		InfoVisChartPanels.addTitle(painterPanel, "Distribution1DHorizontalPainter (same color encoding)");

		JPanel combined = new JPanel(new GridLayout(2, 1));
		combined.add(distributionPanel);
		combined.add(painterPanel);

		SVGFrameTools.dropSVGFrame(combined, "Distribution1D color encoding via colormaps", 500, 300);
	}

	private static List<DailyReturn> createSampleData() {
		List<DailyReturn> readings = new ArrayList<>();
		readings.add(new DailyReturn("Tech", 2.1));
		readings.add(new DailyReturn("Tech", 3.4));
		readings.add(new DailyReturn("Tech", -0.5));
		readings.add(new DailyReturn("Energy", -2.8));
		readings.add(new DailyReturn("Energy", -1.2));
		readings.add(new DailyReturn("Healthcare", 0.8));
		readings.add(new DailyReturn("Healthcare", 1.1));
		readings.add(new DailyReturn("Healthcare", -0.2));
		readings.add(new DailyReturn("Financials", 0.3));
		readings.add(new DailyReturn("Financials", -1.5));
		readings.add(new DailyReturn("Utilities", 0.1));
		return readings;
	}

}
