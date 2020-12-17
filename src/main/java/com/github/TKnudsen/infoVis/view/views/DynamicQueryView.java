package com.github.TKnudsen.infoVis.view.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.TKnudsen.infoVis.view.interaction.controls.InfoVisRangeSlider;
import com.github.TKnudsen.infoVis.view.interaction.controls.InfoVisRangeSliderPanel;
import com.github.TKnudsen.infoVis.view.interaction.controls.InfoVisRangeSliderPanels;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterChangedEvent;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterStatusListener;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.string.TitlePainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanels;
import com.github.TKnudsen.infoVis.view.panels.histogram.Histogram;
import com.github.TKnudsen.infoVis.view.panels.histogram.Histograms;
import com.github.TKnudsen.infoVis.view.visualChannels.position.IPositionEncodingFunction;

import de.javagl.selection.SelectionModel;

public class DynamicQueryView<T> extends JPanel implements Predicate<T>, FilterStatusListener<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Function<T, Number> scaledToNumberFunction;

	private static final int Y_AXIS_WIDTH = 22;
	private final Histogram<T> histogram;

	private final InfoVisRangeSliderPanel rangeSliderPanel;
	private final InfoVisRangeSlider rangeSlider;
	private static final int INTEGER_MULTIPLIER = 100;

	private final Collection<FilterStatusListener<T>> filterStatusListeners = new ArrayList<FilterStatusListener<T>>();

	/**
	 * 
	 * @param data
	 * @param toNumberFunction
	 * @param selectionModel   the selection model is NOT meant to represent the
	 *                         internal (or external) filter status of the dynamic
	 *                         query (queries). It can be used to synchronize data
	 *                         selections within the data distribution chart.
	 */
	public DynamicQueryView(Collection<T> data, Function<T, Number> toNumberFunction,
			SelectionModel<T> selectionModel) {
		this(data, toNumberFunction, selectionModel, 25);
	}

	/**
	 * 
	 * @param data
	 * @param toNumberFunction
	 * @param selectionModel   the selection model is NOT meant to represent the
	 *                         internal (or external) filter status of the dynamic
	 *                         query (queries). It can be used to synchronize data
	 *                         selections within the data distribution chart.
	 * @param binCount         the number of bins for the value histogram. Default:
	 *                         25
	 */
	public DynamicQueryView(Collection<T> data, Function<T, Number> toNumberFunction, SelectionModel<T> selectionModel,
			int binCount) {
		super(new BorderLayout());

		Objects.requireNonNull(data);
		Objects.requireNonNull(toNumberFunction);
		Objects.requireNonNull(selectionModel);

		if (binCount < 1)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + ": bin count must be creater zero");

		this.scaledToNumberFunction = scaledToNumberFunction(toNumberFunction);

		// range slider
		rangeSliderPanel = createRangeSliderPanel(data, toNumberFunction);
		rangeSlider = rangeSliderPanel.getRangeSlider();

		JPanel southGrid = new JPanel(new GridLayout(1, 1));
		southGrid.add(rangeSliderPanel);

		JPanel south = new JPanel(new BorderLayout());
		JPanel westSpacer = new JPanel();
		westSpacer.setPreferredSize(
				new Dimension((int) (Y_AXIS_WIDTH - InfoVisRangeSlider.SLIDER_POINTER_WIDTH * 0.5), 0));
		south.add(westSpacer, BorderLayout.WEST);

		south.add(southGrid, BorderLayout.CENTER);

		add(south, BorderLayout.SOUTH);

		// this is for internal highlighting reasons
		rangeSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {

				// this view implements predicate for the range slider
				FilterChangedEvent<T> filterChangedEvent = new FilterChangedEvent<T>(DynamicQueryView.this,
						DynamicQueryView.this);
				for (FilterStatusListener<T> filterStatusListener : filterStatusListeners)
					filterStatusListener.filterStatusChanged(filterChangedEvent);

				repaint();
			}
		});

		// vertical histogram and spacing
		histogram = Histograms.create(data, scaledToNumberFunction, binCount, true);
		histogram.setDrawXAxis(false); // do not draw the histogram's x axis. it shows numbers according to the
										// INTEGER_MULTIPLIER. show the axis of the slider instead
		histogram.setDrawYAxis(true);
		histogram.setYAxisLegendWidth(Y_AXIS_WIDTH);
		histogram.setXAxisLegendHeight(20);
		Histograms.addInteraction(histogram, selectionModel, true, true);
		addFilterStatusListener(histogram);

		JPanel histogramCanvas = new JPanel(new BorderLayout());
		JPanel eastSpacer = new JPanel();
		eastSpacer.setPreferredSize(new Dimension((int) (InfoVisRangeSlider.SLIDER_POINTER_WIDTH * 0.5), 0));
		histogramCanvas.add(eastSpacer, BorderLayout.EAST);
		histogramCanvas.add(histogram, BorderLayout.CENTER);

		add(histogramCanvas, BorderLayout.CENTER);
	}

	private InfoVisRangeSliderPanel createRangeSliderPanel(Collection<T> data, Function<T, Number> toNumberFunction) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for (T t : data) {
			double d = toNumberFunction.apply(t).doubleValue();
			min = Math.min(min, d);
			max = Math.max(max, d);
		}

		return new InfoVisRangeSliderPanel((int) Math.floor(min * INTEGER_MULTIPLIER),
				(int) Math.ceil(max * INTEGER_MULTIPLIER), (int) Math.floor(min * INTEGER_MULTIPLIER),
				(int) Math.ceil(max * INTEGER_MULTIPLIER), min, max);
	}

	public void setTitle(String title) {
		Collection<TitlePainter> titlePainters = new ArrayList<>();
		for (ChartPainter painter : histogram.getChartPainters())
			if (painter instanceof TitlePainter)
				titlePainters.add((TitlePainter) painter);

		for (TitlePainter titlePainter : titlePainters)
			histogram.removeChartPainter(titlePainter);

		InfoVisChartPanels.addTitle(histogram, title);
	}

	public void addFilterStatusListener(FilterStatusListener<T> listener) {
		this.filterStatusListeners.remove(listener);

		this.filterStatusListeners.add(listener);
	}

	@Override
	/**
	 * changes to the global filter status will be delegated to the histogram.
	 */
	public void filterStatusChanged(FilterChangedEvent<T> filterChangedEvent) {
		histogram.filterStatusChanged(filterChangedEvent);
	}

	/**
	 * Adds a ChangeListener to the slider of the view.
	 *
	 * @param l the ChangeListener to add
	 * @see #fireStateChanged
	 * @see #removeChangeListener
	 */
	public void addChangeListener(ChangeListener l) {
		this.rangeSlider.addChangeListener(l);
	}

	@Override
	public boolean test(T t) {
		return rangeSlider.inRange(scaledToNumberFunction.apply(t).doubleValue());
	}

	public IPositionEncodingFunction getXPositionEncodingFunction() {
		return rangeSlider.getXPositionEncodingFunction();
	}

	/**
	 * the slider is in integers, the value domain is in doubles. to compensate this
	 * all real world doubles are multiplied by the INTEGER_MULTIPLIER;
	 * 
	 * @param toNumberFunction
	 * @return
	 */
	private static <T> Function<T, Number> scaledToNumberFunction(Function<T, Number> toNumberFunction) {
		return t -> toNumberFunction.apply(t).doubleValue() * INTEGER_MULTIPLIER;
	}

	public Histogram<T> getHistogram() {
		return histogram;
	}

	public boolean isShowingTooltips(DynamicQueryView<T> view) {
		return view.getHistogram().isShowingTooltips();
	}

	public void setShowingTooltips(DynamicQueryView<T> view, boolean showingTooltips) {
		view.getHistogram().setShowingTooltips(showingTooltips);
		InfoVisRangeSliderPanels.setShowingTooltips(rangeSliderPanel, showingTooltips);
	}

}
