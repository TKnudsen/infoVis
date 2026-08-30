package com.github.TKnudsen.infoVis.view.panels.histogram;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.dataFactory.DataSets;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;

import de.javagl.selection.LoggingSelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * <p>
 * HistogramTester
 * 
 * Loads the Titanic dataset and tests Histogram panels (vertical + horizontal)
 * using {@link ComplexDataObject} as the interaction and selection domain.
 * 
 * Key goals:
 * </p>
 *
 * @version 1.01
 * @since 2026
 */
public class HistogramTester {

	// ============================================================
	// PARAMETERS (play around here)
	// ============================================================

	/** Which numeric attribute to show (Titanic dataset attribute name). */
	private static final String NUMERIC_ATTRIBUTE = "AGE";
	// alternatives: "Fare", "SibSp", "Parch" (depending on dataset schema)

	/** bin count variants */
	private static final int[] BIN_COUNTS = new int[] { 3, 10, 30 };

	/** create vertical and horizontal versions */
	private static final boolean INCLUDE_VERTICAL = true;
	private static final boolean INCLUDE_HORIZONTAL = true;

	/** show value domain axis variants */
	private static final boolean[] SHOW_VALUE_DOMAIN_AXIS = new boolean[] { true, false };

	/** interaction modes */
	private static final boolean CLICK_SELECTION = true;
	private static final boolean RECTANGLE_SELECTION = true;

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

		// Map ComplexDataObject -> Number (Histogram world-to-number mapping)
		final Function<ComplexDataObject, Number> toNumber = createNumericMapping(NUMERIC_ATTRIBUTE);

		// Pre-filter invalid numeric values (Histogram expects meaningful numbers)
		final List<ComplexDataObject> data = new ArrayList<>();
		for (ComplexDataObject cdo : titanic) {
			if (cdo == null)
				continue;

			Number n = toNumber.apply(cdo);
			if (n == null)
				continue;

			double v = n.doubleValue();
			if (Double.isNaN(v) || Double.isInfinite(v))
				continue;

			data.add(cdo);
		}

		if (data.isEmpty()) {
			throw new IllegalStateException(
					"No numeric values found for attribute '" + NUMERIC_ATTRIBUTE + "'. Check dataset schema.");
		}

		// -------------------------------
		// GLOBAL SELECTION MODEL
		// -------------------------------
		SelectionModel<ComplexDataObject> selectionModel = SelectionModels.create();
		selectionModel.addSelectionListener(new LoggingSelectionListener<>());

		// -------------------------------
		// CREATE HISTOGRAM VARIANTS
		// -------------------------------
		JPanel panel = new JPanel(new GridLayout(BIN_COUNTS.length,0));
		
		for (int bins : BIN_COUNTS) {
			if (INCLUDE_VERTICAL) {
				panel.add(createHistogramVariants(data, toNumber, bins, true, selectionModel));
			}
			if (INCLUDE_HORIZONTAL) {
				panel.add(createHistogramVariants(data, toNumber, bins, false, selectionModel));
			}
		}

		// -------------------------------
		// SHOW
		// -------------------------------
		String title = "HistogramTester -- Titanic (" + NUMERIC_ATTRIBUTE + "), vertical/horizontal variants";
		SVGFrameTools.dropSVGFrame(panel, title, 1600, 350);
	}

	// ============================================================
	// BUILDING BLOCKS
	// ============================================================

	private static JPanel createHistogramVariants(List<ComplexDataObject> data,
			Function<? super ComplexDataObject, Number> mapping, int bins, boolean vertical,
			SelectionModel<ComplexDataObject> selectionModel) {
		
		JPanel grid = new JPanel(new GridLayout(1, 0));

		Objects.requireNonNull(grid, "grid must not be null");
		Objects.requireNonNull(data, "data must not be null");
		Objects.requireNonNull(mapping, "mapping must not be null");
		Objects.requireNonNull(selectionModel, "selectionModel must not be null");

		for (boolean showAxis : SHOW_VALUE_DOMAIN_AXIS) {

			// Instantiate Histogram explicitly (same pattern as
			// AttributeDistributionsView)
			Histogram<ComplexDataObject> hist = Histograms.create(data, mapping, bins, vertical);

			// Show/hide value domain axis (API is orientation-dependent)
			Histograms.setShowValueDomainAxis(hist, showAxis);

			// Interaction wiring (adds selection handler overlay and registers histogram as
			// selection listener)
			Histograms.addInteraction(hist, selectionModel, CLICK_SELECTION, RECTANGLE_SELECTION);

			// Optional: transparent background for embedding in themed containers
			hist.setBackground(null);

			grid.add(hist);
		}
		
		return grid;
	}

	/**
	 * Creates a numeric mapping function for a given attribute name.
	 *
	 * <p>
	 * Uses Parsers.parseDouble(...) to handle heterogeneous value types
	 * (String/Number/etc.) robustly.
	 * </p>
	 */
	private static Function<ComplexDataObject, Number> createNumericMapping(String attribute) {
		Objects.requireNonNull(attribute, "attribute must not be null");

		return cdo -> {
			if (cdo == null)
				return null;
			Object v = cdo.getAttribute(attribute);
			if (v == null)
				return null;
			return Parsers.parseDouble(v);
		};
	}
}