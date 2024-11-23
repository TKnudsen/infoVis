package com.github.TKnudsen.infoVis.view.painters.trajectory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainter;
import com.github.TKnudsen.infoVis.view.tools.BasicStrokes;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints a trajectory in 2D using a scatter plot and the corresponding visual
 * mapping functions to map data (represented as T) into the visual space. This
 * is done in two steps. In the scatter plot painter, T is mapped to Double for
 * the x and the y position. Second, the two Doubles are mapped into the visual
 * space.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2019-2024 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @version 1.03
 */
public class TrajectoryPainter<T> extends ScatterPlotPainter<T> {

	private Point2D lastPoint = null;

	public TrajectoryPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		super(data, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		lastPoint = null;
	}

	@Override
	protected void drawIndividualPoint(Graphics2D g2, Point2D point, float pointSize, Paint pointPaint,
			boolean selected) {

		if (selected) {
			// line
			if (lastPoint != null) {
				DisplayTools.drawLine(g2, lastPoint.getX(), lastPoint.getY(), point.getX(), point.getY(),
						BasicStrokes.get((float) (pointSize + 2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND),
						Color.BLACK);
				DisplayTools.drawLine(g2, lastPoint.getX(), lastPoint.getY(), point.getX(), point.getY(),
						BasicStrokes.get(pointSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), pointPaint);

			}

			// point
			DisplayTools.drawPoint(g2, point.getX(), point.getY(), pointSize * 1.66, Color.BLACK, true);
			DisplayTools.drawPoint(g2, point.getX(), point.getY(), pointSize * 1.33, pointPaint, true);
		} else {
			// line
			if (lastPoint != null)
				DisplayTools.drawLine(g2, lastPoint.getX(), lastPoint.getY(), point.getX(), point.getY(),
						BasicStrokes.get(pointSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), pointPaint);

			// point
			DisplayTools.drawPoint(g2, point.getX(), point.getY(), pointSize * 1.33, pointPaint, true);
		}

		lastPoint = point;
	}

}
