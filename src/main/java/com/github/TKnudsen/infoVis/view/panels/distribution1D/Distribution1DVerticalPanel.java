package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Paint;
import java.awt.geom.RectangularShape;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
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
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.05
 */
public class Distribution1DVerticalPanel extends YAxisNumericalChartPanel<Double>
		implements IColorEncoding<Double>, IRectangleSelection<Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1961946031158322006L;

	private Distribution1DVerticalHighlightPainter distribution1DVerticalPainter;

	// constructor attributes
	private double minGlobal = Double.NaN;
	private double maxGlobal = Double.NaN;

	public Distribution1DVerticalPanel(Collection<Double> values) {
		this(values, Double.NaN, Double.NaN);
	}

	public Distribution1DVerticalPanel(Collection<Double> values, Double minGlobal, Double maxGlobal) {
		this.minGlobal = minGlobal;
		this.maxGlobal = maxGlobal;

		initializeData(values);
	}

	protected void initializeData(Collection<Double> values) {
		if (values == null)
			if (Double.isNaN(minGlobal) || Double.isNaN(maxGlobal))
				throw new IllegalArgumentException("InfoVisBoxPlotVerticalPanel: no valid input given");

		double min = Double.POSITIVE_INFINITY;
		if (!Double.isNaN(minGlobal))
			min = minGlobal;
		else
			min = MathFunctions.getMin(values);

		double max = Double.NEGATIVE_INFINITY;
		if (!Double.isNaN(maxGlobal))
			max = maxGlobal;
		else
			max = MathFunctions.getMax(values);

		initializeYAxisPainter(min, max);

		this.distribution1DVerticalPainter = new Distribution1DVerticalHighlightPainter(values);

		this.addChartPainter(distribution1DVerticalPainter, true);
	}

	@Override
	public void initializeYAxisPainter(Double min, Double max) {
		setYAxisPainter(new YAxisNumericalPainter<Double>(min, max));
	}

	public void clearSpecialValues() {
		distribution1DVerticalPainter.clearSpecialValues();
	}

	public void addSpecialValue(Double worldValue, ShapeAttributes shapeAttributes) {
		distribution1DVerticalPainter.addSpecialValue(worldValue, shapeAttributes);
	}

	public Function<? super Double, ? extends Paint> getColorEncodingFunction() {
		return distribution1DVerticalPainter.getColorEncodingFunction();
	}

	@Override
	public void setColorEncodingFunction(Function<? super Double, ? extends Paint> colorEncodingFunction) {
		this.distribution1DVerticalPainter.setColorEncodingFunction(colorEncodingFunction);
	}

	@Override
	public List<Double> getElementsInRectangle(RectangularShape rectangle) {
		return distribution1DVerticalPainter.getElementsInRectangle(rectangle);
	}
}
