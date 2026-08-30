package com.github.TKnudsen.infoVis.view.painters.axis;

import java.util.List;

/**
 * <p>
 * Interface for all painters with a x axis. The XAxisPainter will provide most
 * of the functionality.
 * </p>
 *
 * @version 1.04
 * @since 2016
 */
public interface IXAxisCategorical<T extends List<String>> {
	public void setXAxisLabels(T labels);

	public void setDrawXAxis(boolean drawXAxis);

	public void setXAxisLegendHeight(double xAxisLegendHeight);
}
