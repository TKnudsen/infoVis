package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ConstantColorEncodingFunction;

public class Distribution1DHorizontalPanels {

	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DHorizontalPanel<>(data, worldToDoubleMapping);
	}

	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data, Color color) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		Distribution1DHorizontalPanel<Double> horizontalPanel = new Distribution1DHorizontalPanel<>(data,
				worldToDoubleMapping);
		horizontalPanel.setColorEncodingFunction(new ConstantColorEncodingFunction<>(color));

		return horizontalPanel;
	}

	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data,
			List<? extends Paint> colors) {
		return createForDoubles(data, colors, Double.NaN, Double.NaN);
	}

	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data, Double minGlobal,
			Double maxGlobal) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DHorizontalPanel<Double>(data, worldToDoubleMapping, null, minGlobal, maxGlobal);
	}

	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data, Color color,
			Double minGlobal, Double maxGlobal) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DHorizontalPanel<Double>(data, worldToDoubleMapping,
				new ConstantColorEncodingFunction<>(color), minGlobal, maxGlobal);
	}

	public static Distribution1DHorizontalPanel<Double> createForDoubles(List<Double> data,
			List<? extends Paint> colors, Double minGlobal, Double maxGlobal) {
		Function<Double, Double> worldToDoubleMapping = p -> p;
		ColorEncodingFunction<Double> colorMapping = new ColorEncodingFunction<Double>(data, colors);

		return new Distribution1DHorizontalPanel<Double>(data, worldToDoubleMapping, colorMapping, minGlobal,
				maxGlobal);
	}

}
