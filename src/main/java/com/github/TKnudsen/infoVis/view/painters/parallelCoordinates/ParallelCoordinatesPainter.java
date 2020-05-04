package com.github.TKnudsen.infoVis.view.painters.parallelCoordinates;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.tools.DoubleMappingTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.ConstantSizeEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints a parallel coordinates plot using visual mapping functions to map data
 * (represented as T) into the visual space. This is done in two steps. First, T
 * is mapped to Double for the individual y positions. Second, the Double values
 * are mapped into the visual space.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @version 1.03
 */
public class ParallelCoordinatesPainter<T> extends ChartPainter
		implements IXPositionEncoding, ISizeEncoding<T>, IColorEncoding<T>, IRectangleSelection<T>, IShapeSelection<T>,
		IClickSelection<T>, ISelectionVisualizer<T>, ITooltip {

	// input data
	protected final List<T> data;

	// screen coordinates of input data
	protected final List<Point2D[]> screenPoints = new CopyOnWriteArrayList<>();

	// overplotting mitigation
	protected boolean overplottingMitigation = false;
	protected float alpha = 1.0f;

	// settable size of dots
	private double pointSize = Double.NaN;

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
				.unmodifiableList(DoubleMappingTools.sanityCheckFilter(data, worldPositionMappingsY, true));

		initializePositionEncodingFunctions();

		refreshDataPoints();
	}

	private void initializePositionEncodingFunctions() {
		this.yPositionEncodingFunctions = new ArrayList<>();

//		List<Double> xValues = new ArrayList<>();
		for (int i = 0; i < worldPositionMappingsY.size(); i++) {
			Function<? super T, Double> worldPositionMappingY = worldPositionMappingsY.get(i);

			List<Double> yValues = new ArrayList<>();
			for (T t : data) {
//				xValues.add((double) i);
				yValues.add(worldPositionMappingY.apply(t).doubleValue());
			}

			StatisticsSupport yStatistics = new StatisticsSupport(yValues);

			this.yPositionEncodingFunctions
					.add(new PositionEncodingFunction(yStatistics.getMin(), yStatistics.getMax(), 0d, 1d, true));

			// TODO expensive as for every axis event every axis is refreshed
			for (IPositionEncodingFunction positionEncodingFunction : this.yPositionEncodingFunctions)
				positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
		}

//		StatisticsSupport xStatistics = new StatisticsSupport(xValues);

		this.xPositionEncodingFunction = new PositionEncodingFunction(0, worldPositionMappingsY.size() - 1, 0d, 1d);
		this.xPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
	}

	protected void refreshDataPoints() {
		screenPoints.clear();

		// chartRectangle == null means that position encoding will make no sense
		// (still)
		if (data == null || chartRectangle == null)
			return;

		for (int i = 0; i < data.size(); i++) {
			T t = data.get(i);

			Point2D[] points = new Point2D[worldPositionMappingsY.size()];

			for (int x = 0; x < worldPositionMappingsY.size(); x++) {
				Function<? super T, Double> worldPositionMappingY = worldPositionMappingsY.get(x);

				double worldX = x;
				double worldY = worldPositionMappingY.apply(t).doubleValue();
				double xP = xPositionEncodingFunction.apply(worldX);
				double yP = yPositionEncodingFunctions.get(x).apply(worldY);

				Point2D point = new Point2D.Double(xP, yP);
				points[x] = point;
			}

			screenPoints.add(points);
		}

		if (overplottingMitigation)
			alpha = Math.max(0.05f, Math.min(1.0f, 1.0f / (screenPoints.size() / 3000.0f)));
	}

	@Override
	public void draw(Graphics2D g2) {
		if (chartRectangle == null)
			return;

		drawLinesAndPoints(g2);
	}

	private void drawLinesAndPoints(Graphics2D g2) {
		Color c = g2.getColor();

		// point size
		double pointSize = this.pointSize;

		for (int i = 0; i < data.size(); i++) {
			Point2D[] point = screenPoints.get(i);
			if (point == null)
				continue;

			boolean selected = false;
			if (selectedFunction != null) {
				Boolean apply = selectedFunction.apply(data.get(i));
				if (apply != null)
					selected = apply.booleanValue();
			}

			if (drawSelectedLast && selected)
				continue;

			Paint colorToPaint = colorMapping.apply(data.get(i));
			if (colorToPaint == null)
				colorToPaint = ColorTools.setAlpha(getPaint(), alpha);

			// new concept with the size-encoding
			if (Double.isNaN(pointSize))
				pointSize = sizeEncodingFunction.apply(data.get(i)).doubleValue();
			if (Double.isNaN(pointSize))
				pointSize = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());

			drawIndividualPoint(g2, point, (float) pointSize, colorToPaint, selected);
		}

		if (drawSelectedLast) // second loop
			for (int i = 0; i < data.size(); i++) {
				Point2D[] point = screenPoints.get(i);
				if (point == null)
					continue;

				boolean selected = false;
				if (selectedFunction != null) {
					Boolean apply = selectedFunction.apply(data.get(i));
					if (apply != null)
						selected = apply.booleanValue();
				}

				if (!selected)
					continue;

				Paint colorToPaint = colorMapping.apply(data.get(i));
				if (colorToPaint == null)
					colorToPaint = ColorTools.setAlpha(getPaint(), alpha);

				// new concept with the size-encoding
				if (Double.isNaN(pointSize))
					pointSize = sizeEncodingFunction.apply(data.get(i)).doubleValue();
				if (Double.isNaN(pointSize))
					pointSize = calculatePointSize(chartRectangle.getWidth(), chartRectangle.getHeight());

				drawIndividualPoint(g2, point, (float) pointSize, colorToPaint, selected);
			}

		g2.setColor(c);
	}

	/**
	 * code is copied from the trajectory painter. may be externalized to a tools
	 * class.
	 * 
	 * @param g2
	 * @param pointArray
	 * @param pointSize
	 * @param pointPaint
	 * @param selected
	 */
	protected void drawIndividualPoint(Graphics2D g2, Point2D[] pointArray, float pointSize, Paint pointPaint,
			boolean selected) {

		Point2D lastPoint = null;

		for (int i = 0; i < pointArray.length; i++) {
			Point2D point = pointArray[i];

			float size = pointSize * 1.33f;
//			float sizeSelected = Math.max(size + 1, pointSize * 1.66f);

			if (selected) {
				// line
				if (lastPoint != null) {
					DisplayTools.drawLine(g2, lastPoint.getX(), lastPoint.getY(), point.getX(), point.getY(),
							new BasicStroke((float) (pointSize + 2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND),
							selectionPaint);
					DisplayTools.drawLine(g2, lastPoint.getX(), lastPoint.getY(), point.getX(), point.getY(),
							new BasicStroke(pointSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), pointPaint);
				}

				// point
//				DisplayTools.drawPoint(g2, point.getX(), point.getY(), sizeSelected, selectionPaint, true);
				DisplayTools.drawPoint(g2, point.getX(), point.getY(), size, pointPaint, true);
			} else {
				// line
				if (lastPoint != null)
					DisplayTools.drawLine(g2, lastPoint.getX(), lastPoint.getY(), point.getX(), point.getY(),
							new BasicStroke(pointSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), pointPaint);

				// point
				DisplayTools.drawPoint(g2, point.getX(), point.getY(), size, pointPaint, true);
			}

			lastPoint = point;
		}
	}

	public static double calculatePointSize(double viewWidth, double viewHeight) {
		return Math.max(1, Math.min(viewWidth, viewHeight) * 0.006);
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
		this.stroke = new BasicStroke((float) size);

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

	public boolean isDynamicAlphaAdjustment() {
		return overplottingMitigation;
	}

	public void setDynamicAlphaAdjustment(boolean dynamicAlphaAdjustment) {
		this.overplottingMitigation = dynamicAlphaAdjustment;
	}

	public double getPointSize() {
		return pointSize;
	}

	public void setPointSize(double pointSize) {
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

		double radius = this.pointSize;
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
