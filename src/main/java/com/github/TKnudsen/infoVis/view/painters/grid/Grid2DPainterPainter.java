package com.github.TKnudsen.infoVis.view.painters.grid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.TKnudsen.infoVis.view.interaction.ITooltip;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.categorical.XAxisCategoricalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.categorical.XYAxisCategoricalChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.categorical.YAxisCategoricalPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.tools.ColorTools;
import com.github.TKnudsen.infoVis.view.tools.Rectangle2DTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Paints a grid of ChartPainters.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 2.01
 */
public class Grid2DPainterPainter<T extends ChartPainter>
		extends XYAxisCategoricalChartPainter<List<String>, List<String>> implements ITooltip, Iterable<T> {

	protected T[][] painters;

	protected Rectangle2D[][] grid;

	private double gridSpacing = Double.NaN;
	private boolean enableToolTipping = true;

	protected String[][] toolTipStringGrid;

	public Grid2DPainterPainter(T[][] painters) {
		this(painters, null);

		setDrawXAxis(false);
		setDrawYAxis(false);

		yAxisPainter.setDrawInnerAxisLegend(false);
		yAxisPainter.setDrawAxisBetweenAxeMarkersOnly(false);
	}

	public Grid2DPainterPainter(T[][] painters, final List<String> axisLabels) {
		this.painters = painters;

		initializePainters();

		initializeXAxisPainter(axisLabels);

		initializeYAxisPainter(axisLabels);

		yAxisPainter.setDrawInnerAxisLegend(true);
		yAxisPainter.setDrawAxisBetweenAxeMarkersOnly(true);
		yAxisPainter.setInvertAxisLabels(false);
	}

	/**
	 * The Grid2DPainter manages drawing some of the visual attributes of the given
	 * painters.
	 */
	protected void initializePainters() {
		if (painters == null)
			return;

		for (int i = 0; i < painters.length; i++)
			for (int j = 0; j < painters[i].length; j++)
				if (painters[i][j] != null) {
					painters[i][j].setDrawOutline(false);
				}
	}

	public void setToolTipStringGrid(String[][] stringGrid) {
		if (stringGrid.length != painters.length && painters[0].length != stringGrid[0].length)
			throw new IllegalArgumentException("toolTipGrid Sizes != PainterGrid Sizes!");
		toolTipStringGrid = stringGrid;
	}

	@Override
	protected void initializeXAxisPainter(List<String> labels) {
		xAxisPainter = new XAxisCategoricalPainter<List<String>>(labels);
	}

	@Override
	protected void initializeYAxisPainter(List<String> labels) {
		yAxisPainter = new YAxisCategoricalPainter<List<String>>(labels);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		drawChart(g2);
	}

	@Override
	public void drawChart(Graphics2D g2) {
		Color c = g2.getColor();

		if (painters != null)
			for (int i = 0; i < painters.length; i++)
				for (int j = 0; j < painters[i].length; j++)
					if (painters[i][j] != null)
						painters[i][j].draw(g2);

		// draw outlines of the grid elements
		if (drawOutline && rectangle != null && getBorderPaint() != null)
			for (int i = 0; i < grid.length; i++)
				for (int j = 0; j < grid[i].length; j++) {
					if (grid[i][j] == null)
						continue;

					// if grid is too small, the outline transparency is reduced
					float cellSize = (float) Math.min(rectangle.getWidth() / grid.length,
							rectangle.getHeight() / grid[0].length);
					float alpha = 1.0f;
					if (cellSize > 10)
						alpha = 0.66f;
					else if (cellSize > 5)
						alpha = 0.33f;
					else if (cellSize > 2)
						alpha = 0.16f;
					else if (cellSize > 1)
						alpha = 0.1f;
					else
						alpha = 0.05f;
					g2.setColor(ColorTools.setAlpha(getBorderPaint(), alpha));
					g2.draw(grid[i][j]);
				}

		// draw outline of the entire grid
		if (drawOutline && rectangle != null) {
			g2.setPaint(getBorderPaint());
			g2.draw(rectangle);
		}

		g2.setColor(c);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		recalculateGrid();
	}

	public boolean isToolTipping() {
		return enableToolTipping;
	}

	@Override
	public void setToolTipping(boolean enableToolTipping) {
		this.enableToolTipping = enableToolTipping;
	}

	@Override
	public ChartPainter getTooltip(Point p) {
		if (enableToolTipping)
			if (grid != null)
				for (int i = 0; i < grid.length; i++)
					for (int j = 0; j < grid[i].length; j++)
						if (grid[i][j].contains(p)) {

							if (painters[i][j] instanceof ITooltip) {
								ChartPainter painter = ((ITooltip) painters[i][j]).getTooltip(p);
								if (painter != null)
									return painter;
							}

							if (toolTipStringGrid != null) {
								StringPainter sr = new StringPainter(toolTipStringGrid[i][j]);
								sr.setRectangle(new Rectangle2D.Double(p.getX() - ((String) sr.getData()).length() * 3,
										p.getY() - 25, ((String) sr.getData()).length() * 6, 20));
								return sr;
							}
						}
		return null;
	}

	public int[] getGridIndex(Point p) {
		if (enableToolTipping)
			if (grid != null)
				for (int i = 0; i < grid.length; i++)
					for (int j = 0; j < grid[i].length; j++)
						if (grid[i][j].contains(p)) {
							return new int[] { i, j };
						}
		return null;
	}

	public void setGridBackgroundColor(Color backgroundColor) {
		super.setBackgroundPaint(backgroundColor);
	}

	@Override
	public void setColor(Color borderColor) {
		super.setColor(borderColor);
		if (painters != null)
			for (int i = 0; i < painters.length; i++)
				for (int j = 0; j < painters[i].length; j++)
					if (painters[i][j] != null)
						painters[i][j].setColor(borderColor);
	}

	@Override
	public void setBackgroundPaint(Paint backgroundColor) {
		super.setBackgroundPaint(backgroundColor);

		if (painters != null)
			for (int i = 0; i < painters.length; i++)
				for (int j = 0; j < painters[i].length; j++)
					if (painters[i][j] != null)
						painters[i][j].setBackgroundPaint(backgroundColor);
	}

	@Override
	public void setBorderPaint(Paint borderColor) {
		super.setBorderPaint(borderColor);

		if (painters != null)
			for (int i = 0; i < painters.length; i++)
				for (int j = 0; j < painters[i].length; j++)
					if (painters[i][j] != null)
						painters[i][j].setBorderPaint(borderColor);
	}

	@Override
	public void setFontColor(Color fontColor) {
		super.setFontColor(fontColor);

		if (painters != null)
			for (int i = 0; i < painters.length; i++)
				for (int j = 0; j < painters[i].length; j++)
					if (painters[i][j] != null)
						painters[i][j].setFontColor(fontColor);
	}

	protected void recalculateGrid() {
		grid = null;

		if (rectangle == null || chartRectangle == null)
			return;

		if (painters == null)
			return;

		double gridSpacing = this.gridSpacing;
		if (Double.isNaN(gridSpacing) && painters.length > 0 && painters[0].length > 0)
			gridSpacing = Rectangle2DTools.calculateSpacingValue(chartRectangle.getWidth(), chartRectangle.getHeight(),
					painters.length, painters[0].length);

		if (painters.length > 0 && painters[0].length > 0)
			grid = Rectangle2DTools.createRectangleMatrix(chartRectangle, painters.length, painters[0].length,
					gridSpacing);

		if (grid != null)
			for (int i = 0; i < grid.length; i++)
				for (int j = 0; j < grid[i].length; j++)
					if (painters[i][j] != null)
						painters[i][j].setRectangle(grid[i][j]);
	}

	public double getGridSpacing() {
		return gridSpacing;
	}

	public void setGridSpacing(double offset) {
		this.gridSpacing = offset;

		recalculateGrid();
	}

	public Rectangle2D[][] getGrid() {
		return grid;
	}

	public void setGrid(Rectangle2D.Double[][] grid) {
		this.grid = grid;
		if (grid == null || grid.length == 0 || grid[0].length == 0)
			return;
		super.setRectangle(new Rectangle2D.Double(grid[0][0].getMinX(), grid[0][0].getMinY(),
				grid[grid.length - 1][grid[0].length - 1].getMaxX() - grid[0][0].getMinX(),
				grid[grid.length - 1][grid[0].length - 1].getMaxY() - grid[0][0].getMinY()));
		for (int i = 0; i < grid.length; i++)
			for (int j = 0; j < grid[i].length; j++)
				if (painters[i][j] != null)
					painters[i][j].setRectangle(grid[i][j]);
	}

	public T[][] getPainters() {
		return painters;
	}

	public T getPainter(int i, int j) {
		return painters[i][j];
	}

	public void setPainters(T[][] painters) {
		this.painters = painters;
		initializePainters();
		recalculateGrid();
	}

	public void setPainter(T painter, int i, int j) {
		if (painters == null)
			throw new NullPointerException();
		if (painters.length < i || painters[0].length < j)
			throw new ArrayIndexOutOfBoundsException();
		this.painters[i][j] = painter;
		initializePainters();
		recalculateGrid();
	}

	@Override
	public Iterator<T> iterator() {
		List<T> painterList = new ArrayList<>();

		if (painters != null)
			for (int i = 0; i < painters.length; i++)
				for (int j = 0; j < painters[i].length; j++)
					painterList.add(painters[i][j]);

		return painterList.iterator();
	}

}
