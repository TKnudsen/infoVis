package com.github.TKnudsen.infoVis.view.ui.filterstatus;

import java.awt.Color;
import java.util.Set;
import java.util.function.Predicate;

import com.github.TKnudsen.infoVis.view.interaction.event.FilterChangedEvent;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterStatusListener;
import com.github.TKnudsen.infoVis.view.painters.number.ScorePainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter;
import com.github.TKnudsen.infoVis.view.painters.string.StringPainter.HorizontalStringAlignment;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.ui.InfoVisColors;

import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;

public class FilterAndSelectionStatusPanel<T> extends InfoVisChartPanel
		implements FilterStatusListener<T>, SelectionListener<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Set<T> keySet;
	private final Predicate<T> filterStatus;
	private final Predicate<T> selectionStatus;

	private boolean showNumbers = true;
	private Color allDataColor = InfoVisColors.ALL_DATA_COLOR;
	private Color filterColor = InfoVisColors.FILTER_COLOR;
	private Color selectionColor = InfoVisColors.SELECTION_COLOR;

	public FilterAndSelectionStatusPanel(Set<T> keySet, Predicate<T> filterStatus, Predicate<T> selectionStatus) {
		this.keySet = keySet;
		this.filterStatus = filterStatus;
		this.selectionStatus = selectionStatus;

		refreshView();
	}

	private void refreshView() {
		this.removeChartPainters();

		int filterCount = 0;
		for (T t : keySet)
			if (filterStatus.test(t))
				filterCount++;

		int selectionCount = 0;
		for (T t : keySet)
			if (selectionStatus.test(t))
				selectionCount++;

		ScorePainter allScorePainter = new ScorePainter(1.0, allDataColor);
		allScorePainter.setBackgroundPaint(null);
		this.addChartPainter(allScorePainter);

		ScorePainter filterScorePainter = new ScorePainter(filterCount / (double) keySet.size(), filterColor);
		filterScorePainter.setBackgroundPaint(null);
		this.addChartPainter(filterScorePainter);

		ScorePainter selectionScorePainter = new ScorePainter(selectionCount / (double) keySet.size(), selectionColor);
		selectionScorePainter.setBackgroundPaint(null);
		this.addChartPainter(selectionScorePainter);

		StringPainter allDataStringPainter = new StringPainter(keySet.size() + " ");
		allDataStringPainter.setHorizontalStringAlignment(HorizontalStringAlignment.RIGHT);
		this.addChartPainter(allDataStringPainter);

		StringPainter filterDataStringPainter = new StringPainter(filterCount + "");
		filterDataStringPainter.setHorizontalStringAlignment(HorizontalStringAlignment.CENTER);
		this.addChartPainter(filterDataStringPainter);

		StringPainter selectedDataStringPainter = new StringPainter(" " + selectionCount);
		selectedDataStringPainter.setHorizontalStringAlignment(HorizontalStringAlignment.LEFT);
		this.addChartPainter(selectedDataStringPainter);

		StringPainter statusStringPainter = new StringPainter(
				" " + selectionCount + " / " + filterCount + " / " + keySet.size());
		statusStringPainter.setHorizontalStringAlignment(HorizontalStringAlignment.CENTER);
//		this.addChartPainter(statusStringPainter);

		revalidate();
		repaint();
	}

	@Override
	public void selectionChanged(SelectionEvent<T> selectionEvent) {
		refreshView();
	}

	@Override
	public void filterStatusChanged(FilterChangedEvent<T> filterChangedEvent) {
		refreshView();
	}

	public boolean isShowNumbers() {
		return showNumbers;
	}

	public void setShowNumbers(boolean showNumbers) {
		this.showNumbers = showNumbers;

		refreshView();
	}

	public Color getAllDataColor() {
		return allDataColor;
	}

	public void setAllDataColor(Color allDataColors) {
		this.allDataColor = allDataColors;

		refreshView();
	}

	public Color getFilterColor() {
		return filterColor;
	}

	public void setFilterColor(Color filterColors) {
		this.filterColor = filterColors;

		refreshView();
	}

	public Color getSelectionColor() {
		return selectionColor;
	}

	public void setSelectionColor(Color selectionColors) {
		this.selectionColor = selectionColors;

		refreshView();
	}

}
