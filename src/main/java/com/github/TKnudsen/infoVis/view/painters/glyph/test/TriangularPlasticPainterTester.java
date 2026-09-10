package com.github.TKnudsen.infoVis.view.painters.glyph.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.glyph.TriangularPlasticPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive demo of {@link TriangularPlasticPainter}: a grid of
 * beveled button glyphs that toggle their pressed look while the mouse is
 * held down.
 *
 * @version 1.0
 * @since 2026
 */
public class TriangularPlasticPainterTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("TriangularPlasticPainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
		frame.add(buttonGrid);

		frame.setSize(320, 220);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

}
