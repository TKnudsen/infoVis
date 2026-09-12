package com.github.TKnudsen.infoVis.view.panels;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * <p>
 * Panel that constrains its nested component to a fixed width:height aspect
 * ratio (1:1, i.e. square, by default), centering it within the available
 * bounds.
 * </p>
 *
 * @version 1.1
 */
public class QuadraticPanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = -4731580232988624669L;

	private final double aspectRatio;

	public QuadraticPanel() {
		this.aspectRatio = 1.0;
	}

	public QuadraticPanel(JComponent nestedComponent) {
		this(nestedComponent, 1.0);
	}

	/**
	 * @param nestedComponent the component to constrain
	 * @param aspectRatio     desired width:height ratio, e.g. 1.0 for square,
	 *                        1.5 for 3:2, 0.5 for a portrait 1:2
	 */
	public QuadraticPanel(JComponent nestedComponent, double aspectRatio) {
		if (aspectRatio <= 0 || !Double.isFinite(aspectRatio))
			throw new IllegalArgumentException("aspectRatio must be positive and finite, was " + aspectRatio);

		this.aspectRatio = aspectRatio;
		this.setLayout(new GridLayout(1, 1));
		this.add(nestedComponent);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		int targetWidth = width;
		int targetHeight = height;

		if (width / aspectRatio > height)
			targetWidth = (int) Math.round(height * aspectRatio);
		else
			targetHeight = (int) Math.round(width / aspectRatio);

		int x0 = x + (int) ((width - targetWidth) * 0.5);
		int y0 = y + (int) ((height - targetHeight) * 0.5);

		super.setBounds(x0, y0, targetWidth, targetHeight);
	}

}
