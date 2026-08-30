package com.github.TKnudsen.infoVis.view.panels.axis;

import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;

/**
 * <p>
 * Static helper methods for {@link XYNumericalChartPanel}, giving access to
 * its X/Y axis painters.
 * </p>
 *
 * @version 1.0
 */
public class XYNumericalChartPanels {

	public static <X extends Number, Y extends Number> XAxisNumericalPainter<X> getXAxisNumericalPainter(
			XYNumericalChartPanel<Y, Y> chartPanel) {
		if (chartPanel == null)
			return null;

		return (XAxisNumericalPainter<X>) chartPanel.xAxisPainter;
	}

	public static <X extends Number, Y extends Number> YAxisNumericalPainter<Y> getYAxisNumericalPainter(
			XYNumericalChartPanel<Y, Y> chartPanel) {
		if (chartPanel == null)
			return null;

		return (YAxisNumericalPainter<Y>) chartPanel.yAxisPainter;
	}
}
