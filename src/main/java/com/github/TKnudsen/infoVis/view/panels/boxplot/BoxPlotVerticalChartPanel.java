package com.github.TKnudsen.infoVis.view.panels.boxplot;

import java.awt.Color;
import java.awt.geom.RectangularShape;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.boxplot.BoxPlotVerticalPainter;
import com.github.TKnudsen.infoVis.view.panels.axis.YAxisNumericalChartPanel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.03
 */
public class BoxPlotVerticalChartPanel extends YAxisNumericalChartPanel<Double> implements IRectangleSelection<Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1961946031158322006L;

	private BoxPlotVerticalPainter boxPlotPainterVertical;

	// constructor attributes
	private StatisticsSupport dataStatistics;
	private double minGlobal = Double.NaN;
	private double maxGlobal = Double.NaN;

	public BoxPlotVerticalChartPanel(double[] data) {
		this.dataStatistics = new StatisticsSupport(data);

		initializeData();
	}

	public BoxPlotVerticalChartPanel(List<Double> data) {
		this.dataStatistics = new StatisticsSupport(data);

		initializeData();
	}

	public BoxPlotVerticalChartPanel(List<Double> data, double minGlobal, double maxGlobal) {
		this.dataStatistics = new StatisticsSupport(data);
		this.minGlobal = minGlobal;
		this.maxGlobal = maxGlobal;

		initializeData();
	}

	public BoxPlotVerticalChartPanel(StatisticsSupport dataStatistics) {
		this.dataStatistics = dataStatistics;

		initializeData();
	}

	public BoxPlotVerticalChartPanel(StatisticsSupport dataStatistics, double minGlobal, double maxGlobal) {
		this.dataStatistics = dataStatistics;
		this.minGlobal = minGlobal;
		this.maxGlobal = maxGlobal;

		initializeData();
	}

	protected void initializeData() {
		if (dataStatistics == null)
			if (Double.isNaN(minGlobal) || Double.isNaN(maxGlobal))
				throw new IllegalArgumentException("InfoVisBoxPlotVerticalPanel: no valid input given");

		double min = dataStatistics.getMin();
		if (!Double.isNaN(minGlobal))
			min = minGlobal;

		double max = dataStatistics.getMax();
		if (!Double.isNaN(maxGlobal))
			max = maxGlobal;

		initializeYAxisPainter(min, max);

		this.boxPlotPainterVertical = new BoxPlotVerticalPainter(dataStatistics);

		this.addChartPainter(boxPlotPainterVertical, true);
	}

	@Override
	public void initializeYAxisPainter(Double min, Double max) {
		setYAxisPainter(new YAxisNumericalPainter<Double>(min, max));
	}

	@Override
	public List<Double> getElementsInRectangle(RectangularShape rectangle) {
		return boxPlotPainterVertical.getElementsInRectangle(rectangle);
	}

	public void setColor(Color color) {
		if (boxPlotPainterVertical != null)
			boxPlotPainterVertical.setColor(color);
	}

//	public void setFlipAxisValues(boolean flip) {
//		this.getYPositionEncodingFunction().setFlipAxisValues(flip);
//	}

}
