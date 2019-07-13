package com.github.TKnudsen.infoVis.view.frames;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Creates SVGFrames for given painters or panels
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.07
 */
public class SVGFrameTools {

	public static SVGFrame dropSVGFrame(ChartPainter painter, String title, int x, int y) {
		InfoVisChartPanel panel = new InfoVisChartPanel();
		panel.addChartPainter(painter);
		return dropSVGFrame(panel, title, x, y, 50, 50);
	}

	public static SVGFrame dropSVGFrame(JPanel panel, String title, int x, int y) {
		return dropSVGFrame(panel, title, x, y, 50, 50);
	}

	public static SVGFrame dropSVGFrame(JPanel panel, String title, int x, int y, int posX, int posY) {
		SVGFrame frame = new SVGFrame();

		frame.setTitle(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(0, 1));

		frame.setSize(x, y);
		frame.setLocation(posX, posY);
		frame.setVisible(true);

		frame.add(panel);
		frame.revalidate();
		frame.repaint();
		return frame;
	}

	public static SVGFrame dropSVGFrame(JComponent panel, String title, int x, int y) {
		SVGFrame frame = new SVGFrame();

		frame.setTitle(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(0, 1));

		frame.setSize(x, y);
		frame.setLocation(50, 50);
		frame.setVisible(true);

		frame.add(panel);
		frame.revalidate();
		frame.repaint();
		return frame;
	}

}
