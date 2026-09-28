package com.github.TKnudsen.infoVis.view.painters.boxplot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * Draws a boxplot as concentric rings around the center of its rectangle
 * instead of along a linear axis: the median, the 25th/75th percentile band,
 * and the whiskers are each a ring at a radius proportional to their
 * [0, 1]-normalized value.
 *
 * <p>
 * Was previously split into an abstract base class ({@code
 * DeprecatedBoxPlotPainter}) designed for hypothetical vertical/horizontal
 * siblings that were never actually implemented -- this was its only
 * subclass, so the two were merged, dropping what only existed to support
 * those non-existent siblings (a linear {@code axisPainter}, {@code
 * IAxisLogarithmicScale}, an unused {@code flipAxis} flag, unread
 * {@code screenCoordinatesInitialized}/{@code dataInitialized} flags, and the
 * unused raw-data/outlier-point-drawing machinery -- this class never actually
 * rendered outlier points).
 * </p>
 *
 * @version 2.0
 * @since 2012
 */
public class RadialBoxplotPainter extends ChartPainter {

	private StatisticsSupport dataStatistics;

	private double minGlobal = Double.NaN;
	private double maxGlobal = Double.NaN;
	private double outlierPercentile = 2.5;

	// [0, 1]-normalized ratios of radiusMax, not axis pixel positions
	private double r25;
	private double r75;
	private double rLowerWhisker;
	private double rUpperWhisker;

	private boolean fill = false;
	private double radiusMax;

	private Stroke dashedstroke = new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
			10.0f, DisplayTools.getDashPattern(), 0);

	public RadialBoxplotPainter(double[] data) {
		this(new StatisticsSupport(data));
	}

	public RadialBoxplotPainter(StatisticsSupport dataStatistics) {
		this.dataStatistics = dataStatistics;

		initializeData();
	}

	private void initializeData() {
		if (dataStatistics == null)
			return;

		intializeScreenCoordinates();

		setBorderPaint(Color.BLACK);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		radiusMax = Math.min(rectangle.getWidth() * 0.5, rectangle.getHeight() * 0.5);

		intializeScreenCoordinates();
	}

	private void intializeScreenCoordinates() {
		if (chartRectangle == null || dataStatistics == null)
			return;

		double min = dataStatistics.getMin();
		double max = dataStatistics.getMax();
		if (!Double.isNaN(minGlobal))
			min = minGlobal;
		if (!Double.isNaN(maxGlobal))
			max = maxGlobal;
		double range = max - min;

		r25 = ratio(dataStatistics.getPercentile(25), min, range);
		r75 = ratio(dataStatistics.getPercentile(75), min, range);
		rLowerWhisker = ratio(dataStatistics.getPercentile(outlierPercentile), min, range);
		rUpperWhisker = ratio(dataStatistics.getPercentile(100 - outlierPercentile), min, range);
	}

	private static double ratio(double value, double min, double range) {
		if (range == 0)
			return 0.5;
		return (value - min) / range;
	}

	@Override
	public void setStroke(BasicStroke stroke) {
		this.stroke = stroke;
		this.dashedstroke = new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
				DisplayTools.getDashPattern(), 0);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		Color c = g2.getColor();

		drawMainQuantile(g2);
		drawLowerWhisker(g2);
		drawUpperWhisker(g2);

		g2.setColor(c);
	}

	/**
	 * Scalability: 1.000.000 iterations take 150 ms. One call takes 0,000.150 ms.
	 */
	private void drawLevel(Graphics2D g2, double ratioOfAxis, Paint color) {
		if (rectangle != null)
			DisplayTools.drawPoint(g2, rectangle.getCenterX(), rectangle.getCenterY(), ratioOfAxis * radiusMax, color,
					false);
	}

	private void drawLowerWhisker(Graphics2D g2) {
		if (rectangle != null)
			if (Math.min(rectangle.getWidth(), rectangle.getHeight()) > 20) {
				g2.setStroke(dashedstroke);
				drawLevel(g2, rLowerWhisker, ColorTools.setAlpha(color, (color.getAlpha() / 255.0f) * 0.33f));
			}
	}

	private void drawUpperWhisker(Graphics2D g2) {
		if (rectangle != null)
			if (Math.min(rectangle.getWidth(), rectangle.getHeight()) > 20) {
				g2.setStroke(dashedstroke);
				drawLevel(g2, rUpperWhisker, color);
			}
	}

	private void drawMainQuantile(Graphics2D g2) {
		if (fill) {
			float strokeWidth = (float) (Math.abs(r25 - r75) * radiusMax);
			g2.setStroke(new BasicStroke(strokeWidth));
			drawLevel(g2, (r25 + r75) * 0.5, ColorTools.setAlpha(color, (color.getAlpha() / 255.0f) * 0.33f));
		}
		g2.setStroke(stroke);
		drawLevel(g2, r25, color);
		drawLevel(g2, r75, color);
	}

	public boolean isFill() {
		return fill;
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}

	public double getMinGlobal() {
		return minGlobal;
	}

	public void setMinGlobal(double minGlobal) {
		this.minGlobal = minGlobal;

		intializeScreenCoordinates();
	}

	public double getMaxGlobal() {
		return maxGlobal;
	}

	public void setMaxGlobal(double maxGlobal) {
		this.maxGlobal = maxGlobal;

		intializeScreenCoordinates();
	}

	public double getOutlierPercentile() {
		return outlierPercentile;
	}

	public void setOutlierPercentile(double outlierPercentile) {
		this.outlierPercentile = outlierPercentile;

		intializeScreenCoordinates();
	}
}
