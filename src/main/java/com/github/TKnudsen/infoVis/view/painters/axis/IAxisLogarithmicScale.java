package com.github.TKnudsen.infoVis.view.painters.axis;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Interface for all painters which are able to paint data with an logarithmic
 * axis.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.09
 */
public interface IAxisLogarithmicScale {
	public boolean isLogarithmicScale();

	public void setLogarithmicScale(boolean logarithmic);
}
