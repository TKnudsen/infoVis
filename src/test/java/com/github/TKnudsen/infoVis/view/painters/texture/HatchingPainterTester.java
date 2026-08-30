package com.github.TKnudsen.infoVis.view.painters.texture;

import java.awt.Color;

import javax.swing.JFrame;

import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;

public class HatchingPainterTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setTitle("HatchingRenderer Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		HatchingPainter hatchingRenderer = new HatchingPainter(
				new Color[] { Color.ORANGE, Color.RED, ColorTools.setAlpha(Color.WHITE, 0.0f) },
				new double[] { 4, 2, 3 }, 45.0f);
		InfoVisChartPanel testingPanel = new InfoVisChartPanel(hatchingRenderer);

		frame.add(testingPanel);
		frame.setSize(800, 300);
		frame.setLocation(50, 50);
		frame.setVisible(true);
	}
}
