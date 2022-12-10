package com.github.TKnudsen.infoVis.view.interaction.controls;

import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import com.github.TKnudsen.infoVis.view.interaction.controls.rangeSlider.RangeSlider;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoder;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * Orientation and Flip of the Slider Thumb can be adjusted through
 * JSliders.HORIZONTAL/VERTICAL orientation and by setting setFlipThumb(true)
 * 
 * <p>
 * Copyright: (c) 2016-2022 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 *
 */
public class InfoVisRangeSlider extends RangeSlider implements IXPositionEncoder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * slider has a width of 16 pixels, 8 of these pixels lead to a margin on both
	 * sides (left and right).
	 */
	public static final int SLIDER_POINTER_WIDTH = 16;

	private final PositionEncodingFunction positionEncodingFunction;

	// listening to the positionEncodingFunction
	private final PositionEncodingFunctionListener myPositionEncodingFunctionListener = this::refreshPositionMapping;

	/**
	 * Creates a horizontal slider using the specified min and max with an initial
	 * value equal to the average of the min plus max. and initial low and high
	 * values both at 50.
	 * 
	 * Attention: the com.jidesoft.swing.RangeSlider basic class produced an
	 * IllegalAccessError in the constructor for some configurations. The following
	 * line executed beforehand helps in some cases
	 * LookAndFeelFactory.setDefaultStyle(1);
	 *
	 * @param min the minimum value of the slider.
	 * @param max the maximum value of the slider.
	 */
	public InfoVisRangeSlider(int min, int max) {
		super(min, max);

		this.positionEncodingFunction = new PositionEncodingFunction(getMinimum(), getMaximum(), 0.0, 0.0, false);

		this.positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		initialize();
	}

	/**
	 * Creates a horizontal slider using the specified min, max, low and high value.
	 * 
	 * Attention: the com.jidesoft.swing.RangeSlider basic class produced an
	 * IllegalAccessError in the constructor for some configurations. The following
	 * line executed beforehand helps in some cases
	 * LookAndFeelFactory.setDefaultStyle(1);
	 *
	 * @param min  the minimum value of the slider.
	 * @param max  the maximum value of the slider.
	 * @param low  the low value of the slider since it is a range.
	 * @param high the high value of the slider since it is a range.
	 */
	public InfoVisRangeSlider(int min, int max, int low, int high) {
		super(min, max, low, high);

		this.positionEncodingFunction = new PositionEncodingFunction(getMinimum(), getMaximum(), 0.0, 0.0, false);

		this.positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		initialize();
	}

	private void initialize() {

		refreshPositionMapping();

		addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentResized(ComponentEvent e) {
				refreshPositionMapping();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});

	}

	public boolean inRange(double worldValue) {
		if (Double.isNaN(worldValue))
			return false;
		if (worldValue >= getValue() && worldValue <= (getExtent() + getValue()))
			return true;
		return false;
	}

	private void refreshPositionMapping() {
		Rectangle bounds = getBounds();

		if (bounds == null || bounds.getMaxX() == 0) {
			this.positionEncodingFunction.setMinPixel(0.0);
			this.positionEncodingFunction.setMaxPixel(0.0);
		} else {
			this.positionEncodingFunction.setMinPixel(bounds.getMinX() + (int) (SLIDER_POINTER_WIDTH * 0.5));
			this.positionEncodingFunction.setMaxPixel(bounds.getMaxX() - (int) (SLIDER_POINTER_WIDTH * 0.5));
		}
	}

	@Override
	public IPositionEncodingFunction getXPositionEncodingFunction() {
		return positionEncodingFunction;
	}

	public boolean isFlipThumb() {
		return getRangeSliderUI().isFlipThumb();
	}

	public void setFlipThumb(boolean invertThumb) {
		this.getRangeSliderUI().setFlipThumb(invertThumb);
	}
}
