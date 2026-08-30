package com.github.TKnudsen.infoVis.view.panels.barchart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.painters.axis.numerical.XAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.axis.numerical.YAxisNumericalPainter;
import com.github.TKnudsen.infoVis.view.painters.barchart.BarChartPainter;
import com.github.TKnudsen.infoVis.view.panels.bins.AbstractBinnedDistributionPanel;

/**
 * <p>
 * Categorical BarChart with filter + selection support.
 * 
 * Uses {@link AbstractBinnedDistributionPanel} for: - three painter layers
 * (global / filter / selection) - picking (point / rectangle) - filter +
 * selection lifecycle
 * </p>
 *
 * @version 1.02
 */
public abstract class BarChart<T> extends AbstractBinnedDistributionPanel<T> implements IBarChart<T> {

	private static final long serialVersionUID = 1L;

	public static final double BARCHART_GRID_SPACING_RATIO = 0.25;

	public enum CategoryOrder {
		ALPHABETICAL, FREQUENCY_DESC, CUSTOM
	}

	public static final String DEFAULT_MISSING_LABEL = "(missing)";

	private final Function<? super T, String> worldToCategoryMapping;

	private final boolean includeMissingBin;
	private final String missingLabel;
	private final CategoryOrder categoryOrder;
	private final List<String> customOrder;

	// Stable bin definitions
	private volatile List<String> binLabels = Collections.emptyList();
	private volatile Map<String, Integer> labelToBinIndex = Collections.emptyMap();

	// OPTIMIZATION: Cache normalized labels to avoid repeated string operations
	private static final int NORMALIZATION_CACHE_SIZE = 10000;
	private final Map<String, String> normalizationCache = new HashMap<>(NORMALIZATION_CACHE_SIZE);

	public BarChart(Collection<? extends T> data, Function<? super T, String> worldToCategoryMapping) {
		this(data, worldToCategoryMapping, CategoryOrder.ALPHABETICAL, null, true, DEFAULT_MISSING_LABEL, null, null);
	}

	public BarChart(Collection<? extends T> data, Function<? super T, String> worldToCategoryMapping,
			CategoryOrder categoryOrder, List<String> customOrder, boolean includeMissingBin, String missingLabel,
			Color globalColor, Color filterColor) {

		super(data, globalColor != null ? globalColor : BarCharts.DEFAULT_COLOR,
				filterColor != null ? filterColor : BarCharts.DEFAULT_FILTER_COLOR);

		Objects.requireNonNull(worldToCategoryMapping, "worldToCategoryMapping must not be null");

		this.worldToCategoryMapping = worldToCategoryMapping;
		this.includeMissingBin = includeMissingBin;
		this.missingLabel = (missingLabel != null && !missingLabel.trim().isEmpty()) ? missingLabel
				: DEFAULT_MISSING_LABEL;

		this.categoryOrder = (categoryOrder != null) ? categoryOrder : CategoryOrder.ALPHABETICAL;
		this.customOrder = (customOrder != null) ? new ArrayList<>(customOrder) : null;

		rebuildBinsFromData(getData());
		init();
	}

	public List<String> getBinLabels() {
		return binLabels;
	}

	public int getBinIndexForLabel(String label) {
		Integer idx = labelToBinIndex.get(normalizeLabelCached(label));
		return (idx != null) ? idx.intValue() : -1;
	}

	@Override
	protected int getBinCount() {
		return binLabels.size();
	}

	@Override
	protected Integer getBinIndex(T item) {
		if (item == null)
			return null;

		String label = worldToCategoryMapping.apply(item);
		label = normalizeLabelCached(label);

		if (label == null) {
			if (!includeMissingBin)
				return null;
			label = missingLabel;
		}

		return labelToBinIndex.get(label);
	}

	@Override
	protected List<? extends Number> computeCounts(Collection<? extends T> items) {
		final int bins = getBinCount();

		// OPTIMIZATION: Use primitive array, convert only at the end
		int[] counts = new int[bins];

		if (items != null && !items.isEmpty()) {
			for (T t : items) {
				Integer idx = getBinIndex(t);
				if (idx != null && idx >= 0 && idx < bins) {
					counts[idx]++;
				}
			}
		}

		// OPTIMIZATION: Convert to List<Double> only once at the end
		List<Double> result = new ArrayList<>(bins);
		for (int count : counts) {
			result.add((double) count);
		}
		return result;
	}

	@Override
	public void initializeXAxisPainter(Number min, Number max) {
		setXAxisPainter(new XAxisNumericalPainter<>(min, max));
	}

	@Override
	public void initializeYAxisPainter(Number min, Number max) {
		YAxisNumericalPainter<Number> yAxis = new YAxisNumericalPainter<>(min, max);
		yAxis.setFlipAxisValues(true);
		setYAxisPainter(yAxis);
	}

	@Override
	public BarChartPainter getBarChartPainter() {
		return getGlobalPainter();
	}

	// =========================
	// OPTIMIZED Internal helpers
	// =========================

	private static final class Counter {
		int value;

		Counter(int v) {
			this.value = v;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}

	private void rebuildBinsFromData(Collection<? extends T> data) {
		if (data == null || data.isEmpty()) {
			this.binLabels = Collections.emptyList();
			this.labelToBinIndex = Collections.emptyMap();
			return;
		}

		// Capacity: if you don't know cardinality, avoid under-sizing.
		// This is conservative; tweak based on observed unique category counts.
		int expectedCategories = Math.min(data.size(), 1 << 16); // cap to avoid absurd prealloc
		int initialCapacity = (int) (expectedCategories / 0.75f) + 1;

		Map<String, Counter> freq = new HashMap<>(Math.max(16, initialCapacity));
		boolean missingSeen = false;

		for (T t : data) {
			if (t == null)
				continue;

			String label = normalizeLabelCached(worldToCategoryMapping.apply(t));
			if (label == null) {
				missingSeen = true;
				continue;
			}

			Counter c = freq.get(label);
			if (c == null) {
				freq.put(label, new Counter(1));
			} else {
				c.value++;
			}
		}

		if (normalizationCache.size() > NORMALIZATION_CACHE_SIZE * 1.5) {
			normalizationCache.clear();
		}

		final List<String> labels;

		switch (categoryOrder) {
		case FREQUENCY_DESC: {
			// Sort entries once, then extract keys (no repeated map lookups in comparator)
			List<Map.Entry<String, Counter>> entries = new ArrayList<>(freq.entrySet());
			entries.sort((a, b) -> {
				int d = Integer.compare(b.getValue().value, a.getValue().value);
				if (d != 0)
					return d;
				return a.getKey().compareTo(b.getKey());
			});

			labels = new ArrayList<>(entries.size() + (includeMissingBin && missingSeen ? 1 : 0));
			for (Map.Entry<String, Counter> e : entries) {
				labels.add(e.getKey());
			}
			break;
		}

		case CUSTOM: {
			// Build labels from keys, then custom-order
			List<String> tmp = new ArrayList<>(freq.keySet());
			labels = applyCustomOrdering(tmp, customOrder);
			break;
		}

		case ALPHABETICAL:
		default: {
			labels = new ArrayList<>(freq.keySet());
			Collections.sort(labels);
			break;
		}
		}

		if (includeMissingBin && missingSeen) {
			labels.add(missingLabel);
		}

		Map<String, Integer> map = new HashMap<>((int) (labels.size() / 0.75f) + 1);
		for (int i = 0; i < labels.size(); i++) {
			map.put(labels.get(i), i);
		}

		this.binLabels = Collections.unmodifiableList(labels);
		this.labelToBinIndex = Collections.unmodifiableMap(map);

		if (this.binLabels.isEmpty()) {
			throw new IllegalStateException("BarChart: No bins could be created from data.");
		}
	}

	/**
	 * OPTIMIZATION: Cached normalization to avoid repeated string operations
	 */
	private String normalizeLabelCached(String label) {
		if (label == null)
			return null;

		// Check cache first
		String cached = normalizationCache.get(label);
		if (cached != null || normalizationCache.containsKey(label)) {
			return cached;
		}

		// Normalize and cache
		String normalized = normalizeLabel(label);
		if (normalizationCache.size() < NORMALIZATION_CACHE_SIZE) {
			normalizationCache.put(label, normalized);
		}
		return normalized;
	}

	private static String normalizeLabel(String label) {
		if (label == null)
			return null;

		String s = label.trim();
		return s.isEmpty() ? null : s;
	}

	private static List<String> applyCustomOrdering(List<String> labels, List<String> customOrder) {
		if (customOrder == null || customOrder.isEmpty()) {
			Collections.sort(labels);
			return labels;
		}

		// OPTIMIZATION: Use Set for O(1) removal checks
		Set<String> remaining = new LinkedHashSet<>(labels);
		List<String> ordered = new ArrayList<>(labels.size());

		for (String wanted : customOrder) {
			String norm = normalizeLabel(wanted);
			if (norm != null && remaining.remove(norm)) {
				ordered.add(norm);
			}
		}

		// OPTIMIZATION: Avoid ArrayList copy if possible
		if (!remaining.isEmpty()) {
			List<String> tail = new ArrayList<>(remaining);
			Collections.sort(tail);
			ordered.addAll(tail);
		}

		return ordered;
	}
}