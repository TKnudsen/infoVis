package com.github.TKnudsen.infoVis.view.painters.glyph.primitives.test;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.painters.glyph.primitives.DirectionalArrowPainter;
import com.github.TKnudsen.infoVis.view.painters.glyph.primitives.DirectionalArrowPainter.Direction;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * Standalone interactive demo of {@link DirectionalArrowPainter}: one cell
 * per cardinal direction, plus an arbitrary 45-degree angle.
 *
 * @version 1.0
 * @since 2026
 */
public class DirectionalArrowPainterTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("DirectionalArrowPainter Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(1, 5, 10, 10));

		for (Direction direction : Direction.values()) {
			DirectionalArrowPainter painter = new DirectionalArrowPainter(direction);
			painter.setPaint(Color.BLUE);
			frame.add(new InfoVisChartPanel(painter));
		}

		DirectionalArrowPainter diagonal = new DirectionalArrowPainter(45);
		diagonal.setPaint(Color.GREEN.darker());
		frame.add(new InfoVisChartPanel(diagonal));

		frame.setSize(900, 200);
		frame.setLocation(50, 50);

		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

}
