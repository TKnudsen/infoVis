package com.github.TKnudsen.infoVis.view.painters.barchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoding;

/**
 * <p>
 * Draws a whisker (error bar) per bar of a {@link BarChartPainter}: a line
 * from a lower to an upper bound, with optional terminator caps, positioned
 * through the exact same {@link IPositionEncodingFunction} the bars
 * themselves use -- so bar height and whisker position can never disagree.
 * </p>
 *
 * <p>
 * Composed with, not a subclass of, the {@link BarChartPainter} it decorates
 * (works with either {@link BarChartVerticalPainter} or
 * {@link BarChartHorizontalPainter}, detected via the
 * {@link IYPositionEncoding}/{@link IXPositionEncoding} marker interfaces
 * those already implement) -- add both painters to the same panel rather
 * than subclassing the bar chart itself. This avoids the old PainterLib
 * {@code ErrorBarChartPainter}'s design, which extended the bar chart
 * directly (tying error bars to one, vertical-only, legacy implementation)
 * and computed bounds through an axis-relative "fraction of max" detour
 * ({@code rlowerWRel}/{@code rupperWRel}) that was then fed back through the
 * bar-height scaling math a second time -- fragile, and vertical-only. Here,
 * lower/upper are plain data values mapped straight through the shared
 * position-encoding function, exactly like a bar's own top edge.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ErrorBarChartPainter extends ChartPainter {

	public enum ErrorMeasure {
		STANDARD_DEVIATION, CONFIDENCE_INTERVAL, MIN_MAX
	}

	private final BarChartPainter barChartPainter;
	private final List<StatisticsSupport> valuesPerBar;

	private ErrorMeasure errorMeasure;
	private double outlierPercentile = 5.0;

	private boolean drawErrorTerminators = true;
	private Paint errorPaint = Color.BLACK;
	private BasicStroke errorStroke;

	private List<Double> lowerValues;
	private List<Double> upperValues;

	public ErrorBarChartPainter(BarChartPainter barChartPainter, List<StatisticsSupport> valuesPerBar) {
		this(barChartPainter, valuesPerBar, ErrorMeasure.CONFIDENCE_INTERVAL);
	}

	public ErrorBarChartPainter(BarChartPainter barChartPainter, List<StatisticsSupport> valuesPerBar,
			ErrorMeasure errorMeasure) {
		if (barChartPainter == null)
			throw new IllegalArgumentException("ErrorBarChartPainter: barChartPainter must not be null");
		if (!(barChartPainter instanceof IYPositionEncoding) && !(barChartPainter instanceof IXPositionEncoding))
			throw new IllegalArgumentException(
					"ErrorBarChartPainter: barChartPainter must be a BarChartVerticalPainter or BarChartHorizontalPainter");
		if (valuesPerBar == null || valuesPerBar.size() != barChartPainter.getData().size())
			throw new IllegalArgumentException(
					"ErrorBarChartPainter: valuesPerBar must have one entry per bar of the given bar chart");

		this.barChartPainter = barChartPainter;
		this.valuesPerBar = valuesPerBar;
		this.errorMeasure = errorMeasure;

		this.setBackgroundPaint(null);

		computeBounds();
	}

	private void computeBounds() {
		lowerValues = new ArrayList<>(valuesPerBar.size());
		upperValues = new ArrayList<>(valuesPerBar.size());

		for (StatisticsSupport stats : valuesPerBar) {
			if (stats == null) {
				lowerValues.add(null);
				upperValues.add(null);
				continue;
			}

			switch (errorMeasure) {
			case STANDARD_DEVIATION:
				lowerValues.add(stats.getMean() - stats.getStandardDeviation());
				upperValues.add(stats.getMean() + stats.getStandardDeviation());
				break;
			case MIN_MAX:
				lowerValues.add(stats.getMin());
				upperValues.add(stats.getMax());
				break;
			case CONFIDENCE_INTERVAL:
			default:
				lowerValues.add(stats.getPercentile(outlierPercentile));
				upperValues.add(stats.getPercentile(100 - outlierPercentile));
				break;
			}
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (getChartRectangle() == null)
			return;

		List<Rectangle2D> barRectangles = barChartPainter.getBarRectangles();
		IPositionEncodingFunction positionEncoding = barChartPainter.getPositionEncodingFunction();
		boolean vertical = barChartPainter instanceof IYPositionEncoding;

		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();

		g2.setPaint(errorPaint);
		g2.setStroke(errorStrokeOrDefault());

		for (int i = 0; i < barRectangles.size() && i < lowerValues.size(); i++) {
			Rectangle2D bar = barRectangles.get(i);
			Double lower = lowerValues.get(i);
			Double upper = upperValues.get(i);
			if (bar == null || lower == null || upper == null)
				continue;

			double lowerPixel = positionEncoding.apply(lower);
			double upperPixel = positionEncoding.apply(upper);

			if (vertical) {
				double x = bar.getCenterX();
				double halfTerminator = bar.getWidth() * 0.25;

				DisplayTools.drawLine(g2, x, lowerPixel, x, upperPixel);
				if (drawErrorTerminators) {
					DisplayTools.drawLine(g2, x - halfTerminator, lowerPixel, x + halfTerminator, lowerPixel);
					DisplayTools.drawLine(g2, x - halfTerminator, upperPixel, x + halfTerminator, upperPixel);
				}
			} else {
				double y = bar.getCenterY();
				double halfTerminator = bar.getHeight() * 0.25;

				DisplayTools.drawLine(g2, lowerPixel, y, upperPixel, y);
				if (drawErrorTerminators) {
					DisplayTools.drawLine(g2, lowerPixel, y - halfTerminator, lowerPixel, y + halfTerminator);
					DisplayTools.drawLine(g2, upperPixel, y - halfTerminator, upperPixel, y + halfTerminator);
				}
			}
		}

		g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);
	}

	private BasicStroke errorStrokeOrDefault() {
		if (errorStroke != null)
			return errorStroke;

		if (getChartRectangle() == null)
			return DisplayTools.standardStroke;

		float width = (float) Math.max(1,
				Math.max(3, Math.min(getChartRectangle().getWidth(), getChartRectangle().getHeight()) / 12.0) * 0.14);
		return new BasicStroke(width);
	}

	public ErrorMeasure getErrorMeasure() {
		return errorMeasure;
	}

	public void setErrorMeasure(ErrorMeasure errorMeasure) {
		this.errorMeasure = errorMeasure;

		computeBounds();
	}

	public double getOutlierPercentile() {
		return outlierPercentile;
	}

	public void setOutlierPercentile(double outlierPercentile) {
		this.outlierPercentile = outlierPercentile;

		computeBounds();
	}

	public boolean isDrawErrorTerminators() {
		return drawErrorTerminators;
	}

	public void setDrawErrorTerminators(boolean drawErrorTerminators) {
		this.drawErrorTerminators = drawErrorTerminators;
	}

	public Paint getErrorPaint() {
		return errorPaint;
	}

	public void setErrorPaint(Paint errorPaint) {
		this.errorPaint = errorPaint;
	}

	public void setErrorStroke(BasicStroke errorStroke) {
		this.errorStroke = errorStroke;
	}

}
