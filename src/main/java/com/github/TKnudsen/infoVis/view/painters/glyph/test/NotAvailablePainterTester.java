package com.github.TKnudsen.infoVis.view.painters.glyph.test;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.glyph.NotAvailablePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive demo of {@link NotAvailablePainter}: default and
 * thick-stroke variants.
 *
 * @version 1.0
 * @since 2026
 */
public class NotAvailablePainterTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("NotAvailablePainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(1, 2, 10, 10));

		NotAvailablePainter painter = new NotAvailablePainter();
		painter.setPaint(Color.DARK_GRAY);
		frame.add(new InfoVisChartPanel(painter));

		NotAvailablePainter thickPainter = new NotAvailablePainter();
		thickPainter.setPaint(Color.DARK_GRAY);
		thickPainter.setStrokeWidthRate(0.2);
		frame.add(new InfoVisChartPanel(thickPainter));

		frame.setSize(400, 200);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

}
