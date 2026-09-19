package com.github.TKnudsen.infoVis.view.painters.treemap;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A generic node in the hierarchy a {@link TreemapPainter} renders. Carries
 * no domain knowledge -- {@code item} is only ever inspected by a caller-
 * supplied {@link TreemapMetric} (leaves) or left {@code null} (internal
 * nodes, whose value comes from {@link TreemapMetric#aggregate(List)}
 * instead).
 *
 * <p>
 * {@code value} and {@code bounds} are populated by {@link TreemapValues}
 * and {@link SquarifiedTreemapLayout} respectively, not by the caller.
 *
 * @version 1.0
 * @since 2026
 */
public class TreemapNode<T> {

	private final T item;
	private final String label;
	private final List<TreemapNode<T>> children = new ArrayList<>();
	private TreemapNode<T> parent;

	private double value = Double.NaN;
	private Rectangle2D bounds;

	/**
	 * @param item  the leaf's underlying object, inspected by a {@link
	 *              TreemapMetric#value(Object)}; {@code null} for an internal
	 *              (non-leaf) node, whose value is derived instead via {@link
	 *              TreemapMetric#aggregate(List)}
	 * @param label display label, e.g. shown as a tooltip or tile caption
	 */
	public TreemapNode(T item, String label) {
		this.item = item;
		this.label = label;
	}

	public void addChild(TreemapNode<T> child) {
		child.parent = this;
		children.add(child);
	}

	public T getItem() {
		return item;
	}

	public String getLabel() {
		return label;
	}

	public List<TreemapNode<T>> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public TreemapNode<T> getParent() {
		return parent;
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public double getValue() {
		return value;
	}

	void setValue(double value) {
		this.value = value;
	}

	public Rectangle2D getBounds() {
		return bounds;
	}

	void setBounds(Rectangle2D bounds) {
		this.bounds = bounds;
	}
}
