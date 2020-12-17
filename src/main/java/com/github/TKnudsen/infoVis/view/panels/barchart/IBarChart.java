package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Point;
import java.awt.geom.RectangularShape;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Helps to handle horizontal and vertical bar charts in the same way. combines
 * general behavior.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public interface IBarChart
		extends IClickSelection<Integer>, IRectangleSelection<Integer>, ISelectionVisualizer<Integer> {

	/**
	 * use direct access to BarChart with care
	 * 
	 * @return
	 */
	public BarChartPainter getBarChartPainter();

	@Override
	default public List<Integer> getElementsAtPoint(Point p) {
		return getBarChartPainter().getElementsAtPoint(p);
	}

	@Override
	default public List<Integer> getElementsInRectangle(RectangularShape rectangle) {
		return getBarChartPainter().getElementsInRectangle(rectangle);
	}

	@Override
	default public void setSelectedFunction(Function<? super Integer, Boolean> selectedFunction) {
		this.getBarChartPainter().setSelectedFunction(selectedFunction);
	}

}
