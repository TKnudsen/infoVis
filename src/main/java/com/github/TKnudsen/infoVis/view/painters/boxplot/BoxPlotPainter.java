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
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints a boxplot
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2022 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.04
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

	protected Stroke dashedstroke = new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
			10.0f, DisplayTools.dashPattern, 0);

	private boolean toolTipping = true;

	// listening to the positionEncodingFunction
	private final PositionEncodingFunctionListener myPositionEncodingFunctionListener = this::intializeScreenCoordinates;

	public BoxPlotPainter(double[] data) {
		double[] copy = Arrays.copyOf(data, data.length);
		Arrays.sort(copy);
		this.dataStatistics = new StatisticsSupport(copy);

		initializePositionEncodingFunction();
	}

	public BoxPlotPainter(Collection<? extends Number> data) {
		double[] primitives = DataConversion.toPrimitives(data);
		Arrays.sort(primitives);
		this.dataStatistics = new StatisticsSupport(primitives);

		initializePositionEncodingFunction();
	}

	public BoxPlotPainter(StatisticsSupport dataStatistics) {
		this.dataStatistics = dataStatistics;

		initializePositionEncodingFunction();
	}

	private void initializePositionEncodingFunction() {
		this.positionEncodingFunction = new PositionEncodingFunction(dataStatistics.getMin(), dataStatistics.getMax(),
				0d, 1d, isInvertedAxis());
		this.positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
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

		Color c = g2.getColor();

		drawMainQuantile(g2);

		drawMedian(g2);

		drawLowerWhisker(g2);

		drawUpperWhisker(g2);

		drawDashedConnectors(g2);

		if (isDrawOutliers())
			drawOutliers(g2);

		g2.setColor(c);
	}

	private void intializeScreenCoordinates() {
		if (chartRectangle == null)
			return;

		medScreen = positionEncodingFunction.apply(dataStatistics.getMedian());
		lowerQuartileScreen = positionEncodingFunction.apply(dataStatistics.getPercentile(25));
		upperQuartileScreen = positionEncodingFunction.apply(dataStatistics.getPercentile(75));
		lowerWhiskerScreen = positionEncodingFunction.apply(dataStatistics.getPercentile(outlierPercentile));
		upperWhiskerScreen = positionEncodingFunction.apply(dataStatistics.getPercentile(100 - outlierPercentile));

		if (isDrawOutliers()) {
			if (outlierValues == null)
				calculateOutlierValues();

			if (outlierValues == null)
				this.outlierScreenCoordinates = null;
			else {
				this.outlierScreenCoordinates = new double[outlierValues.length];
				for (int i = 0; i < outlierValues.length; i++)
					outlierScreenCoordinates[i] = positionEncodingFunction.apply(outlierValues[i]);
			}
		} else
			this.outlierScreenCoordinates = null;

		quartilesRectangle = calculateQuartilesRectangle();
	}

	private void calculateOutlierValues() {
		this.outlierValues = dataStatistics.getOutliers(outlierPercentile);
	}

	abstract Rectangle2D calculateQuartilesRectangle();

	@Override
	public void setStroke(BasicStroke stroke) {
		this.stroke = stroke;
		this.dashedstroke = new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
				DisplayTools.dashPattern, 0);
	}

	protected abstract void drawMedian(Graphics2D g2);

	private void drawMainQuantile(Graphics2D g2) {
		Color c = g2.getColor();
		g2.setPaint(getPaint());

		Stroke s = g2.getStroke();
		g2.setStroke(stroke);

		if (quartilesRectangle != null) {
			if (fill) {
				g2.setColor(ColorTools.setAlpha(getPaint(),
						(float) ((getColor().getAlpha() / 255.0f) * Math.min(1.0, Math.max(0, fillAlpha)))));
				g2.fill(quartilesRectangle);
			}

			g2.setPaint(getPaint());
			g2.draw(quartilesRectangle);
		}

		g2.setStroke(s);
		g2.setColor(c);
	}

	private void drawLowerWhisker(Graphics2D g2) {
		drawLevel(g2, lowerWhiskerScreen, getPaint());
	}

	private void drawUpperWhisker(Graphics2D g2) {
		drawLevel(g2, upperWhiskerScreen, getPaint());
	}

	protected abstract void drawDashedConnectors(Graphics2D g2);

	protected abstract void drawOutliers(Graphics2D g2);

	protected abstract void drawLevel(Graphics2D g2, double ratioOfAxis, Paint col);

	@Override
	public boolean isToolTipping() {
		return toolTipping;
	}

	@Override
	public void setToolTipping(boolean toolTipping) {
		this.toolTipping = toolTipping;
	}

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
