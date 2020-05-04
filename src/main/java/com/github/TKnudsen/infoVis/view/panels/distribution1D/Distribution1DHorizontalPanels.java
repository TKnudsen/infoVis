package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
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

	/**
	 * applies a filter operation using a list of data. Returns a new list, only
	 * containing those elements which can be applied by the position mapping
	 * function.
	 * 
	 * @param data
	 * @param worldPositionMapping
	 * @param warnForQualityLeaks
	 * @return
	 */
	public static <T> List<T> sanityCheckFilter(List<T> data, Function<? super T, Double> worldPositionMapping,
			boolean warnForQualityLeaks) {
		List<T> returnList = new ArrayList<T>();

		try {
			for (T t : data) {
				Double apply = worldPositionMapping.apply(t);
				if (apply != null && !Double.isNaN(apply))
					returnList.add(t);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnList;
	}

}
