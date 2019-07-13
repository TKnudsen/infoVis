package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

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
 * @version 1.02
 *
 */
public abstract class InteractionHandler {

	protected Component component;

	private MouseButton mouseButton = MouseButton.LEFT;

	final boolean acceptMouseButton(MouseEvent e) {
		if (mouseButton.equals(MouseButton.LEFT) && !SwingUtilities.isLeftMouseButton(e))
			return false;
		if (mouseButton.equals(MouseButton.RIGHT) && !SwingUtilities.isRightMouseButton(e))
			return false;

		return true;
	}

	public abstract void attachTo(Component newComponent);

	public MouseButton getMouseButton() {
		return mouseButton;
	}

	public void setMouseButton(MouseButton mouseButton) {
		this.mouseButton = mouseButton;
	}
}
