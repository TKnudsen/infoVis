package com.github.TKnudsen.infoVis.view.frames;

import java.awt.GridLayout;
import java.util.List;

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
 * Copyright: (c) 2016-2022 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.08
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

	/**
	 * 
	 * @param panels   the panels
	 * @param headline the title
	 * @return the frame
	 */
	public static SVGFrame dropSVGFrame(List<JPanel> panels, String headline) {
		JPanel panel = new JPanel(new GridLayout(0, panels.size()));
		for (JPanel p : panels)
			panel.add(p);

		return SVGFrameTools.dropSVGFrame(panel, headline, 1200, 600);
	}

	public static SVGFrame dropSVGFrame(JPanel panel, String title, int x, int y, int posX, int posY) {
		SVGFrame frame = new SVGFrame();

		frame.setTitle(title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(0, 1));

		frame.setSize(x, y);
		frame.setLocation(posX, posY);
		frame.setVisible(true);

		frame.add(panel);
		frame.revalidate();
		frame.repaint();
		return frame;
	}

	public static SVGFrame dropSVGFrame(JComponent component, String title, int x, int y) {
		SVGFrame frame = new SVGFrame();

		frame.setTitle(title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(0, 1));

		frame.setSize(x, y);
		frame.setLocation(50, 50);
		frame.setVisible(true);

		frame.add(component);
		frame.revalidate();
		frame.repaint();
		return frame;
	}

	public static SVGFrame populateSVGFrame(SVGFrame frame, String title, int x, int y) {
		frame.setTitle(title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(0, 1));

		frame.setSize(x, y);
		frame.setLocation(50, 50);
		frame.setVisible(true);

		frame.revalidate();
		frame.repaint();

		return frame;
	}

}
