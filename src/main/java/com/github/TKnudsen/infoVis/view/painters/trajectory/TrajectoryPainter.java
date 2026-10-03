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
 * @version 1.2 -- start/end points are now marked blue/orange (a directional
 *          cue carried over from the legacy PainterLib TrajectoryPainter,
 *          which this class otherwise fully supersedes -- overlaid on top of
 *          screenPoints rather than tracked via drawPoint()'s call order,
 *          since selected points are drawn in a deferred second pass and so
 *          are not necessarily visited in data order), and an optional label
 *          can be drawn in the chart's bottom-left corner via
 *          {@link #setLabel(String)}
 * @since 2019
 */
public class TrajectoryPainter<T> extends ScatterPlotPainter<T> {

	private static final float START_END_MARKER_SIZE_FACTOR = 1.33f;

	// Store last coordinates as primitives to avoid Point2D deref each time
	private float lastX, lastY;
	private boolean hasLast = false;

	private String label;

	public TrajectoryPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		super(data, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);
		hasLast = false;

		markStartAndEnd(g2);
		drawLabel(g2);
	}

	private void markStartAndEnd(Graphics2D g2) {
		if (getChartRectangle() == null || screenPoints == null || screenPoints.isEmpty())
			return;

		float pointSize = (float) calculatePointSize(getChartRectangle().getWidth(), getChartRectangle().getHeight());

		Paint oldPaint = g2.getPaint();

		Point2D start = screenPoints.get(0);
		if (start != null) {
			g2.setPaint(Color.BLUE);
			DisplayTools.drawPoint(g2, (float) start.getX(), (float) start.getY(),
					pointSize * START_END_MARKER_SIZE_FACTOR, true);
		}

		if (screenPoints.size() > 1) {
			Point2D end = screenPoints.get(screenPoints.size() - 1);
			if (end != null) {
				g2.setPaint(Color.ORANGE);
				DisplayTools.drawPoint(g2, (float) end.getX(), (float) end.getY(),
						pointSize * START_END_MARKER_SIZE_FACTOR, true);
			}
		}

		g2.setPaint(oldPaint);
	}

	private void drawLabel(Graphics2D g2) {
		if (label == null || getChartRectangle() == null)
			return;

		Color oldColor = g2.getColor();
		g2.setColor(Color.BLACK);
		g2.drawString(label, (int) getChartRectangle().getX() + 2, (int) (getChartRectangle().getMaxY() - 4));
		g2.setColor(oldColor);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	protected void drawPoint(Graphics2D g2, Point2D point, float pointSize, Paint pointPaint, boolean selected) {
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
