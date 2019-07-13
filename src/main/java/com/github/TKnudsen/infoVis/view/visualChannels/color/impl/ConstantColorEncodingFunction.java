package com.github.TKnudsen.infoVis.view.visualChannels.color.impl;

import java.awt.Color;
import java.awt.Paint;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Provides a constant color for all entities.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2017-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
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
