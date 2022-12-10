package com.github.TKnudsen.infoVis.view.painters.scatterplot;

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
import com.github.TKnudsen.ComplexDataObject.model.tools.Threads;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;
import com.github.TKnudsen.infoVis.view.tools.VisualMappings;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctionListener;
import com.github.TKnudsen.infoVis.view.visualChannels.position.x.IXPositionEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.position.y.IYPositionEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.ConstantSizeEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints a scatter plot using visual mapping functions to map data (represented
 * as T) into the visual space. This is done in two steps. First, T is mapped to
 * Double for the x and the y position. Second, the two Doubles are mapped into
 * the visual space.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @version 2.09
 */
public class ScatterPlotPainter<T> extends ChartPainter
		implements IXPositionEncoding, IYPositionEncoding, ISizeEncoding<T>, IColorEncoding<T>, IRectangleSelection<T>,
		IShapeSelection<T>, IClickSelection<T>, ISelectionVisualizer<T>, ITooltip {

	// input data
	protected final List<T> data;

	// screen coordinates of input data
	protected final List<Point2D> screenPoints = new CopyOnWriteArrayList<>();

	// overplotting mitigation
	protected boolean overplottingMitigation = false;
	protected float alpha = 1.0f;

	// settable size of dots
	private double pointSize = Double.NaN;

	private boolean tooltipping = true;

	// position mapping of data
	private IPositionEncodingFunction xPositionEncodingFunction;
	private IPositionEncodingFunction yPositionEncodingFunction;
	protected boolean externalXPositionEncodingFunction = false;
	protected boolean externalYPositionEncodingFunction = false;

	// listening to yPositionEncodingFunctions
	private final PositionEncodingFunctionListener myPositionEncodingFunctionListener = this::refreshDataPoints;

	// color coding of data
	private Function<? super T, ? extends Paint> colorMapping;

	// maps a T to individual double values which can be mapped to x and y position
	private final Function<? super T, Double> worldPositionMappingX;
	private final Function<? super T, Double> worldPositionMappingY;

	private Function<? super T, Double> sizeEncodingFunction = new ConstantSizeEncodingFunction<>(3);

	private Function<? super T, Boolean> selectedFunction;
	private boolean drawSelectedLast = true;
	private Paint selectionPaint = Color.BLACK;

	private Function<? super T, String> toolTipMapping;

	private boolean refreshingDataPoints;

	public ScatterPlotPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		this.colorMapping = colorMapping;
		this.worldPositionMappingX = worldPositionMappingX;
		this.worldPositionMappingY = worldPositionMappingY;

		this.data = Collections.unmodifiableList(VisualMappings.sanityCheckFilter(data, worldPositionMappingX, true));

		initializePositionEncodingFunctions();

		refreshDataPoints();
	}

	private void initializePositionEncodingFunctions() {
		List<Double> xValues = new ArrayList<>();
		List<Double> yValues = new ArrayList<>();

		for (T t : data) {
			xValues.add(worldPositionMappingX.apply(t).doubleValue());
			yValues.add(worldPositionMappingY.apply(t).doubleValue());
		}

		StatisticsSupport xStatistics = new StatisticsSupport(xValues);
		StatisticsSupport yStatistics = new StatisticsSupport(yValues);

		this.xPositionEncodingFunction = new PositionEncodingFunction(xStatistics.getMin(), xStatistics.getMax(), 0d,
				1d);
		this.xPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.yPositionEncodingFunction = new PositionEncodingFunction(yStatistics.getMin(), yStatistics.getMax(), 0d,
				1d, true);
		this.yPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
	}

	protected void refreshDataPoints() {
		refreshingDataPoints = true;
		screenPoints.clear();

		if (data == null || chartRectangle == null)
			return;

		for (int i = 0; i < data.size(); i++) {

			T t = data.get(i);
			double worldX = worldPositionMappingX.apply(t).doubleValue();
			double worldY = worldPositionMappingY.apply(t).doubleValue();
			double x = xPositionEncodingFunction.apply(worldX);
			double y = yPositionEncodingFunction.apply(worldY);
			Point2D point = new Point2D.Double(x, y);

			screenPoints.add(point);
		}

		if (overplottingMitigation)
			alpha = Math.max(0.05f, Math.min(1.0f, (screenPoints.size() / 5000.0f)));

		refreshingDataPoints = false;
	}

	@Override
	public void draw(Graphics2D g2) {

		if (chartRectangle == null)
			return;

		Color c = g2.getColor();

		// point size
		double pointSize = this.pointSize;

		for (int i = 0; i < data.size(); i++) {
			boolean selected = false;
			if (selectedFunction != null) {
				Boolean apply = selectedFunction.apply(data.get(i));
				if (apply != null)
					selected = apply.booleanValue();
			}

			if (drawSelectedLast && selected)
				continue;

			Point2D point = getScreenPoint(i);
			if (point == null || Double.isNaN(point.getX()) || Double.isNaN(point.getY()))
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
				boolean selected = false;
				if (selectedFunction != null) {
					Boolean apply = selectedFunction.apply(data.get(i));
					if (apply != null)
						selected = apply.booleanValue();
				}

				if (!selected)
					continue;

				Point2D point = getScreenPoint(i);
				if (point == null || Double.isNaN(point.getX()) || Double.isNaN(point.getY()))
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
	 * Creating screenPoints may take some time. In the past some exceptions have
	 * been thrown due to accessing the unfinished list of screenpoints. This method
	 * provides an access method that is more safe.
	 * 
	 * @param i index, according to data
	 * @return
	 */
	private Point2D getScreenPoint(int i) {
		while (refreshingDataPoints) {
			Threads.sleep(10);
		}
		try {
			return screenPoints.get(i);
		} catch (Exception e) {
			return null;
		}
	}

	protected void drawIndividualPoint(Graphics2D g2, Point2D point, float pointSize, Paint pointPaint,
			boolean selected) {

		float size = pointSize * 1.33f;
		if (selected) {
			double pointSizeBig = Math.max(pointSize * 1.66f, pointSize + 2);
			DisplayTools.drawPoint(g2, point.getX(), point.getY(), pointSizeBig, selectionPaint, true);
			DisplayTools.drawPoint(g2, point.getX(), point.getY(), size, pointPaint, true);
		} else
			DisplayTools.drawPoint(g2, point.getX(), point.getY(), size, pointPaint, true);
	}

	public static double calculatePointSize(double viewWidth, double viewHeight) {
		return Math.max(3, Math.min(viewWidth, viewHeight) * 0.006);
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
		if (!externalYPositionEncodingFunction)
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

		this.yPositionEncodingFunction.setMinPixel(rectangle.getMinY());
		this.yPositionEncodingFunction.setMaxPixel(rectangle.getMaxY());
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

		for (int i = 0; i < screenPoints.size(); i++) {
			T worldCord = data.get(i);
			double dX = Math.abs(screenPoints.get(i).getX() - px);
			if (dX < maxRadius) {
				double dY = Math.abs(screenPoints.get(i).getY() - py);
				if (dY < maxRadius)
					pointsInRange.add(new AbstractMap.SimpleEntry<Double, T>(dX + dY, worldCord));
			}
		}

		if (pointsInRange.size() == 0)
			return null;

		Entry<Double, T> first = Collections.min(pointsInRange, Entry.comparingByKey());
		T worldCord = first.getValue();

		String toolTipString = "";

		if (toolTipMapping != null) {
			toolTipString = toolTipMapping.apply(worldCord);
		} else {
			double worldX = worldPositionMappingX.apply(worldCord).doubleValue();
			double worldY = worldPositionMappingX.apply(worldCord).doubleValue();

			toolTipString = MathFunctions.round(worldX, 2) + ", " + MathFunctions.round(worldY, 2);
		}

		StringPainter stringPainter = new StringPainter(toolTipString);

		Rectangle2D rect = ToolTipTools.createToolTipRectangle(chartRectangle, p, 150, 32);
		stringPainter.setRectangle(rect);

		stringPainter.setBackgroundPaint(ColorTools.setAlpha(Color.DARK_GRAY, 0.5f));
		stringPainter.setFontColor(Color.WHITE);
		stringPainter.setFontSize(15);

		return stringPainter;
	}

	public boolean isAlphaAdjustment() {
		return overplottingMitigation;
	}

	public void setAlphaAdjustment(boolean dynamicAlphaAdjustment) {
		this.overplottingMitigation = dynamicAlphaAdjustment;

		refreshDataPoints();
	}

//	/**
//	 * @deprecated renamed. use isAlphaAdjustment instead
//	 * @return boolean
//	 */
//	public boolean isDynamicAlphaAdjustment() {
//		return overplottingMitigation;
//	}

//	/**
//	 * @deprecated renamed. use setAlphaAdjustment instead
//	 * @param dynamicAlphaAdjustment
//	 */
//	public void setDynamicAlphaAdjustment(boolean dynamicAlphaAdjustment) {
//		this.overplottingMitigation = dynamicAlphaAdjustment;
//
//		refreshDataPoints();
//	}

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
			double worldX = worldPositionMappingX.apply(d).doubleValue();
			double worldY = worldPositionMappingY.apply(d).doubleValue();

			double screenX = xPositionEncodingFunction.apply(worldX);
			double screenY = yPositionEncodingFunction.apply(worldY);
			if (shape.contains(screenX, screenY))
				elements.add(d);
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
			double worldX = worldPositionMappingX.apply(d);
			double worldY = worldPositionMappingY.apply(d);

			double screenX = xPositionEncodingFunction.apply(worldX);
			double screenY = yPositionEncodingFunction.apply(worldY);
			if (circle.contains(screenX, screenY))
				elements.add(d);
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

	@Override
	public void setYPositionEncodingFunction(IPositionEncodingFunction yPositionEncodingFunction) {
		this.yPositionEncodingFunction.removePositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.yPositionEncodingFunction = yPositionEncodingFunction;
		this.yPositionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.externalYPositionEncodingFunction = true;
	}

	public Function<? super T, String> getToolTipMapping() {
		return toolTipMapping;
	}

	public void setToolTipMapping(Function<? super T, String> toolTipMapping) {
		this.toolTipMapping = toolTipMapping;
	}

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

}
