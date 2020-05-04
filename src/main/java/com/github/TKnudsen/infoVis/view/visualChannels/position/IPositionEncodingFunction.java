package com.github.TKnudsen.infoVis.view.visualChannels.position;

import java.util.function.Function;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 *
 */
public interface IPositionEncodingFunction extends Function<Number, Double> {

	/**
	 * the inverse mapping has several advantages whenever a screen position shall
	 * be linked to the original input data. User interaction is a common example.
	 * 
	 * @param visual
	 * @return
	 */
	public Number inverseMapping(Double visual);

	public Number getMinWorldValue();

	/**
	 * re-configures the mapping function with a new minimum value for the INPUT
	 * value domain. It is important to be aware of whom is allowed to change the
	 * value domain to avoid side effects if an instance is used in several
	 * contexts.
	 * 
	 * @param minValue
	 */
	public void setMinWorldValue(Number minValue);

	public Number getMaxWorldValue();

	/**
	 * re-configures the mapping function with a new maximum value for the INPUT
	 * value domain. It is important to be aware of whom is allowed to change the
	 * value domain to avoid side effects if an instance is used in several
	 * contexts.
	 * 
	 * @param maxValue
	 */
	public void setMaxWorldValue(Number maxValue);

	public Number getMinPixel();

	/**
	 * re-configures the mapping function with a new minimum value for the OUTPUT
	 * value domain. It is important to be aware of whom is allowed to change the
	 * value domain to avoid side effects if an instance is used in several
	 * contexts.
	 * 
	 * @param minPixel
	 */
	public void setMinPixel(Double minPixel);

	public Number getMaxPixel();

	/**
	 * re-configures the mapping function with a new minimum value for the OUTPUT
	 * value domain. It is important to be aware of whom is allowed to change the
	 * value domain to avoid side effects if an instance is used in several
	 * contexts.
	 * 
	 * @param maxPixel
	 */
	public void setMaxPixel(Double maxPixel);

	/**
	 * needs to be discussed whether this behavior should be included in the
	 * interface or not.
	 * 
	 * @param logarithmicScale
	 */
	public void setLogarithmicScale(boolean logarithmicScale);

	public boolean isFlipAxisValues();

	/**
	 * allows flipping the axis in the visual space. This may particularly make
	 * sense if a y-axis has its origin in (0,0) at the upper left of the canvas
	 * rather than at the lower left.
	 * 
	 * @param flipAxisValues
	 */
	public void setFlipAxisValues(boolean flipAxisValues);

	public void addPositionEncodingFunctionListener(PositionEncodingFunctionListener positionEncodingFunctionListener);

	public void removePositionEncodingFunctionListener(
			PositionEncodingFunctionListener positionEncodingFunctionListener);

	/**
	 * This method has to be called when an additional parameter of the
	 * {@link IPositionEncodingFunction} was changed, i.e., when the internal state
	 * of the {@link IPositionEncodingFunction} was modified. Examples include axis
	 * inversion or logarithmic scales.
	 * 
	 * Notice: modifications to the four main variables (minValue, maxValue,
	 * minPixel, maxPixel) shall not (!) be used with this change event.
	 * 
	 * {@link PositionEncodingFunctionListener} instances should be informed about
	 * this by calling
	 * {@link PositionEncodingFunctionListener#encodingFunctionChanged()}
	 */
	void firePositionEncodingParameterChanged();
}
