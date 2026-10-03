package com.github.TKnudsen.infoVis.view.painters.glyph.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.glyph.CrossHairPainter;
import com.github.TKnudsen.infoVis.view.painters.glyph.HashCodePainter;
import com.github.TKnudsen.infoVis.view.painters.glyph.NotAvailablePainter;
import com.github.TKnudsen.infoVis.view.painters.glyph.RadioPainter;
import com.github.TKnudsen.infoVis.view.painters.glyph.TriangularPlasticPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive gallery of every painter directly in
 * {@code com.github.TKnudsen.infoVis.view.painters.glyph} (not its
 * {@code primitives} or {@code spaceInvaders} subpackages, which have their
 * own galleries): {@link CrossHairPainter}, {@link HashCodePainter},
 * {@link NotAvailablePainter}, {@link RadioPainter}, and
 * {@link TriangularPlasticPainter}.
 *
 * @version 1.0
 * @since 2026
 */
public class GlyphPaintersTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("glyph Painters Gallery");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel grid = new JPanel(new GridLayout(2, 3, 10, 10));

		CrossHairPainter crossHairPainter = new CrossHairPainter(true, true);
		crossHairPainter.setCoordinates(90, 60);
		grid.add(cell("CrossHairPainter", crossHairPainter));

		grid.add(cell("HashCodePainter", new HashCodePainter("example".hashCode(), 8)));

		NotAvailablePainter notAvailablePainter = new NotAvailablePainter();
		notAvailablePainter.setPaint(Color.DARK_GRAY);
		grid.add(cell("NotAvailablePainter", notAvailablePainter));

		RadioPainter radioPainter = new RadioPainter("Option A");
		radioPainter.tick();
		grid.add(cell("RadioPainter (ticked)", radioPainter));

		grid.add(cell("TriangularPlasticPainter", new TriangularPlasticPainter(Color.ORANGE)));

		frame.add(grid);
		frame.setSize(900, 400);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

	private static JPanel cell(String title, com.github.TKnudsen.infoVis.view.painters.ChartPainter painter) {
		InfoVisChartPanel panel = new InfoVisChartPanel(painter);
		panel.setPreferredSize(new Dimension(220, 140));

		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

		JLabel label = new JLabel(title);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.setAlignmentX(Component.CENTER_ALIGNMENT);

		wrapper.add(label);
		wrapper.add(panel);
		return wrapper;
	}

}
