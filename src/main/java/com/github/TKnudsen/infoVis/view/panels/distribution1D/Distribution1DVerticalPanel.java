package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Paint;
import java.awt.geom.RectangularShape;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DVerticalHighlightPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.YAxisNumericalChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.ShapeAttributes;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Vertical distribution chart with distribution painter inside.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.06
 */
public class Distribution1DVerticalPanel<T> extends YAxisNumericalChartPanel<Double>
		implements IColorEncoding<T>, IRectangleSelection<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1961946031158322006L;

	private Distribution1DVerticalHighlightPainter<T> distribution1DVerticalPainter;

	// constructor attributes
	private double minGlobal = Double.NaN;
	private double maxGlobal = Double.NaN;
	
	public Distribution1DVerticalPanel(List<T> values, Function<? super T, Double> worldToDoubleMapping) {
		this(values, worldToDoubleMapping, Double.NaN, Double.NaN);
	}

	public Distribution1DVerticalPanel(List<T> data, Function<? super T, Double> worldToDoubleMapping, Double minGlobal,
			Double maxGlobal) {
		this(data, worldToDoubleMapping, null, minGlobal, maxGlobal);
	}

	public Distribution1DVerticalPanel(List<T> data, Function<? super T, Double> worldToDoubleMapping,
			Function<? super T, ? extends Paint> colorEncodingFunction, Double minGlobal, Double maxGlobal) {
		this.minGlobal = minGlobal;
		this.maxGlobal = maxGlobal;

		initializeData(data, worldToDoubleMapping, colorEncodingFunction);
	}

	protected void initializeData(List<T> data, Function<? super T, Double> worldToDoubleMapping,
			Function<? super T, ? extends Paint> colorEncodingFunction) {

		if (data == null)
			if (Double.isNaN(minGlobal) || Double.isNaN(maxGlobal))
				throw new IllegalArgumentException("InfoVisBoxPlotVerticalPanel: no valid input given");

		double min = Double.POSITIVE_INFINITY;
		if (!Double.isNaN(minGlobal))
			min = minGlobal;
		else
			for (T t : data)
				min = Math.min(min, worldToDoubleMapping.apply(t));

		double max = Double.NEGATIVE_INFINITY;
		if (!Double.isNaN(maxGlobal))
			max = maxGlobal;
		else
			for (T t : data)
				max = Math.max(max, worldToDoubleMapping.apply(t));

		initializeYAxisPainter(min, max);

		this.distribution1DVerticalPainter = new Distribution1DVerticalHighlightPainter<T>(data, worldToDoubleMapping,
				colorEncodingFunction);

		this.addChartPainter(distribution1DVerticalPainter, true);
	}

	@Override
	public void initializeYAxisPainter(Double min, Double max) {
		setYAxisPainter(new YAxisNumericalPainter<Double>(min, max));
	}

	public void clearSpecialValues() {
		distribution1DVerticalPainter.clearSpecialValues();
	}

	public void addSpecialValue(T worldValue, ShapeAttributes shapeAttributes) {
		distribution1DVerticalPainter.addSpecialValue(worldValue, shapeAttributes);
	}

	public Function<? super T, ? extends Paint> getColorEncodingFunction() {
		return distribution1DVerticalPainter.getColorEncodingFunction();
	}

	@Override
	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction) {
		this.distribution1DVerticalPainter.setColorEncodingFunction(colorEncodingFunction);
	}

	@Override
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		return distribution1DVerticalPainter.getElementsInRectangle(rectangle);
	}

	public float getAlpha() {
		return distribution1DVerticalPainter.getAlpha();
	}

	public void setAlpha(float alpha) {
		this.distribution1DVerticalPainter.setAlpha(alpha);
	}

	public void setTriangleSize(double triangleSize) {
		this.distribution1DVerticalPainter.setTriangleSize(triangleSize);
	}

	public boolean isToolTipping() {
		return this.distribution1DVerticalPainter.isToolTipping();
	}

	public void setToolTipping(boolean toolTipping) {
		this.distribution1DVerticalPainter.setToolTipping(toolTipping);
	}
	
}
