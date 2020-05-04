package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.TKnudsen.infoVis.view.interaction.IClickSelection;
import com.github.TKnudsen.infoVis.view.interaction.IRectangleSelection;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

import de.javagl.selection.SelectionModel;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Copyright: (c) 2017-2019 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class SelectionHandler<T> extends InteractionHandler {

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

	private IClickSelection<T> clickSelection;
	private IRectangleSelection<T> rectangleSelection;

	private SelectionModel<T> selectionModel;

	public SelectionHandler(SelectionModel<T> selectionModel, MouseButton mouseButton) {
		this(selectionModel);

		setMouseButton(mouseButton);
	}

	public SelectionHandler(SelectionModel<T> selectionModel) {
		this.selectionModel = selectionModel;

		this.mouseListener = createMouseListener();
		this.mouseMotionListener = createMouseMotionListener();
	}

	public void setClickSelection(IClickSelection<T> clickSelection) {
		this.clickSelection = clickSelection;
	}

	public void setRectangleSelection(IRectangleSelection<T> rectangleSelection) {
		this.rectangleSelection = rectangleSelection;
	}

	private MouseListener createMouseListener() {
		return new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (acceptMouseButton(e)) {
					Rectangle2D selectionRectangle = getSelectionRectangle();
					if (selectionRectangle != null) {
						handleRectangleSelection(selectionRectangle, e.isControlDown(), e.isShiftDown());
					}

					pointSelectionStart = null;
					pointSelectionEnd = null;
					component.repaint();
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (acceptMouseButton(e)) {
					pointSelectionStart = e.getPoint();
					pointSelectionEnd = e.getPoint();
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (acceptMouseButton(e)) {
					handleClickSelection(e.getPoint(), e.isControlDown(), e.isShiftDown());
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
					pointSelectionEnd = e.getPoint();

					component.repaint();
				}
			}
		};

	}

	public void draw(Graphics2D g) {
		Rectangle2D selectionRectangle = getSelectionRectangle();
		if (selectionRectangle != null) {
			g.setStroke(DisplayTools.standardDashedStroke);
			g.setColor(Color.RED);
			g.draw(selectionRectangle);
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

	private void handleClickSelection(Point p, boolean ctrlDown, boolean shiftDown) {
		if (clickSelection != null) {
			updateClickSelection(clickSelection, p, ctrlDown, shiftDown);
		}
	}

	private void updateClickSelection(IClickSelection<T> clickSelection, Point p, boolean ctrlDown, boolean shiftDown) {
		List<T> elements = clickSelection.getElementsAtPoint(p);
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

	private void handleRectangleSelection(Rectangle2D rectangle, boolean ctrlDown, boolean shiftDown) {
		if (rectangleSelection != null) {
			updateRectangleSelection(rectangleSelection, rectangle, ctrlDown, shiftDown);
		}
	}

	private void updateRectangleSelection(IRectangleSelection<T> rectangleSelection, Rectangle2D rectangle,
			boolean ctrlDown, boolean shiftDown) {

		List<T> elements = rectangleSelection.getElementsInRectangle(rectangle);
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
