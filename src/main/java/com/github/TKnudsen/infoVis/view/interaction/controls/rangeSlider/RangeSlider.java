/*
 * Adapted from Swing-Range-Slider (https://github.com/ernieyu/Swing-Range-Slider).
 *
 * The MIT License
 *
 * Copyright (c) 2010 Ernest Yu. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.TKnudsen.infoVis.view.interaction.controls.rangeSlider;

import javax.swing.JSlider;

/**
 * <p>
 * Note that RangeSlider makes use of the default BoundedRangeModel, which
 * supports an inner range defined by a value and an extent. The upper value
 * returned by RangeSlider is simply the lower value plus the extent.
 * </p>
 *
 */
public class RangeSlider extends JSlider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Lock state for lower thumb */
	private boolean lowerThumbLocked = false;

	/** Lock state for upper thumb */
	private boolean upperThumbLocked = false;

	/**
	 * Creates a horizontal range slider with the specified minimum and maximum
	 * values. Both thumbs start at their extreme positions.
	 * 
	 * @param min minimum value
	 * @param max maximum value
	 */
	public RangeSlider(int min, int max) {
		this(JSlider.HORIZONTAL, min, max, min, max);
	}

	/**
	 * Creates a horizontal range slider with the specified range and initial thumb
	 * positions.
	 * 
	 * @param min        minimum value
	 * @param max        maximum value
	 * @param lowerValue initial lower thumb value
	 * @param upperValue initial upper thumb value
	 */
	public RangeSlider(int min, int max, int lowerValue, int upperValue) {
		this(JSlider.HORIZONTAL, min, max, lowerValue, upperValue);
	}

	/**
	 * Creates a range slider with the specified orientation, range, and initial
	 * thumb positions.
	 * 
	 * @param orientation JSlider.HORIZONTAL or JSlider.VERTICAL
	 * @param min         minimum value
	 * @param max         maximum value
	 * @param lowerValue  initial lower thumb value
	 * @param upperValue  initial upper thumb value
	 * @throws IllegalArgumentException if upperValue < lowerValue
	 */
	public RangeSlider(int orientation, int min, int max, int lowerValue, int upperValue) {
		super(orientation, min, max, lowerValue);

		if (upperValue < lowerValue) {
			throw new IllegalArgumentException(
					"upperValue (" + upperValue + ") must be >= lowerValue (" + lowerValue + ")");
		}

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
	 * Sets the lower value in the range. If the lower thumb is locked, this method
	 * returns without making changes.
	 * 
	 * @param value the new lower value
	 */
	@Override
	public void setValue(int value) {
		if (lowerThumbLocked)
			return; // Ignore if locked

		int oldValue = getValue();
		if (oldValue == value) {
			return;
		}

		int oldExtent = getExtent();
		int newValue = Math.min(Math.max(getMinimum(), value), oldValue + oldExtent);
		int newExtent = oldExtent + oldValue - newValue;

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

	public void setHighValue(int value) {
		if (upperThumbLocked)
			return; // Ignore if locked

		int lowerValue = getValue();
		int newExtent = Math.min(Math.max(0, value - lowerValue), getMaximum() - lowerValue);

		setExtent(newExtent);
	}

	/**
	 * Checks if the slider is in its starting position (unused), with the lower
	 * slider at the minimum and the upper slider at the maximum.
	 * 
	 * @return true if both thumbs are at their extreme positions, false otherwise
	 */
	public boolean isInNeutralState() {
		return getValue() == getMinimum() && (getValue() + getExtent() == getMaximum());
	}

	/**
	 * Returns whether the lower thumb is locked.
	 */
	public boolean isLowerThumbLocked() {
		return lowerThumbLocked;
	}

	/**
	 * Sets whether the lower thumb is locked. When locked, the lower thumb cannot
	 * be moved via user interaction or programmatic setValue() calls. Note: Both
	 * thumbs cannot be locked simultaneously.
	 * 
	 * @param locked true to lock the lower thumb, false to unlock it
	 * @throws IllegalStateException if attempting to lock while the upper thumb is
	 *                               already locked
	 */
	public void setLowerThumbLocked(boolean locked) {
//		if (locked && upperThumbLocked)
//			throw new IllegalStateException("Cannot lock both thumbs simultaneously");

		boolean oldValue = this.lowerThumbLocked;
		this.lowerThumbLocked = locked;
		firePropertyChange("lowerThumbLocked", oldValue, locked);
	}

	/**
	 * Returns whether the upper thumb is locked.
	 */
	public boolean isUpperThumbLocked() {
		return upperThumbLocked;
	}

	/**
	 * Sets whether the upper thumb is locked. Note: Both thumbs cannot be locked
	 * simultaneously.
	 */
	public void setUpperThumbLocked(boolean locked) {
		if (locked && lowerThumbLocked)
			throw new IllegalStateException("Cannot lock both thumbs simultaneously");

		boolean oldValue = this.upperThumbLocked;
		this.upperThumbLocked = locked;
		firePropertyChange("upperThumbLocked", oldValue, locked);
	}
}
