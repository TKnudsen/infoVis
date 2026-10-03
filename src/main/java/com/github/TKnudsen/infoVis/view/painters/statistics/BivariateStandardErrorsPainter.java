package com.github.TKnudsen.infoVis.view.painters.statistics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.IXAxis;
import com.github.TKnudsen.infoVis.view.painters.axis.IYAxis;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.string.LegendPainter;

/**
 * <p>
 * Overlays one {@link BivariateStandardErrorPainter} cross per labeled group
 * on one shared X/Y axis, plus a color legend -- a compact way to compare
 * several groups' central tendency and spread on two variables at once (e.g.
 * cluster centroids with their within-cluster spread, on two chosen
 * dimensions).
 * </p>
 *
 * <p>
 * Each group's error-bar half-width is {@code spreadMultiplier} times that
 * group's own standard deviation on each axis (default {@code 2.0}); the
 * shared axis domain is the union, across every group, of
 * {@code mean +/- spreadMultiplier * standardDeviation} -- so by
 * construction every cross fits within the shared axis without clipping.
 * </p>
 *
 * @version 1.0
 * @since 2018
 */
public class BivariateStandardErrorsPainter extends ChartPainter
		implements IXAxis<Double>, IYAxis<Double> {

	private Collection<double[][]> data;
	private Collection<String> labels;
	private List<Color> colors;

	private double spreadMultiplier = 2.0;

	private double minX;
	private double maxX;
	private double minY;
	private double maxY;

	private Rectangle2D.Double xAxisRect;
	private Rectangle2D.Double yAxisRect;
	private Rectangle2D.Double chartRectangle;

	private Collection<BivariateStandardErrorPainter> painters;
	private LegendPainter legendPainter;
	private boolean legendAlignmentLeft = true;
	private XAxisNumericalPainter<Double> xAxisPainter;
	private YAxisNumericalPainter<Double> yAxisPainter;
	private boolean drawXAxis = true;
	private boolean drawYAxis = true;

	/**
	 * Derives the shared axis domain from the data itself: for each group,
	 * {@code mean +/- spreadMultiplier * standardDeviation} on each axis, then
	 * the union across all groups.
	 *
	 * @param data   one {@code double[2][n]} per group -- {@code data[i][0]} is
	 *               that group's X samples, {@code data[i][1]} its Y samples
	 * @param labels one legend label per group, same order as {@code data}
	 * @param colors one color per group, same order as {@code data}
	 */
	public BivariateStandardErrorsPainter(Collection<double[][]> data, Collection<String> labels,
			List<Color> colors) {
		this(data, labels, colors, 2.0, computeDomain(data, 2.0));
	}

	/** Same as the other constructor, with an explicit {@code spreadMultiplier} instead of the default {@code 2.0}. */
	public BivariateStandardErrorsPainter(Collection<double[][]> data, Collection<String> labels, List<Color> colors,
			double spreadMultiplier) {
		this(data, labels, colors, spreadMultiplier, computeDomain(data, spreadMultiplier));
	}

	/** Uses an explicitly supplied shared axis domain instead of deriving one from the data. */
	public BivariateStandardErrorsPainter(Collection<double[][]> data, Collection<String> labels, List<Color> colors,
			double minX, double maxX, double minY, double maxY) {
		this(data, labels, colors, 2.0, new double[] { minX, maxX, minY, maxY });
	}

	private BivariateStandardErrorsPainter(Collection<double[][]> data, Collection<String> labels,
			List<Color> colors, double spreadMultiplier, double[] domain) {
		this.labels = labels;
		this.colors = colors;
		this.data = data;
		this.spreadMultiplier = spreadMultiplier;
		this.minX = domain[0];
		this.maxX = domain[1];
		this.minY = domain[2];
		this.maxY = domain[3];

		initialize();
	}

	private static double[] computeDomain(Collection<double[][]> data, double spreadMultiplier) {
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		if (data != null)
			for (double[][] group : data) {
				StatisticsSupport x = new StatisticsSupport(group[0]);
				StatisticsSupport y = new StatisticsSupport(group[1]);

				minX = Math.min(minX, x.getMean() - spreadMultiplier * x.getStandardDeviation());
				maxX = Math.max(maxX, x.getMean() + spreadMultiplier * x.getStandardDeviation());
				minY = Math.min(minY, y.getMean() - spreadMultiplier * y.getStandardDeviation());
				maxY = Math.max(maxY, y.getMean() + spreadMultiplier * y.getStandardDeviation());
			}

		return new double[] { minX, maxX, minY, maxY };
	}

	private void initialize() {
		if (data == null)
			throw new IllegalArgumentException("data must not be null");

		painters = new ArrayList<>();

		Iterator<String> labelIterator = labels.iterator();
		int i = 0;
		for (double[][] group : data) {
			StatisticsSupport x = new StatisticsSupport(group[0]);
			StatisticsSupport y = new StatisticsSupport(group[1]);

			BivariateStandardErrorPainter painter = new BivariateStandardErrorPainter(group[0], group[1],
					spreadMultiplier * x.getStandardDeviation(), spreadMultiplier * y.getStandardDeviation(),
					labelIterator.next(), minX, maxX, minY, maxY);
			painter.setDrawLabel(false);
			painter.setMeanColor(colors != null && colors.get(i) != null ? colors.get(i) : getColor());
			painters.add(painter);
			i++;
		}

		List<Entry<String, Paint>> legendEntries = new ArrayList<>();
		Iterator<String> legendLabelIterator = labels.iterator();
		for (int j = 0; j < labels.size(); j++)
			legendEntries.add(new SimpleEntry<String, Paint>(legendLabelIterator.next(), colors.get(j)));

		legendPainter = new LegendPainter();
		legendPainter.setData(legendEntries);
		legendPainter.setAlignLeft(legendAlignmentLeft);

		initializeXAxisPainter();
		initializeYAxisPainter();
	}

	private void initializeXAxisPainter() {
		xAxisPainter = new XAxisNumericalPainter<>(minX, maxX);
		xAxisPainter.setFont(font);
	}

	private void initializeYAxisPainter() {
		yAxisPainter = new YAxisNumericalPainter<>(minY, maxY);
		yAxisPainter.setBorderPaint(null);
		yAxisPainter.setDrawOutline(false);
		yAxisPainter.setFont(font);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (drawXAxis)
			xAxisPainter.draw(g2);

		if (drawYAxis)
			yAxisPainter.draw(g2);

		for (BivariateStandardErrorPainter painter : painters)
			painter.draw(g2);

		legendPainter.draw(g2);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null) {
			chartRectangle = null;
			return;
		}

		double x = rectangle.getX();
		double y = rectangle.getY();
		double w = rectangle.getWidth();
		double h = rectangle.getHeight();

		double axisSpaceBottom = Math.min(0.15 * h, 50);
		double axisSpaceLeft = Math.min(0.15 * w, 50);

		chartRectangle = new Rectangle2D.Double(x + axisSpaceLeft, y, w - axisSpaceBottom, h - axisSpaceLeft);
		xAxisRect = new Rectangle2D.Double(x + axisSpaceLeft, y + h - axisSpaceBottom, w - axisSpaceLeft,
				axisSpaceLeft);
		yAxisRect = new Rectangle2D.Double(x, y, axisSpaceLeft, h - axisSpaceBottom);

		for (BivariateStandardErrorPainter painter : painters)
			painter.setRectangle(chartRectangle);

		legendPainter.setRectangle(chartRectangle);

		xAxisPainter.setRectangle(xAxisRect);
		yAxisPainter.setRectangle(yAxisRect);
	}

	public Collection<double[][]> getData() {
		return data;
	}

	public Collection<String> getLabels() {
		return labels;
	}

	public void setLabels(Collection<String> labels) {
		this.labels = labels;
	}

	public List<Color> getColors() {
		return colors;
	}

	public void setColors(List<Color> colors) {
		this.colors = colors;
	}

	public double getSpreadMultiplier() {
		return spreadMultiplier;
	}

	/** Changes how many standard deviations wide each group's error bars are, and re-derives the shared axis domain, then re-initializes. */
	public void setSpreadMultiplier(double spreadMultiplier) {
		this.spreadMultiplier = spreadMultiplier;
		double[] domain = computeDomain(data, spreadMultiplier);
		this.minX = domain[0];
		this.maxX = domain[1];
		this.minY = domain[2];
		this.maxY = domain[3];
		initialize();
		setRectangle(getRectangle());
	}

	@Override
	public void setXAxisMinValue(Double xAxisMinValue) {
		this.minX = xAxisMinValue;
		initialize();
	}

	@Override
	public void setXAxisMaxValue(Double xAxisMaxValue) {
		this.maxX = xAxisMaxValue;
		initialize();
	}

	@Override
	public void setDrawXAxis(boolean drawXAxis) {
		this.drawXAxis = drawXAxis;
	}

	@Override
	public void setXAxisLegendHeight(double xAxisLegendHeight) {
		// not exposed by XAxisNumericalPainter today
	}

	@Override
	public void setXAxisMarkerDistanceInPixels(int markerDistanceInPixels) {
		xAxisPainter.setMarkerDistanceInPixels(markerDistanceInPixels);
	}

	@Override
	public void setXAxisPhysicalUnit(String physicalUnit) {
		xAxisPainter.setPhysicalUnit(physicalUnit);
	}

	@Override
	public void setYAxisPhysicalUnit(String physicalUnit) {
		yAxisPainter.setPhysicalUnit(physicalUnit);
	}

	@Override
	public void setYAxisMinValue(Double yAxisMinValue) {
		this.minY = yAxisMinValue;
		initialize();
	}

	@Override
	public void setYAxisMaxValue(Double yAxisMaxValue) {
		this.maxY = yAxisMaxValue;
		initialize();
	}

	@Override
	public void setDrawYAxis(boolean drawYAxis) {
		this.drawYAxis = drawYAxis;
	}

	@Override
	public void setYAxisLegendWidth(double yAxisLegendWidth) {
		// not exposed by YAxisNumericalPainter today
	}

	@Override
	public void setYAxisMarkerDistanceInPixels(int markerDistanceInPixels) {
		yAxisPainter.setMarkerDistanceInPixels(markerDistanceInPixels);
	}

	public void setFontSize(int fontSize) {
		this.font = new Font(font.getName(), font.getStyle(), fontSize);
		initializeXAxisPainter();
		initializeYAxisPainter();
	}

	public boolean isLegendAlignmentLeft() {
		return legendAlignmentLeft;
	}

	public void setLegendAlignmentLeft(boolean legendAlignmentLeft) {
		this.legendAlignmentLeft = legendAlignmentLeft;

		if (legendPainter != null)
			legendPainter.setAlignLeft(legendAlignmentLeft);
	}
}
