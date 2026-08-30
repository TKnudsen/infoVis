package com.github.TKnudsen.infoVis.view.interaction;

/**
 * @version 1.02
 * @since 2016
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
