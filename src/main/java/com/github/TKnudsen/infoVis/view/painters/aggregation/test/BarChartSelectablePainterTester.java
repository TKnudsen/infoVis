package com.github.TKnudsen.infoVis.view.painters.aggregation.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.aggregation.AggregationBarChartPainter;
import com.github.TKnudsen.infoVis.view.painters.aggregation.AggregationColoredSignificanceBarChartPainter;
import com.github.TKnudsen.infoVis.view.painters.aggregation.BarChartSelectablePainter;
import com.github.TKnudsen.infoVis.view.painters.aggregation.BarsPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Interactive demo of the bucket-based bar chart family -- each panel adds
 * one layer over the previous: {@link BarsPainter} (bars sized by bucket
 * element count), {@link AggregationBarChartPainter} (adds headline and
 * legend), {@link BarChartSelectablePainter} (adds an element-selection
 * overlay), and {@link AggregationColoredSignificanceBarChartPainter} (adds
 * an independently-colored significance-dots strip).
 *
 * @since 2026
 */
public class BarChartSelectablePainterTester {

	private static final List<String> LABELS = Arrays.asList("Apple", "Banana", "Cherry", "Date", "Elderberry");
	private static final List<Color> COLORS = Arrays.asList(Color.RED, Color.YELLOW, Color.PINK, Color.ORANGE, Color.MAGENTA);

	public static void main(String[] args) {
		List<JPanel> panels = new ArrayList<>();
		panels.add(labeled("BarsPainter", createBarsPainterPanel()));
		panels.add(labeled("AggregationBarChartPainter", createAggregationBarChartPanel()));
		panels.add(labeled("BarChartSelectablePainter", createSelectablePanel()));
		panels.add(labeled("AggregationColoredSignificanceBarChartPainter", createColoredSignificancePanel()));

		SVGFrameTools.dropSVGFrameHorizontal(panels, "Bucket-based bar chart family");
	}

	private static JPanel labeled(String title, JPanel content) {
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		JLabel label = new JLabel(title, SwingConstants.CENTER);
		label.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		content.setAlignmentX(JPanel.CENTER_ALIGNMENT);

		wrapper.add(label);
		wrapper.add(content);
		return wrapper;
	}

	private static List<List<Long>> createElementMapping() {
		List<List<Long>> elementMapping = new ArrayList<>();
		elementMapping.add(Arrays.asList(1L, 2L, 3L));
		elementMapping.add(Arrays.asList(1L));
		elementMapping.add(Arrays.asList(1L, 2L, 3L, 4L, 5L));
		elementMapping.add(Arrays.asList(1L, 2L));
		elementMapping.add(Arrays.asList(1L, 2L, 3L, 4L));
		return elementMapping;
	}

	private static JPanel createBarsPainterPanel() {
		BarsPainter<Long> painter = new BarsPainter<>(createElementMapping(), LABELS, COLORS);
		painter.setDrawOutline(true);

		return new InfoVisChartPanel(painter);
	}

	private static JPanel createAggregationBarChartPanel() {
		AggregationBarChartPainter<Long> painter = new AggregationBarChartPainter<>(createElementMapping(), LABELS,
				"AggregationBarChartPainter");
		painter.setColors(COLORS);
		painter.setDrawOutline(true);

		return new InfoVisChartPanel(painter);
	}

	private static JPanel createSelectablePanel() {
		BarChartSelectablePainter<Long> painter = new BarChartSelectablePainter<>(createElementMapping(), LABELS,
				"BarChartSelectablePainter");
		painter.setColors(COLORS);
		painter.setDrawOutline(true);

		// select the odd-numbered elements in each bucket, to make the selection
		// overlay visibly a fraction of each bar rather than all-or-nothing
		Set<Long> selected = new HashSet<>(Arrays.asList(1L, 3L, 5L));
		painter.setSelectedStatus(selected);

		return new InfoVisChartPanel(painter);
	}

	private static JPanel createColoredSignificancePanel() {
		AggregationColoredSignificanceBarChartPainter<Long> painter = new AggregationColoredSignificanceBarChartPainter<>(
				createElementMapping(), LABELS, "AggregationColoredSignificanceBarChartPainter");
		painter.setColors(COLORS);
		painter.setDrawOutline(true);
		painter.setSignificanceDotsColors(Arrays.asList(Color.BLACK, Color.GRAY, Color.BLACK, Color.GRAY, Color.BLACK));

		return new InfoVisChartPanel(painter);
	}
}
