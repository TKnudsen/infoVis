package com.github.TKnudsen.infoVis.view.interaction.controls;

import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.plaf.SliderUI;

import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoder;
import com.jidesoft.swing.RangeSlider;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 *
 */
public class InfoVisRangeSlider extends RangeSlider implements IXPositionEncoder {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3952780438465441561L;

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
//				System.out.println("InfoVisRangeSlider: componentShown");
			}

			@Override
			public void componentResized(ComponentEvent e) {
//				System.out.println("InfoVisRangeSlider: componentResized");
				refreshPositionMapping();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
//				System.out.println("InfoVisRangeSlider: componentMoved");
			}

			@Override
			public void componentHidden(ComponentEvent e) {
//				System.out.println("InfoVisRangeSlider: componentHidden");
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
}
