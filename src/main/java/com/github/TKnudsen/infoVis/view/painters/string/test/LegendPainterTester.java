package com.github.TKnudsen.infoVis.view.painters.string.test;

import java.awt.Color;
import java.awt.Dimension;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.string.LegendPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Demo of {@link LegendPainter}: a short legend (left-aligned) and a longer
 * one with wider labels (right-aligned), to check both the alignment
 * behavior and the box auto-sizing to the widest label.
 *
 * @since 2026
 */
public class LegendPainterTester {

	private static final List<Entry<String, Color>> SHORT_ENTRIES = Arrays.asList(
			new SimpleEntry<>("A", Color.RED), new SimpleEntry<>("B", Color.GREEN.darker()),
			new SimpleEntry<>("C", Color.BLUE));

	private static final List<Entry<String, Color>> LONG_ENTRIES = Arrays.asList(
			new SimpleEntry<>("[0.16-21.00]", Color.ORANGE), new SimpleEntry<>("[22.00-28.00]", Color.MAGENTA),
			new SimpleEntry<>("[28.50-39.00]", Color.CYAN.darker()),
			new SimpleEntry<>("[40.00-80.00]", Color.GRAY));

	public static void main(String[] args) {
		List<JPanel> panels = new ArrayList<>();
		panels.add(labeled("Left-aligned", createPanel(SHORT_ENTRIES, true)));
		panels.add(labeled("Right-aligned, wider labels", createPanel(LONG_ENTRIES, false)));

		SVGFrameTools.dropSVGFrameVertical(panels, "LegendPainter");
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

	private static JPanel createPanel(List<Entry<String, Color>> entries, boolean alignLeft) {
		List<Entry<String, java.awt.Paint>> data = new ArrayList<>();
		for (Entry<String, Color> entry : entries)
			data.add(new SimpleEntry<>(entry.getKey(), entry.getValue()));

		LegendPainter painter = new LegendPainter();
		painter.setData(data);
		painter.setAlignLeft(alignLeft);
		painter.setDrawOutline(true);

		InfoVisChartPanel panel = new InfoVisChartPanel(painter);
		panel.setPreferredSize(new Dimension(300, 150));
		return panel;
	}
}
