package com.github.TKnudsen.infoVis.view.panels.piechart;

import java.awt.Color;
import java.awt.Point;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.painters.piechart.PieChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Pie chart panel
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class PieChartPanel extends InfoVisChartPanel
		implements IClickSelection<Integer>, ISelectionVisualizer<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5405783749506109755L;

	private final PieChartPainter pieChartPainter;

	public PieChartPanel(List<Double> pieces, List<Color> colors) {
		pieChartPainter = new PieChartPainter(pieces, colors);

		this.addChartPainter(pieChartPainter);
	}

	@Override
	public void setSelectedFunction(Function<? super Integer, Boolean> selectedFunction) {
		pieChartPainter.setSelectedFunction(selectedFunction);
	}

	@Override
	public List<Integer> getElementsAtPoint(Point p) {
		return pieChartPainter.getElementsAtPoint(p);
	}

}
