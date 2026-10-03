package com.github.TKnudsen.infoVis.view.painters.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * <p>
 * Paints an octagon (a square with its four corners cut off) as a colored
 * boundary band split into {@code colors.size()} equal angular segments, one
 * color each.
 * </p>
 *
 * <p>
 * The corner cuts are controlled by {@link #setDiagonalSizeRatio(double)}:
 * the ratio of a diagonal (corner) edge's length to a straight (horizontal or
 * vertical) edge's length. The default, {@code 1.0}, gives a regular octagon
 * (all eight edges equal); smaller values shrink the corner cuts, giving a
 * more square-like shape; {@code 0.0} degenerates to a plain square.
 * </p>
 *
 * @version 1.0
 * @since 2019
 */
public class OctagonPainter extends ChartPainter {

	private final List<Color> colors;

	private double diagonalSizeRatio = 1.0;
	private double strokeWidthRate = Double.NaN;

	private List<Shape> segments;

	public OctagonPainter(List<Color> colors) {
		Objects.requireNonNull(colors);

		if (colors.isEmpty())
			throw new IllegalArgumentException("OctagonPainter: colors must not be empty");

		this.colors = Collections.unmodifiableList(new ArrayList<>(colors));
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (segments == null)
			return;

		Color resetColor = g2.getColor();

		for (int i = 0; i < segments.size(); i++) {
			g2.setColor(colors.get(i));
			g2.fill(segments.get(i));
		}

		g2.setColor(resetColor);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null) {
			setChartRectangle(null);
			segments = null;
			return;
		}

		double side = Math.min(rectangle.getWidth(), rectangle.getHeight());
		double cut = diagonalSizeRatio * side / (Math.sqrt(2) + 2 * diagonalSizeRatio);

		double minX = rectangle.getCenterX() - side / 2;
		double minY = rectangle.getCenterY() - side / 2;
		double maxX = minX + side;
		double maxY = minY + side;

		setChartRectangle(new Rectangle2D.Double(minX, minY, side, side));

		Path2D.Double octagon = new Path2D.Double();
		octagon.moveTo(minX + cut, minY);
		octagon.lineTo(maxX - cut, minY);
		octagon.lineTo(maxX, minY + cut);
		octagon.lineTo(maxX, maxY - cut);
		octagon.lineTo(maxX - cut, maxY);
		octagon.lineTo(minX + cut, maxY);
		octagon.lineTo(minX, maxY - cut);
		octagon.lineTo(minX, minY + cut);
		octagon.closePath();

		float strokeWidth = Double.isNaN(strokeWidthRate) ? stroke.getLineWidth() : (float) (side * strokeWidthRate);
		BasicStroke bandStroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
		Area band = new Area(bandStroke.createStrokedShape(octagon));

		segments = computeSegments(band);
	}

	private List<Shape> computeSegments(Area band) {
		Rectangle2D bounds = getChartRectangle();

		// larger than the octagon in every direction, so each wedge fully covers its
		// share of the boundary band once intersected
		Rectangle2D.Double wedgeBounds = new Rectangle2D.Double(bounds.getCenterX() - bounds.getWidth(),
				bounds.getCenterY() - bounds.getHeight(), bounds.getWidth() * 2, bounds.getHeight() * 2);

		List<Shape> result = new ArrayList<>();

		double segmentAngle = 360.0 / colors.size();
		double startAngle = 90;
		for (int i = 0; i < colors.size(); i++) {
			Area wedge = new Area(new Arc2D.Double(wedgeBounds, startAngle, -segmentAngle, Arc2D.PIE));
			wedge.intersect(band);
			result.add(wedge);

			startAngle -= segmentAngle;
		}

		return result;
	}

	public double getDiagonalSizeRatio() {
		return diagonalSizeRatio;
	}

	/**
	 * @param diagonalSizeRatio ratio of a diagonal (corner) edge's length to a
	 *                          straight (horizontal/vertical) edge's length.
	 *                          {@code 1.0} (the default) gives a regular
	 *                          octagon; {@code 0.0} gives a plain square;
	 *                          values above {@code 1.0} shrink the straight
	 *                          edges further, approaching a diamond.
	 */
	public void setDiagonalSizeRatio(double diagonalSizeRatio) {
		if (diagonalSizeRatio < 0)
			throw new IllegalArgumentException("OctagonPainter: diagonalSizeRatio must not be negative");

		this.diagonalSizeRatio = diagonalSizeRatio;

		if (getRectangle() != null)
			setRectangle(getRectangle());
	}

	public double getStrokeWidthRate() {
		return strokeWidthRate;
	}

	public void setStrokeWidthRate(double strokeWidthRate) {
		this.strokeWidthRate = strokeWidthRate;

		if (getRectangle() != null)
			setRectangle(getRectangle());
	}
}
