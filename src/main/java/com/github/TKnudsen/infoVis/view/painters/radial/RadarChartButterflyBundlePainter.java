package com.github.TKnudsen.infoVis.view.painters.radial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * <p>
 * The {@link RadarChartButterflyPainter} counterpart of {@link
 * RadarChartBundlePainter}: same bundle overlay (translucent member series
 * plus a dashed min/max envelope), but with every series -- the bundle
 * members and this chart's own primary series alike -- rendered as the
 * bowed-in butterfly silhouette rather than a straight-edged polygon.
 * </p>
 *
 * @version 1.0
 * @since 2018
 */
public class RadarChartButterflyBundlePainter extends RadarChartButterflyPainter {

	private final RadarChartBundleSupport bundleSupport;

	public RadarChartButterflyBundlePainter(double[] data, List<double[]> bundle) {
		super(data);

		bundleSupport = new RadarChartBundleSupport(data.length, bundle);
		expandAxisRangeToBundle();
	}

	public RadarChartButterflyBundlePainter(List<Double> data, List<double[]> bundle) {
		super(data);

		bundleSupport = new RadarChartBundleSupport(this.data.length, bundle);
		expandAxisRangeToBundle();
	}

	private void expandAxisRangeToBundle() {
		if (bundleSupport.getBundleSize() == 0)
			return;

		yAxisMinValue = Math.min(yAxisMinValue, bundleSupport.getBundleMinValue());
		yAxisMaxValue = Math.max(yAxisMaxValue, bundleSupport.getBundleMaxValue());
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (data == null || data.length == 0 || chartRectangle == null)
			return;

		bundleSupport.draw(g2, this::drawSeries);

		// re-emphasize the primary series' outline on top of the bundle drawn
		// over it above -- it must stay clearly visible regardless of how
		// many members the bundle has
		if (bundleSupport.getBundleSize() > 0)
			drawSeries(g2, data, color, stroke, false);
	}

	@Override
	public String toString() {
		return "RadarChartButterflyBundlePainter";
	}

	public List<double[]> getBundle() {
		return bundleSupport.getBundle();
	}

	public int getBundleSize() {
		return bundleSupport.getBundleSize();
	}

	/** The bundle's per-dimension minimum, or null if the bundle is empty. */
	public double[] getMinima() {
		return bundleSupport.getMinima();
	}

	/** The bundle's per-dimension maximum, or null if the bundle is empty. */
	public double[] getMaxima() {
		return bundleSupport.getMaxima();
	}

	public BasicStroke getBundleStroke() {
		return bundleSupport.getBundleStroke();
	}

	public void setBundleStroke(BasicStroke bundleStroke) {
		bundleSupport.setBundleStroke(bundleStroke);
	}

	public Color getBundleColor() {
		return bundleSupport.getBundleColor();
	}

	public void setBundleColor(Color bundleColor) {
		bundleSupport.setBundleColor(bundleColor);
	}

	public boolean isFillBundleMembers() {
		return bundleSupport.isFillBundleMembers();
	}

	public void setFillBundleMembers(boolean fillBundleMembers) {
		bundleSupport.setFillBundleMembers(fillBundleMembers);
	}

	public boolean isDrawExtremeValues() {
		return bundleSupport.isDrawExtremeValues();
	}

	public void setDrawExtremeValues(boolean drawExtremeValues) {
		bundleSupport.setDrawExtremeValues(drawExtremeValues);
	}
}
