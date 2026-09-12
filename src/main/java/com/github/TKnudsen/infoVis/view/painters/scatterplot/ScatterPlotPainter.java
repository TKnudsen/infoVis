package com.github.TKnudsen.infoVis.view.painters.scatterplot;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.TooltipStringPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.ToolTipTools;

/**
 * <p>
 * Paints a scatter plot using visual mapping functions to map data (represented
 * as T) into the visual space. This is done in two steps. First, T is mapped to
 * Double for the x and the y position. Second, the two Doubles are mapped into
 * the visual space.
 * </p>
 *
 * <p>
 * All CPU-rendering machinery shared with the GPU-based scatterplot painters
 * lives in {@link AbstractScatterPlotPainter}; this class only adds the
 * {@link ITooltip#getTooltip(Point)} implementation, since that is expected to
 * diverge once a GPU-based subclass is introduced (a GPU painter needs a
 * CPU/GPU dispatch a CPU-only painter does not).
 * </p>
 *
 * @version 2.12
 * @since 2018
 */
public class ScatterPlotPainter<T> extends AbstractScatterPlotPainter<T> {

	/**
	 * @param data                  the data elements to plot
	 * @param colorMapping          maps each element to its point color; a null
	 *                              result falls back to the painter's own paint
	 * @param worldPositionMappingX maps each element to its x value in data (world)
	 *                              space
	 * @param worldPositionMappingY maps each element to its y value in data (world)
	 *                              space
	 */
	public ScatterPlotPainter(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY) {
		super(data, colorMapping, worldPositionMappingX, worldPositionMappingY);
	}

	/**
	 * Finds the data element whose screen point is nearest {@code p}, within a
	 * {@link #calculatePointSize} search box (Manhattan distance, not a true
	 * circle), and returns a small label painter for it - or null if tooltips are
	 * disabled, no element is close enough, or a data refresh is currently in
	 * progress.
	 */
	@Override
	public ChartPainter getTooltip(Point p) {
		if (!isToolTipping() || p == null) {
			return null;
		}

		final Rectangle2D cr = chartRectangle;
		if (cr == null) {
			return null;
		}

		// If points are being refreshed, do not attempt tooltip computation.
		// (Optional; you already have the lock, but this avoids unnecessary work.)
		if (refreshingDataPoints) {
			return null;
		}

		screenPointsLock.readLock().lock();
		try {
			if (data == null || data.isEmpty()) {
				return null;
			}
			if (screenPoints == null || screenPoints.isEmpty()) {
				return null;
			}

			final int n = data.size();
			if (screenPoints.size() != n) {
				// inconsistent snapshot (e.g., refresh in progress); skip this tool tip event
				return null;
			}

			final double maxRadius = calculatePointSize(cr.getWidth(), cr.getHeight());
			final double px = p.getX();
			final double py = p.getY();

			double bestDist = Double.POSITIVE_INFINITY;
			T bestElement = null;

			for (int i = 0; i < n; i++) {
				final Point2D sp = screenPoints.get(i);
				if (sp == null) {
					continue;
				}

				final double dx = Math.abs(sp.getX() - px);
				if (dx >= maxRadius) {
					continue;
				}

				final double dy = Math.abs(sp.getY() - py);
				if (dy >= maxRadius) {
					continue;
				}

				final double dist = dx + dy;
				if (dist < bestDist) {
					final T candidate = data.get(i);
					if (candidate != null) { // critical for preventing NPE later
						bestDist = dist;
						bestElement = candidate;
					}
				}
			}

			if (bestElement == null) {
				return null;
			}

			String toolTipString;
			if (getToolTipMapping() != null) {
				toolTipString = getToolTipMapping().apply(bestElement);
			} else {
				// Defensive: world mappings may still return null
				Double wx = getWorldPositionMappingX() != null ? getWorldPositionMappingX().apply(bestElement) : null;
				Double wy = getWorldPositionMappingY() != null ? getWorldPositionMappingY().apply(bestElement) : null;

				if (wx == null || wy == null || wx.isNaN() || wy.isNaN()) {
					return null;
				}

				toolTipString = MathFunctions.round(wx.doubleValue(), 2) + ", "
						+ MathFunctions.round(wy.doubleValue(), 2);
			}

			if (toolTipString == null) {
				return null;
			}

			StringPainter stringPainter = new TooltipStringPainter(toolTipString);

			Rectangle2D rect = ToolTipTools.createToolTipRectangle(cr, p, getToolTipWidth(), getToolTipHeight());
			stringPainter.setRectangle(rect);

			stringPainter.setBackgroundPaint(ColorTools.setAlpha(Color.DARK_GRAY, 0.5f));
			stringPainter.setFontColor(Color.WHITE);
			stringPainter.setFontSize(15);

			return stringPainter;

		} finally {
			screenPointsLock.readLock().unlock();
		}
	}

}
