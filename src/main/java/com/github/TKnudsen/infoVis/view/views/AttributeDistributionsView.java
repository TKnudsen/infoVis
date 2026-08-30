package com.github.TKnudsen.infoVis.view.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainers;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChart;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChart.CategoryOrder;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChartVertical;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarCharts;
import com.github.TKnudsen.infoVis.view.tools.VisualMappingTools;
import com.github.TKnudsen.infoVis.view.ui.InfoVisColors;
import com.github.TKnudsen.infoVis.view.ui.themes.VisualizationTheme;
import com.github.TKnudsen.infoVis.view.ui.themes.VisualizationThemeHandler;
import com.github.TKnudsen.infoVis.view.ui.themes.VisualizationThemeManager;
import com.github.TKnudsen.infoVis.view.ui.themes.VisualizationThemeManager.Subscription;
import com.github.TKnudsen.infoVis.view.ui.themes.VisualizationThemeUtils;

import de.javagl.selection.SelectionModel;

/**
 * <p>
 * Shows one distribution view per attribute: numeric attributes as a
 * {@link DynamicQueryView} (histogram + slider), categorical attributes as a
 * selectable bar chart. Synchronizes all local selections with a global
 * selection model.
 *
 * Main design goals: no recursion between global and local selections,
 * EDT-safe mutations for Swing stability, robust mapping (no -1 or null
 * indices in selection sets), and minimal listener fan-out with safe
 * disposal.
 * </p>
 *
 * @version 1.0
 */
public class AttributeDistributionsView extends JPanel implements PropertyChangeListener, VisualizationThemeHandler {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(AttributeDistributionsView.class.getName());

	// Layout dimensions
	public static final int MINIMUM_WIDTH = 200;
	public static final int MINIMUM_HEIGHT = 200;
	public static final int BIN_COUNT = 20;
	public static final int CHARTS_PER_ROW = 6;

	/**
	 * For addLegendVertical: whether legend text should be rotated.
	 * 
	 * true -> vertical (rotated) labels false -> horizontal labels, but vertically
	 * aligned in the cell
	 */
	private static final boolean VERTICAL_LEGEND_TEXT_ORIENTATION = false;

	// ==================== THEME MANAGEMENT ====================

	private final VisualizationThemeManager themeManager;
	private volatile VisualizationTheme currentTheme;
	private Subscription themeSubscription;

	// ==================== CORE DEPENDENCIES ====================

	private final ComplexDataContainer container;
	private final Set<Long> ids;
	private final SelectionModel<Long> globalSelectionModel;

	// ==================== UI STATE ====================

	private final Map<String, DynamicQueryView<Long>> attributeDynamicQueries = new HashMap<>();
	private final Map<String, BarChart<Long>> attributeBarChartHorizontals = new HashMap<>();

	// ==================== CONSTRUCTION ====================

	public AttributeDistributionsView(ComplexDataContainer container, SelectionModel<Long> globalSelectionModel) {
		this.container = Objects.requireNonNull(container);
		this.ids = ComplexDataContainers.keySetAsLong(container);

		this.globalSelectionModel = Objects.requireNonNull(globalSelectionModel);

		this.themeManager = VisualizationThemeManager.getInstance();
		this.currentTheme = themeManager.getTheme();
		this.themeSubscription = themeManager.addThemeChangeListener(this::handleThemeChange);

		initializeUI();

		// Apply initial theme
		applyTheme(currentTheme);

		LOGGER.info("AttributeDistributionsView initialized");
	}

	/**
	 * Call this when the view is disposed/removed permanently to avoid leaks.
	 * (Theme subscription + global selection listener)
	 */
	public void dispose() {
		// Unregister from theme manager
		if (themeSubscription != null) {
			themeSubscription.close();
			LOGGER.fine("Theme subscription closed");
		}

		try {
			if (globalSelectionModel != null)
				for (String attribute : container.getAttributes()) {
					DynamicQueryView<Long> dq = attributeDynamicQueries.get(attribute);
					if (dq != null) {
						dq.getHistogram().setSelectedFunction(null);
						globalSelectionModel.removeSelectionListener(dq.getHistogram());
					}

					BarChart<Long> barChart = attributeBarChartHorizontals.get(attribute);
					if (barChart != null) {
						barChart.setSelectedFunction(null);
						globalSelectionModel.removeSelectionListener(barChart);
					}
				}
		} catch (Throwable t) {
		}
	}

	// ==================== THEME HANDLING ====================

	@Override
	public void handleThemeChange(VisualizationTheme oldTheme, VisualizationTheme newTheme) {
		this.currentTheme = newTheme;
		runOnEDT(() -> {
			applyTheme(newTheme);
			repaint();
		});
	}

	@Override
	public void applyTheme(VisualizationTheme theme) {
		if (theme == null || container == null)
			return;

		// Update panel border
		setBorder(VisualizationThemeUtils.createThemedTitledBorder("Attribute Value Distributions", theme));

		// Panel background
		setBackground(theme.getBackgroundColor());

		for (String attribute : container.getAttributesSorted()) {
			DynamicQueryView<Long> dynamicQuery = attributeDynamicQueries.get(attribute);
			if (dynamicQuery != null) {
//				dynamicQuery.setBackground(theme.getBackgroundColor());
//				DynamicQueryViews.setAllDataColor(dynamicQuery, theme.getNeutralColor());
//				DynamicQueryViews.setFilterColor(dynamicQuery, theme.getBinColor());
//				DynamicQueryViews.setSelectionColor(dynamicQuery, InfoVisColors.SELECTION_COLOR);
//				dynamicQuery.setFont(theme.getBaseFont());

				VisualizationThemeUtils.applyThemeToComponentOnly(dynamicQuery, theme, true);
				// dynamicQuery.setBorder(VisualizationThemeUtils.createThemedTitledBorder(attribute,
				// currentTheme));
			}

			BarChart<Long> barChart = attributeBarChartHorizontals.get(attribute);
			if (barChart != null) {
//				barChart.setBackground(theme.getBackgroundColor());
//				barChart.getBarChartPainter().setColor(Color.GREEN);
//				barChart.setGlobalColor(theme.getNeutralColor());
//				BarCharts.setFilterPaint(barChart, theme.getBinColor());
//				BarCharts.setSelectionPaint(barChart, InfoVisColors.SELECTION_COLOR);
//				BarCharts.setBorderPaint(barChart, theme.getBorderColor());
//				barChart.setFont(theme.getBaseFont());

				VisualizationThemeUtils.applyThemeToComponentOnly(barChart, theme, true);
				// barChart.setBorder(VisualizationThemeUtils.createThemedTitledBorder(attribute,
				// currentTheme));
			}
		}

		LOGGER.fine("Theme applied to ItemProjectionPanel");
	}

	public void setSelectionColor(Color color) {
		for (String attribute : container.getAttributes()) {
			DynamicQueryView<Long> dynamicQuery = attributeDynamicQueries.get(attribute);
			if (dynamicQuery != null)
				DynamicQueryViews.setSelectionColor(dynamicQuery, color);

			BarChart<Long> barChart = attributeBarChartHorizontals.get(attribute);
			if (barChart != null)
				barChart.setSelectionColor(color);
		}

		LOGGER.fine("Selection color changed in ItemProjectionPanel");

		repaint();
	}

	// ==================== UI INITIALIZATION ====================

	private void initializeUI() {
		setLayout(new BorderLayout());

		int count = container.getAttributesSize();
		this.setLayout(new GridLayout((int) (count / CHARTS_PER_ROW), 0));

		setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));

		if (container == null)
			return;

		for (String attribute : container.getAttributesSorted()) {
			if (attribute.equals("ID"))
				continue;

			if (container.isNumeric(attribute)) {

			} else {
				try {
					BarChart<Long> barchart = createSelectableBarchart(attribute);

					add(barchart);
					attributeBarChartHorizontals.put(attribute, barchart);
				} catch (IllegalStateException e) {
					System.err.println("AttributeDistributionsView.initializeUI: unable to create chart for attribute "
							+ attribute
							+ " due to IllegalStateException. Most likely the attribute produces only a single value");
				}
			}
		}

		for (String attribute : container.getAttributesSorted()) {
			if (attribute.equals("ID"))
				continue;

			if (container.isNumeric(attribute)) {
				try {
					DynamicQueryView<Long> dynamicQuery = createDynamicQuery(attribute, BIN_COUNT);

					add(dynamicQuery);
					attributeDynamicQueries.put(attribute, dynamicQuery);
				} catch (IllegalStateException e) {
					System.err.println("AttributeDistributionsView.initializeUI: unable to create chart for attribute "
							+ attribute
							+ " due to IllegalStateException. Most likely the attribute produces only a single value");
				}
			} else {

			}
		}
	}

	// ==================== PROPERTY CHANGE ====================

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event == null)
			return;

		// No known event types here; repaint defensively
		runOnEDT(this::repaint);

		LOGGER.fine("propertyChange: " + event.getClass().getSimpleName());
	}

	// ==================== DYNAMIC QUERY ====================

	private DynamicQueryView<Long> createDynamicQuery(String attribute, int bins) {

		System.out.println("AttributeDistributionsView.createDynamicQuery for " + attribute);

		final Function<Long, Number> toNumberFunction = id -> {
			if (id == null || container == null)
				return null;
			ComplexDataObject o = container.get(id);
			if (o == null)
				return null;
			return Parsers.parseDouble(o.getAttribute(attribute));
		};

		final List<ComplexDataObject> itemList = ComplexDataContainers.getObjectList(container);
		final List<ComplexDataObject> items = VisualMappingTools.sanityCheckFilter(itemList,
				e -> Parsers.parseDouble(e.getAttribute(attribute)), true);

		final List<Long> data = new ArrayList<>();
		for (ComplexDataObject item : items) {
			Long id = Parsers.parseLong(item.getAttribute("ID"));
			if (id != null)
				data.add(id);
		}

		List<Long> checked = VisualMappingTools.sanityCheckFilter(data, toNumberFunction);

		final DynamicQueryView<Long> dynamicQuery = DynamicQueryViews.createDynamicQuery(checked, toNumberFunction,
				globalSelectionModel, "", bins);
		dynamicQuery.setDrawYAxis(false);

		dynamicQuery.setMissingValuesAreIn(true);
		dynamicQuery.setUpperThumbLocked(true);
		dynamicQuery.setLowerThumbLocked(true);

		// Theme
		DynamicQueryViews.setAllDataColor(dynamicQuery, currentTheme.getNeutralColor());
		DynamicQueryViews.setFilterColor(dynamicQuery, currentTheme.getBinColor());
		DynamicQueryViews.setSelectionColor(dynamicQuery, InfoVisColors.SELECTION_COLOR);
		dynamicQuery.setFont(currentTheme.getBaseFont());

		dynamicQuery.setBorder(VisualizationThemeUtils.createThemedTitledBorder(attribute, currentTheme));

		return dynamicQuery;
	}

	// ==================== SELECTABLE BARCHART (CATEGORICAL) ====================

	private BarChart<Long> createSelectableBarchart(String attribute) {

		System.out.println("AttributeDistributionsView.createSelectableBarchart for " + attribute);

		final Function<Long, String> toStringFunction = id -> {
			if (id == null || container == null)
				return null;
			ComplexDataObject o = container.get(id);
			if (o == null)
				return null;
			return Parsers.parseString(o.getAttribute(attribute));
		};

		// IDs in container
		// final Set<Long> ids = ComplexDataContainers.keySetAsLong(container);

		// Collect labels (unique, sorted)
		final Collection<Object> attributeValueCollection = container.getAttributeValueCollection(attribute);
		final Set<String> labelSet = new TreeSet<>();
		if (attributeValueCollection != null) {
			for (Object o : attributeValueCollection) {
				String s = Parsers.parseString(o);
				if (s != null)
					labelSet.add(s);
			}
		}
		final List<String> labels = new ArrayList<>(labelSet);

		// Create chart
		BarChartVertical<Long> barChart = BarCharts.createBarChartVertical(ids, toStringFunction,
				CategoryOrder.ALPHABETICAL, null, true, "no idea", Color.GREEN, null);
		barChart.setDrawYAxis(false);
		barChart.setBorder(VisualizationThemeUtils.createThemedTitledBorder(attribute, currentTheme));

		// BarCharts.addLegendVertical(barChart, labels, VerticalStringAlignment.DOWN,
		// VERTICAL_LEGEND_TEXT_ORIENTATION);

		// Index selection model for this bar chart
		BarCharts.addInteraction(barChart, true, true, globalSelectionModel);

		return barChart;
	}

	// ==================== HELPERS ====================

	private static void runOnEDT(Runnable r) {
		if (r == null)
			return;

		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			SwingUtilities.invokeLater(r);
		}
	}

}
