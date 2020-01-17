package com.github.TKnudsen.infoVis.view.visualChannels.size.impl;

import java.awt.Component;

import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncodingFunction;

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
 * @version 1.03
 *
 * @param <T>
 */
public class SizeEncodingFunction<T> implements ISizeEncodingFunction<T> {

	private final Component component;
	private final double scale;
	private final double minSize;

	public SizeEncodingFunction(Component component) {
		this(component, 1.0);
	}

	public SizeEncodingFunction(Component component, double scale) {
		this(component, scale, 3.0);
	}

	public SizeEncodingFunction(Component component, double scale, double minSize) {
		this.component = component;
		this.scale = scale;
		this.minSize = minSize;
	}

	@Override
	public Double apply(T t) {
		if (component != null)
			return Math.max(minSize, Math.min(component.getWidth(), component.getHeight()) * 0.005) * scale;

		return minSize;
	}

}
