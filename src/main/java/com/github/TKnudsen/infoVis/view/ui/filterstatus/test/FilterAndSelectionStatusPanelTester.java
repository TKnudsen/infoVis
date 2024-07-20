package com.github.TKnudsen.infoVis.view.ui.filterstatus.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.ui.NimbusUITools;
import com.github.TKnudsen.infoVis.view.ui.filterstatus.FilterAndSelectionStatusPanel;

import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

public class FilterAndSelectionStatusPanelTester {

	public static void main(String[] args) {
		Set<String> data = new HashSet<>(Arrays.asList("a", "b", "c"));
		SelectionModel<String> selectionModel = SelectionModels.create();
		selectionModel.addToSelection("a");

		NimbusUITools.switchToNimbus();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				SVGFrameTools.dropSVGFrame(new FilterAndSelectionStatusPanel<String>(data,
						e -> e.equals("b") ? false : true, selectionModel::isSelected), "Test", 250, 50);
			}
		});
	}

}
