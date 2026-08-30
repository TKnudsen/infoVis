package com.github.TKnudsen.infoVis.view.painters.axis;

import java.util.List;

/**
 * <p>
 * Interface for all painters with a categorical y axis.
 * </p>
 *
 * @version 1.04
 * @since 2016
 */
public interface IYAxisCategorical<T extends List<String>> {
	public void setYAxisLabels(T labels);

	public void setDrawYAxis(boolean drawYAxis);

	public void setYAxisLegendWidth(double yAxisLegendWidth);
}
