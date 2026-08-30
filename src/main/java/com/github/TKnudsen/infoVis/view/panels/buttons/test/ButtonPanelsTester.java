package com.github.TKnudsen.infoVis.view.panels.buttons.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.painters.buttons.CloseButtonPainter;
import com.github.TKnudsen.infoVis.view.painters.buttons.MinusPainter;
import com.github.TKnudsen.infoVis.view.painters.buttons.PlusPainter;
import com.github.TKnudsen.infoVis.view.panels.buttons.CloseButtonPanel;
import com.github.TKnudsen.infoVis.view.panels.buttons.FilterButtonPanel;
import com.github.TKnudsen.infoVis.view.panels.buttons.InteractiveButtonPanel;
import com.github.TKnudsen.infoVis.view.panels.buttons.MinusButtonPanel;
import com.github.TKnudsen.infoVis.view.panels.buttons.PlusButtonPanel;

/**
 * <p>
 * Standalone demo dropping an SVGFrame with one panel per button type
 * (close, minus, plus, icon-based filter).
 * </p>
 *
 * @version 1.0
 */
public class ButtonPanelsTester {

	public static void main(String[] args) {
		List<InteractiveButtonPanel<?>> panels = new ArrayList<>();

		Color focusedColor = Color.RED;
		Color notFocusedColor = Color.GRAY;

		panels.add(new CloseButtonPanel(new CloseButtonPainter(focusedColor, notFocusedColor)));

		panels.add(new MinusButtonPanel(new MinusPainter(focusedColor, notFocusedColor, Color.WHITE)));

		panels.add(new PlusButtonPanel(new PlusPainter(focusedColor, notFocusedColor, Color.WHITE)));

		// requires icons/filterWhite.png relative to the working directory
		panels.add(new FilterButtonPanel(focusedColor, notFocusedColor));

		JPanel grid = new JPanel();
		for (InteractiveButtonPanel<?> p : panels)
			grid.add(p);

		SVGFrameTools.dropSVGFrame(grid, "ButtonPanels", panels.size() * 25, 25);
	}

}
