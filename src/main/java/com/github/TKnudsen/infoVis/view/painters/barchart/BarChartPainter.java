package com.github.TKnudsen.infoVis.view.painters.barchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.bar.BarPainter;
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
 * Basic bar chart painter
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.07
 */
public abstract class BarChartPainter extends ChartPainter
		implements IClickSelection<Integer>, IRectangleSelection<Integer>, ISelectionVisualizer<Integer>, ITooltip {

	// external attributes
	protected List<Double> data = null;
	protected List<Color> colors;
	private Double minValue = 0.0;

	// visuals
	private boolean fillBars = true;
	private boolean drawBarOutlines = true;
	private boolean toolTipping = true;
	private double gridSpacing = Double.NaN;
	private BasicStroke lineStroke = DisplayTools.standardStroke;

	// bar painters
	List<BarPainter> barPainters;

	private IPositionEncodingFunction positionEncodingFunction;
	protected boolean externalPositionEncodingFunction = false;

	// listening to the positionEncodingFunction
	private final PositionEncodingFunctionListener myPositionEncodingFunctionListener = () -> updatePositionEncoding(
			rectangle);

	// interaction
	private Function<? super Integer, Boolean> selectedFunction;

	public BarChartPainter(double[] data, Color[] colors) {
		this.data = DataConversion.doublePrimitivesToList(data);
		this.colors = DataConversion.arrayToList(colors);

		initializePositionEncodingFunction();

		initialize();
	}

	public BarChartPainter(Double[] data, Color[] colors) {
		this.data = DataConversion.arrayToList(data);
		this.colors = DataConversion.arrayToList(colors);

		initializePositionEncodingFunction();

		initialize();
	}

	public BarChartPainter(List<Double> data) {
		this.data = Collections.unmodifiableList(data);

		this.colors = new ArrayList<>();
		for (int i = 0; i < data.size(); i++)
			colors.add(getColor());

		initializePositionEncodingFunction();

		initialize();
	}

	public BarChartPainter(List<Double> data, List<Color> colors) {
		this.data = Collections.unmodifiableList(data);
		this.colors = Collections.unmodifiableList(colors);

		initializePositionEncodingFunction();

		initialize();
	}

	protected void initializePositionEncodingFunction() {
		StatisticsSupport dataStatistics = new StatisticsSupport(data);

		this.positionEncodingFunction = new PositionEncodingFunction(0, dataStatistics.getMax(), 0d, 1d,
				isInvertedAxis());

		this.positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);
	}

	protected abstract boolean isInvertedAxis();

	protected void initialize() {
		if (data != null && colors != null && this.data.size() != colors.size())
			throw new ArrayIndexOutOfBoundsException("Data and Colors of unequal length!");

		initializeBarPainters();
	}

	protected abstract void initializeBarPainters();

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (!externalPositionEncodingFunction)
			updatePositionEncoding(rectangle);
	}

	protected abstract void updatePositionEncoding(Rectangle2D rectangle);

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (barPainters != null)
			for (int i = 0; i < barPainters.size(); i++) {
				BarPainter barPainter = barPainters.get(i);

				barPainter.draw(g2);

				boolean selected = false;
				if (selectedFunction != null && selectedFunction.apply(i))
					selected = true;

				if (selected) {
					g2.setStroke(DisplayTools.thickStroke);
					g2.setPaint(getBorderPaint());
					g2.draw(barPainter.getBarRectangle());
				}
			}
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		for (BarPainter painter : barPainters) {
			ChartPainter tooltip = painter.getTooltip(p);
			if (tooltip != null)
				return tooltip;
		}

		return null;
	}

	@Override
	public List<Integer> getElementsAtPoint(Point p) {
		if (p == null)
			return null;

		List<Integer> indices = new ArrayList<>();

		if (barPainters != null)
			for (int i = 0; i < barPainters.size(); i++) {
				BarPainter barPainter = barPainters.get(i);
				if (barPainter.getBarRectangle().contains(p))
					indices.add(new Integer(i));
			}

		return indices;
	}

	@Override
	public List<Integer> getElementsInRectangle(RectangularShape rectangle) {
		if (rectangle == null)
			return null;

		List<Integer> indices = new ArrayList<>();

		if (barPainters != null)
			for (int i = 0; i < barPainters.size(); i++) {
				BarPainter barPainter = barPainters.get(i);
				if (rectangle.intersects(barPainter.getBarRectangle()))
					indices.add(new Integer(i));
			}

		return indices;
	}

	@Override
	public boolean isToolTipping() {
		return toolTipping;
	}

	@Override
	public void setToolTipping(boolean toolTipping) {
		this.toolTipping = toolTipping;
	}

	public BasicStroke getLineStroke() {
		return lineStroke;
	}

	public void setLineStroke(BasicStroke lineStroke) {
		this.lineStroke = lineStroke;

		if (barPainters != null)
			for (BarPainter barPainter : barPainters)
				barPainter.setLineStroke(lineStroke);
	}

	public List<Color> getColors() {
		return colors;
	}

	public void setColors(Color[] colors) {
		if (colors != null && barPainters != null && colors.length != barPainters.size())
			throw new IllegalArgumentException("InfoVisBarChartPainter: set colors would cause indexing problems");

		this.colors = DataConversion.arrayToList(colors);

		if (barPainters != null)
			for (int i = 0; i < barPainters.size(); i++)
				barPainters.get(i).setPaint(this.colors.get(i));
	}

	@Override
	public void setColor(Color color) {
		super.setPaint(color);

		if (barPainters != null)
			for (BarPainter barPainter : barPainters)
				barPainter.setPaint(color);
	}

	@Override
	public void setPaint(Paint paint) {
		super.setPaint(paint);

		if (barPainters != null)
			for (BarPainter barPainter : barPainters)
				barPainter.setPaint(paint);
	}

	public boolean isDrawBarOutlines() {
		return drawBarOutlines;
	}

	public void setDrawBarOutlines(boolean drawBarOutlines) {
		this.drawBarOutlines = drawBarOutlines;
	}

	public boolean isFillBars() {
		return fillBars;
	}

	public void setFillBars(boolean fillBars) {
		this.fillBars = fillBars;
	}

	public double getGridSpacing() {
		return gridSpacing;
	}

	public void setGridSpacing(double gridSpacing) {
		this.gridSpacing = gridSpacing;
	}

	@Override
	public void setSelectedFunction(Function<? super Integer, Boolean> selectedFunction) {
		this.selectedFunction = selectedFunction;
	}

	@Override
	public void setBackgroundPaint(Paint backgroundColor) {
		super.setBackgroundPaint(backgroundColor);

		if (barPainters != null)
			for (BarPainter barPainter : barPainters)
				barPainter.setBackgroundPaint(backgroundColor);
	}

	public IPositionEncodingFunction getPositionEncodingFunction() {
		return positionEncodingFunction;
	}

	public void setPositionEncodingFunction(IPositionEncodingFunction positionEncodingFunction) {
		this.positionEncodingFunction.removePositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.positionEncodingFunction = positionEncodingFunction;
		this.positionEncodingFunction.addPositionEncodingFunctionListener(myPositionEncodingFunctionListener);

		this.externalPositionEncodingFunction = true;

		if (barPainters != null)
			for (BarPainter barPainter : barPainters)
				barPainter.setPositionEncodingFunction(positionEncodingFunction);
	}

	public Double getMinValue() {
		return minValue;
	}

	public void setMinValue(Double minValue) {
		this.minValue = minValue;

		initialize();
	}

}
