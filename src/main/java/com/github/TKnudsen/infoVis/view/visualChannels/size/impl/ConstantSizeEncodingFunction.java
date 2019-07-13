package com.github.TKnudsen.infoVis.view.visualChannels.size.impl;

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
