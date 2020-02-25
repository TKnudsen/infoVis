package com.github.TKnudsen.infoVis.view.visualChannels.position;

import java.util.Map;
import java.util.TreeMap;

import com.github.TKnudsen.ComplexDataObject.model.comparators.NumberComparator;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LogarithmicNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Uses a fixed, given set of position mappings
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public final class FixedPositionEncodingFunction implements IPositionEncodingFunction {

	private Number minValue;
	private Number maxValue;
	private Double minPixel;
	private Double maxPixel;

	private final Map<Number, Double> mapping;

	private final boolean allowOtherPositions = false;

	private boolean logarithmicScale = false;

	private NormalizationFunction scalingFunction;

	private boolean flipAxisValues = false;

	/**
	 * classical constructor for a non-inverted axis
	 * 
	 * @param minValue
	 * @param maxValue
	 * @param minPixel
	 * @param maxPixel
	 */
	public FixedPositionEncodingFunction(Map<Number, Double> mapping) {
		this.mapping = new TreeMap<>(new NumberComparator());
		this.mapping.putAll(mapping);

		refresh();
	}

	public void addFixedPosition(Number number, Double pixel) {
		this.mapping.put(number, pixel);

		refresh();
	}

	private void refresh() {
		StatisticsSupport numbers = new StatisticsSupport(mapping.keySet());
		this.minValue = numbers.getMin();
		this.maxValue = numbers.getMax();

		StatisticsSupport pixels = new StatisticsSupport(mapping.values());
		this.minPixel = pixels.getMin();
		this.maxPixel = pixels.getMax();

		initializeScalingFunction();
	}

	private void initializeScalingFunction() {
		if (!logarithmicScale)
			this.scalingFunction = new LinearNormalizationFunction(minValue, maxValue, false);
		else
			this.scalingFunction = new LogarithmicNormalizationFunction(minValue, maxValue, false);
	}

	@Override
	public Double apply(Number value) {
		if (mapping.containsKey(value))
			return mapping.get(value);

		if (allowOtherPositions) {
			Number scale = getRelativeMapping(value);
			return relativeValueToMapping(scale);
		}

		throw new IllegalArgumentException(getClass().getSimpleName() + ": undefined value");
	}

	/**
	 * relative output value. while the mapping calculates values between min and
	 * max this function provides relative output coordinates between 0.0 and 1.0
	 * 
	 * Input values exceeding the interval defined by the input domain are not
	 * cropped.
	 * 
	 * @param value numerical value of the input domain.
	 * @return mapped (screen space) value of the given value
	 */
	public Double getRelativeMapping(Number value) {
		return scalingFunction.apply(value).doubleValue();
	}

	/**
	 * scales a relative output value to the mapping (screen space) domain.
	 * 
	 * @param relative
	 * @return
	 */
	public Double relativeValueToMapping(Number relative) {
		if (flipAxisValues)
			return minPixel + (maxPixel - minPixel) * (1 - relative.doubleValue());
		else
			return minPixel + (maxPixel - minPixel) * relative.doubleValue();
	}

	@Override
	/**
	 * accepts a numerical value from the output domain and maps it back to the
	 * input value domain.
	 * 
	 * @param visual
	 * @return
	 */
	public Number inverseMapping(Double visual) {
		double scale = Double.NaN;
		if (!logarithmicScale)
			scale = MathFunctions.linearScale(minPixel, maxPixel, visual, false);
		else {
			System.err.println(
					"PositionEncodingFunction: inverse logarithmic scaling function is incorrect. I assume that the special treatment of values <1 causes this problem.");
			scale = MathFunctions.logarithmicScale(minPixel, maxPixel, visual, false);
		}
		
		if (flipAxisValues)
			scale = 1.0 - scale;
		return minValue.doubleValue() + (maxValue.doubleValue() - minValue.doubleValue()) * scale;
	}

	@Override
	public Number getMinWorldValue() {
		return minValue;
	}

	@Override
	/**
	 * re-configures the mapping function with a new minimum value for the INPUT
	 * value domain. This, however, may cause side effects when the mapping function
	 * is used in several contexts.
	 * 
	 * @param minValue
	 */
	public void setMinWorldValue(Number minValue) {
		this.minValue = minValue;
		this.scalingFunction.setGlobalMin(minValue);
	}

	@Override
	public Number getMaxWorldValue() {
		return maxValue;
	}

	@Override
	/**
	 * re-configures the mapping function with a new maximum value for the INPUT
	 * value domain. This, however, may cause side effects when the mapping function
	 * is used in several contexts.
	 * 
	 * @param maxValue
	 */
	public void setMaxWorldValue(Number maxValue) {
		this.maxValue = maxValue;
		this.scalingFunction.setGlobalMax(maxValue);
	}

	public boolean isLogarithmicScale() {
		return logarithmicScale;
	}

	@Override
	public void setLogarithmicScale(boolean logarithmicScale) {
		this.logarithmicScale = logarithmicScale;

		initializeScalingFunction();
	}

	@Override
	public Double getMinPixel() {
		return minPixel;
	}

	@Override
	/**
	 * re-configures the mapping function with a new minimum value for the OUTPUT
	 * value domain. This, however, may cause side effects when the mapping function
	 * is used in several contexts.
	 * 
	 * @param minPixel
	 */
	public void setMinPixel(Double minPixel) {
		this.minPixel = minPixel;
	}

	@Override
	public Double getMaxPixel() {
		return maxPixel;
	}

	@Override
	/**
	 * re-configures the mapping function with a new maximum value for the OUTPUT
	 * value domain. This, however, may cause side effects when the mapping function
	 * is used in several contexts.
	 * 
	 * @param maxPixel
	 */
	public void setMaxPixel(Double maxPixel) {
		this.maxPixel = maxPixel;
	}

	public boolean isFlipAxisValues() {
		return flipAxisValues;
	}

	@Override
	public void setFlipAxisValues(boolean flipAxisValues) {
		this.flipAxisValues = flipAxisValues;
	}

	public boolean isAllowOtherPositions() {
		return allowOtherPositions;
	}

}
