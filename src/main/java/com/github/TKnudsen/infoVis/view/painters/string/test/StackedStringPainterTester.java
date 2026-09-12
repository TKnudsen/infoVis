package com.github.TKnudsen.infoVis.view.painters.string.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.string.StackedStringPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Demo of {@link StackedStringPainter} in both orientations: horizontal
 * (side by side) and vertical (stacked one over another). Several groups of
 * varying string count are shown for each orientation, since that is the
 * scenario that actually exposed a real bug this class once had (each
 * string's own text was being forced to rotate to match the stacking
 * direction, instead of always reading normally).
 *
 * @since 2026
 */
public class StackedStringPainterTester {

	private static final List<List<String>> GROUPS = Arrays.asList(Arrays.asList("false", "true"),
			Arrays.asList("1", "2", "3"), Arrays.asList("female", "male"),
			Arrays.asList("[3.17-8.05]", "[8.15-15.85]", "[15.90-36.75]", "[37.00-512.32]", "[empty]"),
			Arrays.asList("[0.16-21.00]", "[22.00-28.00]", "[28.50-39.00]", "[40.00-80.00]"));

	private static final List<Color> PALETTE = Arrays.asList(Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.MAGENTA);

	public static void main(String[] args) {
		List<JPanel> panels = new ArrayList<>();
		panels.add(labeled("Horizontal (side by side)", createRow(false)));
		panels.add(labeled("Vertical (stacked)", createRow(true)));

		SVGFrameTools.dropSVGFrameVertical(panels, "StackedStringPainter");
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

	/**
	 * One panel per group, laid out left to right, each panel a differently
	 * sized {@link StackedStringPainter} in the given orientation -- mirrors
	 * how columns of varying bin count sit side by side in a real matrix
	 * view.
	 */
	private static JPanel createRow(boolean vertical) {
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

		for (List<String> strings : GROUPS) {
			StackedStringPainter painter = new StackedStringPainter(strings, PALETTE.subList(0, strings.size()));
			painter.setVerticalStacking(vertical);
			painter.setDrawOutline(true);

			InfoVisChartPanel panel = new InfoVisChartPanel(painter);
			panel.setPreferredSize(new java.awt.Dimension(strings.size() * 80, 150));
			row.add(panel);
		}

		return row;
	}
}
