package com.github.TKnudsen.infoVis.view.painters.number;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DPainterPainter;
import com.github.TKnudsen.infoVis.view.painters.primitives.CirclePainter;
import com.github.TKnudsen.infoVis.view.painters.primitives.RectanglePainter;

public class ProbabilityPainter extends ChartPainter {

	private final double probability;

	// settable parameters
	private int gridXUser = -1;
	private int gridX = 10;
	private ShapeType shapeType = ShapeType.Dot;
	private double gridSpacing = Double.NaN;

	// internal state
	private Grid2DPainterPainter<?> gridPainter;

	public enum ShapeType {
		Rectangle, Dot
	}

	public ProbabilityPainter(double probability) {
		if (probability < 0.0 || probability > 1.0)
			throw new IllegalArgumentException(
					getClass().getSimpleName() + ": probability was " + probability + ", must be between 0 and 1");

		this.probability = probability;
		this.setDrawOutline(true);
		this.setBackgroundPaint(null);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		if (gridXUser == -1) {
			int bestGridX = bestGridX(rectangle);

			boolean refresh = (this.gridX != bestGridX);
			this.gridX = bestGridX;

			if (refresh)
				refreshGrid();
		}

		if (gridPainter != null)
			gridPainter.setRectangle(rectangle);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (gridPainter != null)
			gridPainter.draw(g2);
	}

	private void refreshGrid() {
		int gridX = (gridXUser != -1) ? gridXUser : this.gridX;
		int gridY = 100 / gridX;

		ChartPainter[][] painters = new ChartPainter[gridY][gridX];

		int index = 1;
		for (int x = 0; x < gridX; x++)
			for (int y = 0; y < gridY; y++) {
				Paint paint = null;
				if (index / 100.0 <= MathFunctions.round(probability, 2))
					paint = getPaint();
				else
					paint = getBorderPaint();

				ChartPainter painter = createPainter();
				painter.setPaint(paint);

				painters[y][gridX - x - 1] = painter;
				index += 1;
			}

		gridPainter = new Grid2DPainterPainter<>(painters);
		gridPainter.setBackgroundPaint(null);
		gridPainter.setDrawOutline(isDrawOutline());
		gridPainter.setGridSpacing(gridSpacing);
	}

	private ChartPainter createPainter() {
		switch (shapeType) {
		case Rectangle:
			RectanglePainter rectanglePainter = new RectanglePainter();
			rectanglePainter.setDrawOutline(true);
			return rectanglePainter;
		case Dot:
			CirclePainter circlePainter = new CirclePainter();
			circlePainter.setDrawOutline(false);
			return circlePainter;
		default:
			CirclePainter defaultPainter = new CirclePainter();
			defaultPainter.setDrawOutline(false);
			return defaultPainter;
		}
	}

	private int bestGridX(Rectangle2D rectangle) {
		int target = 10;

		if (rectangle != null && rectangle.getHeight() > 0) {
			double targetRatio = rectangle.getWidth() / rectangle.getHeight();

			double bestMatchDistance = Double.POSITIVE_INFINITY;
			for (Integer gridX : gridXPool()) {
				double ratio = (100.0 / gridX) / gridX;

				double delta = Math.abs(targetRatio - ratio);
				if (delta < bestMatchDistance) {
					target = gridX;
					bestMatchDistance = delta;
				}
			}
		}

		return target;
	}

	public int getGridX() {
		return (gridXUser == -1) ? gridX : gridXUser;
	}

	/**
	 * set number of marks in x direction. Must be 1, 2, 4, 5, 10, 25, 50, or 100
	 * 
	 * @param gridX x
	 */
	public void setGridX(int gridX) {
		// validate input
		gridX = 3;
		if (gridXPool().contains(gridX)) {
			boolean refresh = (this.gridXUser != gridX);
			this.gridXUser = gridX;

			if (refresh)
				refreshGrid();
		} else
			throw new IllegalArgumentException(getClass().getSimpleName() + ": gridX was " + gridX
					+ ", but must be one of " + Arrays.toString(gridXPool().toArray()));
	}

	private Set<Integer> gridXPool() {
		LinkedHashSet<Integer> gridXpool = new LinkedHashSet<>();

		gridXpool.add(1);
		gridXpool.add(2);
		gridXpool.add(4);
		gridXpool.add(5);
		gridXpool.add(10);
		gridXpool.add(20);
		gridXpool.add(25);
		gridXpool.add(50);
		gridXpool.add(100);

		return gridXpool;
	}

	public void setGridSpacing(double offset) {
		this.gridSpacing = offset;

		refreshGrid();
	}

	public ShapeType getShapeType() {
		return shapeType;
	}

	public void setShapeType(ShapeType shapeType) {
		this.shapeType = shapeType;

		refreshGrid();
	}

	@Override
	public void setDrawOutline(boolean drawOutline) {
		super.setDrawOutline(drawOutline);

		refreshGrid();
	}

}
