package com.github.TKnudsen.infoVis.view.painters.glyph.test;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.glyph.CrossHairPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive demo of {@link CrossHairPainter}: a crosshair that
 * follows the mouse across the panel.
 *
 * @version 1.0
 * @since 2026
 */
public class CrossHairPainterTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("CrossHairPainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		CrossHairPainter crossHairPainter = new CrossHairPainter(true, true);
		InfoVisChartPanel panel = new InfoVisChartPanel(crossHairPainter);
		panel.setPreferredSize(new Dimension(300, 150));
		panel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				crossHairPainter.setCoordinates(e.getX(), e.getY());
				panel.repaint();
			}
		});
		frame.add(panel);

		frame.setSize(320, 190);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

}
