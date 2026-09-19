package com.github.TKnudsen.infoVis.view.painters.treemap;

import java.util.List;

/**
 * Determines the numeric value a {@link TreemapPainter} sizes tiles by.
 * Deliberately two separate operations, not one: a treemap needs both "what
 * is a leaf worth" and "how do a folder's children combine into the
 * folder's own worth" -- and the two most obvious metrics disagree on the
 * second part. File size aggregates by summing (a folder's total is the sum
 * of what's in it, matching actual disk usage and standard treemap
 * space-filling semantics); file/path length aggregates by taking the
 * maximum (a folder's "value" for that purpose is its single worst offender,
 * not a meaningless sum of unrelated string lengths). Bundling both
 * operations into one implementation per metric keeps a metric's intent
 * self-contained rather than letting the layout algorithm assume one
 * aggregation rule fits every metric.
 *
 * @param <T> the type of a leaf's underlying item, e.g. a {@code File}
 * @version 1.0
 * @since 2026
 */
public interface TreemapMetric<T> {

	/**
	 * @param leaf a leaf node's underlying item (never an internal node's --
	 *             those are never {@code null} by {@link TreemapNode}'s
	 *             contract, but internal nodes get their value from {@link
	 *             #aggregate(List)} instead of this method)
	 * @return the leaf's raw value under this metric
	 */
	double value(T leaf);

	/**
	 * @param childValues the already-computed values of one internal node's
	 *                     direct children (leaves and/or further internal
	 *                     nodes alike -- this method doesn't need to know
	 *                     which)
	 * @return that internal node's own value under this metric
	 */
	double aggregate(List<Double> childValues);
}
