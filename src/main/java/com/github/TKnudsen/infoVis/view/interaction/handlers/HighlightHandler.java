package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.List;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;

import de.javagl.selection.SelectionModel;

/**
 * <p>
 * Mouse-move-driven counterpart to {@link SelectionHandler}: instead of
 * committing a persistent choice on click/drag, it continuously updates a
 * {@link SelectionModel} with whatever element is currently under the cursor
 * (empty once the cursor leaves), for transient hover highlighting.
 * </p>
 *
 * <p>
 * Reuses {@link SelectionModel} rather than inventing a separate "highlight
 * model" type: the interface itself (add/remove/set/clear a named subset,
 * fire change events) has no notion of how membership was decided, so the
 * exact same, already-proven plumbing works equally well for a hover-driven
 * subset as for a click-driven one. Pass a model dedicated to highlighting
 * (not the one used for real selection) so the two states stay independent.
 * </p>
 *
 * @version 1.00
 * @since 2026
 */
public class HighlightHandler<T> extends InteractionHandler {

	private final MouseListener mouseListener;
	private final MouseMotionListener mouseMotionListener;

	private IClickSelection<T> hoverSelection;

	private final SelectionModel<T> highlightModel;

	public HighlightHandler(SelectionModel<T> highlightModel) {
		this.highlightModel = highlightModel;

		this.mouseListener = createMouseListener();
		this.mouseMotionListener = createMouseMotionListener();
	}

	/**
	 * @param hoverSelection resolves the element(s) under a given point; reuses
	 *                       {@link IClickSelection} since hit-testing a point is
	 *                       exactly the same operation click selection already
	 *                       needs
	 */
	public void setHoverSelection(IClickSelection<T> hoverSelection) {
		this.hoverSelection = hoverSelection;
	}

	private MouseListener createMouseListener() {
		return new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				highlightModel.clear();
			}
		};
	}

	private MouseMotionListener createMouseMotionListener() {
		return new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				updateHover(e.getPoint());
			}
		};
	}

	private void updateHover(Point p) {
		if (hoverSelection == null)
			return;

		List<T> elements = hoverSelection.getElementsAtPoint(p);
		if (elements == null || elements.isEmpty())
			highlightModel.clear();
		else
			// only ever one element highlighted at a time, even if the hit test
			// (e.g. overplotted points) returns several
			highlightModel.setSelection(Collections.singletonList(elements.get(0)));
	}

	@Override
	public void attachTo(Component newComponent) {
		if (component != null) {
			component.removeMouseListener(mouseListener);
			component.removeMouseMotionListener(mouseMotionListener);
		}
		this.component = newComponent;
		if (component != null) {
			component.addMouseListener(mouseListener);
			component.addMouseMotionListener(mouseMotionListener);
		}
	}

	public SelectionModel<T> getHighlightModel() {
		return highlightModel;
	}
}
