package com.github.TKnudsen.infoVis.view.painters.number;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Random;

import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.number.ProbabilityPainter.ShapeType;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.ui.NimbusUITools;

public class ProbabilityPainterTester {

	public static void main(String[] args) {

		NimbusUITools.switchToNimbus();

		Random random = new Random(42);

		JPanel grid = new JPanel(new GridLayout(2, 3));

		ProbabilityPainter probabilityPainter1 = new ProbabilityPainter(random.nextDouble());
		probabilityPainter1.setPaint(new Color(232, 45, 80));
		probabilityPainter1.setShapeType(ShapeType.Rectangle);
		grid.add(new InfoVisChartPanel(probabilityPainter1));

		ProbabilityPainter probabilityPainter1b = new ProbabilityPainter(random.nextDouble());
		probabilityPainter1b.setPaint(new Color(74, 194, 255));
		probabilityPainter1b.setDrawOutline(false);
		probabilityPainter1b.setShapeType(ShapeType.Rectangle);
		grid.add(new InfoVisChartPanel(probabilityPainter1b));

		ProbabilityPainter probabilityPainter1c = new ProbabilityPainter(random.nextDouble());
		probabilityPainter1c.setPaint(new Color(232, 45, 80));
		probabilityPainter1c.setGridSpacing(2.5);
		probabilityPainter1c.setShapeType(ShapeType.Rectangle);
		grid.add(new InfoVisChartPanel(probabilityPainter1c));

		ProbabilityPainter probabilityPainter2 = new ProbabilityPainter(random.nextDouble());
		probabilityPainter2.setPaint(new Color(74, 194, 255));
		probabilityPainter2.setShapeType(ShapeType.Dot);
		grid.add(new InfoVisChartPanel(probabilityPainter2));

		ProbabilityPainter probabilityPainter2b = new ProbabilityPainter(random.nextDouble());
		probabilityPainter2b.setPaint(new Color(232, 45, 80));
		probabilityPainter2b.setDrawOutline(false);
		probabilityPainter2b.setShapeType(ShapeType.Dot);
		grid.add(new InfoVisChartPanel(probabilityPainter2b));

		ProbabilityPainter probabilityPainter2c = new ProbabilityPainter(random.nextDouble());
		probabilityPainter2c.setPaint(new Color(74, 194, 255));
		probabilityPainter2c.setShapeType(ShapeType.Dot);
		probabilityPainter2c.setGridSpacing(2.5);
		grid.add(new InfoVisChartPanel(probabilityPainter2c));

		SVGFrameTools.dropSVGFrame(grid, "Two ProbabilityPainters", 600, 200);
	}

}
