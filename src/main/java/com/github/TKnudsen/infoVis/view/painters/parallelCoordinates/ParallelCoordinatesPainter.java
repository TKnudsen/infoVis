package com.github.TKnudsen.infoVis.view.painters.parallelCoordinates;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
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
import com.github.TKnudsen.infoVis.view.tools.OverplottingMitigationTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.tools.VisualMappingTools;
import com.github.TKnudsen.infoVis.view.visualChannels.IOverplottingMitigation;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.ConstantSizeEncodingFunction;

/**
 * <p>
 * Paints a parallel coordinates plot using visual mapping functions to map data
 * (represented as T) into the visual space. This is done in two steps. First, T
 * is mapped to Double for the individual y positions. Second, the Double values
 * are mapped into the visual space.
 * </p>
 *
 * @version 1.05
 * @since 2018
 */
public class ParallelCoordinatesPainter<T> extends ChartPainter
		implements IXPositionEncoding, ISizeEncoding<T>, IColorEncoding<T>, IRectangleSelection<T>, IShapeSelection<T>,
		IClickSelection<T>, ISelectionVisualizer<T>, ITooltip, IOverplottingMitigation {

	// input data
	protected final List<T> data;

	// screen coordinates of input data
	// protected final List<Point2D[]> screenPoints = new CopyOnWriteArrayList<>();
	protected final List<Point2D[]> screenPoints;

	// overplotting mitigation
	protected boolean overplottingMitigation = false;
	protected float alpha = 1.0f;

	// settable size of dots
	private float pointSize = Float.NaN;

	private boolean tooltipping = true;

	// position mapping of data
	private IPositionEncodingFunction xPositionEncodingFunction;
	private List<IPositionEncodingFunction> yPositionEncodingFunctions;
	protected boolean externalXPositionEncodingFunction = false;
	protected boolean externalYPositionEncodingFunctions = false;

	// listening to yPositionEncodingFunctions
	private final PositionEncodingFunctionListener myPositionEncodingFunctionListener = this::refreshDataPoints;

	// color coding of data
	private Function<? super T, ? extends Paint> colorMapping;

	// maps a T to individual double values which can be mapped to x and y position
	private final List<Function<? super T, Double>> worldPositionMappingsY;

	private Function<? super T, Double> sizeEncodingFunction = new ConstantSizeEncodingFunction<>(3);
	private Function<? super T, Boolean> selectedFunction;
	private boolean drawSelectedLast = true;
	private Paint selectionPaint = Color.BLACK;

	public ParallelCoordinatesPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			List<Function<? super T, Double>> worldPositionMappingsY) {
		this.colorMapping = colorMapping;
		this.worldPositionMappingsY = worldPositionMappingsY;

		// data sanity check
		this.data = Collections
				.unmodifiableList(VisualMappingTools.sanityCheckFilter(data, worldPositionMappingsY, true));

		this.screenPoints = new ArrayList<Point2D[]>(data.size());

		initializePositionEncodingFunctions();

		refreshDataPoints();
	}

	private void initializePositionEncodingFunctions() {
		this.yPositionEncodingFunctions = new ArrayList<>();

		for (int i = 0; i < worldPositionMappingsY.size(); i++) {
			Function<? super T, Double> worldPositionMappingY = worldPositionMappingsY.get(i);

			double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
			for (T t : data) {
				double v = worldPositionMappingY.apply(t);
				if (v < min)
					min = v;
				if (v > max)
					max = v;
			}

			this.yPositionEncodingFunctions.add(new PositionEncodingFunction(min, max, 0d, 1d, true));
		}

		for (IPositionEncodingFunction positionEncodingFunction : this.yPositionEncodingFunctions)
			positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.xPositionEncodingFunction = new PositionEncodingFunction(0, worldPositionMappingsY.size() - 1, 0d, 1d);
		this.xPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
	}

	/**
	 * Efficiently recomputes screen-space points for all data items.
	 *
	 * <p>
	 * This version avoids object churn by reusing the existing {@code screenPoints}
	 * list (no CopyOnWriteArrayList, no new Point2D objects per point). Each poly
	 * line is represented by a primitive float array to minimize GC pressure.
	 * </p>
	 */
	protected void refreshDataPoints() {
		if (data == null || chartRectangle == null)
			return;

		final int n = data.size();
		final int dim = worldPositionMappingsY.size();

		// Ensure capacity and reuse existing arrays if possible
		if (screenPoints.size() != n) {
			screenPoints.clear();
			for (int i = 0; i < n; i++)
				screenPoints.add(new Point2D.Float[dim]);
		}

		for (int i = 0; i < n; i++) {
			final T t = data.get(i);
			final Point2D[] pts = screenPoints.get(i);

			for (int d = 0; d < dim; d++) {
				double worldX = d;
				double worldY = worldPositionMappingsY.get(d).apply(t);

				float xP = xPositionEncodingFunction.apply(worldX).floatValue();
				float yP = yPositionEncodingFunctions.get(d).apply(worldY).floatValue();

				Point2D p = pts[d];
				if (p == null)
					pts[d] = new Point2D.Float(xP, yP);
				else
					((Point2D.Float) p).setLocation(xP, yP);
			}
		}

		if (overplottingMitigation)
			alpha = OverplottingMitigationTools.computeAlpha(screenPoints.size());
	}

	@Override
	public void draw(Graphics2D g2) {
		if (chartRectangle == null)
			return;

		drawLinesAndPoints(g2);
	}

	/**
	 * Optimized drawing of all polylines and points in the parallel coordinates
	 * view.
	 *
	 * <p>
	 * Uses {@link java.awt.geom.GeneralPath} batching to reduce the number of
	 * individual draw calls, minimizes Graphics2D state changes (paint/stroke), and
	 * caches alpha-blended colors. The goal is to drastically reduce CPU load in
	 * Java2D's rendering pipeline when many lines are drawn.
	 * </p>
	 */
	private void drawLinesAndPoints(Graphics2D g2) {
		if (data.isEmpty() || screenPoints.isEmpty())
			return;

		final int n = data.size();
		final int dim = worldPositionMappingsY.size();

		// Resolve point size once per frame
		float basePointSize = this.pointSize;
		if (Float.isNaN(basePointSize))
			basePointSize = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());

		// Prepare strokes once
		final BasicStroke stroke = BasicStrokeTools.get(basePointSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		final BasicStroke selectedStroke = BasicStrokeTools.get(basePointSize + 2, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);

		// Cache alpha-adjusted default paint
		final Paint defaultAlphaPaint = ColorTools.setAlpha(getPaint(), alpha);

		// Backup current Graphics2D state
		final Stroke oldStroke = g2.getStroke();
		final Paint oldPaint = g2.getPaint();

		// Prepare rendering settings
		g2.setStroke(stroke);

		// Optional selection handling
		final boolean hasSelection = (selectedFunction != null);
		final List<Integer> selectedIndices = (drawSelectedLast && hasSelection)
				? new ArrayList<>(Math.min(128, n / 10))
				: null;

		// --- First pass: draw non-selected (or all) lines ---
		Paint currentPaint = oldPaint;
		final GeneralPath path = new GeneralPath(); // reusable path for all lines

		for (int i = 0; i < n; i++) {
			final Point2D[] pts = screenPoints.get(i);
			if (pts == null || pts.length == 0)
				continue;

			// Determine if selected
			boolean selected = false;
			if (hasSelection) {
				final Boolean sel = selectedFunction.apply(data.get(i));
				selected = (sel != null && sel);
			}

			if (drawSelectedLast && selected && selectedIndices != null) {
				selectedIndices.add(i);
				continue;
			}

			// Determine paint (cached)
			Paint paint = (colorMapping != null) ? colorMapping.apply(data.get(i)) : null;
			if (paint == null)
				paint = defaultAlphaPaint;

			// Switch paint only when necessary
			if (paint != currentPaint) {
				g2.setPaint(paint);
				currentPaint = paint;
			}

			drawPolyline(g2, pts, path);
		}

		// --- Second pass: selected items (drawn last for emphasis) ---
		if (selectedIndices != null && !selectedIndices.isEmpty()) {
			for (int idx : selectedIndices) {
				final Point2D[] pts = screenPoints.get(idx);
				if (pts != null && pts.length > 0) {
					g2.setStroke(selectedStroke);
					g2.setPaint(selectionPaint);
					drawPolyline(g2, pts, path);

					g2.setStroke(stroke);
					Paint paint = (colorMapping != null) ? colorMapping.apply(data.get(idx)) : null;
					g2.setPaint(paint);
					drawPolyline(g2, pts, path);
				}
			}
		}

		// Restore original Graphics2D state
		if (currentPaint != oldPaint)
			g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);
	}

	/**
	 * Draws a polyline efficiently using a reusable {@link GeneralPath}.
	 *
	 * <p>
	 * The stroke and paint are assumed to be already configured in the
	 * {@link Graphics2D} context. This method avoids object allocations by reusing
	 * the provided path.
	 * </p>
	 *
	 * @param g2   the graphics context
	 * @param pts  array of screen-space points
	 * @param path reusable path
	 */
	private static void drawPolyline(Graphics2D g2, Point2D[] pts, GeneralPath path) {
		final int len = pts.length;
		if (len == 0 || pts[0] == null)
			return;

		path.reset();
		path.moveTo((float) pts[0].getX(), (float) pts[0].getY());

		for (int i = 1; i < len; i++) {
			Point2D p = pts[i];
			if (p != null)
				path.lineTo((float) p.getX(), (float) p.getY());
		}

		g2.draw(path);
	}

	public static float calculatePointSize(double viewWidth, double viewHeight) {
		return (float) Math.max(1, Math.min(viewWidth, viewHeight) * 0.006);
	}

	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		if (chartRectangle == null)
			return;

		// stroke width...
		double size = Math.floor(Math.min(this.chartRectangle.getWidth(), this.chartRectangle.getHeight()) / 150);
		if (size < 0)
			size = 0;
		if (size % 2 == 1)
			size -= 1;
		size += 1;
		this.stroke = BasicStrokeTools.get((float) size);

		if (!externalXPositionEncodingFunction)
			updateXPositionEncoding(rectangle);
		if (!externalYPositionEncodingFunctions)
			updateYPositionEncoding(rectangle);

		refreshDataPoints();
	}

	private final void updateXPositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		this.xPositionEncodingFunction.setMinPixel(rectangle.getMinX());
		this.xPositionEncodingFunction.setMaxPixel(rectangle.getMaxX());
	}

	private final void updateYPositionEncoding(Rectangle2D rectangle) {
		if (rectangle == null)
			return;

		for (IPositionEncodingFunction yPositionEncodingFunction : yPositionEncodingFunctions) {
			yPositionEncodingFunction.setMinPixel(rectangle.getMinY());
			yPositionEncodingFunction.setMaxPixel(rectangle.getMaxY());
		}
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (!tooltipping)
			return null;

		if (chartRectangle == null)
			return null;

		double maxRadius = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());

		double px = p.getX();
		double py = p.getY();

		List<Entry<Double, T>> pointsInRange = new ArrayList<>();

		int dimIndex = -1;
		for (int i = 0; i < screenPoints.size(); i++) {
			T worldCord = data.get(i);
			for (int j = 0; j < screenPoints.get(i).length; j++) {
				Point2D point = screenPoints.get(i)[j];
				double dX = Math.abs(point.getX() - px);
				if (dX < maxRadius) {
					double dY = Math.abs(point.getY() - py);
					if (dY < maxRadius) {
						pointsInRange.add(new AbstractMap.SimpleEntry<Double, T>(dX + dY, worldCord));
						dimIndex = j;
						break;
					}
				}
			}
		}

		if (pointsInRange.size() == 0)
			return null;

		Entry<Double, T> first = Collections.min(pointsInRange, Entry.comparingByKey());
		T worldCord = first.getValue();

		String toolTipString = "";

		toolTipString = worldPositionMappingsY.get(dimIndex).toString() + ": ";
		toolTipString += (MathFunctions.round(worldPositionMappingsY.get(dimIndex).apply(worldCord), 3) + " ");
//			for (int y = 0; y < worldPositionMappingsY.size(); y++)
//				toolTipString += (MathFunctions.round(worldPositionMappingsY.get(y).apply(worldCord), 3) + " ");

		StringPainter stringPainter = new StringPainter(toolTipString);
		stringPainter.setHorizontalStringAlignment(HorizontalStringAlignment.CENTER);

		Rectangle2D rect = ToolTipTools.createToolTipRectangle(chartRectangle, p, 50 + toolTipString.length() * 6, 32);
		stringPainter.setRectangle(rect);

		stringPainter.setBackgroundPaint(ColorTools.setAlpha(Color.DARK_GRAY, 0.5f));
		stringPainter.setFontColor(Color.WHITE);
		stringPainter.setFontSize(15);

		return stringPainter;
	}

	@Override
	public boolean isAlphaAdjustment() {
		return overplottingMitigation;
	}

	@Override
	public void setAlphaAdjustment(boolean dynamicAlphaAdjustment) {
		this.overplottingMitigation = dynamicAlphaAdjustment;
	}

	public float getPointSize() {
		return pointSize;
	}

	public void setPointSize(float pointSize) {
		this.pointSize = pointSize;
	}

	public Function<? super T, ? extends Paint> getColorMapping() {
		return colorMapping;
	}

	@Override
	public boolean isToolTipping() {
		return tooltipping;
	}

	@Override
	public void setToolTipping(boolean tooltipping) {
		this.tooltipping = tooltipping;
	}

	@Override
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		return getElementsInShape(rectangle);
	}

	@Override
	public List<T> getElementsInShape(Shape shape) {
		if (shape == null)
			return null;

		if (data == null)
			return null;

		List<T> elements = new ArrayList<>();
		for (T d : data) {
			for (int y = 0; y < worldPositionMappingsY.size(); y++) {
				Function<? super T, Double> worldPositionMappingY = worldPositionMappingsY.get(y);

				double worldX = y;
				double worldY = worldPositionMappingY.apply(d).doubleValue();

				double screenX = xPositionEncodingFunction.apply(worldX);
				double screenY = yPositionEncodingFunctions.get(y).apply(worldY);
				if (shape.contains(screenX, screenY))
					elements.add(d);
			}
		}

		return elements;
	}

	@Override
	public List<T> getElementsAtPoint(Point p) {
		if (p == null)
			return null;

		float radius = this.pointSize;
		if (Double.isNaN(pointSize))
			radius = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());

		Ellipse2D circle = new Ellipse2D.Double();
		circle.setFrameFromCenter(p.getX(), p.getY(), p.getX() + radius, p.getY() + radius);

		if (data == null)
			return null;

		List<T> elements = new ArrayList<>();
		for (T d : data) {
			for (int y = 0; y < worldPositionMappingsY.size(); y++) {
				Function<? super T, Double> worldPositionMappingY = worldPositionMappingsY.get(y);

				double worldX = y;
				double worldY = worldPositionMappingY.apply(d).doubleValue();

				double screenX = xPositionEncodingFunction.apply(worldX);
				double screenY = yPositionEncodingFunctions.get(y).apply(worldY);
				if (circle.contains(screenX, screenY))
					elements.add(d);
			}
		}

		return elements;
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
	public void setXPositionEncodingFunction(IPositionEncodingFunction xPositionEncodingFunction) {
		this.xPositionEncodingFunction.removePositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.xPositionEncodingFunction = xPositionEncodingFunction;
		this.xPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.externalXPositionEncodingFunction = true;
	}

	public void setYPositionEncodingFunctions(List<IPositionEncodingFunction> yPositionEncodingFunctions) {
		for (IPositionEncodingFunction positionEncodingFunction : this.yPositionEncodingFunctions)
			positionEncodingFunction.removePositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.yPositionEncodingFunctions = yPositionEncodingFunctions;

		// TODO expensive as for every axis event every axis is refreshed
		for (IPositionEncodingFunction positionEncodingFunction : this.yPositionEncodingFunctions)
			positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.externalYPositionEncodingFunctions = true;
	}

	public Paint getSelectionPaint() {
		return selectionPaint;
	}

	public void setSelectionPaint(Paint selectionPaint) {
		this.selectionPaint = selectionPaint;
	}

	public boolean isDrawSelectedLast() {
		return drawSelectedLast;
	}

	public void setDrawSelectedLast(boolean drawSelectedLast) {
		this.drawSelectedLast = drawSelectedLast;
	}

}
