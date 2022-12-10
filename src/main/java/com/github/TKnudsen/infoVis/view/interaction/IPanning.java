package com.github.TKnudsen.infoVis.view.interaction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Models a shift of the view in x/y direction.
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public interface IPanning {

	/**
	 * apply panning/shifting a view towards deltaY and deltaY in screen/pixel
	 * coordinates.
	 * 
	 * @param deltaX delta x
	 * @param deltaY delty y
	 */
	public void pan(int deltaX, int deltaY);
}
