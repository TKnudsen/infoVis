package com.github.TKnudsen.infoVis.view.painters.axis.numerical;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisCartTools;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisLineAlignment;
import com.github.TKnudsen.infoVis.view.painters.axis.AxisPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncoder;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Abstract basis class for axe drawings.
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.01
 */
public abstract class AxisNumericalPainter<T extends Number> extends AxisPainter implements ITooltip, IPositionEncoder {

	private T minValue;
	private T maxValue;

	protected boolean logarithmicScale = false;
	protected int markerDistanceInPixels = 45;
	protected double markerLineWidth = 3.0;
	protected boolean pruneMinValue = true;
	protected boolean drawAxisBetweenAxeMarkersOnly = true;
	protected boolean enableToolTipping = true;

	protected boolean drawLabels = true;

	private boolean flipAxisValues = false;

	protected final PositionEncodingFunction positionEncodingFunction;

	private int tickCount = -1;

	protected AxisLineAlignment axisLineAlignment = AxisLineAlignment.RIGHT;

	protected boolean drawPhysicalUnit = true;
	protected String physicalUnit = "";

	// internal
	protected List<Entry<Double, String>> markerPositionsWithLabels = new ArrayList<Entry<Double, String>>();

	/**
	 * classical constructor for a non-flipped axis
	 * 
	 * @param start
	 * @param end
	 */
	public AxisNumericalPainter(T start, T end) {
		this(start, end, false);
	}

	public AxisNumericalPainter(T start, T end, boolean flipAxisValues) {
		this.minValue = start;
		this.maxValue = end;

		this.flipAxisValues = flipAxisValues;

		positionEncodingFunction = new PositionEncodingFunction(this.minValue, this.maxValue, 0.0, 0.0, flipAxisValues);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		drawAxis(g2);

		if (drawPhysicalUnit)
			drawPhysU(g2);
	}

	protected abstract void drawAxis(Graphics2D g2);

	protected abstract void drawPhysU(Graphics2D g2);

	/**
	 * accepts new screen coordinates for the axis and the value mapping
	 * 
	 * @param minPixel
	 * @param maxPixel
	 */
	protected final void setAxisWorldCoordinates(double minPixel, double maxPixel) {
		setAxisWorldCoordinatesAndCalculateMarkers(minPixel, maxPixel,
				Math.max(2, Math.abs(maxPixel - minPixel) / (double) markerDistanceInPixels));
	}

	/**
	 * accepts new screen coordinates for the axis. Sets the position mapping and
	 * adds markers to the axis
	 * 
	 * @param minPixel
	 * @param maxPixel
	 * @param markerCount
	 */
	protected final void setAxisWorldCoordinatesAndCalculateMarkers(double minPixel, double maxPixel,
			double markerCount) {

		positionEncodingFunction.setMinPixel(minPixel);
		positionEncodingFunction.setMaxPixel(maxPixel);

		calculateMarkerPositions(minPixel, maxPixel, markerCount);
	}

	protected void calculateMarkerPositions(double minPixel, double maxPixel, double markerCount) {
		// axis markers
		markerPositionsWithLabels = new ArrayList<Entry<Double, String>>();

		// internal variables
		double interval = Math.abs(maxValue.doubleValue() - minValue.doubleValue());
		// double width = Math.abs(maxPixel - minPixel);
		// double width = maxPixel - minPixel;
		// double markerCount = Math.max(2, Math.abs(width) / (double)
		// markerDistanceInPixels);

		if (!logarithmicScale) {
			double valueInterval = AxisCartTools.suggestMeaningfulValueIntervalLinear(interval / markerCount);
			double startValue = minValue.doubleValue();

			// if (!pruneMinValue)
			// markerPositionsWithLabels.add(new AbstractMap.SimpleEntry<Double,
			// String>(minPixel,
			// AxisCartTools.suggestMeaningfulValueString(position1DMapping.apply(startValue))));
			//
			// else {
			double d = startValue;
			double mod = d % valueInterval;
			startValue = d - mod;

			if (startValue < minValue.doubleValue())
				startValue += valueInterval;

			double pixValue = positionEncodingFunction.apply(startValue);

			markerPositionsWithLabels.add(new AbstractMap.SimpleEntry<Double, String>(pixValue,
					AxisCartTools.suggestMeaningfulValueString(startValue)));

			// iterate...
			double loop = valueInterval;
			while (!Double.isNaN(loop) && !Double.isInfinite(loop)
					&& new BigDecimal(loop).doubleValue() <= new BigDecimal(interval).doubleValue()) {
				pixValue = positionEncodingFunction.apply(startValue + loop);
				markerPositionsWithLabels.add(new AbstractMap.SimpleEntry<Double, String>(pixValue,
						AxisCartTools.suggestMeaningfulValueString(startValue + loop)));
				loop += valueInterval;
			}
		} else {
			// logarithmicScale
			List<Double> meaningfulValuesLogarithmic = AxisCartTools.suggestMeaningfulValueIntervalLogarithmic(
					minValue.doubleValue(), maxValue.doubleValue(), (int) markerCount);
			if (meaningfulValuesLogarithmic != null)
				for (Double value : meaningfulValuesLogarithmic) {
					Double pixValue = positionEncodingFunction.apply(value);
					markerPositionsWithLabels.add(new AbstractMap.SimpleEntry<Double, String>(pixValue,
							AxisCartTools.suggestMeaningfulValueString(value)));
				}
		}

		// post-processing: remove identical (redundant) labels.
		// This particularly makes sense for discrete (Integer) Axis painters
		Entry<Double, String> lastEntry = null;
		for (int i = 0; i < markerPositionsWithLabels.size(); i++) {
			Entry<Double, String> entry = markerPositionsWithLabels.get(i);
			if (lastEntry != null && entry.getValue().equals(lastEntry.getValue())) {
				markerPositionsWithLabels.remove(i);
				i--;
			} else
				lastEntry = entry;
		}
	}

	@Override
	public ChartPainter getTooltip(Point p) {

		Number worldX = positionEncodingFunction.inverseMapping(p.getX());

		StringPainter sr = new StringPainter(String.valueOf(worldX));
		sr.setRectangle(new Rectangle2D.Double(p.getX() - 80, p.getY() - 40, 80, 40));
		sr.setFontSize((int) Math.max(12, this.rectangle.getHeight() * 0.66));

		return sr;
	}

	@Override
	public IPositionEncodingFunction getPositionEncodingFunction() {
		return this.positionEncodingFunction;
	}

	public List<Entry<Double, String>> getMarkerPositionsWithLabels() {
		return markerPositionsWithLabels;
	}

	public boolean isDrawPhysicalUnit() {
		return drawPhysicalUnit;
	}

	public void setDrawPhysicalUnit(boolean drawPhysicalUnit) {
		this.drawPhysicalUnit = drawPhysicalUnit;
	}

	public String getPhysicalUnit() {
		return physicalUnit;
	}

	public void setPhysicalUnit(String physicalUnit) {
		this.physicalUnit = physicalUnit;
	}

	public int getMarkerDistanceInPixels() {
		return markerDistanceInPixels;
	}

	public void setMarkerDistanceInPixels(int markerDistanceInPixels) {
		this.markerDistanceInPixels = markerDistanceInPixels;
		setRectangle(rectangle);
	}

	public T getMinValue() {
		return minValue;
	}

	public void setMinValue(T minValue) {
		this.minValue = minValue;
		markerPositionsWithLabels = null;

		this.positionEncodingFunction.setMinWorldValue(minValue);
	}

	public T getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(T maxValue) {
		this.maxValue = maxValue;
		markerPositionsWithLabels = null;

		this.positionEncodingFunction.setMaxWorldValue(maxValue);
	}

	public boolean isPruneMinValue() {
		return pruneMinValue;
	}

	public void setPruneMinValue(boolean pruneMinValue) {
		this.pruneMinValue = pruneMinValue;
	}

	public boolean isDrawAxisBetweenAxeMarkersOnly() {
		return drawAxisBetweenAxeMarkersOnly;
	}

	public void setDrawAxisBetweenAxeMarkersOnly(boolean drawAxisBetweenAxeMarkersOnly) {
		this.drawAxisBetweenAxeMarkersOnly = drawAxisBetweenAxeMarkersOnly;
	}

	public boolean isLogarithmicScale() {
		return logarithmicScale;
	}

	public void setLogarithmicScale(boolean logarithmicScale) {
		this.logarithmicScale = logarithmicScale;

		this.positionEncodingFunction.setLogarithmicScale(logarithmicScale);
	}

	public AxisLineAlignment getAxisLineAlignment() {
		return axisLineAlignment;
	}

	public void setAxisLineAlignment(AxisLineAlignment axisLineAlignment) {
		this.axisLineAlignment = axisLineAlignment;
	}

	public boolean isDrawLabels() {
		return drawLabels;
	}

	public void setDrawLabels(boolean drawLabels) {
		this.drawLabels = drawLabels;
	}

	public int getTickCount() {
		return tickCount;
	}

	public void setTickCount(int tickCount) {
		this.tickCount = tickCount;
	}

	public boolean isFlipAxisValues() {
		return flipAxisValues;
	}

	public void setFlipAxisValues(boolean flipAxisValues) {
		this.flipAxisValues = flipAxisValues;

		positionEncodingFunction.setFlipAxisValues(flipAxisValues);
	}

}
