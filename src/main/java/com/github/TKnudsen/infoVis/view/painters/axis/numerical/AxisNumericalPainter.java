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
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;

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

	protected int markerDistanceInPixels = 45;
	protected double markerLineWidth = 3.0;
	protected boolean pruneMinValue = true;
	protected boolean drawAxisBetweenAxeMarkersOnly = true;
	protected boolean enableToolTipping = true;

	protected boolean drawLabels = true;

	protected final PositionEncodingFunction positionEncodingFunction;
	// listening to the positionEncodingFunction
	private final PositionEncodingFunctionListener myPositionEncodingFunctionListener = () -> markerPositionsWithLabels = null;

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

		positionEncodingFunction = new PositionEncodingFunction(this.minValue, this.maxValue, 0.0, 0.0, flipAxisValues);
		positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
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
		double valueInterval = Math.abs(maxValue.doubleValue() - minValue.doubleValue());

		if (!isLogarithmicScale()) {
			double quantization = AxisCartTools.suggestMeaningfulValueIntervalLinear(valueInterval / markerCount);
			double startValue = minValue.doubleValue();

			double d = startValue;
			double mod = d % quantization;
			startValue = d - mod;

			if (startValue < minValue.doubleValue())
				startValue += quantization;

			double pixValue = positionEncodingFunction.apply(startValue);
			addMarkerPosition(pixValue, startValue);

			// iterate...
			double loop = quantization;
			while (!Double.isNaN(loop) && !Double.isInfinite(loop)
					&& new BigDecimal(loop).doubleValue() <= new BigDecimal(valueInterval).doubleValue()
					&& new BigDecimal(startValue + loop).doubleValue() < maxValue.doubleValue()) {
				pixValue = positionEncodingFunction.apply(startValue + loop);
				addMarkerPosition(pixValue, startValue + loop);
				loop += quantization;
			}
		} else {
			// logarithmicScale
			List<Double> meaningfulValuesLogarithmic = AxisCartTools.suggestMeaningfulValueIntervalLogarithmic(
					minValue.doubleValue(), maxValue.doubleValue(), (int) markerCount);
			if (meaningfulValuesLogarithmic != null)
				for (Double value : meaningfulValuesLogarithmic) {
					Double pixValue = positionEncodingFunction.apply(value);
					addMarkerPosition(pixValue, value);
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

	private void addMarkerPosition(Double pixValue, Double value) {
		markerPositionsWithLabels.add(new AbstractMap.SimpleEntry<Double, String>(pixValue,
				AxisCartTools.suggestMeaningfulValueString(value)));
	}

	@Override
	public ChartPainter getTooltip(Point p) {

		Number worldX = positionEncodingFunction.inverseMapping(p.getX());

		StringPainter sr = new StringPainter(String.valueOf(worldX));
		sr.setRectangle(new Rectangle2D.Double(p.getX() - 80, p.getY() - 40, 80, 40));
		sr.setFontSize((int) Math.max(12, this.rectangle.getHeight() * 0.66));

		return sr;
	}

	public abstract double getAxisAlignmentCoordinate();

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
		return positionEncodingFunction.isLogarithmicScale();
	}

	public void setLogarithmicScale(boolean logarithmicScale) {
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
		return positionEncodingFunction.isFlipAxisValues();
	}

	public void setFlipAxisValues(boolean flipAxisValues) {
		positionEncodingFunction.setFlipAxisValues(flipAxisValues);
	}

}
