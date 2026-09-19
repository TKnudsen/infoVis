package com.github.TKnudsen.infoVis.view.painters.treemap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.interaction.ISelectionVisualizer;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;

/**
 * Renders a {@link TreemapNode} hierarchy (values already computed via
 * {@link TreemapValues#computeValues}, laid out on {@link #setRectangle} via
 * {@link SquarifiedTreemapLayout}) as nested, space-filling rectangles.
 *
 * <p>
 * Only leaf tiles are filled -- an internal node's rectangle is exactly the
 * union of its children's tiles, so filling it too would either be
 * redundant or hide its children, depending on draw order. Internal nodes
 * still get an outline, drawn before descending into their children (so a
 * child's own outline/fill paints on top wherever they'd otherwise
 * coincide), which is what makes the folder-level structure visible at all.
 *
 * <p>
 * Selection follows the same {@link IClickSelection}/{@link
 * IRectangleSelection}/{@link ISelectionVisualizer} contract {@code
 * com.JB.infoVisLib.painters.barchart.BarChart} already uses, so a {@link
 * com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler}
 * attaches the same way. The two selection modes deliberately differ in what
 * they select: a click resolves to the single most specific node (leaf or
 * folder) under the pointer -- the natural target for a "select/drill into
 * this" action -- while a rectangle drag collects every <em>leaf</em>
 * intersecting it, mirroring how a bar chart's rectangle selection picks
 * individual bars rather than bar groups.
 *
 * @param <T> the type of a leaf's underlying item, e.g. a {@code File}
 * @version 1.0
 * @since 2026
 */
public class TreemapPainter<T> extends ChartPainter implements IClickSelection<TreemapNode<T>>,
		IRectangleSelection<TreemapNode<T>>, ISelectionVisualizer<TreemapNode<T>> {

	private TreemapNode<T> root;

	private Color lineColor = Color.BLACK;
	private Color highlightColor = Color.ORANGE;

	private Function<? super TreemapNode<T>, Color> fillColorFunction = n -> Color.LIGHT_GRAY;
	private Function<? super TreemapNode<T>, Boolean> selectedFunction;

	public TreemapPainter(TreemapNode<T> root) {
		this.root = root;
	}

	public void setRoot(TreemapNode<T> root) {
		this.root = root;
	}

	public TreemapNode<T> getRoot() {
		return root;
	}

	/** Fill color for leaf tiles; default is a flat light gray. */
	public void setFillColorFunction(Function<? super TreemapNode<T>, Color> fillColorFunction) {
		this.fillColorFunction = fillColorFunction;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public void setHighlightColor(Color highlightColor) {
		this.highlightColor = highlightColor;
	}

	public Color getHighlightColor() {
		return highlightColor;
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);

		if (root != null && rectangle != null)
			SquarifiedTreemapLayout.layout(root, rectangle);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		if (root == null)
			return;

		drawNode(g2, root);
	}

	private void drawNode(Graphics2D g2, TreemapNode<T> node) {
		Rectangle2D bounds = node.getBounds();
		if (bounds == null || bounds.getWidth() <= 0 || bounds.getHeight() <= 0)
			return;

		if (node.isLeaf()) {
			Color fill = fillColorFunction == null ? Color.LIGHT_GRAY : fillColorFunction.apply(node);
			if (selectedFunction != null && Boolean.TRUE.equals(selectedFunction.apply(node)))
				fill = highlightColor;

			g2.setColor(fill);
			g2.fill(bounds);

			g2.setColor(lineColor);
			g2.draw(bounds);
		} else {
			g2.setColor(lineColor);
			g2.draw(bounds);

			for (TreemapNode<T> child : node.getChildren())
				drawNode(g2, child);
		}
	}

	@Override
	public List<TreemapNode<T>> getElementsAtPoint(Point p) {
		List<TreemapNode<T>> result = new ArrayList<>();
		if (p == null || root == null)
			return result;

		TreemapNode<T> match = findDeepestAt(root, p);
		if (match != null)
			result.add(match);

		return result;
	}

	/**
	 * Descends from {@code node} toward {@code p}, returning the deepest node
	 * whose bounds contain it -- the single most specific tile under the
	 * pointer, be it a leaf or a folder.
	 */
	private TreemapNode<T> findDeepestAt(TreemapNode<T> node, Point p) {
		if (node.getBounds() == null || !node.getBounds().contains(p))
			return null;

		for (TreemapNode<T> child : node.getChildren()) {
			TreemapNode<T> deeper = findDeepestAt(child, p);
			if (deeper != null)
				return deeper;
		}

		return node;
	}

	@Override
	public List<TreemapNode<T>> getElementsInRectangle(RectangularShape rectangle) {
		List<TreemapNode<T>> result = new ArrayList<>();
		if (rectangle == null || root == null)
			return result;

		collectLeavesIntersecting(root, rectangle, result);
		return result;
	}

	private void collectLeavesIntersecting(TreemapNode<T> node, RectangularShape rectangle,
			List<TreemapNode<T>> out) {
		if (node.getBounds() == null || !rectangle.intersects(node.getBounds()))
			return;

		if (node.isLeaf())
			out.add(node);
		else
			for (TreemapNode<T> child : node.getChildren())
				collectLeavesIntersecting(child, rectangle, out);
	}

	@Override
	public void setSelectedFunction(Function<? super TreemapNode<T>, Boolean> selectedFunction) {
		this.selectedFunction = selectedFunction;
	}
}
