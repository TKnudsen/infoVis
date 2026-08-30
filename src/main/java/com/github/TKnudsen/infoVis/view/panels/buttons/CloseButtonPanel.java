package com.github.TKnudsen.infoVis.view.panels.buttons;

import com.github.TKnudsen.infoVis.view.painters.buttons.CloseButtonPainter;

/**
 * @version 1.0
 * @since 2018
 */
public class CloseButtonPanel extends InteractiveButtonPanel<CloseButtonPainter> {

	private static final long serialVersionUID = 1L;

	public CloseButtonPanel(CloseButtonPainter painter) {
		super(painter, "close");
	}

}
