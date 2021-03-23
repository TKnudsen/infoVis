package com.github.TKnudsen.infoVis.view.table;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;

public class TableCellChartPanel extends InfoVisChartPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5097618124579694999L;

	Rectangle2D rectOld;

	public TableCellChartPanel(ChartPainter chartPainter) {
		super(chartPainter);
	}

	@Override
	protected void drawChart(Graphics2D g2) {
		for (ChartPainter chartPainter : getChartPainters()) {
			chartPainter.setFont(this.getFont());

			// special line: adjust rectangle if different
			Rectangle2D rect = new Rectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1);

			if (rectOld == null || rectOld.equals(rect)) {
				chartPainter.setRectangle(rect);
				rectOld = rect;
			}

			chartPainter.draw(g2);
		}
	}

}