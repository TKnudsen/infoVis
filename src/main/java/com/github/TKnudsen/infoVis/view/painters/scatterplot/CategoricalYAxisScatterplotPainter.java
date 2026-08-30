package com.github.TKnudsen.infoVis.view.painters.scatterplot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRange;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IPanning;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.interaction.IZooming;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.IAxisLogarithmicScale;
import com.github.TKnudsen.infoVis.view.painters.axis.categorical.YAxisCategoricalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.categorical.YCatXnumChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.tools.VisualMappingTools;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctions;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingRangeTools;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.ConstantSizeEncodingFunction;

/**
 * <p>
 * Scatterplot with a numerical x axis and a categorical y axis: each element is
 * placed at its x value (mapped via {@code worldPositionMappingX}) and at the
 * fixed y position of its category (mapped via {@code categoryMappingY}).
 * </p>
 *
 * @since 2026
 * @version 1.01 in August 2026
 */
public class CategoricalYAxisScatterplotPainter<T> extends YCatXnumChartPainter<Double, List<String>>
		implements IColorEncoding<T>, ISizeEncoding<T>, IRectangleSelection<T>, IShapeSelection<T>, IClickSelection<T>,
		ISelectionVisualizer<T>, ITooltip, IAxisLogarithmicScale, IZooming, IPanning {

	// input data
	private final List<T> data;

	// full x value range across all data, for clamping zoom/pan (see
	// IZooming/IPanning); the y axis is categorical, so only x is zoomable/pannable
	private NumericRange globalRangeX;

	// screen coordinates of input data, parallel to data; null where the element
	// has no matching category marker
	private final List<Point2D> screenPoints;

	// maps a T to its x value in data (world) space
	private final Function<? super T, Double> worldPositionMappingX;

	// maps a T to its category label (y axis)
	private final Function<? super T, String> categoryMappingY;

	// color coding of data
	private Function<? super T, ? extends Paint> colorMapping;

	private Function<? super T, Double> sizeEncodingFunction = new ConstantSizeEncodingFunction<>(3);

	private double pointSize = Double.NaN;

	private Function<? super T, Boolean> selectedFunction;
	private boolean drawSelectedLast = true;
	private Paint selectionPaint = Color.BLACK;

	private boolean tooltipping = true;
	private int toolTipWidth = 150;
	private int toolTipHeight = 30;
	private Function<? super T, String> toolTipMapping;

	/**
	 * @param data                  the data elements to plot
	 * @param colorMapping          maps each element to its point color; a null
	 *                              result falls back to the painter's own paint
	 * @param worldPositionMappingX maps each element to its x value in data (world)
	 *                              space
	 * @param categoryMappingY      maps each element to its category label on the y
	 *                              axis
	 */
	public CategoricalYAxisScatterplotPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, String> categoryMappingY) {
		this.colorMapping = colorMapping;
		this.worldPositionMappingX = worldPositionMappingX;
		this.categoryMappingY = categoryMappingY;

		this.data = Collections
				.unmodifiableList(VisualMappingTools.sanityCheckFilter(data, worldPositionMappingX, true));

		this.screenPoints = new ArrayList<>(this.data.size());

		initialize();
	}

	private void initialize() {
		NumericRange rangeX = PositionEncodingFunctions.computeRange(data, worldPositionMappingX,
				getClass().getSimpleName() + " (x-axis)");
		this.globalRangeX = rangeX;

		List<String> categories = new ArrayList<>();
		for (T t : data) {
			String category = categoryMappingY.apply(t);
			if (category != null && !categories.contains(category))
				categories.add(category);
		}

		initializeXAxisPainter(rangeX.getMin(), rangeX.getMax());
		initializeYAxisPainter(categories);

		refreshDataPoints();
	}

	/**
	 * Recomputes {@link #screenPoints} from the current data, the x axis painter's
	 * position encoding function, and the y axis painter's per-category marker
	 * positions.
	 */
	protected void refreshDataPoints() {
		screenPoints.clear();

		if (data == null || chartRectangle == null)
			return;

		Map<String, Double> categoryYPixel = new HashMap<>();
		for (Entry<Double, String> marker : yAxisPainter.getMarkerPositionsWithLabels())
			categoryYPixel.put(marker.getValue(), marker.getKey());

		IPositionEncodingFunction xPositionEncodingFunction = xAxisPainter.getPositionEncodingFunction();

		for (T t : data) {
			String category = categoryMappingY.apply(t);
			Double pixelY = category == null ? null : categoryYPixel.get(category);

			if (pixelY == null) {
				screenPoints.add(null);
				continue;
			}

			double pixelX = xPositionEncodingFunction.apply(worldPositionMappingX.apply(t));
			screenPoints.add(new Point2D.Double(pixelX, pixelY));
		}
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		refreshDataPoints();
	}

	@Override
	protected void initializeXAxisPainter(Double min, Double max) {
		this.xAxisPainter = new XAxisNumericalPainter<>(min, max);
	}

	@Override
	protected void initializeYAxisPainter(List<String> labels) {
		this.yAxisPainter = new YAxisCategoricalPainter<>(labels);
	}

	/**
	 * Draws unselected points first, then selected points on top (unless
	 * {@link #drawSelectedLast} is disabled), so a selection is never visually
	 * obscured by unselected points drawn after it.
	 */
	@Override
	public void drawChart(Graphics2D g2) {
		if (chartRectangle == null || data == null || data.isEmpty())
			return;

		Color oldColor = g2.getColor();

		double ps = Double.isNaN(pointSize)
				? ScatterPlotPainter.calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight())
				: pointSize;

		boolean hasSelection = selectedFunction != null;
		boolean twoPhase = drawSelectedLast && hasSelection;

		List<Integer> selectedIndices = twoPhase ? new ArrayList<>() : Collections.emptyList();

		for (int i = 0; i < data.size(); i++) {
			Point2D p = screenPoints.get(i);
			if (p == null)
				continue;

			T t = data.get(i);

			boolean selected = false;
			if (hasSelection) {
				Boolean b = selectedFunction.apply(t);
				selected = b != null && b;
			}

			if (twoPhase && selected) {
				selectedIndices.add(i);
				continue;
			}

			drawPoint(g2, p, t, ps, selected);
		}

		if (twoPhase)
			for (int i : selectedIndices)
				drawPoint(g2, screenPoints.get(i), data.get(i), ps, true);

		g2.setColor(oldColor);
	}

	private void drawPoint(Graphics2D g2, Point2D p, T t, double defaultSize, boolean selected) {
		Paint paint = colorMapping != null ? colorMapping.apply(t) : null;
		if (paint == null)
			paint = getPaint();

		double size = defaultSize;
		double sEnc = sizeEncodingFunction != null ? sizeEncodingFunction.apply(t) : Double.NaN;
		if (!Double.isNaN(sEnc))
			size = sEnc;

		if (selected) {
			double selectedSize = Math.max(size * 1.66, size + 2);
			g2.setPaint(selectionPaint);
			DisplayTools.drawPoint(g2, p.getX(), p.getY(), selectedSize, true);
		}

		g2.setPaint(paint);
		DisplayTools.drawPoint(g2, p.getX(), p.getY(), size, true);
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!tooltipping || p == null || chartRectangle == null || data == null || data.isEmpty())
			return null;

		double maxRadius = ScatterPlotPainter.calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());

		double bestDist = Double.POSITIVE_INFINITY;
		T bestElement = null;

		for (int i = 0; i < data.size(); i++) {
			Point2D sp = screenPoints.get(i);
			if (sp == null)
				continue;

			double dx = Math.abs(sp.getX() - p.getX());
			if (dx >= maxRadius)
				continue;

			double dy = Math.abs(sp.getY() - p.getY());
			if (dy >= maxRadius)
				continue;

			double dist = dx + dy;
			if (dist < bestDist) {
				bestDist = dist;
				bestElement = data.get(i);
			}
		}

		if (bestElement == null)
			return null;

		String toolTipString;
		if (toolTipMapping != null)
			toolTipString = toolTipMapping.apply(bestElement);
		else {
			Double wx = worldPositionMappingX.apply(bestElement);
			String category = categoryMappingY.apply(bestElement);
			if (wx == null || wx.isNaN())
				return null;
			toolTipString = MathFunctions.round(wx.doubleValue(), 2) + ", " + category;
		}

		if (toolTipString == null)
			return null;

		StringPainter stringPainter = new StringPainter(toolTipString);

		Rectangle2D rect = ToolTipTools.createToolTipRectangle(chartRectangle, p, toolTipWidth, toolTipHeight);
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

	/** Delegates to {@link #getElementsInShape(Shape)}. */
	@Override
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		return getElementsInShape(rectangle);
	}

	@Override
	public List<T> getElementsInShape(Shape shape) {
		if (shape == null || data == null)
			return null;

		List<T> elements = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			Point2D p = screenPoints.get(i);
			if (p != null && shape.contains(p))
				elements.add(data.get(i));
		}

		return elements;
	}

	@Override
	public List<T> getElementsAtPoint(Point p) {
		if (p == null || chartRectangle == null || data == null)
			return null;

		double radius = Double.isNaN(pointSize)
				? ScatterPlotPainter.calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight())
				: pointSize;

		Ellipse2D circle = new Ellipse2D.Double();
		circle.setFrameFromCenter(p.getX(), p.getY(), p.getX() + radius, p.getY() + radius);

		return getElementsInShape(circle);
	}

	@Override
	public void setSizeEncodingFunction(Function<? super T, Double> sizeEncodingFunction) {
		this.sizeEncodingFunction = sizeEncodingFunction;
	}

	@Override
	public void setSelectedFunction(Function<? super T, Boolean> selectedFunction) {
		this.selectedFunction = selectedFunction;
	}

	@Override
	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction) {
		this.colorMapping = colorEncodingFunction;
	}

	@Override
	public boolean isLogarithmicScale() {
		return this.xAxisPainter.isLogarithmicScale();
	}

	@Override
	public void setLogarithmicScale(boolean logarithmicScale) {
		this.xAxisPainter.setLogarithmicScale(logarithmicScale);

		refreshDataPoints();
	}

	/**
	 * @return the explicitly set point radius, or {@link Double#NaN} if none was
	 *         set
	 */
	public double getPointSize() {
		return pointSize;
	}

	/**
	 * @param pointSize fixed point radius to use instead of the value computed from
	 *                  the chart rectangle; pass {@link Double#NaN} to clear it
	 */
	public void setPointSize(double pointSize) {
		this.pointSize = pointSize;
	}

	/**
	 * @return whether selected points are drawn in a second pass on top of
	 *         unselected ones
	 */
	public boolean isDrawSelectedLast() {
		return drawSelectedLast;
	}

	public void setDrawSelectedLast(boolean drawSelectedLast) {
		this.drawSelectedLast = drawSelectedLast;
	}

	public Paint getSelectionPaint() {
		return selectionPaint;
	}

	public void setSelectionPaint(Paint selectionPaint) {
		this.selectionPaint = selectionPaint;
	}

	public int getToolTipWidth() {
		return toolTipWidth;
	}

	public void setToolTipWidth(int toolTipWidth) {
		this.toolTipWidth = toolTipWidth;
	}

	public int getToolTipHeight() {
		return toolTipHeight;
	}

	public void setToolTipHeight(int toolTipHeight) {
		this.toolTipHeight = toolTipHeight;
	}

	public Function<? super T, String> getToolTipMapping() {
		return toolTipMapping;
	}

	public void setToolTipMapping(Function<? super T, String> toolTipMapping) {
		this.toolTipMapping = toolTipMapping;
	}

	/**
	 * Zooms the x axis only ({@code zoomY} is ignored) -- the y axis is categorical
	 * (fixed marker rows), so there is nothing to zoom there, mirroring how
	 * {@code TimeSeriesUnivariatePainter} does not pan/zoom its value (y) axis
	 * either.
	 */
	@Override
	public void zoom(Point location, int zoomCount, boolean zoomX, boolean zoomY) {
		if (!zoomX)
			return;

		double worldX = xAxisPainter.getPositionEncodingFunction().inverseMapping(location.getX()).doubleValue();
		double[] newRangeX = PositionEncodingRangeTools.computeZoomedRange(getXAxisMinValue(), getXAxisMaxValue(),
				globalRangeX.getMin(), globalRangeX.getMax(), worldX, zoomCount);

		if (newRangeX != null) {
			setXAxisMinValue(newRangeX[0]);
			setXAxisMaxValue(newRangeX[1]);
		}
	}

	@Override
	public void resetZoom() {
		setXAxisMinValue(globalRangeX.getMin());
		setXAxisMaxValue(globalRangeX.getMax());
	}

	/**
	 * Pans the x axis only ({@code deltaY} is ignored) -- see {@link #zoom}. Not
	 * wired by default: a pan interaction handler drives it via the left mouse
	 * button, same as rectangle selection, so the two would fight over the same
	 * drag if both were attached.
	 */
	@Override
	public void pan(int deltaX, int deltaY) {
		if (deltaX == 0)
			return;

		IPositionEncodingFunction fn = xAxisPainter.getPositionEncodingFunction();
		double fromPixel = fn.getMinPixel().doubleValue();
		double worldDeltaX = fn.inverseMapping(fromPixel + deltaX).doubleValue()
				- fn.inverseMapping(fromPixel).doubleValue();

		double[] newRangeX = PositionEncodingRangeTools.computePannedRange(getXAxisMinValue(), getXAxisMaxValue(),
				globalRangeX.getMin(), globalRangeX.getMax(), worldDeltaX);

		if (newRangeX != null) {
			setXAxisMinValue(newRangeX[0]);
			setXAxisMaxValue(newRangeX[1]);
		}
	}
}
