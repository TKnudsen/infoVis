package com.github.TKnudsen.infoVis.view.panels;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class AspectRatioPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final int xDim;
	private final int yDim;

	public AspectRatioPanel(int xDim, int yDim) {
		this.setLayout(new GridLayout(1, 1));

		this.xDim = xDim;
		this.yDim = yDim;
	}

	public AspectRatioPanel(int xDim, int yDim, JComponent nestedComponent) {
		this(xDim, yDim);

		this.add(nestedComponent);
	}

	public void setNestedComponent(JComponent nestedComponent) {
		this.removeAll();

		this.add(nestedComponent);

		repaint();
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		if (width == 0 || height == 0) {
			super.setBounds(x, y, width, height);
			return;
		}

		double xQ = width / (double) xDim;
		double yQ = height / (double) yDim;

		int w = width;
		int h = height;

		int x0 = x;
		int y0 = y;

		// skip if ratios are super similar (sub-pixel, etc)
		if (Math.abs(xQ - yQ) > 0.001) {
			if (xQ > yQ) {
				// all y space will be used, x reduced
				w = (int) (yQ * xDim);
				x0 += ((width - w) * 0.5);
			}

			if (yQ > xQ) {
				// all x space will be used, y reduced
				h = (int) (xQ * yDim);
				y0 = (int) ((height - h) * 0.5);
			}
		}

		super.setBounds(x0, y0, w, h);
	}

}
