package com.github.TKnudsen.infoVis.view.painters.treemap;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * The squarified treemap layout algorithm (Bruls, Huizing &amp; van Wijk,
 * 2000): recursively partitions a rectangle among a node's children,
 * proportional to their {@link TreemapNode#getValue()}, greedily choosing
 * each row/column to keep tile aspect ratios as close to square as possible
 * rather than filling strictly left-to-right or top-to-bottom.
 *
 * <p>
 * Requires every node's value to already be set (see {@link
 * TreemapValues#computeValues}); populates {@link TreemapNode#getBounds()}
 * for every node in the hierarchy, not just leaves, so an internal
 * (folder-like) node's own rectangle -- the union of its children's tiles --
 * is directly available without having to recompute it.
 *
 * @version 1.0
 * @since 2026
 */
public class SquarifiedTreemapLayout {

	private SquarifiedTreemapLayout() {
		// utility class
	}

	public static <T> void layout(TreemapNode<T> node, Rectangle2D bounds) {
		node.setBounds(bounds);

		List<TreemapNode<T>> children = new ArrayList<>(node.getChildren());
		if (children.isEmpty())
			return;

		// largest first -- standard squarify precondition, keeps early rows/columns
		// closer to square than an arbitrary order would
		children.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

		double totalValue = 0;
		for (TreemapNode<T> child : children)
			totalValue += Math.max(child.getValue(), 0);

		double area = bounds.getWidth() * bounds.getHeight();
		double scale = totalValue > 0 ? area / totalValue : 0;

		List<Double> areas = new ArrayList<>(children.size());
		for (TreemapNode<T> child : children)
			areas.add(Math.max(child.getValue(), 0) * scale);

		squarify(children, areas, bounds);

		// only recurse into non-leaf children -- a leaf's bounds are already set
		// above and it has nothing further to lay out, and skipping it here avoids
		// an otherwise-pointless call per leaf (recursion depth then follows tree
		// depth only, not sibling count, matching squarify's own iterative fix)
		for (TreemapNode<T> child : children)
			if (!child.isLeaf())
				layout(child, child.getBounds());
	}

	/**
	 * Iterative on purpose: an equivalent one-row-or-item-at-a-time recursive
	 * version recurses roughly once per <em>sibling item</em>, not once per tree
	 * level -- for a folder with thousands of files directly inside it (not
	 * nested any deeper), that recursion depth alone was enough to overflow the
	 * stack. A loop keeps this at O(1) stack depth regardless of how many
	 * siblings there are.
	 */
	private static <T> void squarify(List<TreemapNode<T>> items, List<Double> itemAreas, Rectangle2D bounds) {
		List<TreemapNode<T>> row = new ArrayList<>();
		List<Double> rowAreas = new ArrayList<>();
		Rectangle2D remainingBounds = bounds;

		int index = 0;
		while (index < items.size()) {
			TreemapNode<T> next = items.get(index);
			double nextArea = itemAreas.get(index);

			double w = Math.min(remainingBounds.getWidth(), remainingBounds.getHeight());

			List<Double> rowWithNextAreas = new ArrayList<>(rowAreas);
			rowWithNextAreas.add(nextArea);

			if (row.isEmpty() || worst(rowAreas, w) >= worst(rowWithNextAreas, w)) {
				row.add(next);
				rowAreas.add(nextArea);
				index++;
			} else {
				remainingBounds = layoutRow(row, rowAreas, remainingBounds);
				row = new ArrayList<>();
				rowAreas = new ArrayList<>();
			}
		}

		layoutRow(row, rowAreas, remainingBounds);
	}

	/**
	 * The worst (largest) width/height ratio any tile in {@code rowAreas} would
	 * have if laid out into a strip of fixed side {@code w} -- the standard
	 * closed-form version (avoids laying out candidate rectangles just to
	 * measure them): {@code max( w^2*rMax/s^2 , s^2/(w^2*rMin) )}, where s is
	 * the row's total area and rMax/rMin its largest/smallest tile.
	 */
	private static double worst(List<Double> rowAreas, double w) {
		if (rowAreas.isEmpty())
			return Double.POSITIVE_INFINITY;

		double s = 0;
		double rMax = Double.NEGATIVE_INFINITY;
		double rMin = Double.POSITIVE_INFINITY;
		for (double a : rowAreas) {
			s += a;
			rMax = Math.max(rMax, a);
			rMin = Math.min(rMin, a);
		}

		if (s <= 0 || rMin <= 0)
			return Double.POSITIVE_INFINITY;

		double w2 = w * w;
		double s2 = s * s;
		return Math.max((w2 * rMax) / s2, s2 / (w2 * rMin));
	}

	/**
	 * Places {@code row}'s tiles as a strip along {@code bounds}' shorter side
	 * (a column on the left if {@code bounds} is wider than tall, a row along
	 * the top otherwise), each tile's extent along that side proportional to
	 * its area, thickness = row's total area / that side's length.
	 *
	 * @return the remainder of {@code bounds} left after removing the strip
	 */
	private static <T> Rectangle2D layoutRow(List<TreemapNode<T>> row, List<Double> rowAreas, Rectangle2D bounds) {
		if (row.isEmpty())
			return bounds;

		double rowAreaSum = 0;
		for (double a : rowAreas)
			rowAreaSum += a;

		if (rowAreaSum <= 0)
			return bounds;

		boolean columnOnLeft = bounds.getWidth() > bounds.getHeight();
		double w = columnOnLeft ? bounds.getHeight() : bounds.getWidth();
		double thickness = rowAreaSum / w;

		if (columnOnLeft) {
			double y = bounds.getY();
			for (int i = 0; i < row.size(); i++) {
				double h = w * (rowAreas.get(i) / rowAreaSum);
				row.get(i).setBounds(new Rectangle2D.Double(bounds.getX(), y, thickness, h));
				y += h;
			}
			return new Rectangle2D.Double(bounds.getX() + thickness, bounds.getY(), bounds.getWidth() - thickness,
					bounds.getHeight());
		}

		double x = bounds.getX();
		for (int i = 0; i < row.size(); i++) {
			double wi = w * (rowAreas.get(i) / rowAreaSum);
			row.get(i).setBounds(new Rectangle2D.Double(x, bounds.getY(), wi, thickness));
			x += wi;
		}
		return new Rectangle2D.Double(bounds.getX(), bounds.getY() + thickness, bounds.getWidth(),
				bounds.getHeight() - thickness);
	}
}
