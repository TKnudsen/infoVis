package com.github.TKnudsen.infoVis.view.panels.distribution1D;

import java.awt.Color;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.distribution1D.KernelDensityAreaPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.XYNumericalChartPanel;

/**
 * A {@link XYNumericalChartPanel} hosting a single {@link
 * KernelDensityAreaPainter} -- x is the sample domain, y is the estimated
 * density. Purely numerical, no time semantics: build the (evaluationPoints,
 * density) pair with a density estimator (e.g. {@code
 * GaussianKernelDensityEstimator} in DMandML) and pass the result in.
 *
 * @since 2026
 */
public class KernelDensityChart extends XYNumericalChartPanel<Number, Number> {

	private static final long serialVersionUID = 1L;

	private final KernelDensityAreaPainter painter;

	public KernelDensityChart(List<Double> evaluationPoints, List<Double> density, Color fillColor) {
		super();

		painter = new KernelDensityAreaPainter(evaluationPoints, density, fillColor);

		initializeXAxisPainter(MathFunctions.getMin(evaluationPoints), MathFunctions.getMax(evaluationPoints));
		initializeYAxisPainter(0.0, MathFunctions.getMax(density));

		addChartPainter(painter, true, true);
	}

	@Override
	protected void initializeXAxisPainter(Number min, Number max) {
		xAxisPainter = new XAxisNumericalPainter<>(min, max);
	}

	@Override
	protected void initializeYAxisPainter(Number min, Number max) {
		yAxisPainter = new YAxisNumericalPainter<>(min, max);
	}

	public KernelDensityAreaPainter getPainter() {
		return painter;
	}

}
