package com.github.TKnudsen.infoVis.view.painters.axis;

/**
 * <p>
 * Interface for all painters with an x axis. The XAxisPainter will provide most
 * of the functionality.
 * </p>
 *
 * @version 1.05
 * @since 2016
 */
public interface IXAxis<T extends Number> {
	public void setXAxisMinValue(T xAxisMinValue);

	public void setXAxisMaxValue(T xAxisMaxValue);

	public void setDrawXAxis(boolean drawXAxis);

	public void setXAxisLegendHeight(double xAxisLegendHeight);

	public void setXAxisMarkerDistanceInPixels(int markerDistanceInPixels);

	public void setXAxisPhysicalUnit(String physicalUnit);
}
