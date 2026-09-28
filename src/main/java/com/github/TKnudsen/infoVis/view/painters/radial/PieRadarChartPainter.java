package com.github.TKnudsen.infoVis.view.painters.radial;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.ScalingTextPainter;

/**
 * <p>
 * Structures multivariate data according to a pie metaphor, but unlike a
 * regular pie chart every slice has the same angular width -- one per data
 * dimension -- and instead encodes its value as radius, growing from a point
 * at the center out to the full chart radius. Reads like a hybrid of a pie
 * chart and a radar chart: angle carries dimension identity, radius carries
 * value.
 * </p>
 *
 * @version 1.1
 * @since 2015
 */
public class PieRadarChartPainter extends ChartPainter {

	private List<Double> fractions;
	private List<Color> colors;
	private List<String> labels;
	private double labelLODThreshold = 50;

	private Rectangle2D.Double square;
	private Arc2D.Double arc;

	private List<Arc2D.Double> slices;
	private List<Arc2D.Double> filledSlices;
	private List<ScalingTextPainter> textRenderers;

	public PieRadarChartPainter(List<Double> fractions, List<Color> colors, List<String> labels) {
		if (fractions == null || colors == null || labels == null)
			throw new IllegalArgumentException("PieRadarChartPainter: fractions, colors and labels must not be null");
		if (fractions.size() != colors.size() || fractions.size() != labels.size())
			throw new IllegalArgumentException(
					"PieRadarChartPainter: fractions, colors and labels must have the same size");
		if (fractions.isEmpty())
			throw new IllegalArgumentException("PieRadarChartPainter: fractions must not be empty");

		this.fractions = fractions;
		this.colors = colors;
		this.labels = labels;
		
		this.setBackgroundPaint(null);

		initialize();
	}

	private void initialize() {
		square = new Rectangle2D.Double();
		arc = new Arc2D.Double();
		slices = new ArrayList<>();
		filledSlices = new ArrayList<>();
		textRenderers = new ArrayList<>();

		double angle = -360 / (double) fractions.size();
		double currentAngle = 90;

		for (int i = 0; i < fractions.size(); i++) {
			slices.add(new Arc2D.Double(square, currentAngle, angle, Arc2D.PIE));
			filledSlices.add(new Arc2D.Double(square, currentAngle, angle, Arc2D.PIE));

			ScalingTextPainter textRenderer = new ScalingTextPainter(labels.get(i), getFont().getFontName(),
					getFontStyle());
			textRenderer.setBackgroundPaint(null);
			textRenderers.add(textRenderer);

			currentAngle += angle;
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		Color previousColor = g2.getColor();

		for (int i = 0; i < fractions.size(); i++) {
			g2.setColor(colors.get(i));
			g2.fill(filledSlices.get(i));
		}

		g2.setColor(previousColor);
		for (int i = 0; i < fractions.size(); i++) {
			g2.draw(slices.get(i));

			if (arc.getWidth() > labelLODThreshold)
				textRenderers.get(i).draw(g2);
		}

		g2.draw(arc);

		g2.setColor(previousColor);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		double width = rectangle.getWidth();
		double height = rectangle.getHeight();
		double x = rectangle.getX();
		double y = rectangle.getY();

		double edgeLength = Math.min(width, height) - 2;

		square.setRect(x + (width - edgeLength) * 0.5 + 1, y + (height - edgeLength) * 0.5 + 1, edgeLength,
				edgeLength);
		arc.setArc(square, 90, 360, Arc2D.OPEN);

		double xRatio = 0.8;
		double yRatio = 0.25;
		for (int i = 0; i < slices.size(); i++) {
			slices.get(i).setFrame(square);

			double[] balancePoint = calculateBalancePoint(slices.get(i));
			double sliceRadius = balancePoint[0];
			double labelCenterX = balancePoint[1];
			double labelCenterY = balancePoint[2];

			textRenderers.get(i).setRectangle(new Rectangle2D.Double(labelCenterX - xRatio * 0.5 * sliceRadius,
					labelCenterY - yRatio * 0.5 * sliceRadius, xRatio * sliceRadius, yRatio * sliceRadius));
		}

		for (int i = 0; i < fractions.size(); i++) {
			double edge = fractions.get(i) * edgeLength;
			filledSlices.get(i).setFrame(new Rectangle2D.Double(square.getCenterX() - 0.5 * edge,
					square.getCenterY() - 0.5 * edge, edge, edge));
		}
	}

	/**
	 * A point partway out along a slice's bisecting angle, used to center that
	 * slice's label.
	 *
	 * @return {radius, x, y} of the balance point
	 */
	private double[] calculateBalancePoint(Arc2D.Double slice) {
		double ratio = 0.6;
		double angle = slice.getAngleStart() + 0.5 * slice.getAngleExtent();
		double radius = 0.5 * slice.getWidth();
		double x = slice.getCenterX() + ratio * radius * Math.cos(angle * Math.PI / 180);
		double y = slice.getCenterY() - ratio * radius * Math.sin(angle * Math.PI / 180);
		return new double[] { radius, x, y };
	}

	@Override
	public String toString() {
		return "PieRadarChartPainter";
	}

	public double getLabelLODThreshold() {
		return labelLODThreshold;
	}

	public void setLabelLODThreshold(double labelLODThreshold) {
		this.labelLODThreshold = labelLODThreshold;
	}
}
