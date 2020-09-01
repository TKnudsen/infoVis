package com.github.TKnudsen.infoVis.view.panels.boxplot;

import java.awt.Color;
import java.awt.geom.RectangularShape;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.boxplot.BoxPlotHorizontalPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.XAxisNumericalChartPanel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Horizontal boxplot chart
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2020 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.05
 */
public class BoxPlotHorizontalCartPanel extends XAxisNumericalChartPanel<Double>
		implements IRectangleSelection<Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3145084853852206010L;

	private BoxPlotHorizontalPainter boxPlotPainterHorizontal;

	// constructor attributes
	private StatisticsSupport dataStatistics;
	private double minGlobal = Double.NaN;
	private double maxGlobal = Double.NaN;

	public BoxPlotHorizontalCartPanel(double[] data) {
		this.dataStatistics = new StatisticsSupport(data);

		initializeData();
	}

	public BoxPlotHorizontalCartPanel(List<Double> data) {
		this.dataStatistics = new StatisticsSupport(data);

		initializeData();
	}

	public BoxPlotHorizontalCartPanel(List<Double> data, double minGlobal, double maxGlobal) {
		this.dataStatistics = new StatisticsSupport(data);
		this.minGlobal = minGlobal;
		this.maxGlobal = maxGlobal;

		initializeData();
	}

	public BoxPlotHorizontalCartPanel(StatisticsSupport dataStatistics) {
		this.dataStatistics = dataStatistics;

		initializeData();
	}

	public BoxPlotHorizontalCartPanel(StatisticsSupport dataStatistics, double minGlobal, double maxGlobal) {
		this.dataStatistics = dataStatistics;
		this.minGlobal = minGlobal;
		this.maxGlobal = maxGlobal;

		initializeData();
	}

	protected void initializeData() {
		if (dataStatistics == null)
			if (Double.isNaN(minGlobal) || Double.isNaN(maxGlobal))
				throw new IllegalArgumentException("InfoVisBoxPlotHorizontalPanel: no valid input given");

		double min = dataStatistics.getMin();
		if (!Double.isNaN(minGlobal))
			min = minGlobal;

		double max = dataStatistics.getMax();
		if (!Double.isNaN(maxGlobal))
			max = maxGlobal;

		initializeXAxisPainter(min, max);

		this.boxPlotPainterHorizontal = new BoxPlotHorizontalPainter(dataStatistics);

		this.addChartPainter(boxPlotPainterHorizontal, true);
	}

	@Override
	public void initializeXAxisPainter(Double min, Double max) {
		setXAxisPainter(new XAxisNumericalPainter<Double>(min, max));
	}

	@Override
	public List<Double> getElementsInRectangle(RectangularShape rectangle) {
		return boxPlotPainterHorizontal.getElementsInRectangle(rectangle);
	}

	public void setColor(Color color) {
		if (boxPlotPainterHorizontal != null)
			boxPlotPainterHorizontal.setPaint(color);
	}

//	public void setFlipAxisValues(boolean flip) {
//		this.getXPositionEncodingFunction().setFlipAxisValues(flip);
//	}
}
