package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.IColorMap;

/**
 * <p>
 * Base panel rendering an {@link IColorMap} as a swatch with an optional
 * outline.
 * </p>
 *
 * @version 1.0
 */
public abstract class ColorMapPanel extends JPanel {

	/**
		 * 
		 */
	private static final long serialVersionUID = -3820308197407602614L;

	protected IColorMap colorMap;

	private Color outlineColor = Color.BLACK;

	public ColorMapPanel(IColorMap colorMap) {
		this.colorMap = colorMap;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		Color c = g2.getColor();

		drawColorMap(g2);

		if (outlineColor != null) {
			g2.setColor(outlineColor);
			g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}

		g2.setColor(c);
	}

	public void drawColorMap(Graphics2D g) {
		// paintComponent's Graphics is already translated to this component's own
		// origin, so the drawing rectangle must start at (0,0) -- using getBounds()
		// here (this component's position within its PARENT) draws the colormap
		// offset by that parent-relative x/y, landing outside this panel's own
		// clip region whenever the panel isn't at (0,0) in its parent.
		Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());
		colorMap.drawColormap(bounds, g, false);
	}

	public IColorMap getColorMap() {
		return colorMap;
	}

	public void setColorMap(IColorMap colorMap) {
		this.colorMap = colorMap;
	}

	public Color getOutlineColor() {
		return outlineColor;
	}

	public void setOutlineColor(Color outlineColor) {
		this.outlineColor = outlineColor;
	}
}
