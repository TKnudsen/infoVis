package com.github.TKnudsen.infoVis.view.tools.test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.primitives.CirclePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.tools.ImageTools;

/**
 * Demo of {@link ImageTools}: renders a {@link CirclePainter} directly via
 * {@link ImageTools#createImage(com.github.TKnudsen.infoVis.view.painters.ChartPainter, int, int)}
 * and an {@link InfoVisChartPanel} via
 * {@link ImageTools#createImage(InfoVisChartPanel)}, then displays both
 * rendered images.
 *
 * @since 2026
 */
public class ImageToolsTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("ImageTools Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new java.awt.GridLayout(1, 2, 10, 10));

		CirclePainter painter = new CirclePainter();
		painter.setPaint(Color.ORANGE);
		BufferedImage fromPainter = ImageTools.createImage(painter, 200, 200);
		frame.add(new JLabel(new ImageIcon(fromPainter)));

		InfoVisChartPanel panel = new InfoVisChartPanel(painter);
		panel.setSize(200, 200);
		panel.doLayout();
		BufferedImage fromPanel = ImageTools.createImage(panel);
		frame.add(new JLabel(new ImageIcon(fromPanel)));

		frame.setSize(450, 250);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

}
