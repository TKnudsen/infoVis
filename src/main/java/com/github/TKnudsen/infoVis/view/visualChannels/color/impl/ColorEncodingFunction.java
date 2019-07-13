package com.github.TKnudsen.infoVis.view.visualChannels.color.impl;

import java.awt.Color;
import java.awt.Paint;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Stores the color mapping for a given set of data. other data is mapped to a
 * default color.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2017-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class ColorEncodingFunction<T> extends ConstantColorEncodingFunction<T> implements ISelfDescription {

	private Map<T, Paint> colors;

	public ColorEncodingFunction(List<T> data, List<? extends Paint> colors) {
		this(data, colors, Color.BLACK);
	}

	public ColorEncodingFunction(List<T> data, List<? extends Paint> colors, Paint defaultColor) {
		super(defaultColor);

		if (data == null)
			return;

		if (colors != null && data.size() != colors.size())
			throw new IllegalArgumentException(getName() + ": data and color of different size");

		this.colors = new LinkedHashMap<>();

		for (int i = 0; i < data.size(); i++)
			if (colors == null)
				this.colors.put(data.get(i), defaultColor);
			else
				this.colors.put(data.get(i), colors.get(i));
	}

	public ColorEncodingFunction(Map<T, Color> data, Color defaultColor) {
		super(defaultColor);

		this.colors = new LinkedHashMap<>(data);
	}

	public Set<T> getData() {
		return colors.keySet();
	}

	@Override
	public String getName() {
		return "ColorEncodingFunction";
	}

	@Override
	public String getDescription() {
		return "provides colors for given data, other data is mapped to a default color";
	}

	@Override
	public Paint apply(T t) {
		if (colors.containsKey(t))
			return colors.get(t);

		return getDefaultColor();
	}
}
