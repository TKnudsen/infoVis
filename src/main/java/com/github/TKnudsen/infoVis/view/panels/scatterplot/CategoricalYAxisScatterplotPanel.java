package com.github.TKnudsen.infoVis.view.panels.scatterplot;

import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.RectangularShape;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IPanning;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.interaction.IZooming;
import com.github.TKnudsen.infoVis.view.painters.scatterplot.CategoricalYAxisScatterplotPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncoding;
import com.github.TKnudsen.infoVis.view.visualChannels.size.ISizeEncoding;

/**
 * <p>
 * Chart panel wrapping a {@link CategoricalYAxisScatterplotPainter}. Unlike
 * {@link ScatterPlot}, which relies on an external numerical-axis panel to
 * drive its position encoding, {@link CategoricalYAxisScatterplotPainter}
 * manages its own x (numerical) and y (categorical) axes internally, so this
 * panel only needs to host it and delegate the typical scatterplot interfaces.
 * </p>
 *
 * @since 2026
 * @version 1.01 in August 2026
 */
public class CategoricalYAxisScatterplotPanel<T> extends InfoVisChartPanel
		implements IRectangleSelection<T>, IShapeSelection<T>, IClickSelection<T>, ISelectionVisualizer<T>,
		IColorEncoding<T>, ISizeEncoding<T>, IZooming, IPanning {

	private static final long serialVersionUID = 1L;

	protected final CategoricalYAxisScatterplotPainter<T> painter;

	/**
	 * @param data                  the data elements to plot
	 * @param colorMapping          maps each element to its point color; a null
	 *                              result falls back to the painter's own paint
	 * @param worldPositionMappingX maps each element to its x value in data (world)
	 *                              space
	 * @param categoryMappingY      maps each element to its category label on the y
	 *                              axis
	 */
	public CategoricalYAxisScatterplotPanel(List<T> data, Function<? super T, ? extends Paint> colorMapping,
			Function<? super T, Double> worldPositionMappingX, Function<? super T, String> categoryMappingY) {
		this.painter = new CategoricalYAxisScatterplotPainter<>(data, colorMapping, worldPositionMappingX,
				categoryMappingY);

		addChartPainter(painter);

		setBackground(null);
	}

	@Override
	public List<T> getElementsInRectangle(RectangularShape rectangle) {
		return painter.getElementsInRectangle(rectangle);
	}

	@Override
	public List<T> getElementsInShape(Shape shape) {
		return painter.getElementsInShape(shape);
	}

	@Override
	public List<T> getElementsAtPoint(Point p) {
		return painter.getElementsAtPoint(p);
	}

	@Override
	public void setSelectedFunction(Function<? super T, Boolean> selectedFunction) {
		painter.setSelectedFunction(selectedFunction);
	}

	@Override
	public void setSizeEncodingFunction(Function<? super T, Double> sizeEncodingFunction) {
		painter.setSizeEncodingFunction(sizeEncodingFunction);
	}

	@Override
	public void setColorEncodingFunction(Function<? super T, ? extends Paint> colorEncodingFunction) {
		painter.setColorEncodingFunction(colorEncodingFunction);
	}

	public Function<? super T, String> getToolTipMapping() {
		return painter.getToolTipMapping();
	}

	public void setToolTipMapping(Function<? super T, String> toolTipMapping) {
		painter.setToolTipMapping(toolTipMapping);
	}

	public CategoricalYAxisScatterplotPainter<T> getPainter() {
		return painter;
	}

	@Override
	public void zoom(Point location, int zoomCount, boolean zoomX, boolean zoomY) {
		painter.zoom(location, zoomCount, zoomX, zoomY);

		repaint();
	}

	@Override
	public void resetZoom() {
		painter.resetZoom();

		repaint();
	}

	/**
	 * Not wired by default: {@link ScatterPlots#addPanInteraction} drives it via
	 * the left mouse button, same as rectangle selection, so the two would fight
	 * over the same drag if both were attached.
	 */
	@Override
	public void pan(int deltaX, int deltaY) {
		painter.pan(deltaX, deltaY);

		repaint();
	}
}
