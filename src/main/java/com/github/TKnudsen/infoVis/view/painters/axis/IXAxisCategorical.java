package com.github.TKnudsen.infoVis.view.painters.axis;

import java.util.List;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Interface for all painters with a x axis. The XAxisPainter will provide most
 * of the functionality.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public interface IXAxisCategorical<T extends List<String>> {
	public void setXAxisLabels(T labels);

	public void setDrawXAxis(boolean drawXAxis);

	public void setXAxisLegendHeight(double xAxisLegendHeight);
}
