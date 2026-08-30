package com.github.TKnudsen.infoVis.view.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.interaction.handlers.FilterStatusHander;
import com.github.TKnudsen.infoVis.view.interaction.handlers.FilterStatusHanders;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChartHorizontalValueBased;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChartsValueBased;

import de.javagl.selection.SelectionModel;

/**
 * <p>
 * Panel showing all/filtered/selected item counts as a labeled horizontal
 * bar chart, refreshing on filter status or selection changes.
 * </p>
 *
 * @version 1.0
 */
public class FilterStatusView<T> extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Collection<T> dataAll;

	private final FilterStatusHander<T> filterStatusHandler;

	private final SelectionModel<T> selectionModel;

	private final Color defaultColor;
	private final Color filterColor;
	private final Color selectionColor;

	private boolean drawAxis = false;

	public FilterStatusView(Collection<T> data, FilterStatusHander<T> filterStatusHandler,
			SelectionModel<T> selectionModel, Color defaultColor, Color filterColor, Color selectionColor) {
		super(new BorderLayout());

		this.dataAll = data;
		this.filterStatusHandler = filterStatusHandler;
		this.selectionModel = selectionModel;

		this.defaultColor = defaultColor;
		this.filterColor = filterColor;
		this.selectionColor = selectionColor;

		filterStatusHandler.addFilterStatusListener(e -> refreshView());
		selectionModel.addSelectionListener(e -> refreshView());

		refreshView();
	}

	private void refreshView() {
		removeAll();

		JPanel labels = new JPanel(new GridLayout(0, 1));
		labels.setPreferredSize(new Dimension(80, 0));
		labels.add(new JLabel("All datasets"));
		labels.add(new JLabel("After filtering"));
		labels.add(new JLabel("Selected"));
		add(labels, BorderLayout.WEST);

		BarChartHorizontalValueBased barChart = createBarChart();
		add(barChart, BorderLayout.CENTER);

		JPanel counts = new JPanel(new GridLayout(0, 1));
		counts.setPreferredSize(new Dimension(35, 0));
		counts.add(new JLabel(String.valueOf(dataAll.size()), JLabel.CENTER));
		counts.add(new JLabel(String.valueOf(FilterStatusHanders.filter(dataAll, filterStatusHandler).size()),
				JLabel.CENTER));
		counts.add(new JLabel(String.valueOf(selectionModel.getSelection().size()), JLabel.CENTER));
		add(counts, BorderLayout.EAST);
	}

	private BarChartHorizontalValueBased createBarChart() {
		List<Double> bars = new ArrayList<>();
		List<Color> colors = new ArrayList<>();

		bars.add((double) dataAll.size());
		colors.add(defaultColor);

		bars.add((double) FilterStatusHanders.filter(dataAll, filterStatusHandler).size());
		colors.add(filterColor);

		bars.add((double) selectionModel.getSelection().size());
		colors.add(selectionColor);

		BarChartHorizontalValueBased barChart = BarChartsValueBased.createBarChartHorizontal(bars, colors);
		barChart.setBackground(null);
		barChart.setDrawXAxis(drawAxis);

		return barChart;
	}

	public boolean isDrawAxis() {
		return drawAxis;
	}

	public void setDrawAxis(boolean drawAxis) {
		this.drawAxis = drawAxis;
	}

}
