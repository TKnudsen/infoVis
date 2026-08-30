package com.github.TKnudsen.infoVis.view.painters.trajectory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.painters.scatterplot.ScatterPlotPainter;
import com.github.TKnudsen.infoVis.view.tools.BasicStrokeTools;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * Paints a trajectory in 2D using a scatter plot and the corresponding visual
 * mapping functions to map data (represented as T) into the visual space. This
 * is done in two steps. In the scatter plot painter, T is mapped to Double for
 * the x and the y position. Second, the two Doubles are mapped into the visual
 * space.
 * </p>
 *
 * @version 1.1 (optimized)
 * @since 2019
 */
public class TrajectoryPainter<T> extends ScatterPlotPainter<T> {

	// private Point2D lastPoint = null;

	// Store last coordinates as primitives to avoid Point2D deref each time
	private float lastX, lastY;
	private boolean hasLast = false;

	public TrajectoryPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		super(data, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);
		hasLast = false;
	}

	@Override
	protected void drawPoint(Graphics2D g2, Point2D point, float pointSize, Paint pointPaint, boolean selected) {
//		if (selected) {
//			// line
//			if (lastPoint != null) {
//				DisplayTools.drawLine(g2, lastPoint.getX(), lastPoint.getY(), point.getX(), point.getY(),
//						BasicStrokeTools.get((float) (pointSize + 2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND),
//						Color.BLACK);
//				DisplayTools.drawLine(g2, lastPoint.getX(), lastPoint.getY(), point.getX(), point.getY(),
//						BasicStrokeTools.get(pointSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), pointPaint);
//
//			}
//
//			// point
//			DisplayTools.drawPoint(g2, point.getX(), point.getY(), pointSize * 1.66, Color.BLACK, true);
//			DisplayTools.drawPoint(g2, point.getX(), point.getY(), pointSize * 1.33, pointPaint, true);
//		} else {
//			// line
//			if (lastPoint != null)
//				DisplayTools.drawLine(g2, lastPoint.getX(), lastPoint.getY(), point.getX(), point.getY(),
//						BasicStrokeTools.get(pointSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), pointPaint);
//
//			// point
//			DisplayTools.drawPoint(g2, point.getX(), point.getY(), pointSize * 1.33, pointPaint, true);
//		}
//
//		lastPoint = point;

		final float x = (float) point.getX();
		final float y = (float) point.getY();

		// Precompute strokes once per call
		final float coreWidth = Math.max(1f, pointSize); // ensure visible
		final float haloWidth = coreWidth + 2f;
		final BasicStroke coreStroke = BasicStrokeTools.get(coreWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		final BasicStroke haloStroke = BasicStrokeTools.get(haloWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		// Draw connecting segment if we have a previous point
		if (hasLast) {
			if (selected) {
				// selection in big in the back, then core data color
				DisplayTools.drawLine(g2, lastX, lastY, x, y, haloStroke, Color.BLACK);
				DisplayTools.drawLine(g2, lastX, lastY, x, y, coreStroke, pointPaint);
			} else {
				DisplayTools.drawLine(g2, lastX, lastY, x, y, coreStroke, pointPaint);
			}
		}

		// Draw point mark (two-layer when selected for contrast)
		final Paint oldPaint = g2.getPaint();
		if (selected) {
			g2.setPaint(Color.BLACK);
			DisplayTools.drawPoint(g2, x, y, pointSize * 1.66f, true);
			g2.setPaint(pointPaint);
			DisplayTools.drawPoint(g2, x, y, pointSize * 1.33f, true);
		} else {
			g2.setPaint(pointPaint);
			DisplayTools.drawPoint(g2, x, y, pointSize * 1.33f, true);
		}
		g2.setPaint(oldPaint);

		// Update "last" point
		lastX = x;
		lastY = y;
		hasLast = true;
	}

}
