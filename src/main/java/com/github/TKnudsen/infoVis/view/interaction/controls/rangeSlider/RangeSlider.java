package com.github.TKnudsen.infoVis.view.interaction.controls.rangeSlider;

import javax.swing.JSlider;

/**
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sub-license, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * https://github.com/ernieyu/Swing-range-slider
 * 
 * An extension of JSlider to select a range of values using two thumb controls.
 * The thumb controls are used to select the lower and upper value of a range
 * with predetermined minimum and maximum values.
 * 
 * <p>
 * Note that RangeSlider makes use of the default BoundedRangeModel, which
 * supports an inner range defined by a value and an extent. The upper value
 * returned by RangeSlider is simply the lower value plus the extent.
 * </p>
 */
public class RangeSlider extends JSlider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param min min
	 * @param max max
	 */
	public RangeSlider(int min, int max) {
		this(JSlider.HORIZONTAL, min, max, min, max);
	}

	/**
	 * 
	 * @param min        min
	 * @param max        max
	 * @param lowerValue lower
	 * @param upperValue upper
	 */
	public RangeSlider(int min, int max, int lowerValue, int upperValue) {
		this(JSlider.HORIZONTAL, min, max, lowerValue, upperValue);
	}

	/**
	 * 
	 * @param orientation JSlider.HORIZONTAL orientation
	 * @param min         min
	 * @param max         max
	 * @param lowerValue  lower
	 * @param upperValue  upper
	 */
	public RangeSlider(int orientation, int min, int max, int lowerValue, int upperValue) {
		super(orientation, min, max, lowerValue);

		setHighValue(upperValue);
	}

	/**
	 * Overrides the superclass method to install the UI delegate to draw two
	 * thumbs.
	 */
	@Override
	public void updateUI() {
		setUI(new RangeSliderUI(this));
		updateLabelUIs();
	}

	public RangeSliderUI getRangeSliderUI() {
		return (RangeSliderUI) ui;
	}

	/**
	 * Returns the lower value in the range.
	 */
	@Override
	public int getValue() {
		return super.getValue();
	}

	public void setLowValue(int value) {
		setValue(value);
	}

	/**
	 * Sets the lower value in the range.
	 */
	@Override
	public void setValue(int value) {
		int oldValue = getValue();
		if (oldValue == value) {
			return;
		}

		// Compute new value and extent to maintain upper value.
		int oldExtent = getExtent();
		int newValue = Math.min(Math.max(getMinimum(), value), oldValue + oldExtent);
		int newExtent = oldExtent + oldValue - newValue;

		// Set new value and extent, and fire a single change event.
		getModel().setRangeProperties(newValue, newExtent, getMinimum(), getMaximum(), getValueIsAdjusting());
	}

	/**
	 * Returns the upper value in the range.
	 * 
	 * @return int
	 */
	public int getHighValue() {
		return getValue() + getExtent();
	}

	/**
	 * Sets the upper value in the range.
	 * 
	 * @param value int
	 */
	public void setHighValue(int value) {
		// Compute new extent.
		int lowerValue = getValue();
		int newExtent = Math.min(Math.max(0, value - lowerValue), getMaximum() - lowerValue);

		// Set extent to set upper value.
		setExtent(newExtent);
	}
}
