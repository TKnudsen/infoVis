package com.github.TKnudsen.infoVis.view.panels.trajectory;

import java.awt.Paint;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.painters.trajectory.TrajectoryPainter;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.ScatterPlot;
import com.github.TKnudsen.infoVis.view.visualChannels.size.impl.SizeEncodingFunction;

/**
 * <p>
 * Chart panel for trajectory data mapped to numerical x and y axes.
 * </p>
 *
 * @version 1.02
 * @since 2019
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

	/**
	 * Installs a {@link TrajectoryPainter} instead of the default
	 * {@link com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainter
	 * ScatterPlotPainter}; deliberately does not call {@code super}, per the
	 * override contract documented on {@link ScatterPlot#initializePainter(List)}.
	 */
	@Override
	protected void initializePainter(List<T> data) {
		this.scatterPlotPainter = new TrajectoryPainter<T>(data, this.getColorMapping(), getWorldPositionMappingX(),
				getWorldPositionMappingY());

		this.scatterPlotPainter.setSizeEncodingFunction(new SizeEncodingFunction<>(this));
	}
}
