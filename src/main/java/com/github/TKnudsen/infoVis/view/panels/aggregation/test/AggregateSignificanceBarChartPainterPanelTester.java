package com.github.TKnudsen.infoVis.view.panels.aggregation.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.panels.aggregation.AggregateSignificanceBarChartPainterPanel;

import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * Interactive demo of {@link AggregateSignificanceBarChartPainterPanel}:
 * click a bar to select it (control-click to add to the selection), or the
 * close button in the corner to remove the panel.
 *
 * @since 2026
 */
public class AggregateSignificanceBarChartPainterPanelTester {

	public static void main(String[] args) {
		List<String> labels = Arrays.asList("A", "B", "C");

		List<List<Long>> elementMapping = new ArrayList<>();
		Random random = new Random();
		for (int i = 0; i < labels.size(); i++) {
			List<Long> entries = new ArrayList<>();
			for (int j = 0; j < random.nextInt(100); j++)
				entries.add(random.nextLong());
			elementMapping.add(entries);
		}

		String headline = "Headline";

		SelectionModel<Long> selectionModel = SelectionModels.create();

		AggregateSignificanceBarChartPainterPanel<Long> panel = new AggregateSignificanceBarChartPainterPanel<>(elementMapping, labels,
				headline, selectionModel);

		selectionModel.addSelectionListener(panel);

		SVGFrameTools.dropSVGFrame(panel, headline, 1000, 700);
	}
}
