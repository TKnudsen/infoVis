package com.github.TKnudsen.infoVis.view.panels.grid;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DPainterPainter;
import com.github.TKnudsen.infoVis.view.painters.primitives.RectanglePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Panel that interactively manages a 2D data aggregation.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2020-2023 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class Grid2DAggregationPanel<T> extends InfoVisChartPanel implements IClickSelection<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * data elements, aggregated into a grid, e.g., by a controller using the
	 * ViewTransformation
	 */
	private List<T>[][] data;

	/**
	 * relative world values of the data elements
	 */
	private final Function<? super T, Number> worldRelativeValueMapping;

	/**
	 * maps aggregated values to colors
	 */
	private final Function<Number, Color> colorMapping;

	// internal data values
	private Grid2DPainterPainter<RectanglePainter> gridPainter;
	private Grid2DPaintersChartPanel<RectanglePainter> grid2dPaintersPanel;

	public Grid2DAggregationPanel(List<T>[][] data, Function<? super T, Number> worldRelativeValueMapping,
			Function<Number, Color> colorMapping) {
		Objects.requireNonNull(data);
		Objects.requireNonNull(worldRelativeValueMapping);
		Objects.requireNonNull(colorMapping);

		this.data = data;
		this.worldRelativeValueMapping = worldRelativeValueMapping;
		this.colorMapping = colorMapping;

		this.setLayout(new GridLayout(1, 1));

		initialize();
	}

	protected void initialize() {
		RectanglePainter[][] heatmap = new RectanglePainter[data.length][data[0].length];

		for (int x = 0; x < data.length; x++)
			for (int y = 0; y < data[0].length; y++) {
				List<Double> values = new ArrayList<>();

				for (T t : data[x][y]) {
					Number n = worldRelativeValueMapping.apply(t);
					if (n == null)
						throw new IllegalArgumentException(
								"Grid2DAggregationPanel: worldValueMapping contained null for " + t);

					double v = n.doubleValue();
					if (Double.isNaN(v))
						throw new IllegalArgumentException(
								"Grid2DAggregationPanel: worldValueMapping contained NaN for " + t);

					values.add(v);
				}

				StatisticsSupport statistics = new StatisticsSupport(values);
				Color color = values.isEmpty() ? null : colorMapping.apply(statistics.getMean());

				RectanglePainter painter = new RectanglePainter();
				painter.setPaint(color);
				painter.setBackgroundPaint(null);

				// invert y axis
				heatmap[x][data[0].length - 1 - y] = painter;
			}

		gridPainter = new Grid2DPainterPainter<>(heatmap);
		gridPainter.setBackgroundPaint(null);
		gridPainter.setDrawOutline(false);

		grid2dPaintersPanel = new Grid2DPaintersChartPanel<RectanglePainter>(gridPainter);

		this.add(grid2dPaintersPanel);
	}

	@Override
	public List<T> getElementsAtPoint(Point p) {
		int[] gridIndex = gridPainter.getGridIndex(p);
		return gridIndex == null ? null : data[gridIndex[0]][gridIndex[1]];
	}

}
