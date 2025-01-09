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
 * Creates horizontal distribution 1D painters
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2022 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class Distribution1DHorizontalPainters {

	public static Distribution1DHorizontalPainter<Double> createHorizontalForDoubles(List<Double> data) {
		Function<Number, Double> worldToDoubleMapping = p -> p.doubleValue();

		return new Distribution1DHorizontalPainter<>(data, worldToDoubleMapping, null);
	}

	public static Distribution1DHorizontalPainter<Double> createHorizontalForDoubles(List<Double> data, Color color) {
		Function<Number, Double> worldToDoubleMapping = p -> p.doubleValue();

		return new Distribution1DHorizontalPainter<>(data, worldToDoubleMapping,
				new ConstantColorEncodingFunction<>(color));
	}

	public static Distribution1DHorizontalPainter<Double> createHorizontalForDoubles(List<Double> data,
			List<? extends Paint> colors) {
		Function<Number, Double> worldToDoubleMapping = p -> p.doubleValue();
		ColorEncodingFunction<Double> colorMapping = new ColorEncodingFunction<Double>(data, colors);

		return new Distribution1DHorizontalPainter<>(data, worldToDoubleMapping, colorMapping);
	}

	public static Distribution1DHorizontalHighlightPainter<Double> createHorizontalHighlightForDoubles(
			List<Double> data) {
		Function<Number, Double> worldToDoubleMapping = p -> p.doubleValue();

		return new Distribution1DHorizontalHighlightPainter<>(data, worldToDoubleMapping, null);
	}

	public static Distribution1DHorizontalHighlightPainter<Double> createHorizontalHighlightForDoubles(
			List<Double> data, Color color) {
		Function<Number, Double> worldToDoubleMapping = p -> p.doubleValue();

		return new Distribution1DHorizontalHighlightPainter<>(data, worldToDoubleMapping,
				new ConstantColorEncodingFunction<>(color));
	}

	public static Distribution1DHorizontalHighlightPainter<Double> createHorizontalHighlightForDoubles(
			List<Double> data, List<? extends Paint> colors) {
		Function<Number, Double> worldToDoubleMapping = p -> p.doubleValue();
		ColorEncodingFunction<Double> colorMapping = new ColorEncodingFunction<Double>(data, colors);

		return new Distribution1DHorizontalHighlightPainter<>(data, worldToDoubleMapping, colorMapping);
	}

}
