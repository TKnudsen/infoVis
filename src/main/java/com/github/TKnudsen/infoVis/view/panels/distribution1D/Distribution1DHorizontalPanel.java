package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Paint;
import java.util.Collection;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRange;
import com.github.TKnudsen.ComplexDataObject.model.tools.NumericRangeTools;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DHorizontalHighlightPainter;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.XAxisNumericalChartPanel;

/**
 * <p>
 * Horizontal distribution chart with distribution painter inside.
 * </p>
 *
 * @version 2.07
 * @since 2018
 */
public class Distribution1DHorizontalPanel<T> extends XAxisNumericalChartPanel<Double>
		implements Distribution1DPanel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1961946031158322006L;

	private Distribution1DHorizontalHighlightPainter<T> distribution1DHorizontalPainter;

	private double minGlobal = Double.NaN;
	private double maxGlobal = Double.NaN;

	public Distribution1DHorizontalPanel(Collection<T> values,
			Function<? super T, ? extends Number> worldToDoubleMapping) {
		this(values, worldToDoubleMapping, Double.NaN, Double.NaN);
	}

	public Distribution1DHorizontalPanel(Collection<T> data, Function<? super T, ? extends Number> worldToDoubleMapping,
			double minGlobal, double maxGlobal) {
		this(data, worldToDoubleMapping, null, minGlobal, maxGlobal);
	}

	public Distribution1DHorizontalPanel(Collection<T> data, Function<? super T, ? extends Number> worldToDoubleMapping,
			Function<? super T, ? extends Paint> colorEncodingFunction, double minGlobal, double maxGlobal) {
		this.minGlobal = minGlobal;
		this.maxGlobal = maxGlobal;

		initializeData(data, worldToDoubleMapping, colorEncodingFunction);
	}

	protected void initializeData(Collection<T> data, Function<? super T, ? extends Number> worldPositionMappingX,
			Function<? super T, ? extends Paint> colorEncodingFunction) {

		if (data == null)
			if (Double.isNaN(minGlobal) || Double.isNaN(maxGlobal))
				throw new IllegalArgumentException("Distribution1DHorizontalPanel: no valid input given");

		NumericRange rangeX = NumericRangeTools.computeFiniteRangeStrict(data, worldPositionMappingX, null, null);
		
		initializeXAxisPainter(rangeX.getMin(), rangeX.getMax());

		this.distribution1DHorizontalPainter = new Distribution1DHorizontalHighlightPainter<T>(data,
				worldPositionMappingX, colorEncodingFunction);

		this.addChartPainter(distribution1DHorizontalPainter, true);
	}

	@Override
	public void initializeXAxisPainter(Double min, Double max) {
		setXAxisPainter(new XAxisNumericalPainter<Double>(min, max));
	}

	public boolean isHighlightsAtTheUpperBound() {
		return distribution1DHorizontalPainter.isHighlightsAtTheUpperBound();
	}

	public void setHighlightsAtTheUpperBound(boolean highlightsAtTheUpperBound) {
		this.distribution1DHorizontalPainter.setHighlightsAtTheUpperBound(highlightsAtTheUpperBound);
	}

	@Override
	public Distribution1DPainter<T> getDistribution1DPainter() {
		return distribution1DHorizontalPainter;
	}

}
