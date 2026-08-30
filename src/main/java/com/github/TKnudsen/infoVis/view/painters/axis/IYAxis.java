package com.github.TKnudsen.infoVis.view.painters.axis;

/**
 * <p>
 * Interface for all painters with a y axis. The YAxisPainter will provide most
 * of the functionality.
 * </p>
 *
 * @version 1.05
 * @since 2016
 */
public interface IYAxis<T extends Number> {
	public void setYAxisMinValue(T yAxisMinValue);

	public void setYAxisMaxValue(T yAxisMaxValue);

	public void setDrawYAxis(boolean drawYAxis);

	public void setYAxisLegendWidth(double yAxisLegendWidth);

	public void setYAxisMarkerDistanceInPixels(int markerDistanceInPixels);

	public void setYAxisPhysicalUnit(String physicalUnit);
}
