package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Paint;
import java.awt.geom.RectangularShape;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DHorizontalHighlightPainter;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.Distribution1DPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.XAxisNumericalChartPanel;

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
 * Copyright: (c) 2018-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.06
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

	public Distribution1DHorizontalPanel(Collection<T> values, Function<? super T, Double> worldToDoubleMapping) {
		this(values, worldToDoubleMapping, Double.NaN, Double.NaN);
	}

	public Distribution1DHorizontalPanel(Collection<T> data, Function<? super T, Double> worldToDoubleMapping,
			Double minGlobal, Double maxGlobal) {
		this(data, worldToDoubleMapping, null, minGlobal, maxGlobal);
	}

	public Distribution1DHorizontalPanel(Collection<T> data, Function<? super T, Double> worldToDoubleMapping,
			Function<? super T, ? extends Paint> colorEncodingFunction, Double minGlobal, Double maxGlobal) {
		this.minGlobal = minGlobal;
		this.maxGlobal = maxGlobal;

		initializeData(data, worldToDoubleMapping, colorEncodingFunction);
	}

	protected void initializeData(Collection<T> data, Function<? super T, Double> worldToDoubleMapping,
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

		initializeXAxisPainter(min, max);

		this.distribution1DHorizontalPainter = new Distribution1DHorizontalHighlightPainter<T>(data,
				worldToDoubleMapping, colorEncodingFunction);

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
