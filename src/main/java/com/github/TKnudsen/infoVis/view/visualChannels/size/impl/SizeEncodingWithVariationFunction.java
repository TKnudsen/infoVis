package com.github.TKnudsen.infoVis.view.visualChannels.size.impl;

import java.awt.Component;
import java.util.Map;

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
public class SizeEncodingWithVariationFunction<T> extends SizeEncodingFunction<T> {

	private final Map<T, Double> scales;

	public SizeEncodingWithVariationFunction(Component component, Map<T, Double> scales) {
		this(component, 1.0, scales);
	}

	public SizeEncodingWithVariationFunction(Component component, double scale, Map<T, Double> scales) {
		super(component, scale);

		this.scales = scales;
	}

	@Override
	public Double apply(T t) {
		double size = super.apply(t);

		if (scales.containsKey(t))
			return size * scales.get(t);
		return size;
	}

}
