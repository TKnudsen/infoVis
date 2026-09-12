package com.github.TKnudsen.infoVis.view.panels.scatterplot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IHighlightVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.handlers.HighlightHandler;
import com.github.TKnudsen.infoVis.view.interaction.handlers.LassoSelectionHandler;
import com.github.TKnudsen.infoVis.view.interaction.handlers.MouseButton;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.IAxisLogarithmicScale;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;

import de.javagl.selection.SelectionModel;

/**
 * <p>
 * Scatter plot matrix chart panel: a grid of individual {@link ScatterPlot}
 * cells, one per attribute pair.
 * </p>
 *
 * <p>
 * {@link #getElementsAtPoint(Point)}, {@link #getElementsInRectangle(RectangularShape)}
 * and {@link #getElementsInShape(Shape)} translate a point/shape/rectangle
 * given in this panel's own coordinate space into whichever cell's local
 * coordinate space it actually falls in (via each cell's
 * {@link ScatterPlot#getBounds()} within this panel), and delegate to that
 * cell's own selection logic -- this is the correct API for programmatic
 * queries against the whole matrix.
 * </p>
 *
 * <p>
 * It is NOT, however, how mouse-driven interaction has to be wired: the
 * individual cells are child components that fully tile this panel via
 * {@link GridLayout}, so Swing dispatches every mouse event to the cell
 * under the cursor, never to this panel itself. A
 * {@code SelectionHandler}/{@code LassoSelectionHandler} attached to this
 * panel would therefore never receive any events. Use
 * {@link #addInteraction(SelectionModel, boolean, boolean, boolean)}, which
 * wires each cell individually against one shared {@link SelectionModel}.
 * </p>
 *
 * @version 2.09
 * @since 2018
 */
public class ScatterPlotMatrixChartPanel extends InfoVisChartPanel implements IRectangleSelection<Double[]>,
		IClickSelection<Double[]>, IShapeSelection<Double[]>, ISelectionVisualizer<Double[]>,
		IHighlightVisualizer<Double[]>, ISizeEncoding<Double[]>, IAxisLogarithmicScale {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2949962927634263599L;

	private List<Double[]> data;

	private List<String> attributeNames;

	private Function<? super Double[], ? extends Paint> colorMapping;

	private ScatterPlot<Double[]>[][] infoVisScatterPlotChartPanels;

	/**
	 * Every cell holds its own, freshly-created 2-element {@code Double[]}
	 * projections of {@link #data} (one attribute pair per cell, with NaN rows
	 * filtered out independently per cell -- so identical row indices across
	 * cells do NOT generally refer to the same original row). This map recovers,
	 * for any such local projection object, the original {@link #data} row it
	 * was derived from, so that a selection made against one cell's local
	 * objects can be resolved to the same original row's identity and reflected
	 * in every other cell -- i.e. linked brushing across the matrix.
	 */
	private final Map<Double[], Double[]> localToOriginal = new IdentityHashMap<>();

	/**
	 * 
	 * @param data multi-dimensional data
	 */
	public ScatterPlotMatrixChartPanel(List<Double[]> data) {
		this(data, null, null);
	}

	/**
	 * 
	 * @param data   multi-dimensional data
	 * @param colors colors
	 */
	public ScatterPlotMatrixChartPanel(List<Double[]> data, List<Color> colors) {
		this(data, colors, null);
	}

	/**
	 * 
	 * @param data           multi-dimensional data
	 * @param colors         colors
	 * @param attributeNames attribute nameso
	 */
	public ScatterPlotMatrixChartPanel(List<Double[]> data, List<Color> colors, List<String> attributeNames) {

		if (colors != null && data.size() != colors.size())
			throw new IllegalArgumentException("InfoVisScatterPlotMatrixChartPanel: unequal list sizes.");

		this.data = Collections.unmodifiableList(data);
		this.colorMapping = new ColorEncodingFunction<>(data, colors, Color.BLACK);

		this.attributeNames = attributeNames;

		refreshScatterplotMatrix();
	}

	public ScatterPlotMatrixChartPanel(ColorEncodingFunction<Double[]> colorMapping) {
		this.colorMapping = colorMapping;

		Set<Double[]> data = colorMapping.getData();
		List<Double[]> temp = new CopyOnWriteArrayList<>(data);
		this.data = Collections.unmodifiableList(temp);

		refreshScatterplotMatrix();
	}

	public ScatterPlotMatrixChartPanel(List<Double[]> data, ColorEncodingFunction<Double[]> colorMapping) {
		this.data = Collections.unmodifiableList(data);
		this.colorMapping = colorMapping;

		refreshScatterplotMatrix();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	@SuppressWarnings("unchecked")
	private void refreshScatterplotMatrix() {
		if (data == null)
			return;

		if (data.isEmpty())
			throw new IllegalArgumentException("ScatterPlotMatrixChartPanel: data must not be empty.");

		if (attributeNames == null)
			attributeNames = new ArrayList<>();

		if (attributeNames.size() > data.get(0).length)
			throw new IllegalArgumentException("ScatterPlotMatrixChartPanel: attributeNames.size() ("
					+ attributeNames.size() + ") exceeds the data's dimensionality (" + data.get(0).length + ").");

		while (attributeNames.size() < data.get(0).length)
			attributeNames.add("");

		infoVisScatterPlotChartPanels = new ScatterPlot[attributeNames.size()][attributeNames.size()];

		localToOriginal.clear();

		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++) {

				List<Double[]> localdata = new ArrayList<>();
				List<Paint> localColors = new ArrayList<>();

				for (int i = 0; i < data.size(); i++)
					if (Double.isNaN(data.get(i)[x]) || Double.isNaN(data.get(i)[y]))
						continue;
					else {
						Double[] localPoint = new Double[] { data.get(i)[x], data.get(i)[y] };
						localdata.add(localPoint);
						localColors.add(colorMapping.apply(data.get(i)));
						localToOriginal.put(localPoint, data.get(i));
					}

				ScatterPlot<Double[]> panel = ScatterPlots.createForDoubles(localdata, localColors);

				infoVisScatterPlotChartPanels[x][y] = panel;
			}

		removeAll();
		this.setLayout(new GridLayout(attributeNames.size(), attributeNames.size()));

		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++)
				add(infoVisScatterPlotChartPanels[x][y]);

		setBackground(null);
	}

	/**
	 * @return the child cell whose bounds (in this panel's own coordinate
	 *         space) contain {@code p}, or null if none does
	 */
	private ScatterPlot<Double[]> cellAt(Point p) {
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++) {
				ScatterPlot<Double[]> cell = infoVisScatterPlotChartPanels[x][y];
				if (cell != null && cell.getBounds().contains(p))
					return cell;
			}
		return null;
	}

	@Override
	public List<Double[]> getElementsAtPoint(Point p) {
		if (p == null)
			return new ArrayList<>();

		// p arrives in this (outer) panel's coordinate space -- find which single
		// cell it actually falls in (cells don't overlap under GridLayout, so at
		// most one can contain it) and translate it into that cell's own local
		// coordinate space before delegating, instead of the previous behavior of
		// handing the same untranslated point to every cell.
		ScatterPlot<Double[]> cell = cellAt(p);
		if (cell == null)
			return new ArrayList<>();

		Rectangle cellBounds = cell.getBounds();
		Point local = new Point(p.x - cellBounds.x, p.y - cellBounds.y);
		return cell.getElementsAtPoint(local);
	}

	@Override
	public List<Double[]> getElementsInRectangle(RectangularShape rectangle) {
		List<Double[]> selectedElements = new ArrayList<>();

		if (rectangle == null)
			return selectedElements;

		// rectangle arrives in this (outer) panel's coordinate space and may span
		// several cells (e.g. a large rubber-band selection); for each cell it
		// actually overlaps, translate the same rectangle into that cell's own
		// local coordinate space before delegating. A cell's own screenPoints
		// never fall outside its own bounds, so translating without also clipping
		// to the cell's bounds is still geometrically correct.
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++) {
				ScatterPlot<Double[]> cell = infoVisScatterPlotChartPanels[x][y];
				if (cell == null)
					continue;

				Rectangle cellBounds = cell.getBounds();
				if (!rectangle.intersects(cellBounds))
					continue;

				Rectangle2D.Double localRect = new Rectangle2D.Double(rectangle.getMinX() - cellBounds.x,
						rectangle.getMinY() - cellBounds.y, rectangle.getWidth(), rectangle.getHeight());

				selectedElements.addAll(cell.getElementsInRectangle(localRect));
			}

		return selectedElements;
	}

	@Override
	public List<Double[]> getElementsInShape(Shape shape) {
		List<Double[]> selectedElements = new ArrayList<>();

		if (shape == null)
			return selectedElements;

		// Same translation principle as getElementsInRectangle, generalized to an
		// arbitrary shape (e.g. a lasso polygon) via AffineTransform instead of
		// simple coordinate subtraction.
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++) {
				ScatterPlot<Double[]> cell = infoVisScatterPlotChartPanels[x][y];
				if (cell == null)
					continue;

				Rectangle cellBounds = cell.getBounds();
				if (!shape.intersects(cellBounds))
					continue;

				Shape localShape = AffineTransform.getTranslateInstance(-cellBounds.x, -cellBounds.y)
						.createTransformedShape(shape);

				selectedElements.addAll(cell.getElementsInShape(localShape));
			}

		return selectedElements;
	}

	@Override
	public void setSelectedFunction(Function<? super Double[], Boolean> selectedFunction) {
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++)
				if (infoVisScatterPlotChartPanels[x][y] != null)
					infoVisScatterPlotChartPanels[x][y].setSelectedFunction(selectedFunction);
	}

	@Override
	public void setHighlightedFunction(Function<? super Double[], Boolean> highlightedFunction) {
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++)
				if (infoVisScatterPlotChartPanels[x][y] != null)
					infoVisScatterPlotChartPanels[x][y].setHighlightedFunction(highlightedFunction);
	}

	@Override
	public void setSizeEncodingFunction(Function<? super Double[], Double> sizeEncodingFunction) {
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++)
				if (infoVisScatterPlotChartPanels[x][y] != null)
					infoVisScatterPlotChartPanels[x][y].setSizeEncodingFunction(sizeEncodingFunction);
	}

	public void setDrawAxes(boolean drawAxes) {
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++)
				if (infoVisScatterPlotChartPanels[x][y] != null) {
					infoVisScatterPlotChartPanels[x][y].setDrawXAxis(drawAxes);
					infoVisScatterPlotChartPanels[x][y].setDrawYAxis(drawAxes);
				}
	}

	/**
	 * @return the logarithmic-scale state of the first cell (0,0); every cell is
	 *         kept in sync by {@link #setLogarithmicScale(boolean)}
	 */
	@Override
	public boolean isLogarithmicScale() {
		return infoVisScatterPlotChartPanels[0][0] != null && infoVisScatterPlotChartPanels[0][0].isLogarithmicScale();
	}

	/**
	 * Broadcasts the logarithmic-scale setting to every cell of the matrix (both
	 * axes of each cell, via {@link ScatterPlot#setLogarithmicScale(boolean)}).
	 */
	@Override
	public void setLogarithmicScale(boolean logarithmicScale) {
		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++)
				if (infoVisScatterPlotChartPanels[x][y] != null)
					infoVisScatterPlotChartPanels[x][y].setLogarithmicScale(logarithmicScale);
	}

	/**
	 * Wires interactive, matrix-wide linked brushing: click/rectangle/lasso
	 * selection made against any one cell is resolved to the identity of the
	 * original {@link #data} rows (via {@link #localToOriginal}) and reflected
	 * across every other cell showing the same rows.
	 *
	 * <p>
	 * Because the individual cells are child components that fully cover this
	 * panel (see the class Javadoc), the handlers cannot be attached to this
	 * panel itself -- each cell gets its own {@code SelectionHandler}/
	 * {@code LassoSelectionHandler}, attached directly to that cell. Their
	 * click/rectangle/shape selection sources are adapters that call the cell's
	 * own (correct, local-coordinate) selection logic and then translate the
	 * resulting local projection objects into the original row objects via
	 * {@link #localToOriginal}, before handing them to the one
	 * {@code selectionModel} shared by all cells. Each cell's
	 * {@link ScatterPlot#setSelectedFunction(Function)} is likewise given a
	 * function that performs the same local-to-original translation before
	 * checking {@code selectionModel.isSelected(...)}, so a row selected via one
	 * cell highlights correctly in every cell, not just the one that was
	 * clicked.
	 * </p>
	 *
	 * @param selectionModel     the selection model shared by all cells,
	 *                           operating on original {@link #data} row objects
	 * @param clickSelection     whether to enable click selection
	 * @param rectangleSelection whether to enable rectangle (rubber-band)
	 *                           selection
	 * @param lassoSelection     whether to enable lasso selection (right mouse
	 *                           button)
	 */
	public void addInteraction(SelectionModel<Double[]> selectionModel, boolean clickSelection,
			boolean rectangleSelection, boolean lassoSelection) {
		// A cell's own SelectionHandler repaints only that cell (on the mouse
		// event that triggered the selection change). Since a selection made in
		// one cell must show up in every OTHER cell too, and nothing else drives
		// their repaint until the mouse happens to move over them, explicitly
		// repaint every cell whenever the shared selection actually changes.
		selectionModel.addSelectionListener(selectionEvent -> {
			for (int x = 0; x < attributeNames.size(); x++)
				for (int y = 0; y < attributeNames.size(); y++) {
					ScatterPlot<Double[]> cell = infoVisScatterPlotChartPanels[x][y];
					if (cell != null)
						cell.repaint();
				}
		});

		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++) {
				ScatterPlot<Double[]> cell = infoVisScatterPlotChartPanels[x][y];
				if (cell == null)
					continue;

				if (clickSelection || rectangleSelection) {
					SelectionHandler<Double[]> selectionHandler = new SelectionHandler<>(selectionModel);
					selectionHandler.attachTo(cell);

					if (clickSelection)
						selectionHandler.setClickSelection(p -> toOriginal(cell.getElementsAtPoint(p)));

					if (rectangleSelection)
						selectionHandler
								.setRectangleSelection(r -> toOriginal(cell.getElementsInRectangle(r)));

					cell.addChartPainter(new ChartPainter() {
						@Override
						public void draw(Graphics2D g2) {
							selectionHandler.draw(g2);
						}
					});
				}

				if (lassoSelection) {
					LassoSelectionHandler<Double[]> lassoSelectionHandler = new LassoSelectionHandler<>(
							selectionModel, MouseButton.RIGHT);
					lassoSelectionHandler.attachTo(cell);
					lassoSelectionHandler.setShapeSelection(shape -> toOriginal(cell.getElementsInShape(shape)));

					cell.addChartPainter(new ChartPainter() {
						@Override
						public void draw(Graphics2D g2) {
							lassoSelectionHandler.draw(g2);
						}
					});
				}

				cell.setSelectedFunction(localPoint -> {
					Double[] original = localToOriginal.get(localPoint);
					return original != null && selectionModel.isSelected(original);
				});
			}
	}

	/**
	 * Wires interactive, matrix-wide linked hover highlighting: mousing over a
	 * point in any one cell resolves it to the identity of the original
	 * {@link #data} row (via {@link #localToOriginal}, same as
	 * {@link #addInteraction(SelectionModel, boolean, boolean, boolean)}) and
	 * reflects that as highlighted across every other cell showing the same row.
	 * <p>
	 * Deliberately reuses {@link SelectionModel} for {@code highlightModel}
	 * rather than a dedicated "highlight model" type: the interface itself (a
	 * named subset of elements plus change events) has no notion of how
	 * membership was decided, so the same proven plumbing already used for
	 * click/rectangle/lasso selection works equally well for a hover-driven
	 * subset. Pass a model separate from the one given to
	 * {@link #addInteraction(SelectionModel, boolean, boolean, boolean)} so
	 * selection and highlighting remain independent states.
	 *
	 * @param highlightModel the selection model used to hold the currently
	 *                       hover-highlighted row (if any), shared by all cells,
	 *                       operating on original {@link #data} row objects
	 */
	public void addHighlightInteraction(SelectionModel<Double[]> highlightModel) {
		// same rationale as addInteraction's selectionModel listener: a cell's own
		// HighlightHandler repaints only that cell, but a hover in one cell must
		// be reflected in every other cell too.
		highlightModel.addSelectionListener(selectionEvent -> {
			for (int x = 0; x < attributeNames.size(); x++)
				for (int y = 0; y < attributeNames.size(); y++) {
					ScatterPlot<Double[]> cell = infoVisScatterPlotChartPanels[x][y];
					if (cell != null)
						cell.repaint();
				}
		});

		for (int x = 0; x < attributeNames.size(); x++)
			for (int y = 0; y < attributeNames.size(); y++) {
				ScatterPlot<Double[]> cell = infoVisScatterPlotChartPanels[x][y];
				if (cell == null)
					continue;

				HighlightHandler<Double[]> highlightHandler = new HighlightHandler<>(highlightModel);
				highlightHandler.setHoverSelection(p -> toOriginal(cell.getElementsAtPoint(p)));
				highlightHandler.attachTo(cell);

				cell.setHighlightedFunction(localPoint -> {
					Double[] original = localToOriginal.get(localPoint);
					return original != null && highlightModel.isSelected(original);
				});
			}
	}

	/**
	 * Translates a cell's own local (2-element, per-attribute-pair projection)
	 * selection results into the original {@link #data} row objects they were
	 * derived from, via {@link #localToOriginal}, dropping any that (should not
	 * happen, but defensively) have no known origin.
	 */
	private List<Double[]> toOriginal(List<Double[]> localElements) {
		List<Double[]> result = new ArrayList<>();
		for (Double[] local : localElements) {
			Double[] original = localToOriginal.get(local);
			if (original != null)
				result.add(original);
		}
		return result;
	}
}