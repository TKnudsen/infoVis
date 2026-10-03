package com.github.TKnudsen.infoVis.view.painters.radial;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * Classic radar/spider chart: one axis per data dimension, radiating from a
 * shared center, with the data values connected by a closed straight-edge
 * polygon.
 * </p>
 *
 * <p>
 * Vertex placement and axis drawing are shared with {@link
 * RadarChartButterflyPainter}, which overrides only {@link
 * #buildChartPath(int[][], int, int)} to bow each edge in toward the center
 * instead of connecting vertices directly -- the two share everything except
 * that one step.
 * </p>
 *
 * @version 1.0
 * @since 2018
 */
public class RadarChartPainter extends ChartPainter {

	protected double[] data;

	protected double yAxisMinValue = 0.0;
	protected double yAxisMaxValue = 1.0;
	protected boolean drawAxes = true;

	protected boolean fill = false;
	protected boolean boundary = false;
	protected boolean drawCoordinateMarks = true;

	protected double pointRadius = Double.NaN;

	protected String label;

	private Color axisColor = Color.GRAY;
	private List<Color> axesColors = null;

	/**
	 * A fixed, theme-independent default so the chart's area fill is reliably
	 * visible without the caller having to call {@link #setPaint(java.awt.Paint)}
	 * first
	 * -- the inherited look-and-feel-driven default paint ({@link
	 * ChartPainter#getPaint()}) is meant for text/outlines, not necessarily for
	 * an area fill, and can end up visually indistinguishable from the
	 * background under some themes.
	 */
	private static final Color DEFAULT_FILL_COLOR = new Color(70, 130, 180);

	public RadarChartPainter(double[] data) {
		if (data == null)
			throw new IllegalArgumentException("RadarChartPainter: data must not be null");

		this.data = data.clone();
		setPaint(DEFAULT_FILL_COLOR);
		this.stroke = DisplayTools.thickStroke;

		initialize();
	}

	public RadarChartPainter(List<Double> data) {
		this(toPrimitives(data));
	}

	private static double[] toPrimitives(List<Double> data) {
		if (data == null)
			throw new IllegalArgumentException("RadarChartPainter: data must not be null");

		return DataConversion.toPrimitives(data);
	}

	protected void initialize() {
		if (data != null) {
			yAxisMinValue = MathFunctions.getMin(data);
			yAxisMaxValue = MathFunctions.getMax(data);
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (data == null || data.length == 0 || getChartRectangle() == null)
			return;

		if (drawAxes)
			drawAxes(g2);

		drawRadar(g2);

		if (isDrawOutline())
			DisplayTools.drawRectangle(g2, getRectangle(), getBorderPaint());
	}

	/**
	 * The axis endpoints, one per data dimension, evenly spaced around the
	 * circle starting straight up and going clockwise.
	 */
	protected int[][] calculateAxesEndpoints() {
		double maxRadius = Math.min(getChartRectangle().getWidth(), getChartRectangle().getHeight()) / 2;
		int xMean = (int) getChartRectangle().getCenterX();
		int yMean = (int) getChartRectangle().getCenterY();

		int[][] axesEndpoints = new int[data.length][2];
		for (int i = 0; i < data.length; i++) {
			double angle = ((double) i / data.length) * 2 * Math.PI;
			axesEndpoints[i][0] = (int) (xMean + Math.sin(angle) * maxRadius);
			axesEndpoints[i][1] = (int) (yMean - Math.cos(angle) * maxRadius);
		}
		return axesEndpoints;
	}

	protected void drawAxes(Graphics2D g2) {
		int xMean = (int) getChartRectangle().getCenterX();
		int yMean = (int) getChartRectangle().getCenterY();
		int[][] axesEndpoints = calculateAxesEndpoints();

		for (int i = 0; i < data.length; i++) {
			if (axesColors != null && i < axesColors.size()) {
				g2.setColor(axesColors.get(i));
				g2.setStroke(DisplayTools.thickStroke);
				g2.drawLine(xMean, yMean, axesEndpoints[i][0], axesEndpoints[i][1]);
			}
			g2.setColor(axisColor);
			g2.setStroke(DisplayTools.standardStroke);
			g2.drawLine(xMean, yMean, axesEndpoints[i][0], axesEndpoints[i][1]);
		}
	}

	/**
	 * This chart's own {@link #getData() data}, laid out as polygon vertices.
	 * Delegates to {@link #calculateVertexCoordinates(double[])}.
	 */
	protected int[][] calculateVertexCoordinates() {
		return calculateVertexCoordinates(data);
	}

	/**
	 * Lays out an arbitrary series (this chart's own data, or an externally
	 * supplied series of the same dimensionality, such as a bundle member) as
	 * polygon vertices: one per dimension, each at the distance from the center
	 * that its value -- normalized against this chart's own {@link
	 * #getYAxisMinValue() min}/{@link #getYAxisMaxValue() max} -- maps to along
	 * its axis.
	 *
	 * @param seriesData one value per dimension, same length as {@link
	 *                    #getData()}
	 */
	protected int[][] calculateVertexCoordinates(double[] seriesData) {
		double maxRadius = Math.min(getChartRectangle().getWidth(), getChartRectangle().getHeight()) / 2;
		int xMean = (int) getChartRectangle().getCenterX();
		int yMean = (int) getChartRectangle().getCenterY();

		double valueRange = yAxisMaxValue - yAxisMinValue;

		int[][] vertices = new int[seriesData.length][2];
		for (int i = 0; i < seriesData.length; i++) {
			double angle = ((double) i / seriesData.length) * 2 * Math.PI;
			double normalizedValue = valueRange == 0 ? 0.5 : (seriesData[i] - yAxisMinValue) / valueRange;
			double radius = normalizedValue * maxRadius;

			vertices[i][0] = (int) (xMean + Math.sin(angle) * radius);
			vertices[i][1] = (int) (yMean - Math.cos(angle) * radius);
		}
		return vertices;
	}

	/**
	 * Builds the closed shape connecting the data polygon's vertices.
	 * {@link RadarChartButterflyPainter} overrides this to bow each edge toward
	 * the center instead of connecting vertices with a straight line.
	 */
	protected GeneralPath buildChartPath(int[][] vertices, int xMean, int yMean) {
		GeneralPath path = new GeneralPath();

		path.moveTo(vertices[0][0], vertices[0][1]);
		for (int i = 1; i < vertices.length; i++)
			path.lineTo(vertices[i][0], vertices[i][1]);
		path.lineTo(vertices[0][0], vertices[0][1]);

		return path;
	}

	protected void drawRadar(Graphics2D g2) {
		Color previousColor = g2.getColor();
		Stroke previousStroke = g2.getStroke();

		drawSeries(g2, data, getColor(), stroke, fill);

		if (drawCoordinateMarks) {
			double markRadius = Double.isNaN(pointRadius)
					? Math.min(getChartRectangle().getWidth(), getChartRectangle().getHeight()) * 0.0033
					: pointRadius;

			int[][] vertices = calculateVertexCoordinates();
			g2.setColor(getColor());
			for (int i = 0; i < vertices.length; i++)
				DisplayTools.drawPoint(g2, vertices[i][0], vertices[i][1], markRadius, getColor(), true);
		}

		if (label != null) {
			g2.setColor(Color.BLACK);
			g2.drawString(label, (int) getChartRectangle().getMinX() + 5, (int) getChartRectangle().getMinY() + 12);
		}

		g2.setColor(previousColor);
		g2.setStroke(previousStroke);
	}

	/**
	 * Draws one series -- this chart's own {@link #getData() data}, or an
	 * externally supplied series of the same dimensionality -- as a closed
	 * polygon via {@link #buildChartPath(int[][], int, int)}, without
	 * coordinate marks or the label (those belong to this chart's own primary
	 * series and are drawn once by {@link #drawRadar(Graphics2D)}).
	 * <p>
	 * Exposed as a reusable building block for subclasses that layer several
	 * series over the same axes, such as a bundle of related profiles.
	 *
	 * @param seriesData    one value per dimension, same length as {@link
	 *                      #getData()}
	 * @param seriesColor   fill/outline color for this series
	 * @param seriesStroke  outline stroke for this series
	 * @param fillSeries    whether to fill the polygon; the outline is always
	 *                      drawn in addition when {@link #isBoundary()} is set
	 */
	protected void drawSeries(Graphics2D g2, double[] seriesData, Color seriesColor, Stroke seriesStroke,
			boolean fillSeries) {
		int xMean = (int) getChartRectangle().getCenterX();
		int yMean = (int) getChartRectangle().getCenterY();

		int[][] vertices = calculateVertexCoordinates(seriesData);
		GeneralPath path = buildChartPath(vertices, xMean, yMean);

		g2.setStroke(seriesStroke);

		if (fillSeries) {
			g2.setColor(seriesColor);
			g2.fill(path);
		}
		if (boundary || !fillSeries) {
			g2.setColor(seriesColor);
			g2.draw(path);
		}
	}

	public double[] getData() {
		return data;
	}

	@Override
	public String toString() {
		return "RadarChartPainter";
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}

	public void setDrawAxes(boolean drawAxes) {
		this.drawAxes = drawAxes;
	}

	public void setDrawCoordinateMarks(boolean drawCoordinateMarks) {
		this.drawCoordinateMarks = drawCoordinateMarks;
	}

	public boolean isBoundary() {
		return boundary;
	}

	public void setBoundary(boolean boundary) {
		this.boundary = boundary;
	}

	public void setPointRadius(double pointRadius) {
		this.pointRadius = pointRadius;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Color getAxisColor() {
		return axisColor;
	}

	public void setAxisColor(Color axisColor) {
		this.axisColor = axisColor;
	}

	public List<Color> getAxesColors() {
		return axesColors;
	}

	/**
	 * An optional highlight color drawn on top of the default {@link
	 * #setAxisColor(Color) axis color}, one per dimension, in data order. Fewer
	 * entries than dimensions is tolerated -- the remaining axes simply keep
	 * their default color.
	 *
	 * @param axesColors per-axis highlight colors, or null to clear them
	 */
	public void setAxesColors(List<Color> axesColors) {
		this.axesColors = axesColors;
	}

	public double getYAxisMinValue() {
		return yAxisMinValue;
	}

	public void setYAxisMinValue(double yAxisMinValue) {
		this.yAxisMinValue = yAxisMinValue;
	}

	public double getYAxisMaxValue() {
		return yAxisMaxValue;
	}

	public void setYAxisMaxValue(double yAxisMaxValue) {
		this.yAxisMaxValue = yAxisMaxValue;
	}
}
