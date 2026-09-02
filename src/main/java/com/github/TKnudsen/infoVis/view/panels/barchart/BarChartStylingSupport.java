package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Paint;

import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * <p>
 * Shared tooltip/border-paint/grid-spacing plumbing for {@link BarCharts} and
 * {@link BarChartsValueBased} (code review finding #32). The two classes
 * operate on unrelated type hierarchies -- {@code BarChart<T>} (item-based,
 * three painter layers via {@code AbstractBinnedDistributionPanel}) and
 * {@code IBarChartValueBased} (value-based, a single painter) -- with no
 * shared supertype beyond both declaring {@code getBarChartPainter()}, so
 * this holds only the operations that were genuinely byte-identical in
 * spirit between the two: the "prefer the panel's own tooltip flag, fall
 * back to the painter" pattern, and simple null-safe painter getters.
 * </p>
 *
 * <p>
 * Deliberately NOT unified here: {@code setGridSpacing}/{@code setBorderPaint}
 * /{@code setSelectionPaint} and {@code addInteraction} -- {@link BarCharts}'
 * versions apply to all three painter layers (global/filter/selection) via
 * {@code AbstractBinnedDistributionPanel}, a concept {@code
 * IBarChartValueBased} (a single painter) has no equivalent for, and {@code
 * addInteraction}'s wiring has genuinely drifted between the two (different
 * defensiveness around {@code instanceof Component}/{@code InfoVisChartPanel}
 * and when the selected-function/overlay-painter get installed) -- forcing
 * those into one shared implementation would either silently change behavior
 * or require a larger interface unification beyond this fix's scope.
 * </p>
 */
final class BarChartStylingSupport {

	/**
	 * @param barChart the bar chart panel or component (checked for
	 *                 {@link InfoVisChartPanel})
	 * @param painter  its bar chart painter, used as a fallback
	 * @return the panel's own tooltip flag if it is an {@link InfoVisChartPanel},
	 *         otherwise the painter's
	 */
	static boolean isToolTipping(Object barChart, BarChartPainter painter) {
		if (barChart instanceof InfoVisChartPanel)
			return ((InfoVisChartPanel) barChart).isShowingTooltips();

		return painter != null && painter.isToolTipping();
	}

	/**
	 * @param barChart    the bar chart panel or component (checked for
	 *                    {@link InfoVisChartPanel})
	 * @param toolTipping the new tooltip flag
	 * @param painter     its bar chart painter, used as a fallback
	 */
	static void setToolTipping(Object barChart, boolean toolTipping, BarChartPainter painter) {
		if (barChart instanceof InfoVisChartPanel) {
			((InfoVisChartPanel) barChart).setShowingTooltips(toolTipping);
			return;
		}

		if (painter != null)
			painter.setToolTipping(toolTipping);
	}

	/**
	 * @param painter a bar chart painter, possibly null
	 * @return its border paint, or null if the painter itself is null
	 */
	static Paint getBorderPaint(BarChartPainter painter) {
		return painter != null ? painter.getBorderPaint() : null;
	}

	/**
	 * @param painter a bar chart painter, possibly null
	 * @return its grid spacing, or 0.0 if the painter itself is null
	 */
	static double getGridSpacing(BarChartPainter painter) {
		return painter != null ? painter.getGridSpacing() : 0.0;
	}

	/**
	 * Avoid instantiation.
	 */
	private BarChartStylingSupport() {
	}
}
