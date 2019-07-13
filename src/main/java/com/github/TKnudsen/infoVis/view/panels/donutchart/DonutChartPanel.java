package com.github.TKnudsen.infoVis.view.panels.donutchart;

import java.awt.Color;
import java.awt.Point;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.painters.donutchart.DonutChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Donut chart panel
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class DonutChartPanel extends InfoVisChartPanel
		implements IClickSelection<Integer>, ISelectionVisualizer<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5405783749506109755L;

	private final DonutChartPainter donutChartPainter;

	public DonutChartPanel(List<Double> pieces, List<Color> colors) {
		donutChartPainter = new DonutChartPainter(pieces, colors);

		this.addChartPainter(donutChartPainter);
	}

	@Override
	public void setSelectedFunction(Function<? super Integer, Boolean> selectedFunction) {
		donutChartPainter.setSelectedFunction(selectedFunction);
	}

	@Override
	public List<Integer> getElementsAtPoint(Point p) {
		return donutChartPainter.getElementsAtPoint(p);
	}

	public double getDonutRadiusRelative() {
		return donutChartPainter.getDonutRadiusRelative();
	}

	public void setDonutRadiusRelative(double donutRadiusRelative) {
		this.donutChartPainter.setDonutRadiusRelative(donutRadiusRelative);
	}

}