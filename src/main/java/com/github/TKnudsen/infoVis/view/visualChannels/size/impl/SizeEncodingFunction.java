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

	public SizeEncodingFunction(Component component) {
		this(component, 1.0);
	}

	public SizeEncodingFunction(Component component, double scale) {
		this.component = component;
		this.scale = scale;
	}

	@Override
	public Double apply(T t) {
		double size = 3;

		if (component != null)
			size = Math.max(3, Math.min(component.getWidth(), component.getHeight()) * 0.005) * scale;

		return size;
	}

}
