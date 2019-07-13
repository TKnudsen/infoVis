package com.github.TKnudsen.infoVis.view.painters.axis;

import java.util.List;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Interface for all painters with a categorical y axis.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public interface IYAxisCategorical<T extends List<String>> {
	public void setYAxisLabels(T labels);

	public void setDrawYAxis(boolean drawYAxis);

	public void setYAxisLegendWidth(double yAxisLegendWidth);
}
