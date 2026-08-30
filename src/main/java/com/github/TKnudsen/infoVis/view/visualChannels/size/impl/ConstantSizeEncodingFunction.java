package com.github.TKnudsen.infoVis.view.visualChannels.size.impl;

import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncodingFunction;

/**
 * @version 1.03
 * @since 2016
 */
public class ConstantSizeEncodingFunction<T> implements ISizeEncodingFunction<T> {

	private final double size;

	public ConstantSizeEncodingFunction(double size) {
		this.size = size;
	}

	@Override
	public Double apply(T t) {
		return size;
	}

}
