package com.github.TKnudsen.infoVis.view.painters.treemap;

import java.util.ArrayList;
import java.util.List;

/**
 * Populates every node's {@link TreemapNode#getValue()} in one bottom-up
 * pass, from a {@link TreemapMetric}: leaves via {@link
 * TreemapMetric#value(Object)}, every internal node via {@link
 * TreemapMetric#aggregate(List)} over its already-computed children. Must
 * run before {@link SquarifiedTreemapLayout#layout}, which sizes tiles from
 * these values.
 *
 * @version 1.0
 * @since 2026
 */
public class TreemapValues {

	private TreemapValues() {
		// utility class
	}

	/**
	 * @return {@code node}'s own value, same as {@code node.getValue()} after
	 *         this call -- convenient for a caller that only needs the root's
	 *         total, without also needing to hold onto {@code node} separately
	 */
	public static <T> double computeValues(TreemapNode<T> node, TreemapMetric<T> metric) {
		double value;

		if (node.isLeaf())
			value = metric.value(node.getItem());
		else {
			List<Double> childValues = new ArrayList<>(node.getChildren().size());
			for (TreemapNode<T> child : node.getChildren())
				childValues.add(computeValues(child, metric));
			value = metric.aggregate(childValues);
		}

		node.setValue(value);
		return value;
	}
}
