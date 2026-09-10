package com.github.TKnudsen.infoVis.view.painters.glyph.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.glyph.RadioPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive demo: a group of {@link RadioPainter} glyphs with
 * mutually-exclusive ticking wired up by this tester (clicking one ticks it
 * and unticks every other member of the group) -- RadioPainter itself only
 * draws a single glyph and tracks its own ticked state, so the "only one
 * ticked at a time" behavior has to be provided by the caller, as done here.
 *
 * @version 1.0
 * @since 2026
 */
public class RadioPainterTester {

	private static final int ROW_HEIGHT = 30;

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("RadioPainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel list = new JPanel();
		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

		String[] options = { "Daily", "Weekly", "Monthly", "Quarterly", "Yearly" };
		List<RadioPainter> group = new ArrayList<>();

		for (String option : options) {
			RadioPainter radioPainter = new RadioPainter(option);
			group.add(radioPainter);

			InfoVisChartPanel panel = new InfoVisChartPanel(radioPainter);
			panel.setPreferredSize(new Dimension(200, ROW_HEIGHT));
			panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

			panel.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					for (RadioPainter other : group)
						other.untick();
					radioPainter.tick();
					list.repaint();
				}
			});

			list.add(panel);
		}

		group.get(0).tick();

		frame.setLayout(new BorderLayout());
		frame.add(list, BorderLayout.NORTH);
		frame.setSize(250, options.length * ROW_HEIGHT + 40);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

}
