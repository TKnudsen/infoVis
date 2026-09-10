package com.github.TKnudsen.infoVis.view.painters.glyph.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.glyph.CrossHairPainter;
import com.github.TKnudsen.infoVis.view.painters.glyph.TriangularPlasticPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive demo of {@link CrossHairPainter} (crosshair follows
 * the mouse) and {@link TriangularPlasticPainter} (a grid of beveled button
 * glyphs that toggle their pressed look on click).
 *
 * @version 1.0
 * @since 2026
 */
public class CrossHairAndTriangularPlasticPainterTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("CrossHairPainter / TriangularPlasticPainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		CrossHairPainter crossHairPainter = new CrossHairPainter(true, true);
		InfoVisChartPanel crossHairPanel = new InfoVisChartPanel(crossHairPainter);
		crossHairPanel.setPreferredSize(new Dimension(300, 150));
		crossHairPanel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				crossHairPainter.setCoordinates(e.getX(), e.getY());
				crossHairPanel.repaint();
			}
		});
		frame.add(crossHairPanel, BorderLayout.NORTH);

		JPanel buttonGrid = new JPanel(new GridLayout(2, 3, 4, 4));
		Color[] colors = { Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.MAGENTA, Color.CYAN };
		for (Color color : colors) {
			TriangularPlasticPainter painter = new TriangularPlasticPainter(color);
			InfoVisChartPanel panel = new InfoVisChartPanel(painter);
			panel.setPreferredSize(new Dimension(80, 60));
			panel.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					painter.setClicked(true);
					panel.repaint();
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					painter.setClicked(false);
					panel.repaint();
				}
			});
			buttonGrid.add(panel);
		}
		frame.add(buttonGrid, BorderLayout.CENTER);

		frame.setSize(320, 350);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

}
