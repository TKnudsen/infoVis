package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ConstantColorEncodingFunction;

public class Distribution1DVerticalPanels {

	public static Distribution1DVerticalPanel<Double> createForDoubles(List<Double> data) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DVerticalPanel<>(data, worldToDoubleMapping);
	}

	public static Distribution1DVerticalPanel<Double> createForDoubles(List<Double> data, Color color) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		Distribution1DVerticalPanel<Double> verticalPanel = new Distribution1DVerticalPanel<>(data,
				worldToDoubleMapping);
		verticalPanel.setColorEncodingFunction(new ConstantColorEncodingFunction<>(color));

		return verticalPanel;
	}

	public static Distribution1DVerticalPanel<Double> createForDoubles(List<Double> data,
			List<? extends Paint> colors) {
		return createForDoubles(data, colors, Double.NaN, Double.NaN);
	}

	public static Distribution1DVerticalPanel<Double> createForDoubles(List<Double> data, Double minGlobal,
			Double maxGlobal) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DVerticalPanel<Double>(data, worldToDoubleMapping, minGlobal, maxGlobal);
	}

	public static Distribution1DVerticalPanel<Double> createForDoubles(List<Double> data, Color color, Double minGlobal,
			Double maxGlobal) {
		Function<Double, Double> worldToDoubleMapping = p -> p;

		return new Distribution1DVerticalPanel<Double>(data, worldToDoubleMapping,
				new ConstantColorEncodingFunction<>(color), minGlobal, maxGlobal);
	}

	public static Distribution1DVerticalPanel<Double> createForDoubles(List<Double> data, List<? extends Paint> colors,
			Double minGlobal, Double maxGlobal) {
		Function<Double, Double> worldToDoubleMapping = p -> p;
		ColorEncodingFunction<Double> colorMapping = new ColorEncodingFunction<Double>(data, colors);

		return new Distribution1DVerticalPanel<Double>(data, worldToDoubleMapping, colorMapping, minGlobal, maxGlobal);
	}

}
