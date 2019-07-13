package com.github.TKnudsen.infoVis.view.painters.axis;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Interface for all painters with a y axis. The YAxisPainter will provide most
 * of the functionality.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 */
public interface IYAxis<T extends Number> {
	public void setYAxisMinValue(T yAxisMinValue);

	public void setYAxisMaxValue(T yAxisMaxValue);

	public void setDrawYAxis(boolean drawYAxis);

	public void setYAxisLegendWidth(double yAxisLegendWidth);

	public void setYAxisMarkerDistanceInPixels(int markerDistanceInPixels);

	public void setYAxisPhysicalUnit(String physicalUnit);
}
