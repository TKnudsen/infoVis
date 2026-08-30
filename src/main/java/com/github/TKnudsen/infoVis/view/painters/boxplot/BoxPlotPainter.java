package com.github.TKnudsen.infoVis.view.painters.boxplot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.tools.VisualMappingTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;

/**
 * <p>
 * Base class for box plot painters with optimized Graphics2D handling.
 * 
 * Uses cached strokes, float-based rendering, and avoids unnecessary state
 * changes. Designed for high-performance drawing of many box plots.
 * </p>
 *
 * @version 2.1 (optimized)
 * @since 2016
 */
public abstract class BoxPlotPainter extends ChartPainter implements IRectangleSelection<Double>, ITooltip {

	// derived data
	protected StatisticsSupport dataStatistics;
	protected double[] outlierValues;
	protected double outlierPercentile = 2.5;
	private boolean drawOutliers = true;

	// little helpers
	protected double lowerQuartileScreen;
	protected double upperQuartileScreen;
	protected double medScreen;
	protected double lowerWhiskerScreen;
	protected double upperWhiskerScreen;
	protected double[] outlierScreenCoordinates;

	protected Rectangle2D quartilesRectangle;

	protected boolean fill = true;
	private float fillAlpha = 0.66f;

	private IPositionEncodingFunction positionEncodingFunction;
	protected boolean externalPositionEncodingFunction = false;

	protected Stroke dashedstroke;
	private boolean toolTipping = true;

	// listening to the positionEncodingFunction
	private final PositionEncodingFunctionListener myPositionEncodingFunctionListener = this::intializeScreenCoordinates;

	public BoxPlotPainter(double[] data) {
		double[] copy = DataConversion.toPrimitives(VisualMappingTools.sanityCheckFilter(DataConversion.doubleToList(data),
				Number::doubleValue));
		Arrays.sort(copy);
		this.dataStatistics = new StatisticsSupport(copy);

		initializePositionEncodingFunction();

		setBackgroundPaint(null);
	}

	public BoxPlotPainter(Collection<? extends Number> data) {
		double[] primitives = DataConversion.toPrimitives(VisualMappingTools.sanityCheckFilter(data, Number::doubleValue));
		Arrays.sort(primitives);
		this.dataStatistics = new StatisticsSupport(primitives);

		initializePositionEncodingFunction();

		setBackgroundPaint(null);
	}

	/**
	 * No raw data available here to sanity-check -- dataStatistics is already
	 * computed by the caller. The caller is responsible for having filtered
	 * null/NaN values before building it (see VisualMappingTools.sanityCheckFilter,
	 * used by the other two constructors).
	 */
	public BoxPlotPainter(StatisticsSupport dataStatistics) {
		this.dataStatistics = dataStatistics;

		initializePositionEncodingFunction();

		setBackgroundPaint(null);
	}

	private void initializePositionEncodingFunction() {
		this.positionEncodingFunction = new PositionEncodingFunction(dataStatistics.getMin(), dataStatistics.getMax(),
				0d, 1d, isInvertedAxis());
		this.positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		// initialize once here to avoid null dashed stroke during first draw
		this.dashedstroke = new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
				DisplayTools.getDashPattern(), 0);
	}

	protected abstract boolean isInvertedAxis();

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (!externalPositionEncodingFunction)
			updatePositionEncoding(rectangle);

		intializeScreenCoordinates();
	}

	protected abstract void updatePositionEncoding(Rectangle2D rectangle);

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);
		final Color oldColor = g2.getColor();

		drawMainQuantile(g2);
		drawMedian(g2);
		drawLowerWhisker(g2);
		drawUpperWhisker(g2);
		drawDashedConnectors(g2);

		if (drawOutliers)
			drawOutliers(g2);

		g2.setColor(oldColor);
	}

	private void intializeScreenCoordinates() {
		if (chartRectangle == null)
			return;

		medScreen = positionEncodingFunction.apply(dataStatistics.getMedian());
		lowerQuartileScreen = positionEncodingFunction.apply(dataStatistics.getPercentile(25));
		upperQuartileScreen = positionEncodingFunction.apply(dataStatistics.getPercentile(75));
		lowerWhiskerScreen = positionEncodingFunction.apply(dataStatistics.getPercentile(outlierPercentile));
		upperWhiskerScreen = positionEncodingFunction.apply(dataStatistics.getPercentile(100 - outlierPercentile));

		if (drawOutliers) {
			if (outlierValues == null)
				outlierValues = dataStatistics.getOutliers(outlierPercentile);

			if (outlierValues == null)
				outlierScreenCoordinates = null;
			else {
				final int n = outlierValues.length;
				outlierScreenCoordinates = new double[n];
				for (int i = 0; i < n; i++)
					outlierScreenCoordinates[i] = positionEncodingFunction.apply(outlierValues[i]);
			}
		} else {
			outlierScreenCoordinates = null;
		}

		quartilesRectangle = calculateQuartilesRectangle();
	}

	@Override
	public void setStroke(BasicStroke stroke) {
		this.stroke = stroke;
		this.dashedstroke = new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
				DisplayTools.getDashPattern(), 0);
	}

	abstract Rectangle2D calculateQuartilesRectangle();

	protected abstract void drawMedian(Graphics2D g2);

	protected abstract void drawDashedConnectors(Graphics2D g2);

	protected abstract void drawOutliers(Graphics2D g2);

	protected abstract void drawLevel(Graphics2D g2, double ratioOfAxis, Paint color);

	private void drawMainQuantile(Graphics2D g2) {
		if (quartilesRectangle == null)
			return;

		final Paint oldPaint = g2.getPaint();
		final Stroke oldStroke = g2.getStroke();
		g2.setStroke(stroke);

		// Fill and draw using DisplayTools-friendly state reuse
		if (fill) {
			Paint fillPaint = ColorTools.setAlpha(getPaint(),
					(float) ((getColor().getAlpha() / 255.0f) * Math.min(1.0, Math.max(0, fillAlpha))));
			g2.setPaint(fillPaint);
			g2.fill(quartilesRectangle);
		}

		g2.setPaint(getPaint());
		g2.draw(quartilesRectangle);

		g2.setStroke(oldStroke);
		g2.setPaint(oldPaint);
	}

	private void drawLowerWhisker(Graphics2D g2) {
		drawLevel(g2, lowerWhiskerScreen, getPaint());
	}

	private void drawUpperWhisker(Graphics2D g2) {
		drawLevel(g2, upperWhiskerScreen, getPaint());
	}

	// ---- Tool tip and interaction ----

	@Override
	public boolean isToolTipping() {
		return toolTipping;
	}

	@Override
	public void setToolTipping(boolean toolTipping) {
		this.toolTipping = toolTipping;
	}

	// ---- getters and setters ----

	public boolean isFill() {
		return fill;
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}

	public double getOutlierPercentile() {
		return outlierPercentile;
	}

	public void setOutlierPercentile(double outlierPercentile) {
		this.outlierPercentile = outlierPercentile;

		intializeScreenCoordinates();
	}

	public boolean isDrawOutliers() {
		return drawOutliers;
	}

	public void setDrawOutliers(boolean drawOutliers) {
		this.drawOutliers = drawOutliers;

		intializeScreenCoordinates();
	}

	public IPositionEncodingFunction getPositionEncodingFunction() {
		return positionEncodingFunction;
	}

	public void setPositionEncodingFunction(IPositionEncodingFunction positionEncodingFunction) {
		this.positionEncodingFunction.removePositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.positionEncodingFunction = positionEncodingFunction;
		this.positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.externalPositionEncodingFunction = true;
	}

	public float getFillAlpha() {
		return fillAlpha;
	}

	public void setFillAlpha(float fillAlpha) {
		this.fillAlpha = fillAlpha;
	}
}
