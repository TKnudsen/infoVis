package com.github.TKnudsen.infoVis.view.painters.string.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Paint;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.LegendaryPainter;
import com.github.TKnudsen.infoVis.view.painters.string.ScalingTextPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StackedStringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringOvalOutlinePainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.TitlePainter;
import com.github.TKnudsen.infoVis.view.painters.string.UpperRightLabelPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive gallery of every painter in
 * {@code com.github.TKnudsen.infoVis.view.painters.string}:
 * {@link StringPainter} (default, aligned, vertical, and truncated variants),
 * {@link TitlePainter}, {@link ScalingTextPainter},
 * {@link StringOvalOutlinePainter}, {@link UpperRightLabelPainter},
 * {@link LegendaryPainter}, and {@link StackedStringPainter} (horizontal and
 * vertical).
 *
 * @version 1.0
 * @since 2026
 */
public class StringPackagePaintersTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("view.painters.string Gallery");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel grid = new JPanel(new GridLayout(3, 4, 10, 10));

		grid.add(cell("StringPainter (default)", new StringPainter("Hello World")));

		StringPainter leftAligned = new StringPainter("Left aligned");
		leftAligned.setHorizontalStringAlignment(HorizontalStringAlignment.LEFT);
		grid.add(cell("StringPainter (LEFT)", leftAligned));

		StringPainter rightAligned = new StringPainter("Right aligned");
		rightAligned.setHorizontalStringAlignment(HorizontalStringAlignment.RIGHT);
		grid.add(cell("StringPainter (RIGHT)", rightAligned));

		StringPainter vertical = new StringPainter("Vertical");
		vertical.setVerticalOrientation(true);
		grid.add(cell("StringPainter (vertical)", vertical));

		StringPainter truncated = new StringPainter(
				"This text is far too long to fit and gets truncated automatically");
		grid.add(cell("StringPainter (truncated)", truncated));

		grid.add(cell("TitlePainter", new TitlePainter("A Chart Title")));

		grid.add(cell("ScalingTextPainter", new ScalingTextPainter("Scaled", "Arial", java.awt.Font.BOLD)));

		StringOvalOutlinePainter ovalPainter = new StringOvalOutlinePainter("42");
		ovalPainter.setOvalBackgroundPaint(Color.YELLOW);
		grid.add(cell("StringOvalOutlinePainter", ovalPainter));

		grid.add(cellOnHost("UpperRightLabelPainter", new UpperRightLabelPainter("New")));

		LegendaryPainter legendaryPainter = new LegendaryPainter();
		List<Entry<String, Paint>> entries = new ArrayList<>();
		entries.add(new SimpleEntry<>("Alpha", Color.RED));
		entries.add(new SimpleEntry<>("Beta", Color.BLUE));
		entries.add(new SimpleEntry<>("Gamma", Color.GREEN.darker()));
		legendaryPainter.setData(entries);
		grid.add(cell("LegendaryPainter", legendaryPainter));

		StackedStringPainter horizontalStack = new StackedStringPainter(Arrays.asList("Red", "Green", "Blue"),
				Arrays.asList(Color.RED, Color.GREEN.darker(), Color.BLUE));
		grid.add(cell("StackedStringPainter (horizontal)", horizontalStack));

		StackedStringPainter verticalStack = new StackedStringPainter(Arrays.asList("Red", "Green", "Blue"),
				Arrays.asList(Color.RED, Color.GREEN.darker(), Color.BLUE));
		verticalStack.setVerticalStacking(true);
		grid.add(cell("StackedStringPainter (vertical)", verticalStack));

		frame.add(grid, BorderLayout.CENTER);
		frame.setSize(1100, 750);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

	private static JPanel cell(String title, ChartPainter painter) {
		JPanel panel = new InfoVisChartPanel(painter);
		return labeled(title, panel);
	}

	/**
	 * wraps painter in a panel with a visible light-gray host background, for
	 * painters meant to overlay other content
	 */
	private static JPanel cellOnHost(String title, ChartPainter painter) {
		JPanel host = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		host.setLayout(new BorderLayout());
		InfoVisChartPanel overlay = new InfoVisChartPanel(painter);
		overlay.setOpaque(false);
		host.add(overlay);
		return labeled(title, host);
	}

	private static JPanel labeled(String title, JPanel content) {
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		JLabel label = new JLabel(title);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		content.setAlignmentX(Component.CENTER_ALIGNMENT);
		content.setPreferredSize(new Dimension(220, 140));

		wrapper.add(label);
		wrapper.add(content);
		return wrapper;
	}

}
