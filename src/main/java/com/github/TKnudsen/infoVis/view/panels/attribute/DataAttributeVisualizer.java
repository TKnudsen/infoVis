package com.github.TKnudsen.infoVis.view.panels.attribute;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainers;
import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.frames.SVGFrame;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanels;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChartHorizontalValueBased;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChartsValueBased;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotHorizontalChartPanel;
import com.github.TKnudsen.infoVis.view.panels.boxplot.Boxplots;
import com.github.TKnudsen.infoVis.view.panels.histogram.Histogram;
import com.github.TKnudsen.infoVis.view.panels.histogram.Histograms;
import com.github.TKnudsen.infoVis.view.tools.VisualMappingTools;

/**
 * <p>
 * Creates interactive charts for numeric (histograms, boxplots) and categorical
 * (bar charts) attributes.
 * </p>
 *
 * @version 1.0
 */
public class DataAttributeVisualizer {

	private static final int DEFAULT_FRAME_WIDTH = 400;
	private static final int DEFAULT_FRAME_HEIGHT = 300;
	private static final int DEFAULT_MAX_HISTOGRAM_BINS = 12;
	private static final int DEFAULT_MAX_CATEGORIES = 20;

	/**
	 * Plot all attributes in the container.
	 * 
	 * @param dataContainer Container to visualize
	 * @return List of created frames (for cleanup)
	 */
	public static List<SVGFrame> plotAttributes(ComplexDataContainer dataContainer) {
		return plotAttributes(dataContainer, DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT);
	}

	/**
	 * Plot all attributes with custom frame size.
	 * 
	 * @param dataContainer Container to visualize
	 * @param frameWidth    Width of each frame
	 * @param frameHeight   Height of each frame
	 * @return List of created frames (for cleanup)
	 */
	public static List<SVGFrame> plotAttributes(ComplexDataContainer dataContainer, int frameWidth, int frameHeight) {

		if (dataContainer == null) {
			System.err.println("DataVisualizer: Cannot plot null container");
			return new ArrayList<>();
		}

		List<SVGFrame> frames = new ArrayList<>();

		for (String attribute : dataContainer.getAttributeNames()) {
			SVGFrame frame = plotAttribute(dataContainer, attribute, frameWidth, frameHeight);
			if (frame != null) {
				frames.add(frame);
			}
		}

		return frames;
	}

	/**
	 * Plot a single attribute.
	 * 
	 * @param dataContainer Container containing the attribute
	 * @param attribute     Attribute name to plot
	 * @return Created frame (or null if failed)
	 */
	public static SVGFrame plotAttribute(ComplexDataContainer dataContainer, String attribute) {
		return plotAttribute(dataContainer, attribute, DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT);
	}

	/**
	 * Plot a single attribute with custom frame size.
	 * 
	 * @param dataContainer Container containing the attribute
	 * @param attribute     Attribute name to plot
	 * @param frameWidth    Width of frame
	 * @param frameHeight   Height of frame
	 * @return Created frame (or null if failed)
	 */
	public static SVGFrame plotAttribute(ComplexDataContainer dataContainer, String attribute, int frameWidth,
			int frameHeight) {

		if (dataContainer == null || attribute == null) {
			System.err.println("DataVisualizer: Invalid parameters");
			return null;
		}

		try {
			InfoVisChartPanel panel;

			if (dataContainer.isNumeric(attribute)) {
				panel = createNumericChart(dataContainer, attribute);
			} else {
				panel = createCategoricalChart(dataContainer, attribute);
			}

			if (panel == null) {
				System.err.println("DataVisualizer: Failed to create chart for " + attribute);
				return null;
			}

			// Style panel
			panel.setBackground(Color.WHITE);

			// Create frame
			SVGFrame frame = SVGFrameTools.dropSVGFrame(panel, attribute, frameWidth, frameHeight);
			frame.setVisible(true);
			frame.setAlwaysOnTop(true);

			return frame;

		} catch (Exception e) {
			System.err.println("DataVisualizer: Error plotting attribute " + attribute + ": " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Create chart for numeric attribute.
	 */
	private static InfoVisChartPanel createNumericChart(ComplexDataContainer dataContainer, String attribute) {

		// Get numeric values
		double[] numbers = ComplexDataContainers.getAttributeValuesNumerical(dataContainer, attribute, true, true,
				true);

		List<Number> list = DataConversion.doubleToNumberList(numbers);

		// Filter invalid values
		list = VisualMappingTools.sanityCheckFilter(list, Number::doubleValue, false);

		if (list.isEmpty()) {
			System.err.println("DataVisualizer: No valid numeric values for " + attribute);
			return null;
		}

		// Choose chart type based on attribute name or data characteristics
		if (shouldUseBoxplot(attribute, list)) {
			return createBoxplot(list);
		} else {
			return createHistogram(list);
		}
	}

	/**
	 * Determine if box plot is more appropriate than histogram.
	 */
	private static boolean shouldUseBoxplot(String attribute, Collection<Number> numbers) {
		// Use box plot for specific attributes (customize as needed)
		if (attribute.contains("length") || attribute.contains("duration") || attribute.contains("time")
				|| attribute.contains("seconds")) {

			// Only if range is large
			StatisticsSupport stats = new StatisticsSupport(numbers);
			double range = stats.getMax() - stats.getMin();
			return range > 100;
		}
		return false;
	}

	/**
	 * Create histogram for numeric data.
	 */
	private static InfoVisChartPanel createHistogram(Collection<Number> numbers) {
		int distinctCount = (int) numbers.stream().distinct().count();
		int binCount = Math.min(distinctCount, DEFAULT_MAX_HISTOGRAM_BINS);

		Histogram<Number> histogram = Histograms.create(numbers, binCount, false);
		return histogram;
	}

	/**
	 * Create boxplot for numeric data.
	 */
	private static InfoVisChartPanel createBoxplot(Collection<Number> numbers) {
		StatisticsSupport statistics = new StatisticsSupport(numbers);

		// Use reasonable bounds for boxplot
		double min = statistics.getMin();
		double max = statistics.getMax();

		// Extend max slightly for better visualization
		max = max * 1.05;

		BoxPlotHorizontalChartPanel boxplot = Boxplots.createHorizontalBoxplot(numbers, Number::doubleValue, min, max);

		return boxplot;
	}

	/**
	 * Create chart for categorical attribute.
	 */
	private static InfoVisChartPanel createCategoricalChart(ComplexDataContainer dataContainer, String attribute) {

		// Get categorical values
		Collection<String> categories = ComplexDataContainers.getAttributeValuesCategorical(dataContainer, attribute,
				true, true, true);

		if (categories.isEmpty()) {
			System.err.println("DataVisualizer: No categorical values for " + attribute);
			return null;
		}

		// Count top categories
		Map<String, Integer> topCategories = DataConversion.countOccurrences(categories, DEFAULT_MAX_CATEGORIES);

		// Create bar chart
		List<String> labels = new ArrayList<>(topCategories.keySet());
		List<Integer> bars = new ArrayList<>();
		for (String label : labels) {
			bars.add(topCategories.get(label));
		}

		BarChartHorizontalValueBased chart = BarChartsValueBased.createBarChartHorizontal(bars, Color.GRAY);
		BarChartsValueBased.addLegend(chart, labels);

		// Add title
		int uniqueCount = (int) categories.stream().distinct().count();
		String titleSuffix = (uniqueCount <= DEFAULT_MAX_CATEGORIES) ? " [" + uniqueCount + " categories]"
				: " [top " + DEFAULT_MAX_CATEGORIES + " of " + uniqueCount + "]";

		String title = "Distribution for " + attribute + titleSuffix;
		InfoVisChartPanels.addTitle(chart, title);

		return chart;
	}

	/**
	 * Close all frames (cleanup).
	 * 
	 * @param frames Frames to close
	 */
	public static void closeAllFrames(List<SVGFrame> frames) {
		if (frames == null) {
			return;
		}

		for (SVGFrame frame : frames) {
			if (frame != null) {
				frame.dispose();
			}
		}
	}
}