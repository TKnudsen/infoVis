package com.github.TKnudsen.infoVis.view.tools;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class BorderTools {
	public static JPanel wrapWithTitle(String title, JComponent component) {
		JPanel panel = new JPanel(new GridLayout(1, 1));
		panel.add(component);
		panel.setBorder(BorderFactory.createTitledBorder(title));
		return panel;
	}
}
