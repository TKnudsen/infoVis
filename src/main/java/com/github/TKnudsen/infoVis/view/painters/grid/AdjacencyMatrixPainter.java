package com.github.TKnudsen.infoVis.view.painters.grid;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Draws an n x n adjacency/similarity matrix as a rotated (45 degree)
 * triangular grid of diamonds: cell {@code (x, y)} for {@code y >= x} is
 * filled with {@code data[x][y]}, with row labels drawn along the diagonal.
 * A single coordinate or a pair of coordinates can be highlighted, which
 * traces an outline along that row/column of diamonds.
 *
 * <p>
 * Implements {@link IClickSelection} for hit testing: clicking a row label
 * returns that single index, clicking a diagonal cell also returns a single
 * index, and clicking an off-diagonal cell returns the pair of indices it
 * represents. This only reports what was clicked -- it does not update
 * {@link #getHighLightedCoordinate()} itself, so a caller wanting
 * click-to-highlight behavior should feed the result back via
 * {@link #setHighLightedCoordinate(int[])} and repaint.
 * </p>
 *
 * @since 2011
 */
public class AdjacencyMatrixPainter extends ChartPainter implements IClickSelection<Integer> {

	private Color[][] data;
	private String[] labels;

	private int legendWidth = 100;
	private double localLegendWidth = Double.NaN;

	private double gridWidth;
	private double gridHeight;

	private BasicStroke highLightingStroke;
	private int[] highLightedCoordinate;

	// cached hit-test geometry, rebuilt on every draw() once localLegendWidth is
	// known from actual font metrics
	private Rectangle2D[] labelRectangles;
	private Polygon[][] cellPolygons;

	public AdjacencyMatrixPainter(Color[][] data, String[] labels) {
		this.data = data;
		this.labels = labels;
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (rectangle == null || data == null || labels == null || labels.length <= 2)
			return;

		Color color = g2.getColor();
		Font font = g2.getFont();
		Stroke strokeTmp = g2.getStroke();

		int suggestedLabelSize = (int) (rectangle.getHeight() / labels.length - 1);
		int suggestedWidthSize = (int) rectangle.getWidth() / 40;
		int labelFontSize = (int) Math.max(8, Math.min(suggestedLabelSize, suggestedWidthSize));

		g2.setFont(new Font("Tahoma", Font.BOLD, labelFontSize));

		FontMetrics m = g2.getFontMetrics();
		localLegendWidth = 0;
		for (String label : labels)
			localLegendWidth = Math.max(localLegendWidth, m.stringWidth(label));
		localLegendWidth = Math.min(localLegendWidth + 8, legendWidth);

		g2.setStroke(stroke);

		labelRectangles = new Rectangle2D[labels.length];
		cellPolygons = new Polygon[labels.length][labels.length];

		for (int x = 0; x < labels.length; x++) {
			String s = labels[x];
			while (m.stringWidth(s) > localLegendWidth)
				s = s.substring(0, s.length() - 1);

			boolean labelHighlighted = highLightedCoordinate != null
					&& ((highLightedCoordinate.length > 1 && (highLightedCoordinate[0] == x || highLightedCoordinate[1] == x))
							|| (highLightedCoordinate.length == 1 && highLightedCoordinate[0] == x));
			g2.setFont(new Font("Tahoma", labelHighlighted ? Font.BOLD : Font.PLAIN, labelFontSize));

			g2.setColor(Color.black);
			g2.drawString(s, (int) rectangle.getX() + 8, (int) (rectangle.getY() + 2 + gridHeight * x) + m.getHeight() / 3 + (int) (gridHeight / 2));

			Rectangle2D labelRect = new Rectangle2D.Double(rectangle.getX() + 2, rectangle.getY() + 2 + gridHeight * x, localLegendWidth,
					gridHeight);
			g2.draw(labelRect);
			labelRectangles[x] = labelRect;

			for (int y = x; y < labels.length; y++) {
				double firstX = rectangle.getX() + 2 + localLegendWidth + (Math.abs(x - y) - 1.0) * (gridWidth / 2);
				double firstY = rectangle.getY() + 2 + gridHeight * x + Math.abs(y - x) * (gridHeight / 2);

				int[] xCoords;
				int[] yCoords;
				if (x == y) {
					xCoords = new int[] { (int) (firstX + gridWidth / 2), (int) (firstX + gridWidth), (int) (firstX + gridWidth / 2) };
					yCoords = new int[] { (int) firstY, (int) (firstY + gridHeight / 2), (int) (firstY + gridHeight) };
				} else {
					xCoords = new int[] { (int) firstX, (int) (firstX + gridWidth / 2), (int) (firstX + gridWidth), (int) (firstX + gridWidth / 2) };
					yCoords = new int[] { (int) (firstY + gridHeight / 2), (int) firstY, (int) (firstY + gridHeight / 2), (int) (firstY + gridHeight) };
				}

				Polygon cellPolygon = new Polygon(xCoords, yCoords, xCoords.length);
				g2.setColor(data[x][y]);
				g2.fill(cellPolygon);
				cellPolygons[x][y] = cellPolygon;
			}
		}

		drawHighlighting(g2);

		g2.setColor(color);
		g2.setStroke(highLightingStroke);
		g2.drawLine((int) rectangle.getX(), (int) rectangle.getY(), (int) (rectangle.getX() + 2 + localLegendWidth), (int) (rectangle.getY() + 2));
		g2.drawLine((int) (rectangle.getX() + 2 + localLegendWidth), (int) (rectangle.getY() + 2),
				(int) (rectangle.getX() + 2 + localLegendWidth + labels.length * gridWidth / 2),
				(int) (rectangle.getY() + 2 + labels.length * gridHeight / 2));
		g2.drawLine((int) (rectangle.getX() + 2 + localLegendWidth + labels.length * gridWidth / 2),
				(int) (rectangle.getY() + 2 + labels.length * gridHeight / 2), (int) (rectangle.getX() + 2 + localLegendWidth),
				(int) (rectangle.getY() + labels.length * gridHeight));
		g2.drawLine((int) rectangle.getX(), (int) (rectangle.getY() + labels.length * gridHeight), (int) (rectangle.getX() + localLegendWidth),
				(int) (rectangle.getY() + labels.length * gridHeight));
		g2.drawLine((int) rectangle.getX(), (int) rectangle.getY(), (int) rectangle.getX(), (int) (rectangle.getY() + labels.length * gridHeight));

		g2.setStroke(strokeTmp);
		g2.setFont(font);
		g2.setColor(color);
	}

	/**
	 * Traces an outline along the highlighted row/column(s): a pair
	 * ({@link #highLightedCoordinate}.length == 2) outlines both rows, a single
	 * coordinate outlines just that one.
	 */
	private void drawHighlighting(Graphics2D g2) {
		if (highLightedCoordinate == null)
			return;

		if (highLightedCoordinate.length > 1) {
			if (highLightedCoordinate[0] >= labels.length || highLightedCoordinate[1] >= labels.length)
				return;

			g2.setStroke(highLightingStroke);
			g2.setColor(Color.orange);

			int lower = Math.min(highLightedCoordinate[0], highLightedCoordinate[1]);
			int higher = Math.max(highLightedCoordinate[0], highLightedCoordinate[1]);

			for (int x = 0; x < labels.length; x++)
				for (int y = 0; y < labels.length; y++) {
					if (y < x)
						continue;

					if (y == x && (x == lower || y == higher))
						g2.drawRect((int) rectangle.getX() + 2, (int) (rectangle.getY() + 2 + gridHeight * x), (int) localLegendWidth, (int) gridHeight);

					if (x == lower)
						drawHighlightEdgesAscending(g2, x, y);

					if (x == higher)
						drawHighlightEdgesDescending(g2, x, y);
				}
		} else if (highLightedCoordinate[0] < labels.length) {
			g2.setStroke(highLightingStroke);
			g2.setColor(Color.orange);

			int index = highLightedCoordinate[0];
			for (int x = 0; x < labels.length; x++)
				for (int y = 0; y < labels.length; y++) {
					if (y < x)
						continue;

					if (y == x && x == index)
						g2.drawRect((int) rectangle.getX() + 2, (int) (rectangle.getY() + 2 + gridHeight * x), (int) localLegendWidth, (int) gridHeight);

					if (x == index)
						drawHighlightEdgesFull(g2, x, y);
				}
		}
	}

	private void drawHighlightEdgesAscending(Graphics2D g2, int x, int y) {
		double firstX = rectangle.getX() + 2 + localLegendWidth + (Math.abs(x - y) - 1.0) * (gridWidth / 2);
		double firstY = rectangle.getY() + 2 + gridHeight * x + Math.abs(y - x) * (gridHeight / 2);

		if (x == y) {
			g2.drawLine((int) (firstX + gridWidth / 2), (int) firstY, (int) (firstX + gridWidth), (int) (firstY + gridHeight / 2));
			g2.drawLine((int) (firstX + gridWidth / 2), (int) firstY, (int) (firstX + gridWidth / 2), (int) (firstY + gridHeight));
		} else {
			g2.drawLine((int) (firstX + gridWidth / 2), (int) firstY, (int) (firstX + gridWidth), (int) (firstY + gridHeight / 2));
			g2.drawLine((int) firstX, (int) (firstY + gridHeight / 2), (int) (firstX + gridWidth / 2), (int) (firstY + gridHeight));
		}
		if (y == labels.length - 1)
			g2.drawLine((int) (firstX + gridWidth), (int) (firstY + gridHeight / 2), (int) (firstX + gridWidth / 2), (int) (firstY + gridHeight));
	}

	private void drawHighlightEdgesDescending(Graphics2D g2, int x, int y) {
		if (x != y)
			return;

		double firstX = rectangle.getX() + 2 + localLegendWidth + (Math.abs(x - y) - 1.0) * (gridWidth / 2);
		double firstY = rectangle.getY() + 2 + gridHeight * x - Math.abs(y - x) * (gridHeight / 2);

		g2.drawLine((int) (firstX + gridWidth), (int) (firstY + gridHeight / 2), (int) (firstX + gridWidth / 2), (int) (firstY + gridHeight));
		g2.drawLine((int) (firstX + gridWidth / 2), (int) firstY, (int) (firstX + gridWidth / 2), (int) (firstY + gridHeight));

		for (int y2 = x - 1; y2 >= 0; y2--) {
			firstX = rectangle.getX() + 2 + localLegendWidth + (Math.abs(x - y2) - 1.0) * (gridWidth / 2);
			firstY = rectangle.getY() + 2 + gridHeight * x - Math.abs(y2 - x) * (gridHeight / 2);
			g2.drawLine((int) firstX, (int) (firstY + gridHeight / 2), (int) (firstX + gridWidth / 2), (int) firstY);
			g2.drawLine((int) (firstX + gridWidth), (int) (firstY + gridHeight / 2), (int) (firstX + gridWidth / 2), (int) (firstY + gridHeight));
			if (y2 == 0)
				g2.drawLine((int) (firstX + gridWidth), (int) (firstY + gridHeight / 2), (int) (firstX + gridWidth / 2), (int) firstY);
		}
	}

	private void drawHighlightEdgesFull(Graphics2D g2, int x, int y) {
		double firstX = rectangle.getX() + 2 + localLegendWidth + (Math.abs(x - y) - 1.0) * (gridWidth / 2);
		double firstY = rectangle.getY() + 2 + gridHeight * x + Math.abs(y - x) * (gridHeight / 2);

		if (x == y) {
			g2.drawLine((int) (firstX + gridWidth / 2), (int) firstY, (int) (firstX + gridWidth), (int) (firstY + gridHeight / 2));
			g2.drawLine((int) (firstX + gridWidth / 2), (int) firstY, (int) (firstX + gridWidth / 2), (int) (firstY + gridHeight));
			g2.drawLine((int) (firstX + gridWidth), (int) (firstY + gridHeight / 2), (int) (firstX + gridWidth / 2), (int) (firstY + gridHeight));

			for (int y2 = x - 1; y2 >= 0; y2--) {
				firstX = rectangle.getX() + 2 + localLegendWidth + (Math.abs(x - y2) - 1.0) * (gridWidth / 2);
				firstY = rectangle.getY() + 2 + gridHeight * x - Math.abs(y2 - x) * (gridHeight / 2);
				g2.drawLine((int) firstX, (int) (firstY + gridHeight / 2), (int) (firstX + gridWidth / 2), (int) firstY);
				g2.drawLine((int) (firstX + gridWidth), (int) (firstY + gridHeight / 2), (int) (firstX + gridWidth / 2), (int) (firstY + gridHeight));
				if (y2 == 0)
					g2.drawLine((int) (firstX + gridWidth), (int) (firstY + gridHeight / 2), (int) (firstX + gridWidth / 2), (int) firstY);
			}
		} else {
			g2.drawLine((int) (firstX + gridWidth / 2), (int) firstY, (int) (firstX + gridWidth), (int) (firstY + gridHeight / 2));
			g2.drawLine((int) firstX, (int) (firstY + gridHeight / 2), (int) (firstX + gridWidth / 2), (int) (firstY + gridHeight));
		}
		if (y == labels.length - 1)
			g2.drawLine((int) (firstX + gridWidth), (int) (firstY + gridHeight / 2), (int) (firstX + gridWidth / 2), (int) (firstY + gridHeight));
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;

		if (rectangle == null || data == null || data.length == 0)
			return;

		legendWidth = (int) (rectangle.getWidth() / 2);
		if (Double.isNaN(localLegendWidth))
			localLegendWidth = rectangle.getWidth() / 4;

		gridWidth = 2 * (rectangle.getWidth() - 8 - localLegendWidth) / (data.length + 1);
		gridHeight = (rectangle.getHeight() - 8) / data[0].length;

		stroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		highLightingStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	}

	/**
	 * Hit-tests a screen point against the label rows first, then the diamond
	 * cells (checked in the order drawn, so on-diagonal before off-diagonal).
	 * A label or a diagonal cell returns a single-element list with that row's
	 * index; an off-diagonal cell returns both of its indices.
	 */
	@Override
	public List<Integer> getElementsAtPoint(Point p) {
		if (labelRectangles != null)
			for (int x = 0; x < labelRectangles.length; x++)
				if (labelRectangles[x] != null && labelRectangles[x].contains(p))
					return Collections.singletonList(x);

		if (cellPolygons != null)
			for (int x = 0; x < cellPolygons.length; x++)
				for (int y = x; y < cellPolygons[x].length; y++)
					if (cellPolygons[x][y] != null && cellPolygons[x][y].contains(p))
						return x == y ? Collections.singletonList(x) : Arrays.asList(x, y);

		return null;
	}

	public Color[][] getData() {
		return data;
	}

	public void setData(Color[][] adjacencyMatrix) {
		this.data = adjacencyMatrix.clone();
	}

	public void setLegendWidth(int legendWidth) {
		this.legendWidth = legendWidth;
	}

	public int getLegendWidth() {
		return legendWidth;
	}

	public void setLabels(String[] labels) {
		this.labels = labels;
	}

	public String[] getLabels() {
		return labels;
	}

	public void setHighLightingStroke(BasicStroke stroke) {
		this.highLightingStroke = stroke;
	}

	public int[] getHighLightedCoordinate() {
		return highLightedCoordinate;
	}

	/**
	 * @param highLightedCoordinate one index to highlight a single row/column, or
	 *                              two indices to highlight both rows/columns of
	 *                              a pair; {@code null} to clear
	 */
	public void setHighLightedCoordinate(int[] highLightedCoordinate) {
		this.highLightedCoordinate = highLightedCoordinate;
	}
}
