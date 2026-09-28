package com.github.TKnudsen.infoVis.view.painters.radial;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.radial.IntervalArcGaugePainter.ElementVisibility;

/**
 * <p>
 * Nests several {@link IntervalArcGaugePainter} rings at decreasing radius,
 * each showing only its percentile ring (no shaded bands, no median
 * pointer), so multiple angle distributions can be compared as one set of
 * concentric bands. The outermost ring additionally carries the shared zero
 * reference line and degree legend.
 * </p>
 *
 * <p>
 * Re-implemented from the multi-series variant of the glyph design described
 * in:
 * </p>
 * <p>
 * Using Dashboard Networks to Visualize Multiple Patient Histories: A Design
 * Study on Post-Operative Prostate Cancer. IEEE Transactions on Visualization
 * and Computer Graphics (TVCG), pp. 1615-1628 (2018).
 * doi: 10.1109/TVCG.2018.2803829
 * </p>
 *
 * @version 1.0
 * @since 2018
 */
public class IntervalArcGaugeComparisonPainter extends ChartPainter {

	private final List<StatisticsSupport> angleDistributions;
	private Map<StatisticsSupport, Paint> seriesPaints = new HashMap<>();
	private final LinkedHashMap<StatisticsSupport, IntervalArcGaugePainter> gaugesBySeries = new LinkedHashMap<>();

	public IntervalArcGaugeComparisonPainter(Collection<StatisticsSupport> angleDistributions) {
		this(angleDistributions, null);
	}

	public IntervalArcGaugeComparisonPainter(Collection<StatisticsSupport> angleDistributions,
			Map<StatisticsSupport, Paint> seriesPaints) {
		this.angleDistributions = new ArrayList<>(angleDistributions);

		if (seriesPaints != null)
			this.seriesPaints = seriesPaints;

		setBackgroundPaint(null);

		createGauges();
	}

	private void createGauges() {
		for (int i = 0; i < angleDistributions.size(); i++) {
			StatisticsSupport series = angleDistributions.get(i);

			IntervalArcGaugePainter gauge = new IntervalArcGaugePainter(series);
			gauge.setBandVisibility(ElementVisibility.NEVER);
			gauge.setRingOutlineVisibility(ElementVisibility.NEVER);
			gauge.setShowMedianPointer(false);
			gauge.setShowSupportRing(true);

			boolean isOutermost = i + 1 == angleDistributions.size();
			if (isOutermost) {
				gauge.setShowZeroReferenceLine(true);
				gauge.setShowScaleLegend(true);
			}

			gaugesBySeries.put(series, gauge);
		}
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		// the innermost series gets the smallest ring, the outermost the full
		// rectangle -- so overlapping rings never fully hide a smaller one
		double smallestScale = Math.max(0.2, -0.08 * gaugesBySeries.size() + 0.75);

		int index = 0;
		for (StatisticsSupport series : gaugesBySeries.keySet()) {
			double scale = smallestScale + (1 - smallestScale) * ((index + 1) / (double) gaugesBySeries.size());

			double ringWidth = rectangle.getWidth() * scale;
			double ringHeight = rectangle.getHeight() * scale;
			Rectangle2D.Double ringRectangle = new Rectangle2D.Double(rectangle.getMinX(),
					rectangle.getMinY() + (rectangle.getHeight() - ringHeight) * 0.5, ringWidth, ringHeight);

			IntervalArcGaugePainter gauge = gaugesBySeries.get(series);
			gauge.setRectangle(ringRectangle);
			gauge.setPercentileRingThicknessRatio(
					defaultPercentileRingThicknessRatio(gaugesBySeries.size()) / Math.pow(scale, 1.35));

			index++;
		}
	}

	public static double defaultPercentileRingThicknessRatio(int seriesCount) {
		return Math.max(0.05, -0.0125 * seriesCount + 0.1);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		for (IntervalArcGaugePainter gauge : gaugesBySeries.values())
			gauge.draw(g2);
	}

	public void setSeriesPaint(StatisticsSupport series, Paint paint) {
		seriesPaints.put(series, paint);

		if (gaugesBySeries.containsKey(series))
			gaugesBySeries.get(series).setPaint(paint);
	}

	public void setMedianPointerPaint(StatisticsSupport series, Paint paint) {
		if (gaugesBySeries.containsKey(series))
			gaugesBySeries.get(series).setMedianPointerPaint(paint);
	}

	public void setShowMedianPointer(StatisticsSupport series, boolean show) {
		if (gaugesBySeries.containsKey(series))
			gaugesBySeries.get(series).setShowMedianPointer(show);
	}

	public void setBandVisibility(StatisticsSupport series, ElementVisibility visibility) {
		if (gaugesBySeries.containsKey(series))
			gaugesBySeries.get(series).setBandVisibility(visibility);
	}

	public void setRingOutlineVisibility(StatisticsSupport series, ElementVisibility visibility) {
		if (gaugesBySeries.containsKey(series))
			gaugesBySeries.get(series).setRingOutlineVisibility(visibility);
	}

	public void setShowSupportRing(StatisticsSupport series, boolean show) {
		if (gaugesBySeries.containsKey(series))
			gaugesBySeries.get(series).setShowSupportRing(show);
	}

	@Override
	public void setBorderPaint(Paint borderPaint) {
		for (IntervalArcGaugePainter gauge : gaugesBySeries.values())
			gauge.setBorderPaint(borderPaint);
	}

	public void setBorderPaint(StatisticsSupport series, Paint paint) {
		if (gaugesBySeries.containsKey(series))
			gaugesBySeries.get(series).setBorderPaint(paint);
	}

	public void setAngleScaleDegrees(double angleScaleDegrees) {
		for (IntervalArcGaugePainter gauge : gaugesBySeries.values())
			gauge.setAngleScaleDegrees(angleScaleDegrees);
	}

	@Override
	public void setFont(Font font) {
		for (IntervalArcGaugePainter gauge : gaugesBySeries.values())
			gauge.setFont(font);
	}

	public void setShowScaleLegend(boolean showScaleLegend) {
		int index = 0;
		for (StatisticsSupport series : gaugesBySeries.keySet()) {
			if (index + 1 == angleDistributions.size())
				gaugesBySeries.get(series).setShowScaleLegend(showScaleLegend);
			index++;
		}
	}

	public Collection<IntervalArcGaugePainter> getGauges() {
		return gaugesBySeries.values();
	}

	@Override
	public String toString() {
		return "IntervalArcGaugeComparisonPainter";
	}
}
