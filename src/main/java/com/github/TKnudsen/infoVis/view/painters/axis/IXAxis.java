package com.github.TKnudsen.infoVis.view.painters.axis;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Interface for all painters with an x axis. The XAxisPainter will provide most
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
public interface IXAxis<T extends Number> {
	public void setXAxisMinValue(T xAxisMinValue);

	public void setXAxisMaxValue(T xAxisMaxValue);

	public void setDrawXAxis(boolean drawXAxis);

	public void setXAxisLegendHeight(double xAxisLegendHeight);

	public void setXAxisMarkerDistanceInPixels(int markerDistanceInPixels);

	public void setXAxisPhysicalUnit(String physicalUnit);
}
