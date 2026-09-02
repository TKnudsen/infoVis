package com.github.TKnudsen.infoVis.view.panels.scatterPlot;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlot;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlotIndexedGPU;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlotSpriteGPU;

/**
 * <p>
 * Pins down a gap an adversarial re-review found in the painter-level
 * characterization tests in {@code ScatterPlotPainterHierarchyCharacterizationTest}:
 * those construct {@code ScatterPlotPainter}/{@code ScatterPlotIndexedGPUPainter}
 * /{@code ScatterPlotSpriteGPUPainter} directly, so they never exercised the
 * panel constructors ({@code ScatterPlot}/{@code ScatterPlotIndexedGPU}/
 * {@code ScatterPlotSpriteGPU}) that real callers actually use. Those panel
 * constructors call {@code AbstractScatterPlotPanel.initializeData(List)}
 * <b>before</b> the painter is ever constructed, and that method had its own,
 * separate strict range computation -- so a degenerate (single-value) dataset
 * still threw {@code DegenerateRangeException} there even after the painter
 * layer was fixed to tolerate it (code review finding #10/#28), for all three
 * panel types, predating this session's scatterplot hierarchy extraction.
 * </p>
 *
 * <p>
 * Deliberately just tests construction, not post-layout rendering: these
 * panels are never added to a realized window here, and {@code
 * InfoVisChartPanel.updateBounds()} no-ops on a non-displayable component
 * ({@code !isDisplayable()}), so {@code chartRectangle} never gets assigned
 * regardless -- exercising that would need a real (even if invisible) window
 * and is what the manual {@code ScatterPlotTester}/{@code ScatterPlotGPUTester}
 * classes are for. The bug this pins down is specifically about the
 * constructor throwing before ever reaching that point.
 * </p>
 */
public class ScatterPlotPanelCharacterizationTest {

	private static final List<double[]> DEGENERATE_POINT = Arrays.asList(new double[] { 5, 5 });

	private static final Function<double[], Double> WORLD_X = p -> p[0];
	private static final Function<double[], Double> WORLD_Y = p -> p[1];

	@Test
	public void degenerateRangeDataset_scatterPlotPanelTolerates() {
		new ScatterPlot<>(DEGENERATE_POINT, null, WORLD_X, WORLD_Y);
	}

	@Test
	public void degenerateRangeDataset_scatterPlotIndexedGpuPanelTolerates() {
		new ScatterPlotIndexedGPU<>(DEGENERATE_POINT, null, WORLD_X, WORLD_Y);
	}

	@Test
	public void degenerateRangeDataset_scatterPlotSpriteGpuPanelTolerates() {
		new ScatterPlotSpriteGPU<>(DEGENERATE_POINT, null, WORLD_X, WORLD_Y);
	}
}
