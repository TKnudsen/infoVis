package com.github.TKnudsen.infoVis.view.painters.piechart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Basic pie chart painter
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class PieChartPainter extends ChartPainter implements IClickSelection<Integer>, ISelectionVisualizer<Integer> {

	// data
	protected final List<Double> piecesRelative;
	protected final List<Color> colors;

	// visuals
	protected List<Shape> arcSegments;
	protected Rectangle2D.Double square;
	protected Arc2D.Double arc;

	// interaction
	private Function<? super Integer, Boolean> selectedFunction;

	public PieChartPainter(List<Double> pieces, List<Color> colors) {
		Objects.requireNonNull(pieces);
		Objects.requireNonNull(colors);

		if (pieces.size() != colors.size())
			throw new IllegalStateException(
					"PieChartPainter: size of input data must be equal (" + pieces.size() + "!=" + colors.size() + ")");

		if (pieces.isEmpty())
			throw new IllegalStateException("PieChartPainter: ists are empty!");

		double sum = pieces.stream().mapToDouble(Double::doubleValue).sum();

		List<Double> fract = new ArrayList<Double>();
		for (Double d : pieces)
			fract.add(MathFunctions.linearScale(0, sum, d, false));
		this.piecesRelative = Collections.unmodifiableList(fract);

		this.colors = Collections.unmodifiableList(colors);

		// visuals
		square = new Rectangle2D.Double();
		arc = new Arc2D.Double();
		setDrawOutline(true);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		drawPie(g2);
	}

	protected void drawPie(Graphics2D g2) {
		if (arcSegments == null)
			return;

		Color c = g2.getColor();

		for (int i = 0; i < piecesRelative.size(); i++)
			drawPieSegment(g2, i);

		if (isDrawOutline()) {
			g2.setPaint(getBorderPaint());
			g2.draw(arc);
		}

		g2.setColor(c);
	}

	protected void drawPieSegment(Graphics2D g2, Integer index) {
		if (arcSegments == null)
			return;

		Stroke s = g2.getStroke();

		Shape segment = arcSegments.get(index);
		if (segment == null)
			return;

		g2.setColor(colors.get(index));
		g2.fill(segment);

		if (selectedFunction != null && selectedFunction.apply(index)) {
			g2.setStroke(DisplayTools.thickStroke);
			g2.setPaint(getBorderPaint());
			g2.draw(segment);
		}

		g2.setStroke(s);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null)
			return;

		double width = rectangle.getWidth();
		double height = rectangle.getHeight();
		double minX = rectangle.getX();
		double minY = rectangle.getY();

		double edgeLength = Math.min(width, height) - 2;

		square.setRect(minX + (width - edgeLength) * 0.5 + 1, minY + (height - edgeLength) * 0.5 + 1, edgeLength,
				edgeLength);
		arc.setArc(square, 90, 360, Arc2D.OPEN);

		arcSegments = new ArrayList<>();

		double arcStart = 90;
		for (int i = 0; i < piecesRelative.size(); i++) {
			double angle = piecesRelative.get(i) * 360;
			Arc2D.Double segment = new Arc2D.Double(square, arcStart, -angle, Arc2D.PIE);
			Area seg = new Area(segment);
			arcSegments.add(seg);

			arcStart -= angle;
		}
	}

	@Override
	public List<Integer> getElementsAtPoint(Point p) {
		if (p == null)
			return null;

		List<Integer> indices = new ArrayList<>();

		for (int i = 0; i < arcSegments.size(); i++) {
			Shape segment = arcSegments.get(i);
			if (segment.contains(p))
				indices.add(new Integer(i));
		}

		return indices;
	}

	public Function<? super Integer, Boolean> getSelectedFunction() {
		return this.selectedFunction;
	}

	@Override
	public void setSelectedFunction(Function<? super Integer, Boolean> selectedFunction) {
		this.selectedFunction = selectedFunction;
	}
}
