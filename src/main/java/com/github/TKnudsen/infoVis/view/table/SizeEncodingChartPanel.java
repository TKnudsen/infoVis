package com.github.TKnudsen.infoVis.view.table;

import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ComponentTools;

class SizeEncodingChartPanel extends TableCellChartPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3050718018043200316L;

	private double relativeSize;
	private ChartPainter painter;

	SizeEncodingChartPanel(ChartPainter r, double relativeSize) {
		super(r);

		this.painter = r;
		this.relativeSize = relativeSize;
	}

	protected void initializeComponentListener() {
		addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentResized(ComponentEvent e) {
				if (painter != null) {
					Rectangle2D rectanglePainterRectangle = calculateRectanglePainterRectangle(painter.isDrawOutline());
					SizeEncodingChartPanel.this.painter.setRectangle(rectanglePainterRectangle);
				}
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
	}
	
	@Override
	protected void drawChart(Graphics2D g2) {
		for (ChartPainter chartPainter : getChartPainters()) {
			chartPainter.setFont(this.getFont());

			// special line: adjust rectangle if different
			Rectangle2D rect = calculateRectanglePainterRectangle(painter.isDrawOutline());

			if (rectOld == null || rectOld.equals(rect)) {
				chartPainter.setRectangle(rect);
				rectOld = rect;
			}

			chartPainter.draw(g2);
		}
	}

	protected void drawPainters(Graphics2D g) {
		if (painter != null) {
			Rectangle2D rectanglePainterRectangle = calculateRectanglePainterRectangle(painter.isDrawOutline());
			painter.setRectangle(rectanglePainterRectangle);
			painter.draw((Graphics2D) g);
		}
	}

	/**
	 * calculates the size of the actual rectanglePainter rectangle
	 * 
	 * @return
	 */
	private Rectangle2D calculateRectanglePainterRectangle(boolean excludeBorderPixel) {
		Rectangle2D rect = ComponentTools.getCompontentDrawableRectangle(this);

		double area = rect.getWidth() * rect.getHeight() * relativeSize;
		if (excludeBorderPixel)
			area = Math.max(0.0, rect.getWidth() - 1) * Math.max(0.0, rect.getHeight() - 1) * relativeSize;
		double w_ = Math.sqrt(area * (rect.getWidth() / rect.getHeight()));
		double h_ = Math.sqrt(area * (rect.getHeight() / rect.getWidth()));

		Rectangle2D scaled = new Rectangle2D.Double(rect.getCenterX() - 0.5 * w_, rect.getCenterY() - 0.5 * h_, w_, h_);
		return scaled;
	}
}
