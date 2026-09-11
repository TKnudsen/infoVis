package com.github.TKnudsen.infoVis.view.painters.aggregation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link AggregationBarChartPainter} with element-level selection: any
 * subset of elements can be marked selected, which is rendered as a
 * proportionally-sized overlay drawn on top of the base bars (its height, or
 * width in horizontal orientation, reflects the selected share of the
 * bucket with the most elements).
 *
 * @param <O> the element type held in each bucket
 * @since 2013
 */
public class BarChartSelectablePainter<O> extends AggregationBarChartPainter<O> {

	// Internal attributes
	private BarsPainter<O> barRendererSelected;
	private Map<O, Boolean> selectedStatus = new HashMap<>();
	private int maxBarElementCount;
	private int maxBarElementSelectedCount;

	// Settable attributes
	private List<Color> colorsForSelection = null;

	/**
	 * Creates a selectable bar chart painter.
	 *
	 * @param elementMapping mapping of elements to bars
	 * @param labeling       labels for each bar
	 * @param headline       optional headline for the chart
	 * @throws IllegalArgumentException if elementMapping is null or empty
	 */
	public BarChartSelectablePainter(List<List<O>> elementMapping, List<String> labeling, String headline) {
		super(elementMapping, labeling, headline);

		if (elementMapping == null || elementMapping.isEmpty())
			throw new IllegalArgumentException("elementMapping cannot be null or empty");

		this.maxBarElementCount = calculateMaxBarElementCount(elementMapping);
	}

	/**
	 * Calculates the maximum number of elements in any bar.
	 *
	 * @param elementMapping the element mapping
	 * @return the maximum count
	 */
	private int calculateMaxBarElementCount(List<List<O>> elementMapping) {
		int max = 0;
		for (List<O> elements : elementMapping)
			if (elements != null)
				max = Math.max(max, elements.size());

		return max;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (g2 == null)
			return;

		super.draw(g2);

		// Draw selected elements overlay
		if (barRendererSelected != null)
			barRendererSelected.draw(g2);

		// Draw legend if enabled
		if (drawLegend)
			drawLegend(g2);
	}

	/**
	 * Returns the entities clicked at the given mouse event location.
	 *
	 * @param e the mouse event
	 * @return list of clicked entities, or null if no entities were clicked
	 */
	public List<O> getClickedEntities(MouseEvent e) {
		if (e == null || barsPainter == null)
			return null;

		Rectangle2D rect = barsPainter.getRectangle();
		if (rect == null || !rect.contains(e.getPoint()))
			return null;

		return barsPainter.getClickedEntities(e);
	}

	/**
	 * Updates the renderer for selected elements. Creates an overlay that
	 * displays only the selected elements from each bar. This method is
	 * automatically called when the selection status changes.
	 */
	public void updateSelectedBarRenderer() {
		if (selectedStatus == null || selectedStatus.isEmpty()) {
			barRendererSelected = null;
			return;
		}

		List<List<O>> selectedMapping = buildSelectedElementMapping();
		this.maxBarElementSelectedCount = calculateMaxBarElementCount(selectedMapping);

		this.barRendererSelected = createSelectedBarRenderer(selectedMapping);

		// Update layout if rectangle is set
		if (rectangle != null)
			setRectangle(rectangle);
	}

	/**
	 * Builds the element mapping containing only selected elements.
	 *
	 * @return element mapping with only selected elements
	 */
	private List<List<O>> buildSelectedElementMapping() {
		List<List<O>> selectedMapping = new ArrayList<>();
		List<List<O>> originalMapping = getElementMapping();

		if (originalMapping == null)
			return selectedMapping;

		for (List<O> bar : originalMapping) {
			List<O> selectedInBar = new ArrayList<>();

			if (bar != null) {
				for (O element : bar) {
					Boolean isSelected = selectedStatus.get(element);
					if (isSelected != null && isSelected)
						selectedInBar.add(element);
				}
			}

			selectedMapping.add(selectedInBar);
		}

		return selectedMapping;
	}

	/**
	 * Creates a new bar renderer for selected elements.
	 *
	 * @param elementMapping the mapping of selected elements
	 * @return configured BarsPainter for selected elements
	 */
	private BarsPainter<O> createSelectedBarRenderer(List<List<O>> elementMapping) {
		BarsPainter<O> renderer;

		if (colorsForSelection != null) {
			renderer = new BarsPainter<>(elementMapping, null, colorsForSelection);
		} else {
			renderer = new BarsPainter<>(elementMapping, null);
			renderer.setColor(Color.GRAY);
		}

		renderer.setOffset(getOffset());
		renderer.setBackgroundPaint(null);
		renderer.setFill(isFill());
		renderer.setVerticalOrientation(isVerticalOrientation());

		return renderer;
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (rectangle == null) {
			barRendererSelected = null;
			return;
		}

		if (barRendererSelected != null && chartRectangle != null) {
			Rectangle2D selectionRect = calculateSelectionRectangle();
			barRendererSelected.setRectangle(selectionRect);
		}
	}

	/**
	 * Calculates the rectangle for the selection overlay based on the ratio of
	 * selected elements to total elements.
	 *
	 * @return rectangle for selected elements overlay
	 */
	private Rectangle2D calculateSelectionRectangle() {
		if (maxBarElementCount == 0 || chartRectangle == null)
			return new Rectangle2D.Double(0, 0, 0, 0);

		double ratio = maxBarElementSelectedCount / (double) maxBarElementCount;

		if (isVerticalOrientation()) {
			return new Rectangle2D.Double(chartRectangle.getX(), chartRectangle.getY() + chartRectangle.getHeight() * (1.0 - ratio),
					chartRectangle.getWidth(), chartRectangle.getHeight() * ratio);
		} else {
			return new Rectangle2D.Double(chartRectangle.getX(), chartRectangle.getY(), chartRectangle.getWidth() * ratio,
					chartRectangle.getHeight());
		}
	}

	@Override
	public void setBorderPaint(Paint borderPaint) {
		super.setBorderPaint(borderPaint);

		if (barRendererSelected != null)
			barRendererSelected.setBorderPaint(borderPaint);
	}

	/**
	 * Returns a copy of the current selection status map.
	 *
	 * @return defensive copy of the selection status map
	 */
	public Map<O, Boolean> getSelectedStatus() {
		return new HashMap<>(selectedStatus);
	}

	/**
	 * Sets the selection status for all elements.
	 *
	 * @param selectedStatus map of element to selection status; null to clear all
	 *                       selections
	 */
	public void setSelectedStatus(Map<O, Boolean> selectedStatus) {
		if (selectedStatus == null)
			this.selectedStatus = new HashMap<>();
		else
			this.selectedStatus = new HashMap<>(selectedStatus);

		updateSelectedBarRenderer();
	}

	/**
	 * Sets the selection status using a set of selected elements. All elements in
	 * the set will be marked as selected, all others as deselected.
	 *
	 * @param selectedElements set of elements to mark as selected; null to clear
	 *                         all selections
	 */
	public void setSelectedStatus(Set<O> selectedElements) {
		if (selectedElements == null) {
			this.selectedStatus.clear();
			updateSelectedBarRenderer();
			return;
		}

		Map<O, Boolean> newStatus = new HashMap<>();

		// Mark all existing elements as de-selected
		for (O element : this.selectedStatus.keySet())
			newStatus.put(element, false);

		// Mark selected elements as selected
		for (O element : selectedElements)
			newStatus.put(element, true);

		setSelectedStatus(newStatus);
	}

	/**
	 * Returns the custom colors used for rendering selected elements.
	 *
	 * @return list of colors for selection, or null if using default gray
	 */
	public List<Color> getColorsForSelection() {
		return colorsForSelection;
	}

	/**
	 * Sets custom colors for rendering selected elements.
	 *
	 * @param colorsForSelection list of colors to use, or null to use default
	 *                           gray
	 */
	public void setColorsForSelection(List<Color> colorsForSelection) {
		this.colorsForSelection = colorsForSelection;

		// Update renderer if it exists
		if (barRendererSelected != null) {
			updateSelectedBarRenderer();
		}
	}

	// ==================== CONVENIENCE METHODS ====================

	/**
	 * Selects a single element.
	 *
	 * @param element the element to select
	 */
	public void selectElement(O element) {
		if (element != null) {
			selectedStatus.put(element, true);
			updateSelectedBarRenderer();
		}
	}

	/**
	 * De-selects a single element.
	 *
	 * @param element the element to de-select
	 */
	public void deselectElement(O element) {
		if (element != null) {
			selectedStatus.put(element, false);
			updateSelectedBarRenderer();
		}
	}

	/**
	 * Clears all selections.
	 */
	public void clearSelection() {
		selectedStatus.clear();
		barRendererSelected = null;
	}

	/**
	 * Checks if an element is currently selected.
	 *
	 * @param element the element to check
	 * @return true if selected, false otherwise
	 */
	public boolean isSelected(O element) {
		Boolean status = selectedStatus.get(element);
		return status != null && status;
	}

	/**
	 * Returns the number of selected elements across all bars.
	 *
	 * @return total count of selected elements
	 */
	public int getSelectedCount() {
		int count = 0;
		for (Boolean selected : selectedStatus.values())
			if (selected != null && selected)
				count++;

		return count;
	}

	/**
	 * Returns whether any elements are currently selected.
	 *
	 * @return true if at least one element is selected
	 */
	public boolean hasSelection() {
		return getSelectedCount() > 0;
	}
}
