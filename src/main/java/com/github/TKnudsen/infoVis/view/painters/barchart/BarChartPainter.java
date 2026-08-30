package com.github.TKnudsen.infoVis.view.painters.barchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collection;
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
 * Basic bar chart painter
 * </p>
 *
 * @version 2.11
 * @since 2016
 */
public abstract class BarChartPainter extends ChartPainter
		implements IClickSelection<Integer>, IRectangleSelection<Integer>, ISelectionVisualizer<Integer>, ITooltip {

	// external attributes
	protected List<? extends Number> data = null;
	protected List<Color> colors;
	private Number minValue = 0.0;

	// visuals
	private boolean fillBars = true;
	private boolean drawBarOutlines = true;
	private boolean toolTipping = true;
	private double gridSpacing = Double.NaN;
	private BasicStroke lineStroke = DisplayTools.standardStroke;
	private Paint selectionPaint = null;

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
		assignFilteredDataAndColors(DataConversion.doubleToList(data), DataConversion.arrayToList(colors));

		initializePositionEncodingFunction();

		initialize();
	}

	public BarChartPainter(Number[] data, Color[] colors) {
		assignFilteredDataAndColors(DataConversion.arrayToList(data), DataConversion.arrayToList(colors));

		initializePositionEncodingFunction();

		initialize();
	}

	public BarChartPainter(Collection<? extends Number> data) {
		List<Color> defaultColors = new ArrayList<>();
		for (int i = 0; i < data.size(); i++)
			defaultColors.add((Color) getPaint());

		assignFilteredDataAndColors(new ArrayList<>(data), defaultColors);

		initializePositionEncodingFunction();

		initialize();
	}

	public BarChartPainter(Collection<? extends Number> data, List<Color> colors) {
		assignFilteredDataAndColors(new ArrayList<>(data), new ArrayList<>(colors));

		initializePositionEncodingFunction();

		initialize();
	}

	/**
	 * Sanity check, mirroring VisualMappingTools.sanityCheckFilter's null/NaN
	 * policy (see also BoxPlotPainter). Not implemented via
	 * VisualMappingTools.sanityCheckFilter directly: unlike the other painters in
	 * this package, a bar chart's data IS the value (no separate
	 * Function&lt;T, Double&gt; mapping) and colors is a second list
	 * positionally tied to it index-for-index -- a plain sanityCheckFilter
	 * call on data alone would silently desynchronize colors from the bars
	 * they used to belong to for every dropped entry after the first.
	 */
	private void assignFilteredDataAndColors(List<? extends Number> rawData, List<Color> rawColors) {
		List<Number> filteredData = new ArrayList<>();
		List<Color> filteredColors = new ArrayList<>();

		for (int i = 0; i < rawData.size(); i++) {
			Number value = rawData.get(i);
			if (value != null && !Double.isNaN(value.doubleValue())) {
				filteredData.add(value);
				filteredColors.add(rawColors.get(i));
			} else
				System.err.println(
						"BarChartPainter: bar value " + value + " did not pass the sanity check and was ignored");
		}

		this.data = filteredData;
		this.colors = filteredColors;
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

		this.setBackgroundPaint(null);
		for (BarPainter barPainter : barPainters)
			barPainter.setBackgroundPaint(null);
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
					if (selectionPaint == null) {
						System.err.println("BarChartPainter: no selection paint defined, using border color");
						g2.setStroke(DisplayTools.thickStroke);
						g2.setPaint(getBorderPaint());
					} else
						g2.setPaint(getSelectionPaint());
					g2.draw(barPainter.getBarRectangle());
				}
			}
	}

	/**
	 * convenient method, for tool tips, etc.
	 * 
	 * @return
	 */
	public List<? extends Number> getData() {
		return Collections.unmodifiableList(data);
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
		setColors(DataConversion.arrayToList(colors));
	}

	public void setColors(List<Color> colors) {
		if (colors == null) {
			this.colors = new ArrayList<>();
			for (int i = 0; i < data.size(); i++)
				this.colors.add(null);
		}

		if (colors != null && barPainters != null && colors.size() != barPainters.size())
			throw new IllegalArgumentException("InfoVisBarChartPainter: set colors would cause indexing problems");

		this.colors = colors;

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

		if (barPainters != null)
			for (BarPainter barPainter : barPainters)
				barPainter.setDrawBarOutlines(drawBarOutlines);
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

	@Override
	public void setBorderPaint(Paint backgroundColor) {
		super.setBorderPaint(backgroundColor);

		if (barPainters != null)
			for (BarPainter barPainter : barPainters)
				barPainter.setBorderPaint(backgroundColor);
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

	public Number getMinValue() {
		return minValue;
	}

	public void setMinValue(Number minValue) {
		this.minValue = minValue;

		initialize();
	}

	public Paint getSelectionPaint() {
		return selectionPaint;
	}

	public void setSelectionPaint(Paint selectionPaint) {
		this.selectionPaint = selectionPaint;
	}

}
