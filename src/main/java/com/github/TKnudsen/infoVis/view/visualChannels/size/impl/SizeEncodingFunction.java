package com.github.TKnudsen.infoVis.view.visualChannels.size.impl;

import java.awt.Component;

import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 *
 * @param <T> t
 */
public class SizeEncodingFunction<T> implements ISizeEncodingFunction<T> {

	private final Component component;

	/**
	 * can be used to adjust the relative point size. The default value is 1.0 which
	 * means that points will have 0.5% of the min(width,height) of a component.
	 */
	private final double scale;

	private final double minSize;

	public SizeEncodingFunction(Component component) {
		this(component, 1.0);
	}

	public SizeEncodingFunction(Component component, double scale) {
		this(component, scale, 3.0);
	}

	/**
	 * 
	 * @param component the component where the the size encoding shall be live.
	 *                  with and height of the component decides about the size of
	 *                  size encodings.
	 * @param scale     can be used to adjust the relative point size. The default
	 *                  value is 1.0 which means that points will have 0.5% of the
	 *                  min(width,height) of a component.
	 * @param minSize   minimum size of a point. default is 3.0
	 */
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
