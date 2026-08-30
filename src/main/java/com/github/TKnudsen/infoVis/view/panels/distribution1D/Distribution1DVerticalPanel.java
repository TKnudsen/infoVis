package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Paint;
import java.util.Collection;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRange;
import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRangeTools;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DPainter;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DVerticalHighlightPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.YAxisNumericalChartPanel;

/**
 * <p>
 * Vertical distribution chart with distribution painter inside.
 * </p>
 *
 * @version 2.08
 * @since 2018
 */
public class Distribution1DVerticalPanel<T> extends YAxisNumericalChartPanel<Double> implements Distribution1DPanel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1961946031158322006L;

	private Distribution1DVerticalHighlightPainter<T> distribution1DVerticalPainter;

	// constructor attributes
	private double minGlobal = Double.NaN;
	private double maxGlobal = Double.NaN;

	public Distribution1DVerticalPanel(Collection<T> values,
			Function<? super T, ? extends Number> worldToDoubleMapping) {
		this(values, worldToDoubleMapping, Double.NaN, Double.NaN);
	}

	public Distribution1DVerticalPanel(Collection<T> data, Function<? super T, ? extends Number> worldToDoubleMapping,
			double minGlobal, double maxGlobal) {
		this(data, worldToDoubleMapping, null, minGlobal, maxGlobal);
	}

	public Distribution1DVerticalPanel(Collection<T> data, Function<? super T, ? extends Number> worldToDoubleMapping,
			Function<? super T, ? extends Paint> colorEncodingFunction, double minGlobal, double maxGlobal) {
		this.minGlobal = minGlobal;
		this.maxGlobal = maxGlobal;

		initializeData(data, worldToDoubleMapping, colorEncodingFunction);
	}

	protected void initializeData(Collection<T> data, Function<? super T, ? extends Number> worldPositionMappingY,
			Function<? super T, ? extends Paint> colorEncodingFunction) {

		if (data == null)
			if (Double.isNaN(minGlobal) || Double.isNaN(maxGlobal))
				throw new IllegalArgumentException("InfoVisBoxPlotVerticalPanel: no valid input given");

		NumericRange rangeY = NumericRangeTools.computeFiniteRangeStrict(data, worldPositionMappingY, null, null);

		initializeYAxisPainter(rangeY.getMin(), rangeY.getMax());

		this.distribution1DVerticalPainter = new Distribution1DVerticalHighlightPainter<T>(data, worldPositionMappingY,
				colorEncodingFunction);
		this.distribution1DVerticalPainter.setBackgroundPaint(null);

		this.addChartPainter(distribution1DVerticalPainter, true);
	}

	@Override
	public void initializeYAxisPainter(Double min, Double max) {
		setYAxisPainter(new YAxisNumericalPainter<Double>(min, max));
	}

	@Override
	public Distribution1DPainter<T> getDistribution1DPainter() {
		return distribution1DVerticalPainter;
	}
}
