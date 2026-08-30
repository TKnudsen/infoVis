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
 * Helps to handle horizontal and vertical bar charts in the same way. combines
 * general behavior.
 * </p>
 *
 * @version 1.01
 * @since 2016
 */
public interface IBarChartValueBased
		extends IClickSelection<Integer>, IRectangleSelection<Integer>, ISelectionVisualizer<Integer> {

	/**
	 * use direct access to BarChartValueBased with care
	 * 
	 * @return bar chart painter
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
