package com.github.TKnudsen.infoVis.view.views;

import java.awt.BorderLayout;
import java.awt.Color;
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
import com.github.TKnudsen.infoVis.view.interaction.controls.InfoVisRangeSliders;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterChangedEvent;
import com.github.TKnudsen.infoVis.view.interaction.event.FilterStatusListener;
import com.github.TKnudsen.infoVis.view.interaction.handlers.TooltipHandler;
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

//	public static final int Y_AXIS_WIDTH = 22;
	public static final int Y_AXIS_WIDTH = 28;
	private final Histogram<T> histogram;

	private final JPanel eastSpacer;
	private final InfoVisRangeSliderPanel rangeSliderPanel;
	private final InfoVisRangeSlider rangeSlider;
	private static final int INTEGER_MULTIPLIER = 1000;
	private static double LARGE_VALUE_MITIGATOR = 1.0;

	private boolean missingValuesAreIn = false;

	/**
	 * tool tip
	 */
	private ChartPainter toolTipPainter = null;
	private boolean toolTipping = true;
	private TooltipHandler tooltipHandler;

	/**
	 * listeners
	 */
	private final Collection<FilterStatusListener<T>> filterStatusListeners = new ArrayList<FilterStatusListener<T>>();

	/**
	 * 
	 * @param data             the data distribution
	 * @param toNumberFunction the function that maps the data to numbers
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
	 * @param data             the data distribution
	 * @param toNumberFunction the function that maps the data to numbers
	 * @param selectionModel   the selection model is NOT meant to represent the
	 *                         internal (or external) filter status of the dynamic
	 *                         query (queries). It can be used to synchronize data
	 *                         selections within the data distribution chart.
	 * @param binCount         the number of bins for the value histogram. Default:
	 *                         25
	 */
	public DynamicQueryView(Collection<T> data, Function<T, Number> toNumberFunction, SelectionModel<T> selectionModel,
			int binCount) {
		this(data, toNumberFunction, selectionModel, binCount, null, null);
	}

	/**
	 * 
	 * @param data             the data distribution
	 * @param toNumberFunction the function that maps the data to numbers
	 * @param selectionModel   the selection model is NOT meant to represent the
	 *                         internal (or external) filter status of the dynamic
	 *                         query (queries). It can be used to synchronize data
	 *                         selections within the data distribution chart.
	 * @param binCount         the number of bins for the value histogram. Default:
	 *                         25
	 * @param defaultColor     color of the overall data distribution
	 * @param filterColor      color of the data that is filtered out
	 */
	public DynamicQueryView(Collection<T> data, Function<T, Number> toNumberFunction, SelectionModel<T> selectionModel,
			int binCount, Color defaultColor, Color filterColor) {
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
		rangeSlider.getRangeSliderUI().setRangeColor(filterColor != null ? filterColor : Color.DARK_GRAY);
		rangeSlider.setMinimumSize(new Dimension(0, InfoVisRangeSlider.SLIDER_POINTER_WIDTH));
		rangeSlider.setPreferredSize(new Dimension(0, InfoVisRangeSlider.SLIDER_POINTER_WIDTH));
		rangeSlider.setMaximumSize(new Dimension(0, InfoVisRangeSlider.SLIDER_POINTER_WIDTH));

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

				if (rangeSlider.isInNeutralState())
					rangeSlider.getRangeSliderUI().setRangeColor(filterColor != null ? filterColor : Color.DARK_GRAY);
				else
					rangeSlider.getRangeSliderUI()
							.setRangeColor(filterColor != null ? filterColor : Histograms.DEFAULT_COLOR);

				repaint();
			}
		});

		// vertical histogram and spacing
		histogram = Histograms.create(data, scaledToNumberFunction, null, null, binCount, true, defaultColor,
				filterColor);
		histogram.setDrawXAxis(false); // do not draw the histogram's x axis. it shows numbers according to the
										// INTEGER_MULTIPLIER. show the axis of the slider instead
		histogram.setDrawYAxis(true);
		histogram.setYAxisOverlay(false);
		histogram.setYAxisLegendWidth(Y_AXIS_WIDTH);
		histogram.setXAxisLegendHeight(20); // has no effect, looks like as for 30
		histogram.setShowingTooltips(true);
		Histograms.addInteraction(histogram, selectionModel, true, true);
		addFilterStatusListener(histogram);

		JPanel histogramCanvas = new JPanel(new BorderLayout());
		eastSpacer = new JPanel();
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

		if (max > Integer.MAX_VALUE / INTEGER_MULTIPLIER) {
//			throw new IllegalArgumentException("DynamicQueryView: maximum value for the dynamic query must not exceed "
//					+ Integer.MAX_VALUE / INTEGER_MULTIPLIER);

			// change LARGE_VALUE_MITIGATOR from 1.0 to...
			double dec = Math.ceil(Math.log10(max / (Integer.MAX_VALUE / INTEGER_MULTIPLIER)));
			LARGE_VALUE_MITIGATOR = 1 / Math.pow(10, dec);
		}

		// no need any more: range slider was replaced
		// LookAndFeelFactory.setDefaultStyle(1);
		return new InfoVisRangeSliderPanel((int) Math.floor(min * (LARGE_VALUE_MITIGATOR * INTEGER_MULTIPLIER)),
				(int) Math.ceil(max * (LARGE_VALUE_MITIGATOR * INTEGER_MULTIPLIER)),
				(int) Math.floor(min * (LARGE_VALUE_MITIGATOR * INTEGER_MULTIPLIER)),
				(int) Math.ceil(max * (LARGE_VALUE_MITIGATOR * INTEGER_MULTIPLIER)), min, max);
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

	public void removeFilterStatusListener(FilterStatusListener<T> listener) {
		this.filterStatusListeners.remove(listener);
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
	 */
	public void addChangeListener(ChangeListener l) {
		this.rangeSlider.addChangeListener(l);
	}

	@Override
	public boolean test(T t) {
		// this is new: a range slider is only active if the two sliders are not in
		// minimum-maximum (default) constellation
		if (rangeSlider.isInNeutralState())
			return true;

		double d = scaledToNumberFunction.apply(t).doubleValue();
		if (Double.isNaN(d) && missingValuesAreIn)
			return true;
		return rangeSlider.inRange(d);
	}

	public IPositionEncodingFunction getXPositionEncodingFunction() {
		System.err.println(
				"DynamicQueryView:getXPositionEncodingFunction returns the range slider position encoding function which has "
						+ (LARGE_VALUE_MITIGATOR * INTEGER_MULTIPLIER) + "times too big values");
		return rangeSlider.getXPositionEncodingFunction();
	}

	/**
	 * the slider is in integers, the value domain is in doubles. To compensate this
	 * all real world doubles are multiplied by the
	 * LARGE_VALUE_MITIGATOR*INTEGER_MULTIPLIER;
	 * 
	 * @param toNumberFunction
	 * @return
	 */
	private static <T> Function<T, Number> scaledToNumberFunction(Function<T, Number> toNumberFunction) {
		return t -> toNumberFunction.apply(t).doubleValue() * (LARGE_VALUE_MITIGATOR * INTEGER_MULTIPLIER);
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

	public Number getMinimumRangeBound() {
		return InfoVisRangeSliders.getMinRangeBound(rangeSlider).doubleValue()
				/ (double) (LARGE_VALUE_MITIGATOR * INTEGER_MULTIPLIER);
	}

	public void setMinimumRangeBound(Number value) {
		rangeSlider.setLowValue((int) (value.doubleValue() * (LARGE_VALUE_MITIGATOR * INTEGER_MULTIPLIER)));
	}

	public Number getMaximumRangeBound() {
		return InfoVisRangeSliders.getMaxRangeBound(rangeSlider).doubleValue()
				/ (double) (LARGE_VALUE_MITIGATOR * INTEGER_MULTIPLIER);
	}

	public void setMaximumRangeBound(Number value) {
		rangeSlider.setHighValue((int) (value.doubleValue() * (LARGE_VALUE_MITIGATOR * INTEGER_MULTIPLIER)));
	}

	public boolean isMissingValuesAreIn() {
		return missingValuesAreIn;
	}

	public void setMissingValuesAreIn(boolean missingValuesAreIn) {
		this.missingValuesAreIn = missingValuesAreIn;
	}
}
