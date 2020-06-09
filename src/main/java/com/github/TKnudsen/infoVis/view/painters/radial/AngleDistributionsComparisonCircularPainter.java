package com.github.TKnudsen.infoVis.view.painters.radial;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

public class AngleDistributionsComparisonCircularPainter extends ChartPainter {

	private final List<StatisticsSupport> distributions;
	private Map<StatisticsSupport, Paint> paints = new HashMap<>();
	private LinkedHashMap<StatisticsSupport, AngleDistributionCircularPainter> painters = new LinkedHashMap<>();

	public AngleDistributionsComparisonCircularPainter(Collection<StatisticsSupport> distributions) {
		this(distributions, null);
	}

	public AngleDistributionsComparisonCircularPainter(Collection<StatisticsSupport> distributions,
			Map<StatisticsSupport, Paint> paints) {
		this.distributions = new ArrayList<>(distributions);

		if (paints != null)
			this.paints = paints;

		this.setBackgroundPaint(null);

		initialize();
	}

	private void initialize() {
		for (int i = 0; i < distributions.size(); i++) {
			StatisticsSupport distribution = distributions.get(i);
			AngleDistributionCircularPainter painter = new AngleDistributionCircularPainter(distribution);
			painter.setDrawCircleArea(false);
			painter.setDrawCircleOutline(false);
			painter.setDrawMeanLine(false);
			painter.setDrawBoxPlotSupportingRadius(true);
			if (i + 1 == distributions.size()) {
				painter.setDrawZeroLine(true);
				painter.setDrawLegend(true);
			}
			painters.put(distribution, painter);
		}
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		// create rectangles for the individual painters. principle: the last painter
		// receives the largest rectangle
		double i = 0;
		double minScale = Math.max(0.2, -0.08 * painters.size() + 0.75);
		for (StatisticsSupport distribution : painters.keySet()) {
			double scale = minScale + (1 - minScale) * ((i++ + 1) / (double) painters.size());
			double w = rectangle.getWidth() * scale;
			double h = rectangle.getHeight() * scale;
			Double rect = new Rectangle2D.Double(rectangle.getMinX(),
					rectangle.getMinY() + (rectangle.getHeight() - h) * 0.5, w, h);
			painters.get(distribution).setRectangle(rect);
			painters.get(distribution).setBoxplotStokeWidthRelative(
					defaultBoxplotStokeWidthRelative(painters.size()) * (1 / (Math.pow(scale, 1.35))));
		}
	}

	public static double defaultBoxplotStokeWidthRelative(int distributionsCount) {
		return Math.max(0.05, -0.0125 * distributionsCount + 0.1);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		for (StatisticsSupport distribution : painters.keySet()) {
			painters.get(distribution).draw(g2);
		}
	}

	public void setPaint(StatisticsSupport distribution, Paint paint) {
		this.paints.put(distribution, paint);

		if (painters.containsKey(distribution))
			painters.get(distribution).setPaint(paint);
	}

	public void setMeanLinePaint(StatisticsSupport distribution, Paint paint) {
		if (painters.containsKey(distribution))
			painters.get(distribution).setMeanLinePaint(paint);
	}

	public void setDrawMeanLine(StatisticsSupport distribution, boolean draw) {
		if (painters.containsKey(distribution))
			painters.get(distribution).setDrawMeanLine(draw);
	}

	public void setDrawCircleAreastable(StatisticsSupport distribution, boolean draw) {
		if (painters.containsKey(distribution))
			painters.get(distribution).setDrawCircleArea(draw);
	}

	public void setDrawCircleOutlinetable(StatisticsSupport distribution, boolean draw) {
		if (painters.containsKey(distribution))
			painters.get(distribution).setDrawCircleOutline(draw);
	}

	public void setDrawBoxPlotSupportingRadius(StatisticsSupport distribution, boolean draw) {
		if (painters.containsKey(distribution))
			painters.get(distribution).setDrawBoxPlotSupportingRadius(draw);
	}

	@Override
	public void setBorderPaint(Paint borderPaint) {
		for (AngleDistributionCircularPainter painter : painters.values())
			painter.setBorderPaint(borderPaint);
	}

	public void setMaxAngle(double maxAngle) {
		for (AngleDistributionCircularPainter painter : painters.values())
			painter.setMaxAngle(maxAngle);
	}

	public void setBorderPaint(StatisticsSupport distribution, Paint paint) {
		if (painters.containsKey(distribution))
			painters.get(distribution).setBorderPaint(paint);
	}

	public void setFont(Font font) {
		for (AngleDistributionCircularPainter painter : painters.values())
			painter.setFont(font);
	}

	public void setDrawLegend(boolean drawLegend) {
		int i = 0;
		for (StatisticsSupport distribution : painters.keySet())
			if (i++ + 1 == distributions.size())
				painters.get(distribution).setDrawLegend(drawLegend);
	}

	public Collection<AngleDistributionCircularPainter> getAngleDistributionCircularPainters() {
		return painters.values();
	}
}
