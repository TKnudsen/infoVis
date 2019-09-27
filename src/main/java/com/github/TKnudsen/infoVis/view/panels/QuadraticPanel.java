package com.github.TKnudsen.infoVis.view.panels;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class QuadraticPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4731580232988624669L;

	public QuadraticPanel() {
	}

	public QuadraticPanel(JComponent nestedComponent) {
		this.setLayout(new GridLayout(1, 1));
		this.add(nestedComponent);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		int min = (width > height) ? height : width;

		int x0 = x;
		int y0 = y;

		if (width > min)
			x0 += ((width - min) * 0.5);

		if (height > min)
			y0 = (int) ((height - min) * 0.5);

		super.setBounds(x0, y0, min, min);
	}

}
