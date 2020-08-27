package com.github.TKnudsen.infoVis.data.barChart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class BarChartDataForTesting {

	public static List<Double> values() {
		List<Double> values = new ArrayList<>();

		values.add(22.0);
		values.add(21.0);
		values.add(3.0);
		values.add(17.0);
		values.add(6.0);
		values.add(4.0);

		return values;
	}

	public static List<Color> colors() {
		List<Color> colors = new ArrayList<>();

		colors.add(new Color(106, 161, 215));
		colors.add(new Color(236, 137, 69));
		colors.add(new Color(172, 172, 172));
		colors.add(new Color(254, 196, 27));
		colors.add(new Color(82, 120, 193));
		colors.add(new Color(81, 193, 94));

		return colors;
	}

	public static List<String> labels() {
		List<String> labels = new ArrayList<>();

		labels.add("A");
		labels.add("B");
		labels.add("C");
		labels.add("D");
		labels.add("E");
		labels.add("F");

		return labels;
	}
}
