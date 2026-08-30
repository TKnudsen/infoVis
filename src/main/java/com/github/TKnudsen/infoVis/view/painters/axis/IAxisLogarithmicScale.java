package com.github.TKnudsen.infoVis.view.painters.axis;

/**
 * <p>
 * Interface for all painters which are able to paint data with an logarithmic
 * axis.
 * </p>
 *
 * @version 1.09
 * @since 2016
 */
public interface IAxisLogarithmicScale {
	public boolean isLogarithmicScale();

	public void setLogarithmicScale(boolean logarithmic);
}
