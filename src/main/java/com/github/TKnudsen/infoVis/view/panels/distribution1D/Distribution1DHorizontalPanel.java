package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Paint;
import java.awt.geom.RectangularShape;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DHorizontalHighlightPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.XAxisNumericalChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.ShapeAttributes;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Horizontal distribution chart with distribution painter inside.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.04
 */
public class Distribution1DHorizontalPanel extends XAxisNumericalChartPanel<Double>
		implements IColorEncoding<Double>, IRectangleSelection<Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1961946031158322006L;

	private Distribution1DHorizontalHighlightPainter distribution1DHorizontalPainter;

	private double minGlobal = Double.NaN;
	private double maxGlobal = Double.NaN;

	public Distribution1DHorizontalPanel(Collection<Double> values) {
		this(values, Double.NaN, Double.NaN);
	}

	public Distribution1DHorizontalPanel(Collection<Double> values, Double minGlobal, Double maxGlobal) {
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

		initializeXAxisPainter(min, max);

		this.distribution1DHorizontalPainter = new Distribution1DHorizontalHighlightPainter(values);

		this.addChartPainter(distribution1DHorizontalPainter, true);
	}

	@Override
	public void initializeXAxisPainter(Double min, Double max) {
		setXAxisPainter(new XAxisNumericalPainter<Double>(min, max));
	}

	public void clearSpecialValues() {
		distribution1DHorizontalPainter.clearSpecialValues();
	}

	public void addSpecialValue(Double worldValue, ShapeAttributes shapeAttributes) {
		distribution1DHorizontalPainter.addSpecialValue(worldValue, shapeAttributes);
	}

	public Function<? super Double, ? extends Paint> getColorEncodingFunction() {
		return distribution1DHorizontalPainter.getColorEncodingFunction();
	}

	@Override
	public void setColorEncodingFunction(Function<? super Double, ? extends Paint> colorEncodingFunction) {
		this.distribution1DHorizontalPainter.setColorEncodingFunction(colorEncodingFunction);
	}

	@Override
	public List<Double> getElementsInRectangle(RectangularShape rectangle) {
		return distribution1DHorizontalPainter.getElementsInRectangle(rectangle);
	}

	public boolean isHighlightsAtTheUpperBound() {
		return distribution1DHorizontalPainter.isHighlightsAtTheUpperBound();
	}

	public void setHighlightsAtTheUpperBound(boolean highlightsAtTheUpperBound) {
		this.distribution1DHorizontalPainter.setHighlightsAtTheUpperBound(highlightsAtTheUpperBound);
	}

}
