package com.github.TKnudsen.infoVis.view.panels.buttons;

import com.github.TKnudsen.infoVis.view.painters.buttons.MinusPainter;

/**
 * @version 1.0
 * @since 2018
 */
public class MinusButtonPanel extends InteractiveButtonPanel<MinusPainter> {

	private static final long serialVersionUID = 1L;

	public MinusButtonPanel(MinusPainter painter) {
		super(painter, "minus");
	}

}
