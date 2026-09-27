package com.github.TKnudsen.infoVis.view.painters.parallelCoordinates;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.tools.BasicStrokeTools;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;

/**
 * <p>
 * Draws one aggregate glyph per group of a parallel-coordinates dataset,
 * instead of one line per individual data item: a filled band (the group's
 * spread per axis, min/max or interquartile range) plus a bold center line
 * (the group's mean or median per axis).
 * </p>
 *
 * <p>
 * Intentionally agnostic of where the groups come from (clustering, manual
 * categories, time buckets, ...) -- it only needs {@code List<List<T>>} plus
 * the same per-dimension value mappings {@link ParallelCoordinatesPainter}
 * uses. One instance handles the whole list of groups; selection/tooltip
 * interfaces operate on the group index (an {@code Integer}), not on
 * individual rows, since a group is the unit of interaction here.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ParallelCoordinatesAggregatePainter<T> extends ChartPainter implements IXPositionEncoding,
		IRectangleSelection<Integer>, IShapeSelection<Integer>, IClickSelection<Integer>,
		ISelectionVisualizer<Integer>, ITooltip {

	public enum CenterStatistic {
		MEAN, MEDIAN
	}

	public enum BandStatistic {
		MIN_MAX, INTERQUARTILE_RANGE
	}

	protected final List<List<T>> groups;
	protected final List<Function<? super T, Double>> worldPositionMappingsY;

	private Function<? super Integer, ? extends Paint> groupColorMapping;

	private final CenterStatistic centerStatistic;
	private final BandStatistic bandStatistic;

	// per-group, per-dimension aggregates, computed once (not per draw() call)
	private final List<double[]> centerValues = new ArrayList<>();
	private final List<double[]> lowerValues = new ArrayList<>();
	private final List<double[]> upperValues = new ArrayList<>();
	private final List<double[]> outlierLowerValues = new ArrayList<>();
	private final List<double[]> outlierUpperValues = new ArrayList<>();

	// screen coordinates, refreshed on setRectangle()/position-encoding changes
	private final List<Point2D[]> centerScreenPoints = new ArrayList<>();
	private final List<Point2D[]> lowerScreenPoints = new ArrayList<>();
	private final List<Point2D[]> upperScreenPoints = new ArrayList<>();
	private final List<Point2D[]> outlierLowerScreenPoints = new ArrayList<>();
	private final List<Point2D[]> outlierUpperScreenPoints = new ArrayList<>();

	private boolean drawCenterLine = true;
	private boolean drawOutlierLines = true;
	private float bandAlpha = 0.25f;
	private static final int OUTLIER_LOWER_PERCENTILE = 5;
	private static final int OUTLIER_UPPER_PERCENTILE = 95;

	private IPositionEncodingFunction xPositionEncodingFunction;
	private List<IPositionEncodingFunction> yPositionEncodingFunctions;
	private boolean externalXPositionEncodingFunction = false;
	private boolean externalYPositionEncodingFunctions = false;

	private final PositionEncodingFunctionListener myPositionEncodingFunctionListener = this::refreshScreenPoints;

	private Function<? super Integer, Boolean> selectedFunction;
	private boolean tooltipping = true;

	public ParallelCoordinatesAggregatePainter(List<List<T>> groups,
			List<Function<? super T, Double>> worldPositionMappingsY,
			Function<? super Integer, ? extends Paint> groupColorMapping) {
		this(groups, worldPositionMappingsY, groupColorMapping, CenterStatistic.MEDIAN,
				BandStatistic.INTERQUARTILE_RANGE);
	}

	public ParallelCoordinatesAggregatePainter(List<List<T>> groups,
			List<Function<? super T, Double>> worldPositionMappingsY,
			Function<? super Integer, ? extends Paint> groupColorMapping, CenterStatistic centerStatistic,
			BandStatistic bandStatistic) {
		if (groups == null || groups.isEmpty())
			throw new IllegalArgumentException("ParallelCoordinatesAggregatePainter: groups must not be empty");
		if (worldPositionMappingsY == null || worldPositionMappingsY.isEmpty())
			throw new IllegalArgumentException(
					"ParallelCoordinatesAggregatePainter: worldPositionMappingsY must not be empty");

		List<List<T>> defensiveCopy = new ArrayList<>();
		for (List<T> group : groups)
			defensiveCopy.add(Collections.unmodifiableList(new ArrayList<>(group)));
		this.groups = Collections.unmodifiableList(defensiveCopy);

		this.worldPositionMappingsY = worldPositionMappingsY;
		this.groupColorMapping = groupColorMapping;
		this.centerStatistic = centerStatistic;
		this.bandStatistic = bandStatistic;

		computeAggregates();
		initializePositionEncodingFunctions();
		refreshScreenPoints();
	}

	private void computeAggregates() {
		int dimensions = worldPositionMappingsY.size();

		for (List<T> group : groups) {
			double[] center = new double[dimensions];
			double[] lower = new double[dimensions];
			double[] upper = new double[dimensions];
			double[] outlierLower = new double[dimensions];
			double[] outlierUpper = new double[dimensions];

			for (int d = 0; d < dimensions; d++) {
				Function<? super T, Double> mapping = worldPositionMappingsY.get(d);

				List<Double> values = new ArrayList<>(group.size());
				for (T t : group)
					values.add(mapping.apply(t));

				StatisticsSupport stats = new StatisticsSupport(values);

				center[d] = centerStatistic == CenterStatistic.MEAN ? stats.getMean() : stats.getMedian();

				if (bandStatistic == BandStatistic.MIN_MAX) {
					lower[d] = stats.getMin();
					upper[d] = stats.getMax();
				} else {
					lower[d] = stats.getPercentile(25);
					upper[d] = stats.getPercentile(75);
				}

				outlierLower[d] = stats.getPercentile(OUTLIER_LOWER_PERCENTILE);
				outlierUpper[d] = stats.getPercentile(OUTLIER_UPPER_PERCENTILE);
			}

			centerValues.add(center);
			lowerValues.add(lower);
			upperValues.add(upper);
			outlierLowerValues.add(outlierLower);
			outlierUpperValues.add(outlierUpper);
		}
	}

	private void initializePositionEncodingFunctions() {
		int dimensions = worldPositionMappingsY.size();

		double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
		for (List<double[]> series : List.of(lowerValues, upperValues, centerValues, outlierLowerValues,
				outlierUpperValues))
			for (double[] values : series)
				for (double v : values) {
					if (v < min)
						min = v;
					if (v > max)
						max = v;
				}

		this.yPositionEncodingFunctions = new ArrayList<>();
		for (int d = 0; d < dimensions; d++) {
			PositionEncodingFunction f = new PositionEncodingFunction(min, max, 0d, 1d, true);
			f.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
			this.yPositionEncodingFunctions.add(f);
		}

		this.xPositionEncodingFunction = new PositionEncodingFunction(0, dimensions - 1, 0d, 1d);
		this.xPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
	}

	private void refreshScreenPoints() {
		centerScreenPoints.clear();
		lowerScreenPoints.clear();
		upperScreenPoints.clear();
		outlierLowerScreenPoints.clear();
		outlierUpperScreenPoints.clear();

		if (chartRectangle == null || xPositionEncodingFunction == null)
			return;

		int dimensions = worldPositionMappingsY.size();

		for (int g = 0; g < groups.size(); g++) {
			centerScreenPoints.add(toScreenPoints(centerValues.get(g), dimensions));
			lowerScreenPoints.add(toScreenPoints(lowerValues.get(g), dimensions));
			upperScreenPoints.add(toScreenPoints(upperValues.get(g), dimensions));
			outlierLowerScreenPoints.add(toScreenPoints(outlierLowerValues.get(g), dimensions));
			outlierUpperScreenPoints.add(toScreenPoints(outlierUpperValues.get(g), dimensions));
		}
	}

	private Point2D[] toScreenPoints(double[] worldValues, int dimensions) {
		Point2D[] pts = new Point2D[dimensions];
		for (int d = 0; d < dimensions; d++) {
			float x = xPositionEncodingFunction.apply((double) d).floatValue();
			float y = yPositionEncodingFunctions.get(d).apply(worldValues[d]).floatValue();
			pts[d] = new Point2D.Float(x, y);
		}
		return pts;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (chartRectangle == null)
			return;

		boolean hasSelection = selectedFunction != null;

		Stroke oldStroke = g2.getStroke();
		Paint oldPaint = g2.getPaint();

		for (int g = 0; g < groups.size(); g++) {
			boolean selected = !hasSelection || Boolean.TRUE.equals(selectedFunction.apply(g));

			Paint color = groupColorMapping.apply(g);
			float alpha = selected ? bandAlpha : bandAlpha * 0.3f;

			drawBand(g2, g, ColorTools.setAlpha(color, alpha));

			if (drawOutlierLines) {
				g2.setStroke(DisplayTools.standardDashedStroke);
				g2.setPaint(selected ? color : ColorTools.setAlpha(color, 0.3f));
				drawPolyline(g2, outlierLowerScreenPoints.get(g));
				drawPolyline(g2, outlierUpperScreenPoints.get(g));
			}

			if (drawCenterLine) {
				g2.setStroke(BasicStrokeTools.get(selected ? 2.5f : 1f));
				g2.setPaint(selected ? color : ColorTools.setAlpha(color, 0.3f));
				drawPolyline(g2, centerScreenPoints.get(g));
			}
		}

		g2.setStroke(oldStroke);
		g2.setPaint(oldPaint);
	}

	private void drawBand(Graphics2D g2, int groupIndex, Paint fillPaint) {
		Point2D[] lower = lowerScreenPoints.get(groupIndex);
		Point2D[] upper = upperScreenPoints.get(groupIndex);
		if (lower.length == 0)
			return;

		GeneralPath path = new GeneralPath();
		path.moveTo(lower[0].getX(), lower[0].getY());
		for (int d = 1; d < lower.length; d++)
			path.lineTo(lower[d].getX(), lower[d].getY());
		for (int d = upper.length - 1; d >= 0; d--)
			path.lineTo(upper[d].getX(), upper[d].getY());
		path.closePath();

		g2.setPaint(fillPaint);
		g2.fill(path);
	}

	private static void drawPolyline(Graphics2D g2, Point2D[] pts) {
		if (pts.length == 0)
			return;

		GeneralPath path = new GeneralPath();
		path.moveTo(pts[0].getX(), pts[0].getY());
		for (int i = 1; i < pts.length; i++)
			path.lineTo(pts[i].getX(), pts[i].getY());

		g2.draw(path);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null || chartRectangle == null)
			return;

		if (!externalXPositionEncodingFunction) {
			xPositionEncodingFunction.setMinPixel(rectangle.getMinX());
			xPositionEncodingFunction.setMaxPixel(rectangle.getMaxX());
		}
		if (!externalYPositionEncodingFunctions)
			for (IPositionEncodingFunction f : yPositionEncodingFunctions) {
				f.setMinPixel(rectangle.getMinY());
				f.setMaxPixel(rectangle.getMaxY());
			}

		refreshScreenPoints();
	}

	@Override
	public void setXPositionEncodingFunction(IPositionEncodingFunction xPositionEncodingFunction) {
		this.xPositionEncodingFunction.removePositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.xPositionEncodingFunction = xPositionEncodingFunction;
		this.xPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.externalXPositionEncodingFunction = true;

		refreshScreenPoints();
	}

	public void setYPositionEncodingFunctions(List<IPositionEncodingFunction> yPositionEncodingFunctions) {
		for (IPositionEncodingFunction f : this.yPositionEncodingFunctions)
			f.removePositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.yPositionEncodingFunctions = yPositionEncodingFunctions;

		for (IPositionEncodingFunction f : this.yPositionEncodingFunctions)
			f.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.externalYPositionEncodingFunctions = true;

		refreshScreenPoints();
	}

	@Override
	public void setSelectedFunction(Function<? super Integer, Boolean> selectedFunction) {
		this.selectedFunction = selectedFunction;
	}

	@Override
	public List<Integer> getElementsAtPoint(Point p) {
		List<Integer> hits = new ArrayList<>();
		for (int g = 0; g < groups.size(); g++)
			if (bandContains(g, p.getX(), p.getY()))
				hits.add(g);
		return hits;
	}

	@Override
	public List<Integer> getElementsInRectangle(RectangularShape rectangle) {
		return getElementsInShape(rectangle);
	}

	@Override
	public List<Integer> getElementsInShape(Shape shape) {
		List<Integer> hits = new ArrayList<>();
		for (int g = 0; g < groups.size(); g++)
			for (Point2D p : centerScreenPoints.get(g))
				if (shape.contains(p)) {
					hits.add(g);
					break;
				}
		return hits;
	}

	private boolean bandContains(int groupIndex, double x, double y) {
		Point2D[] lower = lowerScreenPoints.get(groupIndex);
		Point2D[] upper = upperScreenPoints.get(groupIndex);

		GeneralPath path = new GeneralPath();
		path.moveTo(lower[0].getX(), lower[0].getY());
		for (int d = 1; d < lower.length; d++)
			path.lineTo(lower[d].getX(), lower[d].getY());
		for (int d = upper.length - 1; d >= 0; d--)
			path.lineTo(upper[d].getX(), upper[d].getY());
		path.closePath();

		return path.contains(x, y);
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!tooltipping || chartRectangle == null)
			return null;

		List<Integer> hits = getElementsAtPoint(p);
		if (hits.isEmpty())
			return null;

		int groupIndex = hits.get(0);
		String text = "Group " + groupIndex + " (n=" + groups.get(groupIndex).size() + ")";

		StringPainter stringPainter = new StringPainter(text);
		stringPainter.setHorizontalStringAlignment(HorizontalStringAlignment.CENTER);

		Rectangle2D rect = ToolTipTools.createToolTipRectangle(chartRectangle, p, 50 + text.length() * 6, 32);
		stringPainter.setRectangle(rect);
		stringPainter.setBackgroundPaint(ColorTools.setAlpha(Color.DARK_GRAY, 0.5f));
		stringPainter.setFontColor(Color.WHITE);
		stringPainter.setFontSize(15);

		return stringPainter;
	}

	@Override
	public boolean isToolTipping() {
		return tooltipping;
	}

	@Override
	public void setToolTipping(boolean tooltipping) {
		this.tooltipping = tooltipping;
	}

	public boolean isDrawCenterLine() {
		return drawCenterLine;
	}

	public void setDrawCenterLine(boolean drawCenterLine) {
		this.drawCenterLine = drawCenterLine;
	}

	public boolean isDrawOutlierLines() {
		return drawOutlierLines;
	}

	public void setDrawOutlierLines(boolean drawOutlierLines) {
		this.drawOutlierLines = drawOutlierLines;
	}

	public float getBandAlpha() {
		return bandAlpha;
	}

	public void setBandAlpha(float bandAlpha) {
		this.bandAlpha = bandAlpha;
	}

	public List<List<T>> getGroups() {
		return groups;
	}

}
