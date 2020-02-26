package com.github.TKnudsen.infoVis.view.panels.grid;

import java.util.Collection;
import java.util.Iterator;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DPainterPainter;

public class Grid2DPaintersChartPanels {
	public static <T extends ChartPainter> Grid2DPaintersChartPanel<T> create(Collection<T> chartPainters) {

		// heuristics to guess grid sizes x and y
		int x = (int) Math.ceil(Math.sqrt(chartPainters.size()));
		int y = (int) Math.floor(Math.sqrt(chartPainters.size()));
		if (Math.sqrt(chartPainters.size()) - (int) Math.floor(Math.sqrt(chartPainters.size())) > 0.5)
			y++;

		x = Math.max(x, 1);
		y = Math.max(y, 1);

		@SuppressWarnings("unchecked")
		T[][] painters = (T[][]) new ChartPainter[x][y];

		int x_ = 0;
		int y_ = 0;
		Iterator<T> iterator = chartPainters.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			painters[x_][y_++] = iterator.next();
			i++;
			if (i % x == 0)
				x_++;
			y_ = y_ % y;
		}

		Grid2DPainterPainter<T> grid2dPainterPainter = new Grid2DPainterPainter<>(painters);

		Grid2DPaintersChartPanel<T> grid = new Grid2DPaintersChartPanel<T>(grid2dPainterPainter);

		return grid;
	}
}
