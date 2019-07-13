package com.github.TKnudsen.infoVis.view.painters.donutchart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.painters.piechart.PieChartPainter;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Donut chart painter. Crops a center pie area from the pie segments of the
 * inherited PieChartPainter
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class DonutChartPainter extends PieChartPainter {

	private double donutRadiusRelative = 0.2;
	protected Rectangle2D.Double squareInner;
	private Arc2D.Double arcInner;

	public DonutChartPainter(List<Double> pieces, List<Color> colors) {
		super(pieces, colors);

		squareInner = new Rectangle2D.Double();
		arcInner = new Arc2D.Double();
	}

	@Override
	protected void drawPie(Graphics2D g2) {
		Color c = g2.getColor();

		super.drawPie(g2);

		if (isDrawOutline()) {
			g2.setPaint(getBorderPaint());
			g2.draw(arcInner);
		}

		g2.setColor(c);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		// given space
		double width = square.getWidth();
		double height = square.getHeight();
		double x = square.getX();
		double y = square.getY();

		List<Shape> segments = new ArrayList<Shape>();
		Area a2 = new Area(new Ellipse2D.Double(x + width * donutRadiusRelative, y + height * donutRadiusRelative,
				width * (1 - 2 * donutRadiusRelative), height * (1 - 2 * donutRadiusRelative)));
		for (int i = 0; i < piecesRelative.size(); i++) {
			Area seg = (Area) arcSegments.get(i);
			seg.subtract(a2);
			segments.add(seg);
		}
		arcSegments = segments;

		squareInner.setRect(new Rectangle2D.Double(x + width * donutRadiusRelative, y + height * donutRadiusRelative,
				width * (1 - 2 * donutRadiusRelative), height * (1 - 2 * donutRadiusRelative)));
		arcInner.setArc(squareInner, 90, 360, Arc2D.OPEN);
	}

	public double getDonutRadiusRelative() {
		return donutRadiusRelative;
	}

	public void setDonutRadiusRelative(double donutRadiusRelative) {
		this.donutRadiusRelative = donutRadiusRelative;

		setRectangle(rectangle);
	}

}
