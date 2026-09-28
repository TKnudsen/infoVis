package com.github.TKnudsen.infoVis.view.painters.radial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * Shared implementation for drawing a bundle of related series -- one
 * per-dimension min/max envelope plus the individual member series -- behind
 * a radar chart's own primary series. Composed into (not extended by) {@link
 * RadarChartBundlePainter} and {@link RadarChartButterflyBundlePainter},
 * since Java's single inheritance leaves no shared base for two classes that
 * each already extend a different {@link RadarChartPainter} variant.
 * </p>
 *
 * @version 1.0
 * @since 2018
 */
class RadarChartBundleSupport {

	/**
	 * Callback through which this class draws one series without depending on
	 * either {@link RadarChartPainter} subclass directly -- implemented by the
	 * owning painter as a one-line delegation to its own (inherited) {@code
	 * drawSeries} method, so the bundle automatically renders with whichever
	 * {@code buildChartPath} variant (straight-edged or butterfly) that painter
	 * uses for its own primary series.
	 */
	@FunctionalInterface
	interface SeriesRenderer {
		void drawSeries(Graphics2D g2, double[] seriesData, Color seriesColor, Stroke seriesStroke,
				boolean fillSeries);
	}

	private final List<double[]> bundle;
	private final double[] minima;
	private final double[] maxima;

	private BasicStroke bundleStroke = DisplayTools.standardStroke;
	private Color bundleColor = Color.BLUE;
	private boolean fillBundleMembers = false;
	private boolean drawExtremeValues = true;

	/**
	 * @param dimensions the owning chart's dimensionality; every bundle member
	 *                   must have exactly this many values
	 * @param bundle     the member series, or null/empty for an empty bundle
	 */
	RadarChartBundleSupport(int dimensions, List<double[]> bundle) {
		this.bundle = bundle == null ? new ArrayList<>() : new ArrayList<>(bundle);

		for (double[] member : this.bundle)
			if (member.length != dimensions)
				throw new IllegalArgumentException(
						"RadarChartBundleSupport: every bundle member must have " + dimensions + " values");

		if (this.bundle.isEmpty()) {
			minima = null;
			maxima = null;
		} else {
			minima = new double[dimensions];
			maxima = new double[dimensions];
			Arrays.fill(minima, Double.POSITIVE_INFINITY);
			Arrays.fill(maxima, Double.NEGATIVE_INFINITY);

			for (double[] member : this.bundle)
				for (int i = 0; i < dimensions; i++) {
					minima[i] = Math.min(minima[i], member[i]);
					maxima[i] = Math.max(maxima[i], member[i]);
				}
		}
	}

	/** The lowest value across the whole bundle, or NaN if it is empty. */
	double getBundleMinValue() {
		if (minima == null)
			return Double.NaN;

		double min = Double.POSITIVE_INFINITY;
		for (double value : minima)
			min = Math.min(min, value);
		return min;
	}

	/** The highest value across the whole bundle, or NaN if it is empty. */
	double getBundleMaxValue() {
		if (maxima == null)
			return Double.NaN;

		double max = Double.NEGATIVE_INFINITY;
		for (double value : maxima)
			max = Math.max(max, value);
		return max;
	}

	void draw(Graphics2D g2, SeriesRenderer renderer) {
		if (bundle.isEmpty())
			return;

		float alpha = (float) Math.sqrt(1.0 / bundle.size());
		Color translucentBundleColor = ColorTools.setAlpha(bundleColor, alpha);

		if (drawExtremeValues) {
			renderer.drawSeries(g2, minima, translucentBundleColor, DisplayTools.standardDashedStroke, false);
			renderer.drawSeries(g2, maxima, translucentBundleColor, DisplayTools.standardDashedStroke, false);
		}

		for (double[] member : bundle)
			renderer.drawSeries(g2, member, translucentBundleColor, bundleStroke, fillBundleMembers);
	}

	List<double[]> getBundle() {
		return Collections.unmodifiableList(bundle);
	}

	int getBundleSize() {
		return bundle.size();
	}

	double[] getMinima() {
		return minima == null ? null : minima.clone();
	}

	double[] getMaxima() {
		return maxima == null ? null : maxima.clone();
	}

	BasicStroke getBundleStroke() {
		return bundleStroke;
	}

	void setBundleStroke(BasicStroke bundleStroke) {
		this.bundleStroke = bundleStroke;
	}

	Color getBundleColor() {
		return bundleColor;
	}

	void setBundleColor(Color bundleColor) {
		this.bundleColor = bundleColor;
	}

	boolean isFillBundleMembers() {
		return fillBundleMembers;
	}

	void setFillBundleMembers(boolean fillBundleMembers) {
		this.fillBundleMembers = fillBundleMembers;
	}

	boolean isDrawExtremeValues() {
		return drawExtremeValues;
	}

	void setDrawExtremeValues(boolean drawExtremeValues) {
		this.drawExtremeValues = drawExtremeValues;
	}
}
