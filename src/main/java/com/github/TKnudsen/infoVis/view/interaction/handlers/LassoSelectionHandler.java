package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.TKnudsen.infoVis.view.interaction.IShapeSelection;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

import de.javagl.selection.SelectionModel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2018-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class LassoSelectionHandler<T> extends InteractionHandler {

	/**
	 * the path reflecting the lasso interaction
	 */
	private GeneralPath polyLine = new GeneralPath();
	/**
	 * mouse observation information needed for inheriting panels
	 */
	private Point pointSelectionStart = null;

	/**
	 * mouse observation information needed for inheriting panels
	 */
	private Point pointSelectionEnd = null;

	private final MouseListener mouseListener;
	private final MouseMotionListener mouseMotionListener;

	private IShapeSelection<T> shapeSelection;

	private SelectionModel<T> selectionModel;

	public LassoSelectionHandler(SelectionModel<T> selectionModel, MouseButton mouseButton) {
		this(selectionModel);

		this.setMouseButton(mouseButton);
	}

	public LassoSelectionHandler(SelectionModel<T> selectionModel) {
		this.selectionModel = selectionModel;

		this.mouseListener = createMouseListener();
		this.mouseMotionListener = createMouseMotionListener();
	}

	public void setShapeSelection(IShapeSelection<T> shapeSelection) {
		this.shapeSelection = shapeSelection;
	}

	private MouseListener createMouseListener() {
		return new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (acceptMouseButton(e)) {
					polyLine.closePath();

					handleLassoSelection(polyLine, e.isControlDown(), e.isShiftDown());

					polyLine = null;

					component.repaint();
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (acceptMouseButton(e)) {
					polyLine = new GeneralPath();
					polyLine.moveTo(e.getX(), e.getY());

					component.repaint();
				}
			}
		};
	}

	private MouseMotionListener createMouseMotionListener() {
		return new MouseMotionAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (acceptMouseButton(e)) {
					polyLine.lineTo(e.getX(), e.getY());

					component.repaint();
				}
			}
		};
	}

	public void draw(Graphics2D g) {
		if (polyLine != null) {
			g.setStroke(DisplayTools.standardDashedStroke);
			g.setColor(Color.RED);
			g.draw(polyLine);
		}
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

	public Rectangle2D getSelectionRectangle() {
		if (pointSelectionStart == null || pointSelectionEnd == null) {
			return null;
		}
		double minX = Math.min(pointSelectionStart.getX(), pointSelectionEnd.getX());
		double minY = Math.min(pointSelectionStart.getY(), pointSelectionEnd.getY());
		double width = Math.abs(pointSelectionStart.getX() - pointSelectionEnd.getX());
		double height = Math.abs(pointSelectionStart.getY() - pointSelectionEnd.getY());
		return new Rectangle2D.Double(minX, minY, width, height);
	}

	private void handleLassoSelection(GeneralPath lassoShape, boolean ctrlDown, boolean shiftDown) {
		if (lassoShape != null) {
			updateLassoSelection(shapeSelection, lassoShape, ctrlDown, shiftDown);
		}
	}

	private void updateLassoSelection(IShapeSelection<T> shapeSelection, Shape shape, boolean ctrlDown,
			boolean shiftDown) {
		List<T> elements = shapeSelection.getElementsInShape(shape);

		if (ctrlDown) {
			selectionModel.addToSelection(elements);
		} else {
			selectionModel.setSelection(elements);
		}

		if (ctrlDown && !shiftDown) {
			selectionModel.addToSelection(elements);
		} else if (!ctrlDown && shiftDown) {
			selectionModel.removeFromSelection(elements);
		} else if (ctrlDown && shiftDown) {
			System.err.println(
					getClass().getSimpleName() + ": ignoring selection even because SHIFT and STRG have been pressed.");
		} else {
			selectionModel.setSelection(elements);
		}
	}

	public SelectionModel<T> getSelectionModel() {
		return selectionModel;
	}

}
