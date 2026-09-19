package com.github.TKnudsen.infoVis.view.panels.treemap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.github.TKnudsen.infoVis.view.interaction.handlers.SelectionHandler;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.painters.treemap.TreemapNode;
import com.github.TKnudsen.infoVis.view.painters.treemap.TreemapPainter;
import com.github.TKnudsen.infoVis.view.panels.InfoVisChartPanel;
import com.github.TKnudsen.infoVis.view.ui.InfoVisColors;

import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

/**
 * Self-contained treemap widget: owns a {@link TreemapPainter}, wires its
 * click/rectangle selection and the selection-visualization overlay
 * internally, and handles double-click drill-down -- the same way other
 * infoVis chart panels (e.g. {@code Histogram}, {@code ScatterPlot}) build
 * and own their painter(s) rather than leaving every caller to hand-wire a
 * painter, an {@link InfoVisChartPanel}, and a {@link SelectionHandler}
 * together themselves.
 *
 * <p>
 * Domain-specific consequences of interacting with the treemap (e.g. an
 * application also revealing the double-clicked node somewhere else in its
 * own UI) are exposed via {@link #setOnDrillDown(Consumer)} and {@link
 * #setOnDisplayRootChanged(Consumer)} rather than built in here, since this
 * panel has no knowledge of what {@code T} represents.
 *
 * @param <T> the type of a leaf's underlying item, e.g. a {@code File}
 * @version 1.0
 * @since 2026
 */
public class TreemapPanel<T> extends InfoVisChartPanel {

	private static final long serialVersionUID = 1L;

	private final TreemapPainter<T> treemapPainter;
	private final SelectionHandler<TreemapNode<T>> selectionHandler;

	private TreemapNode<T> displayRoot;

	/** Fired whenever {@link #showNode(TreemapNode)} changes the displayed scope, for any reason (drill-down, external navigation, reset). */
	private Consumer<TreemapNode<T>> onDisplayRootChanged;

	/** Fired only when a double-click drills into a node -- not on every {@link #showNode(TreemapNode)} call, so an application can react to the user's gesture specifically (e.g. reveal it elsewhere) without that reaction re-firing on every programmatic navigation. */
	private Consumer<TreemapNode<T>> onDrillDown;

	public TreemapPanel(TreemapNode<T> root) {
		super();

		treemapPainter = new TreemapPainter<>(root);
		treemapPainter.setHighlightColor(InfoVisColors.SELECTION_COLOR);
		displayRoot = root;

		SelectionModel<TreemapNode<T>> selectionModel = SelectionModels.create();
		selectionHandler = new SelectionHandler<>(selectionModel);
		selectionHandler.attachTo(this);
		selectionHandler.setClickSelection(treemapPainter);
		selectionHandler.setRectangleSelection(treemapPainter);

		treemapPainter.setSelectedFunction(new Function<TreemapNode<T>, Boolean>() {
			@Override
			public Boolean apply(TreemapNode<T> t) {
				return selectionHandler.getSelectionModel().isSelected(t);
			}
		});

		addChartPainter(treemapPainter);
		addChartPainter(new ChartPainter() {
			@Override
			public void draw(Graphics2D g2) {
				selectionHandler.draw(g2);
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() != 2)
					return;

				List<TreemapNode<T>> hit = treemapPainter.getElementsAtPoint(e.getPoint());
				if (hit.isEmpty() || hit.get(0).isLeaf())
					return;

				TreemapNode<T> target = hit.get(0);
				showNode(target);
				if (onDrillDown != null)
					onDrillDown.accept(target);
			}
		});
	}

	public TreemapPainter<T> getTreemapPainter() {
		return treemapPainter;
	}

	public SelectionModel<TreemapNode<T>> getSelectionModel() {
		return selectionHandler.getSelectionModel();
	}

	public void setFillColorFunction(Function<? super TreemapNode<T>, Color> fillColorFunction) {
		treemapPainter.setFillColorFunction(fillColorFunction);
	}

	public void setLineColor(Color lineColor) {
		treemapPainter.setLineColor(lineColor);
	}

	public void setHighlightColor(Color highlightColor) {
		treemapPainter.setHighlightColor(highlightColor);
	}

	public void setOnDisplayRootChanged(Consumer<TreemapNode<T>> onDisplayRootChanged) {
		this.onDisplayRootChanged = onDisplayRootChanged;
	}

	public void setOnDrillDown(Consumer<TreemapNode<T>> onDrillDown) {
		this.onDrillDown = onDrillDown;
	}

	/** @return the node the treemap currently fills its canvas with -- the whole tree, or wherever navigation last landed. */
	public TreemapNode<T> getDisplayRoot() {
		return displayRoot;
	}

	/** @return whether {@link #goUp()} would do anything -- {@link #getDisplayRoot()} isn't the tree's actual root. */
	public boolean canGoUp() {
		return displayRoot != null && displayRoot.getParent() != null;
	}

	/** Navigates to {@link #getDisplayRoot()}'s parent, if any. */
	public void goUp() {
		if (canGoUp())
			showNode(displayRoot.getParent());
	}

	/**
	 * Switches the displayed scope to {@code node} (e.g. drilling into a
	 * folder, resetting to the full tree, or following external navigation
	 * elsewhere in the application), relayouting it to fill the current
	 * canvas, and firing {@link #setOnDisplayRootChanged(Consumer)}.
	 */
	public void showNode(TreemapNode<T> node) {
		if (node == null)
			return;

		displayRoot = node;
		treemapPainter.setRoot(node);
		if (treemapPainter.getRectangle() != null)
			treemapPainter.setRectangle(treemapPainter.getRectangle());

		repaint();

		if (onDisplayRootChanged != null)
			onDisplayRootChanged.accept(node);
	}
}
