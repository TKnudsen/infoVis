package com.github.TKnudsen.infoVis.view.panels.grid;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DPainterPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.tools.ComponentTools;

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
 * @version 2.01
 */
public class Grid2DPaintersChartPanel<P extends ChartPainter> extends InfoVisChartPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7833259569761438713L;

	private final Grid2DPainterPainter<P> painterGrid;

	public Grid2DPaintersChartPanel(Grid2DPainterPainter<P> painterGrid) {
		this.painterGrid = painterGrid;
	}

	@Override
	/**
	 * uses the rectangle information provided with the layout and assigns it to the
	 * painters
	 */
	protected void updatePainterRectangles() {
		super.updatePainterRectangles();

		Rectangle2D rectangle = ComponentTools.getCompontentDrawableRectangle(this);

		if (rectangle == null)
			return;

		if (painterGrid != null)
			painterGrid.setRectangle(rectangle);
	}

	@Override
	protected void drawChart(Graphics2D g2) {
		super.drawChart(g2);

		if (painterGrid != null)
			painterGrid.draw(g2);
	}

}
