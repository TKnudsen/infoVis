package com.github.TKnudsen.infoVis.view.panels.barchart;

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
 * @since 2026
 */
public interface IBarChart<E> extends IClickSelection<E>, IRectangleSelection<E>, ISelectionVisualizer<E> {

	/**
	 * use direct access to BarChart with care
	 * 
	 * @return bar chart painter
	 */
	public BarChartPainter getBarChartPainter();

}
