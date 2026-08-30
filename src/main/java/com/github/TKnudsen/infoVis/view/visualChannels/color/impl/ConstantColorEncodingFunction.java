package com.github.TKnudsen.infoVis.view.visualChannels.color.impl;

import java.awt.Color;
import java.awt.Paint;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncodingFunction;

/**
 * <p>
 * Provides a constant color for all entities.
 * </p>
 *
 * @version 1.03
 * @since 2017
 */
public class ConstantColorEncodingFunction<T> implements IColorEncodingFunction<T>, ISelfDescription {

	private Paint defaultColor;

	public ConstantColorEncodingFunction(Paint defaultColor) {
		this.defaultColor = defaultColor;
	}

	@Override
	public String getName() {
		return "ConstantColorMappingFunction";
	}

	@Override
	public String getDescription() {
		return "provides a constant color for all entities";
	}

	@Override
	public Paint apply(T t) {
		return defaultColor;
	}

	public Paint getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(Color defaultColor) {
		this.defaultColor = defaultColor;
	}
}
