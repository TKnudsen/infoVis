package com.github.TKnudsen.infoVis.view.painters.distribution1D;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ConstantColorEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Creates vertical distribution 1D painters.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class Distribution1DVerticalPainters {

	public static Distribution1DVerticalPainter<Double> createVerticalForDoubles(List<Double> data) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DVerticalPainter<>(data, worldToDoubleMapping, null);
	}

	public static Distribution1DVerticalPainter<Double> createVerticalForDoubles(List<Double> data, Color color) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DVerticalPainter<>(data, worldToDoubleMapping,
				new ConstantColorEncodingFunction<>(color));
	}

	public static Distribution1DVerticalPainter<Double> createVerticalForDoubles(List<Double> data,
			List<? extends Paint> colors) {
		Function<Double, Double> worldToDoubleMapping = p -> p;
		ColorEncodingFunction<Double> colorMapping = new ColorEncodingFunction<Double>(data, colors);

		return new Distribution1DVerticalPainter<>(data, worldToDoubleMapping, colorMapping);
	}

	public static Distribution1DVerticalHighlightPainter<Double> createVerticalHighlightForDoubles(List<Double> data) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DVerticalHighlightPainter<>(data, worldToDoubleMapping, null);
	}

	public static Distribution1DVerticalHighlightPainter<Double> createVerticalHighlightForDoubles(List<Double> data,
			Color color) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DVerticalHighlightPainter<>(data, worldToDoubleMapping,
				new ConstantColorEncodingFunction<>(color));
	}

	public static Distribution1DVerticalHighlightPainter<Double> createVerticalHighlightForDoubles(List<Double> data,
			List<? extends Paint> colors) {
		Function<Double, Double> worldToDoubleMapping = p -> p;
		ColorEncodingFunction<Double> colorMapping = new ColorEncodingFunction<Double>(data, colors);

		return new Distribution1DVerticalHighlightPainter<>(data, worldToDoubleMapping, colorMapping);
	}

}
