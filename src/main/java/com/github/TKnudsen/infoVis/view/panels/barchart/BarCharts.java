package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;
import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter;
import com.github.TKnudsen.infoVis.view.painters.grid.Grid2DPainterPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.panels.bins.AbstractBinnedDistributionPanel;

import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * <p>
 * Factory + helper utilities for the new categorical BarChart panels.
 * 
 * Supports: - Horizontal and Vertical variants - Legends - Styling
 * (border/selection paint, tooltipping, grid spacing) - Interaction wiring
 * (click + rectangle selection)
 * 
 * Notes: - BarChart is item-based (selection domain is T / T, not bin index). -
 * Styling operations are applied to all three bar chart painter layers if the
 * component is an AbstractBinnedDistributionPanel.
 * </p>
 *
 * @version 2.01
 * @since 2016
 */
public final class BarCharts {

	public static final Color DEFAULT_COLOR = Color.GRAY;
	public static final Color DEFAULT_FILTER_COLOR = Color.DARK_GRAY;

	// ============================================================
	// FACTORY METHODS
	// ============================================================

	/**
	 * Minimal default vertical categorical bar chart (alphabetical order,
	 * missing-bin on).
	 */
	public static <T> BarChartVertical<T> createBarChartVertical(Collection<? extends T> data,
			java.util.function.Function<? super T, String> worldToCategoryMapping) {

		return new BarChartVertical<>(data, worldToCategoryMapping);
	}

	/**
	 * Minimal default horizontal categorical bar chart (alphabetical order,
	 * missing-bin on).
	 */
	public static <T> BarChartHorizontal<T> createBarChartHorizontal(Collection<? extends T> data,
			java.util.function.Function<? super T, String> worldToCategoryMapping) {

		return new BarChartHorizontal<>(data, worldToCategoryMapping);
	}

	/**
	 * Full constructor wrapper (vertical).
	 */
	public static <T> BarChartVertical<T> createBarChartVertical(Collection<? extends T> data,
			java.util.function.Function<? super T, String> worldToCategoryMapping,
			BarChart.CategoryOrder categoryOrder, List<String> customOrder, boolean includeMissingBin,
			String missingLabel, Color allDataColor, Color filterColor) {

		return new BarChartVertical<>(data, worldToCategoryMapping, categoryOrder,
				customOrder, includeMissingBin, missingLabel, allDataColor, filterColor);
	}

	/**
	 * Full constructor wrapper (horizontal).
	 */
	public static <T> BarChartHorizontal<T> createBarChartHorizontal(Collection<? extends T> data,
			java.util.function.Function<? super T, String> worldToCategoryMapping,
			BarChart.CategoryOrder categoryOrder, List<String> customOrder, boolean includeMissingBin,
			String missingLabel, Color allDataColor, Color filterColor) {

		return new BarChartHorizontal<>(data, worldToCategoryMapping,
				categoryOrder, customOrder, includeMissingBin, missingLabel, allDataColor, filterColor);
	}

	// ============================================================
	// LEGEND
	// ============================================================

	/**
	 * Adds a legend row (one {@link StringPainter} per label) and applies a
	 * horizontal string alignment strategy.
	 *
	 * Intended for legends where labels are rendered horizontally.
	 *
	 * @param chart     bar chart panel
	 * @param labels    labels in bin order
	 * @param alignment horizontal alignment within each legend cell
	 */
	public static void addLegendHorizontal(InfoVisChartPanel chart, List<String> labels,
			HorizontalStringAlignment alignment) {

		Objects.requireNonNull(chart, "chart must not be null");
		Objects.requireNonNull(labels, "labels must not be null");

		// Validate label count matches bin count if possible
		if (chart instanceof BarChart<?>) {
			BarChart<?> barChart = (BarChart<?>) chart;
			int expectedCount = barChart.getBinLabels().size();
			if (labels.size() != expectedCount) {
				throw new IllegalArgumentException(
						"Legend labels count (" + labels.size() + ") does not match bin count (" + expectedCount + ")");
			}
		}

		if (alignment == null)
			alignment = HorizontalStringAlignment.CENTER;

		StringPainter[][] painters = new StringPainter[1][labels.size()];

		// Font alignment: use the panel font (same as axes / theme).
		java.awt.Font legendFont = chart.getFont();

		for (int i = 0; i < labels.size(); i++) {
			StringPainter sp = new StringPainter(labels.get(i));
			sp.setBackgroundPaint(null);

			// caller decides whether labels are horizontal or vertical orientation;
			// we only apply the requested alignment here.
			sp.setHorizontalStringAlignment(alignment);

			if (legendFont != null) {
				sp.setFont(legendFont);
			}

			painters[0][i] = sp;
		}

		Grid2DPainterPainter<StringPainter> gridPainter = new Grid2DPainterPainter<>(painters);
		gridPainter.setBackgroundPaint(null);

		chart.addChartPainter(gridPainter);
	}

	/**
	 * Adds a legend row (one {@link StringPainter} per label) and applies a
	 * vertical string alignment strategy.
	 *
	 * Intended for legends where labels are rendered vertically (rotated) OR where
	 * vertical alignment is explicitly desired for horizontal labels.
	 *
	 * @param chart             bar chart panel
	 * @param labels            labels in bin order
	 * @param verticalAlignment vertical alignment within each legend cell
	 */
	public static void addLegendVertical(BarChartVertical<?> barChart, List<String> labels,
			StringPainter.VerticalStringAlignment verticalAlignment, boolean verticalTextOrientation) {

		Objects.requireNonNull(barChart, "barChart must not be null");
		Objects.requireNonNull(labels, "labels must not be null");

		// Validate label count matches bin count if possible
		int expectedCount = barChart.getBinLabels().size();
		if (labels.size() != expectedCount) {
			throw new IllegalArgumentException(
					"Legend labels count (" + labels.size() + ") does not match bin count (" + expectedCount + ")");
		}

		if (verticalAlignment == null)
			verticalAlignment = StringPainter.VerticalStringAlignment.CENTER;

		StringPainter[][] painters = new StringPainter[labels.size()][1];

		// Font alignment: use the panel font (same as axes / theme).
		java.awt.Font legendFont = barChart.getFont();

		for (int i = 0; i < labels.size(); i++) {
			StringPainter sp = new StringPainter(Parsers.parseString(labels.get(i)));
			sp.setBackgroundPaint(null);
			sp.setVerticalOrientation(verticalTextOrientation);

			// caller decides whether labels are horizontal or vertical orientation;
			// we only apply the requested alignment here.
			sp.setVerticalStringAlignment(verticalAlignment);

			if (legendFont != null) {
				sp.setFont(legendFont);
			}

			painters[i][0] = sp;
		}

		Grid2DPainterPainter<StringPainter> gridPainter = new Grid2DPainterPainter<>(painters);
		gridPainter.setBackgroundPaint(null);

		barChart.addChartPainter(gridPainter);
	}

	// ============================================================
	// INTERACTION
	// ============================================================

	public static <T> SelectionModel<T> addInteraction(BarChart<T> barChart) {
		return addInteraction(barChart, true, true, null);
	}

	/**
	 * Wires up a SelectionHandler to the given IBarChart.
	 *
	 * - clickInteraction: click selects picked elements - rectangleSelection:
	 * rectangle selects picked elements
	 *
	 * IMPORTANT: The selection domain is T (data objects), not bins.
	 */
	public static <T> SelectionModel<T> addInteraction(BarChart<T> barChart, boolean clickInteraction,
			boolean rectangleSelection, SelectionModel<T> selectionModel) {

		Objects.requireNonNull(barChart, "barChart must not be null");

		if (selectionModel == null)
			selectionModel = SelectionModels.create();

		SelectionHandler<T> selectionHandler = new SelectionHandler<>(selectionModel);

		selectionHandler.attachTo(barChart);

		if (clickInteraction)
			selectionHandler.setClickSelection(barChart);

		if (rectangleSelection)
			selectionHandler.setRectangleSelection(barChart);

		// Draw selection rectangle overlay
		barChart.addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		selectionModel.addSelectionListener(barChart);
		if (selectionModel.getSelection().size() > 0)
			barChart.setSelectedFunction(selectionModel::isSelected);

		return selectionModel;
	}

	// ============================================================
	// STYLING HELPERS
	// ============================================================

	public static void setLayerColors(BarChart<?> barChart, Color globalColor, Color filterColor,
			Color selectionColor) {
		Objects.requireNonNull(barChart, "barChart must not be null");

		barChart.setGlobalColor(globalColor);
		barChart.setFilterColor(filterColor);
		barChart.setSelectionColor(selectionColor);
	}

	public static double getGridSpacing(BarChart<?> barChart) {
		Objects.requireNonNull(barChart, "barChart must not be null");
		BarChartPainter p = barChart.getBarChartPainter();
		return (p != null) ? p.getGridSpacing() : 0.0;
	}

	public static void setGridSpacing(BarChart<?> barChart, double gridSpacing) {
		Objects.requireNonNull(barChart, "barChart must not be null");
		forEachPainter(barChart, p -> p.setGridSpacing(gridSpacing));
	}

	public static boolean isToolTipping(BarChart<?> barChart) {
		Objects.requireNonNull(barChart, "barChart must not be null");

		if (barChart instanceof InfoVisChartPanel)
			return ((InfoVisChartPanel) barChart).isShowingTooltips();

		BarChartPainter p = barChart.getBarChartPainter();
		return p != null && p.isToolTipping();
	}

	public static void setToolTipping(BarChart<?> barChart, boolean toolTipping) {
		Objects.requireNonNull(barChart, "barChart must not be null");

		barChart.setShowingTooltips(toolTipping);
	}

	public static Paint getBorderPaint(BarChart<?> barChart) {
		Objects.requireNonNull(barChart, "barChart must not be null");

		BarChartPainter p = barChart.getBarChartPainter();
		return (p != null) ? p.getBorderPaint() : null;
	}

	public static void setBorderPaint(BarChart<?> barChart, Paint borderPaint) {
		Objects.requireNonNull(barChart, "barChart must not be null");
		forEachPainter(barChart, p -> p.setBorderPaint(borderPaint));
	}

	public static <T> void setFilterPaint(BarChart<T> barChart, Color filterPaint) {
		Objects.requireNonNull(barChart, "barChart must not be null");
		barChart.setFilterColor(filterPaint);
	}

	public static <T> void setSelectionPaint(BarChart<T> barChart, Color selectionPaint) {
		Objects.requireNonNull(barChart, "barChart must not be null");
		barChart.setSelectionColor(selectionPaint);
		forEachPainter(barChart, p -> p.setSelectionPaint(selectionPaint));
	}

	/**
	 * Convenience: set base colors for the three layers (global/filter). Selection
	 * painter color is controlled by setSelectionPaint.
	 *
	 * Note: This updates painter colors only (no bin re-computation).
	 */
	public static void setLayerColors(BarChart<?> barChart, Color allDataColor, Color filterColor) {
		Objects.requireNonNull(barChart, "barChart must not be null");

		if (barChart instanceof AbstractBinnedDistributionPanel<?>) {
			@SuppressWarnings("rawtypes")
			AbstractBinnedDistributionPanel p = (AbstractBinnedDistributionPanel) barChart;

			if (allDataColor != null && p.getGlobalPainter() != null)
				p.getGlobalPainter().setColor(allDataColor);

			if (filterColor != null && p.getFilterPainter() != null)
				p.getFilterPainter().setColor(filterColor);

			if (barChart instanceof Component)
				((Component) barChart).repaint();
		} else {
			// Fallback: treat main painter as filter painter
			if (filterColor != null && barChart.getBarChartPainter() != null) {
				barChart.getBarChartPainter().setColor(filterColor);
				if (barChart instanceof Component)
					((Component) barChart).repaint();
			}
		}
	}

	// ============================================================
	// INTERNAL: painter iteration
	// ============================================================

	@FunctionalInterface
	private interface PainterConsumer {
		void accept(BarChartPainter painter);
	}

	/**
	 * Applies an operation to all three painters when possible. Otherwise applies
	 * only to the main painter from IBarChart.
	 */
	private static void forEachPainter(BarChart<?> barChart, PainterConsumer op) {
		if (barChart == null || op == null)
			return;

		// Best: we have the 3-layer getters
		if (barChart instanceof AbstractBinnedDistributionPanel<?>) {
			@SuppressWarnings("rawtypes")
			AbstractBinnedDistributionPanel p = (AbstractBinnedDistributionPanel) barChart;

			BarChartPainter g = p.getGlobalPainter();
			BarChartPainter f = p.getFilterPainter();
			BarChartPainter s = p.getSelectionPainter();

			if (g != null)
				op.accept(g);
			if (f != null)
				op.accept(f);
			if (s != null)
				op.accept(s);

			return;
		}

		// Fallback: apply to single painter
		BarChartPainter main = barChart.getBarChartPainter();
		if (main != null)
			op.accept(main);
	}

	/**
	 * Avoid instantiation.
	 */
	private BarCharts() {
		// no-op
	}
}
