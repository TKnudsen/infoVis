package com.github.TKnudsen.infoVis.view.panels.trajectory;

import java.awt.Paint;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.painters.trajectory.TrajectoryPainter;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlot;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.SizeEncodingFunction;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Chart panel for trajectory data mapped to numerical x and y axes.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class TrajectoryChartPanel<T> extends ScatterPlot<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5657437557932197054L;

	public TrajectoryChartPanel(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		super(data, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

	@Override
	protected void initializePainter() {
		this.scatterPlotPainter = new TrajectoryPainter<T>(getData(), this.getColorMapping(),
				getWorldPositionMappingX(), getWorldPositionMappingY());

		this.scatterPlotPainter.setSizeEncodingFunction(new SizeEncodingFunction<>(this));
	}
}
