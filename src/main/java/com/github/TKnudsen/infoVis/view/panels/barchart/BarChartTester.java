package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.dataFactory.DataSets;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.VerticalStringAlignment;
import com.github.TKnudsen.infoVis.view.painters.string.TitlePainter;

import de.javagl.selection.LoggingSelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * <p>
 * BarChartTester
 * 
 * Loads the Titanic dataset and tests BarChart panels (vertical + horizontal)
 * using {@link ComplexDataObject} as the interaction and selection domain.
 * 
 * This tester creates:
 * </p>
 *
 * @version 1.03
 * @since 2026
 */
public class BarChartTester {

	// ============================================================
	// PARAMETERS
	// ============================================================

	private static final String CATEGORICAL_ATTRIBUTE = "CLASSID";

	private static final boolean INCLUDE_VERTICAL = true;
	private static final boolean INCLUDE_HORIZONTAL = true;

	private static final boolean CLICK_SELECTION = true;
	private static final boolean RECTANGLE_SELECTION = true;

	private static final BarChart.CategoryOrder[] CATEGORY_ORDERS = new BarChart.CategoryOrder[] {
			BarChart.CategoryOrder.ALPHABETICAL };

	private static final boolean[] INCLUDE_MISSING_BIN = new boolean[] { true, false };

	// ------------------------------------------------------------
	// LEGEND PORTFOLIO: 4 variants
	// ------------------------------------------------------------

	/** For horizontal charts: 3 alignment strategies */
	private static final HorizontalStringAlignment[] HORIZONTAL_LEGEND_ALIGNMENTS = new HorizontalStringAlignment[] {
			HorizontalStringAlignment.LEFT, HorizontalStringAlignment.CENTER, HorizontalStringAlignment.RIGHT };

	/** For vertical charts: 3 alignment strategies */
	private static final VerticalStringAlignment[] VERTICAL_LEGEND_ALIGNMENTS = new VerticalStringAlignment[] {
			VerticalStringAlignment.UP, VerticalStringAlignment.CENTER, VerticalStringAlignment.DOWN };

	/**
	 * For addLegendVertical: whether legend text should be rotated.
	 * 
	 * true -> vertical (rotated) labels false -> horizontal labels, but vertically
	 * aligned in the cell
	 */
	private static final boolean VERTICAL_LEGEND_TEXT_ORIENTATION = false;

	// ============================================================
	// MAIN
	// ============================================================

	public static void main(String[] args) {

		// -------------------------------
		// DATA (Titanic)
		// -------------------------------
		List<ComplexDataObject> titanic = DataSets.titanicDataSet();
		if (titanic == null || titanic.isEmpty()) {
			throw new IllegalStateException("Titanic dataset is empty or not available via DataSets.titanicDataSet()");
		}

		final Function<ComplexDataObject, String> toCategory = createCategoricalMapping(CATEGORICAL_ATTRIBUTE);

		final List<ComplexDataObject> data = new ArrayList<>();
		for (ComplexDataObject cdo : titanic) {
			if (cdo == null)
				continue;

			String s = toCategory.apply(cdo);
			if (s == null || s.trim().isEmpty())
				continue;

			data.add(cdo);
		}

		if (data.isEmpty()) {
			throw new IllegalStateException(
					"No categorical values found for attribute '" + CATEGORICAL_ATTRIBUTE + "'. Check dataset schema.");
		}

		final List<String> labels = collectUniqueLabels(data, toCategory);

		// -------------------------------
		// GLOBAL SELECTION MODEL
		// -------------------------------
		SelectionModel<ComplexDataObject> selectionModel = SelectionModels.create();
		selectionModel.addSelectionListener(new LoggingSelectionListener<>());

		// -------------------------------
		// LAYOUT
		// -------------------------------
		// Each (order x legendVariant) forms one row.
		// Each row contains the 4 base charts (VERT miss:on/off + HORZ miss:on/off).
		final int baseChartsPerRow = (INCLUDE_VERTICAL ? INCLUDE_MISSING_BIN.length : 0)
				+ (INCLUDE_HORIZONTAL ? INCLUDE_MISSING_BIN.length : 0);

		final int legendVariantsCount = 4; // NONE + 3 alignments
		final int totalRows = CATEGORY_ORDERS.length * legendVariantsCount;

		JPanel panel = new JPanel(new GridLayout(totalRows, baseChartsPerRow));

		// -------------------------------
		// CREATE PORTFOLIO
		// -------------------------------
		for (BarChart.CategoryOrder order : CATEGORY_ORDERS) {

			for (LegendVariant legendVariant : enumerateLegendVariants()) {

				// ----- VERTICAL charts (missing on/off)
				for (boolean includeMissing : INCLUDE_MISSING_BIN) {
					if (INCLUDE_VERTICAL) {
						panel.add(createBarChartVariant(data, toCategory, labels, true, order, includeMissing,
								legendVariant, selectionModel));
					}
				}

				// ----- HORIZONTAL charts (missing on/off)
				for (boolean includeMissing : INCLUDE_MISSING_BIN) {
					if (INCLUDE_HORIZONTAL) {
						panel.add(createBarChartVariant(data, toCategory, labels, false, order, includeMissing,
								legendVariant, selectionModel));
					}
				}
			}
		}

		// -------------------------------
		// SHOW
		// -------------------------------
		String title = "BarChartTester Titanic (" + CATEGORICAL_ATTRIBUTE + ") 16 charts (NONE + 3 alignment legends)";
		SVGFrameTools.dropSVGFrame(panel, title, 2200, 1200);
	}

	// ============================================================
	// BUILDING BLOCKS
	// ============================================================

	private static BarChart<ComplexDataObject> createBarChartVariant(List<ComplexDataObject> data,
			Function<? super ComplexDataObject, String> mapping, List<String> labels, boolean vertical,
			BarChart.CategoryOrder order, boolean includeMissingBin, LegendVariant legendVariant,
			SelectionModel<ComplexDataObject> selectionModel) {

		Objects.requireNonNull(data, "data must not be null");
		Objects.requireNonNull(mapping, "mapping must not be null");
		Objects.requireNonNull(selectionModel, "selectionModel must not be null");
		Objects.requireNonNull(legendVariant, "legendVariant must not be null");

		final String missingLabel = "missing";

		// ------------------------------------------------------------
		// CREATE CHART
		// ------------------------------------------------------------
		BarChart<ComplexDataObject> chart;

		if (vertical) {
			chart = BarCharts.createBarChartVertical(data, mapping, order, null, includeMissingBin, missingLabel, null,
					null);
		} else {
			chart = BarCharts.createBarChartHorizontal(data, mapping, order, null, includeMissingBin, missingLabel,
					null, null);
		}

		// ------------------------------------------------------------
		// FONT BASELINE (axes + legend + title should match)
		// ------------------------------------------------------------
		if (chart.getFont() == null) {
			chart.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 12));
		}

		// ------------------------------------------------------------
		// LEGEND VARIANT (orientation-dependent)
		// ------------------------------------------------------------
		if (legendVariant.type != LegendVariantType.NONE && labels != null && !labels.isEmpty()) {

			if (vertical) {
				// Vertical bar chart => use addLegendVertical (UP/CENTER/DOWN)
				BarCharts.addLegendVertical((BarChartVertical<?>) chart, labels, legendVariant.verticalAlignment,
						VERTICAL_LEGEND_TEXT_ORIENTATION);
			} else {
				// Horizontal bar chart => use addLegendHorizontal (LEFT/CENTER/RIGHT)
				BarCharts.addLegendHorizontal(chart, labels, legendVariant.horizontalAlignment);
			}
		}

		// ------------------------------------------------------------
		// TITLE (chart characteristics)
		// ------------------------------------------------------------
		String title = buildChartTitle(CATEGORICAL_ATTRIBUTE, vertical, order, includeMissingBin, legendVariant);
		chart.addChartPainter(new TitlePainter(title));

		// ------------------------------------------------------------
		// INTERACTION
		// ------------------------------------------------------------
		BarCharts.addInteraction(chart, CLICK_SELECTION, RECTANGLE_SELECTION, selectionModel);

		chart.setBackground(null);

		return chart;
	}

	private static String buildChartTitle(String attribute, boolean vertical, BarChart.CategoryOrder order,
			boolean includeMissingBin, LegendVariant legendVariant) {

		StringBuilder sb = new StringBuilder();

		if (attribute != null && !attribute.trim().isEmpty())
			sb.append(attribute.trim()).append(" | ");

		sb.append(vertical ? "VERT" : "HORZ");

		if (order != null)
			sb.append(" | order=").append(order.name());

		sb.append(" | missing:").append(includeMissingBin ? "on" : "off");

		sb.append(" | legend=").append(legendVariant.toShortString(vertical));

		return sb.toString();
	}

	// ============================================================
	// LEGEND VARIANTS (4 total)
	// ============================================================

	private enum LegendVariantType {
		NONE, ALIGNMENT
	}

	private static final class LegendVariant {
		private final LegendVariantType type;
		private final HorizontalStringAlignment horizontalAlignment; // used when chart is horizontal
		private final VerticalStringAlignment verticalAlignment; // used when chart is vertical

		private LegendVariant(LegendVariantType type, HorizontalStringAlignment h, VerticalStringAlignment v) {
			this.type = type;
			this.horizontalAlignment = h;
			this.verticalAlignment = v;
		}

		static LegendVariant none() {
			return new LegendVariant(LegendVariantType.NONE, null, null);
		}

		static LegendVariant alignment(HorizontalStringAlignment h, VerticalStringAlignment v) {
			return new LegendVariant(LegendVariantType.ALIGNMENT, h, v);
		}

		String toShortString(boolean chartIsVertical) {
			if (type == LegendVariantType.NONE)
				return "NONE";

			// Alignment depends on chart orientation
			return chartIsVertical ? ("V:" + verticalAlignment.name()) : ("H:" + horizontalAlignment.name());
		}
	}

	/**
	 * Returns exactly 4 legend variants: - NONE - 3 alignment variants
	 *
	 * Each alignment variant contains both: - one HorizontalStringAlignment (for
	 * horizontal charts) - one VerticalStringAlignment (for vertical charts)
	 *
	 * We map them by index: 0: LEFT <-> UP 1: CENTER<-> CENTER 2: RIGHT <-> DOWN
	 */
	private static List<LegendVariant> enumerateLegendVariants() {
		List<LegendVariant> list = new ArrayList<>(4);

		list.add(LegendVariant.none());

		// index-based coupling (keeps the variant count exactly 3)
		list.add(LegendVariant.alignment(HorizontalStringAlignment.LEFT, VerticalStringAlignment.UP));
		list.add(LegendVariant.alignment(HorizontalStringAlignment.CENTER, VerticalStringAlignment.CENTER));
		list.add(LegendVariant.alignment(HorizontalStringAlignment.RIGHT, VerticalStringAlignment.DOWN));

		return list;
	}

	// ============================================================
	// MAPPING + LABELS
	// ============================================================

	private static Function<ComplexDataObject, String> createCategoricalMapping(String attribute) {
		Objects.requireNonNull(attribute, "attribute must not be null");

		return cdo -> {
			if (cdo == null)
				return null;
			Object v = cdo.getAttribute(attribute);
			if (v == null)
				return null;
			return Parsers.parseString(v);
		};
	}

	private static List<String> collectUniqueLabels(Collection<? extends ComplexDataObject> data,
			Function<? super ComplexDataObject, String> mapping) {

		Set<String> labelSet = new TreeSet<>();

		if (data != null) {
			for (ComplexDataObject cdo : data) {
				if (cdo == null)
					continue;
				String s = mapping.apply(cdo);
				if (s == null)
					continue;
				s = s.trim();
				if (s.isEmpty())
					continue;
				labelSet.add(s);
			}
		}

		return new ArrayList<>(labelSet);
	}
}