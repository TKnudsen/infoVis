package com.github.TKnudsen.infoVis.view.painters.scatterplot;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * A {@link ScatterPlotPainter} that draws each element as a cross rather
 * than a filled point -- meant to be layered over a regular
 * {@link ScatterPlotPainter} to distinguish a secondary set of elements
 * (e.g. manually labeled/ground-truth points among a larger unlabeled
 * set), using {@link AbstractScatterPlotPainter#drawPoint} exactly as
 * {@code TrajectoryPainter} does for its own connecting-line variant.
 * Per-element color (and thus any alpha adjustment, e.g. by relevance
 * score) is still driven entirely by the color mapping passed to the
 * constructor -- this class only changes the drawn shape.
 *
 * @param <T> the data element type
 *
 * @version 1.0
 * @since 2026
 */
public class CrossScatterPlotPainter<T> extends ScatterPlotPainter<T> {

	public CrossScatterPlotPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		super(data, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

	@Override
	protected void drawPoint(Graphics2D g2, Point2D point, float pointSize, Paint pointPaint, boolean selected) {
		if (selected) {
			g2.setPaint(getSelectionPaint());
			DisplayTools.drawCross(g2, (float) point.getX(), (float) point.getY(), pointSize * 1.66f);
		}

		g2.setPaint(pointPaint);
		DisplayTools.drawCross(g2, (float) point.getX(), (float) point.getY(), pointSize);
	}

}
