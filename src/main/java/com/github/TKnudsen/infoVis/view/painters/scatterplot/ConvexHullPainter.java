package com.github.TKnudsen.infoVis.view.painters.scatterplot;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctions;
import com.github.TKnudsen.infoVis.view.visualPrimitives.ConvexHullTools;

/**
 * <p>
 * Draws one convex hull per group of a data collection, meant to be layered
 * behind a {@link ScatterPlotPainter} over the same data (e.g. via
 * {@code InfoVisChartPanel.addChartPainter}) to visualize class/cluster
 * membership -- a class-colored scatterplot with each class's extent
 * outlined. Manages its own world-to-pixel position encoding independently
 * of any paired scatterplot painter, so the two stay decoupled; they just
 * need to be constructed with the same world position mappings.
 * </p>
 *
 * <p>
 * Groups with fewer than 3 points have no sensible hull and are skipped
 * (nothing is drawn for them).
 * </p>
 *
 * @param <T> the data element type
 * @param <G> the group key type (e.g. a class label)
 *
 * @version 1.0
 * @since 2026
 */
public class ConvexHullPainter<T, G> extends ChartPainter {

	private final List<T> data;
	private final Function<? super T, ? extends G> groupMapping;
	private final Function<? super T, Double> worldPositionMappingX;
	private final Function<? super T, Double> worldPositionMappingY;
	private final Function<? super G, ? extends Paint> groupColorMapping;

	private IPositionEncodingFunction xPositionEncodingFunction;
	private IPositionEncodingFunction yPositionEncodingFunction;

	/** alpha used to fill each hull; null disables the fill (outline only) */
	private Float fillAlpha = 0.15f;

	private Map<G, List<Point2D>> hullsByGroup = new LinkedHashMap<>();

	/**
	 * @param data                  the data elements to compute hulls over
	 * @param groupMapping          maps each element to its group (e.g. class
	 *                              label); a null result excludes the element
	 * @param worldPositionMappingX maps each element to its x value in data
	 *                              (world) space -- must match the paired
	 *                              scatterplot painter's own mapping for the two
	 *                              to align visually
	 * @param worldPositionMappingY maps each element to its y value in data
	 *                              (world) space
	 * @param groupColorMapping     maps each group to the color its hull is
	 *                              filled/outlined with; a null result falls
	 *                              back to this painter's own paint
	 */
	public ConvexHullPainter(List<T> data, Function<? super T, ? extends G> groupMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, Double> worldPositionMappingY,
			Function<? super G, ? extends Paint> groupColorMapping) {
		this.data = data;
		this.groupMapping = groupMapping;
		this.worldPositionMappingX = worldPositionMappingX;
		this.worldPositionMappingY = worldPositionMappingY;
		this.groupColorMapping = groupColorMapping;

		initializePositionEncodingFunctions();
	}

	private void initializePositionEncodingFunctions() {
		this.xPositionEncodingFunction = PositionEncodingFunctions.createPositionEncodingFunctionTolerant(data,
				worldPositionMappingX, 0d, 1d, false, getClass().getSimpleName() + " (x-axis)");
		this.yPositionEncodingFunction = PositionEncodingFunctions.createPositionEncodingFunctionTolerant(data,
				worldPositionMappingY, 0d, 1d, true, getClass().getSimpleName() + " (y-axis)");
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		xPositionEncodingFunction.setMinPixel(rectangle.getMinX());
		xPositionEncodingFunction.setMaxPixel(rectangle.getMaxX());
		yPositionEncodingFunction.setMinPixel(rectangle.getMinY());
		yPositionEncodingFunction.setMaxPixel(rectangle.getMaxY());

		recomputeHulls();
	}

	private void recomputeHulls() {
		Map<G, List<Point2D>> pointsByGroup = new LinkedHashMap<>();

		if (data != null)
			for (T t : data) {
				if (t == null)
					continue;

				G group = groupMapping.apply(t);
				if (group == null)
					continue;

				Double worldX = worldPositionMappingX.apply(t);
				Double worldY = worldPositionMappingY.apply(t);
				if (worldX == null || worldY == null || worldX.isNaN() || worldY.isNaN())
					continue;

				double x = xPositionEncodingFunction.apply(worldX);
				double y = yPositionEncodingFunction.apply(worldY);

				pointsByGroup.computeIfAbsent(group, g -> new ArrayList<>()).add(new Point2D.Double(x, y));
			}

		Map<G, List<Point2D>> hulls = new LinkedHashMap<>();
		for (Map.Entry<G, List<Point2D>> entry : pointsByGroup.entrySet()) {
			List<Point2D> hull = ConvexHullTools.computeConvexHull(entry.getValue());
			if (hull.size() >= 3)
				hulls.put(entry.getKey(), hull);
		}
		hullsByGroup = hulls;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (chartRectangle == null || hullsByGroup.isEmpty())
			return;

		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();

		for (Map.Entry<G, List<Point2D>> entry : hullsByGroup.entrySet()) {
			List<Point2D> hull = entry.getValue();

			Paint groupPaint = groupColorMapping != null ? groupColorMapping.apply(entry.getKey()) : null;
			if (groupPaint == null)
				groupPaint = getPaint();

			Path2D.Double path = new Path2D.Double();
			path.moveTo(hull.get(0).getX(), hull.get(0).getY());
			for (int i = 1; i < hull.size(); i++)
				path.lineTo(hull.get(i).getX(), hull.get(i).getY());
			path.closePath();

			if (fillAlpha != null) {
				g2.setPaint(ColorTools.setAlpha(groupPaint, fillAlpha));
				g2.fill(path);
			}

			g2.setPaint(groupPaint);
			g2.setStroke(stroke);
			g2.draw(path);
		}

		g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);
	}

	/** @return the current per-group computed hulls (unmodifiable view semantics not enforced; read-only by convention) */
	public Map<G, List<Point2D>> getHullsByGroup() {
		return hullsByGroup;
	}

	/** @return the alpha used to fill each hull, or null if fill is disabled */
	public Float getFillAlpha() {
		return fillAlpha;
	}

	/** @param fillAlpha alpha used to fill each hull; null disables the fill (outline only) */
	public void setFillAlpha(Float fillAlpha) {
		this.fillAlpha = fillAlpha;
	}

}
