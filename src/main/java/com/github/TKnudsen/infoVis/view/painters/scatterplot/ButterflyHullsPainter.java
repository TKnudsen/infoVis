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
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.position.PositionEncodingFunctions;
import com.github.TKnudsen.infoVis.view.visualPrimitives.ButterflyShapeGenerator;

/**
 * <p>
 * Draws one "butterfly hull" per group of a data collection -- a curved
 * refinement of the group's convex hull that follows the point cloud's
 * concavities more closely than the plain straight-edged hull drawn by
 * {@link ConvexHullPainter}. Implements the technique of:
 * </p>
 *
 * <p>
 * Schreck, T., Schuessler, M., Zeilfelder, F., Worm, K.: Butterfly plots for
 * visual analysis of large point cloud data. Proceedings of the 16th
 * International Conference in Central Europe on Computer Graphics,
 * Visualization and Computer Vision (WSCG'08), pp. 33-40, 2008.
 * </p>
 *
 * <p>
 * Meant to be layered behind a {@link ScatterPlotPainter} over the same data
 * (e.g. via {@code InfoVisChartPanel.addChartPainter}), exactly like
 * {@link ConvexHullPainter} -- the two are drop-in alternatives to each
 * other. Groups with fewer than 3 points are skipped.
 * </p>
 *
 * @param <T> the data element type
 * @param <G> the group key type (e.g. a class label)
 *
 * @version 1.0
 * @since 2026
 */
public class ButterflyHullsPainter<T, G> extends ChartPainter {

	private final List<T> data;
	private final Function<? super T, ? extends G> groupMapping;
	private final Function<? super T, Double> worldPositionMappingX;
	private final Function<? super T, Double> worldPositionMappingY;
	private final Function<? super G, ? extends Paint> groupColorMapping;

	private IPositionEncodingFunction xPositionEncodingFunction;
	private IPositionEncodingFunction yPositionEncodingFunction;

	/** recursion depth budget per hull edge; see {@link ButterflyShapeGenerator} */
	private int depth = 4;

	/** area-recovery fraction (0..1) required to accept a refinement; see {@link ButterflyShapeGenerator} */
	private double recovery = 0.03;

	/** alpha used to fill each hull; null disables the fill (outline only) */
	private Float fillAlpha = 0.15f;

	private Map<G, Path2D> hullsByGroup = new LinkedHashMap<>();

	/**
	 * @param data                  the data elements to compute butterfly hulls
	 *                              over
	 * @param groupMapping          maps each element to its group (e.g. class
	 *                              label); a null result excludes the element
	 * @param worldPositionMappingX maps each element to its x value in data
	 *                              (world) space -- must match the paired
	 *                              scatterplot painter's own mapping for the two
	 *                              to align visually
	 * @param worldPositionMappingY maps each element to its y value in data
	 *                              (world) space
	 * @param groupColorMapping     maps each group to the color its hull is
	 *                              filled/outlined with; a null result falls back
	 *                              to this painter's own paint
	 */
	public ButterflyHullsPainter(List<T> data, Function<? super T, ? extends G> groupMapping,
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

		Map<G, Path2D> hulls = new LinkedHashMap<>();
		for (Map.Entry<G, List<Point2D>> entry : pointsByGroup.entrySet()) {
			if (entry.getValue().size() < 3)
				continue;

			Path2D shape = ButterflyShapeGenerator.generateButterflyHullShape(entry.getValue(), depth, recovery);
			if (shape != null)
				hulls.put(entry.getKey(), shape);
		}
		hullsByGroup = hulls;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (chartRectangle == null || hullsByGroup.isEmpty())
			return;

		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();

		for (Map.Entry<G, Path2D> entry : hullsByGroup.entrySet()) {
			Path2D shape = entry.getValue();

			Paint groupPaint = groupColorMapping != null ? groupColorMapping.apply(entry.getKey()) : null;
			if (groupPaint == null)
				groupPaint = getPaint();

			if (fillAlpha != null) {
				g2.setPaint(ColorTools.setAlpha(groupPaint, fillAlpha));
				g2.fill(shape);
			}

			g2.setPaint(groupPaint);
			g2.setStroke(stroke);
			g2.draw(shape);
		}

		g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);
	}

	/** @return the current per-group computed butterfly hulls, as an unmodifiable view */
	public Map<G, Path2D> getHullsByGroup() {
		return Collections.unmodifiableMap(hullsByGroup);
	}

	public int getDepth() {
		return depth;
	}

	/** @param depth recursion depth budget per hull edge */
	public void setDepth(int depth) {
		this.depth = depth;
		recomputeHulls();
	}

	public double getRecovery() {
		return recovery;
	}

	/** @param recovery area-recovery fraction (0..1) required to accept a refinement */
	public void setRecovery(double recovery) {
		this.recovery = recovery;
		recomputeHulls();
	}

	public Float getFillAlpha() {
		return fillAlpha;
	}

	/** @param fillAlpha alpha used to fill each hull; null disables the fill (outline only) */
	public void setFillAlpha(Float fillAlpha) {
		this.fillAlpha = fillAlpha;
	}

}
