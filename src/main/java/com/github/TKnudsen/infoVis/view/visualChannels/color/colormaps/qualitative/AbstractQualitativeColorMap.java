package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.qualitative;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.AbstractColorMap;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;

/**
 * @since 2012
 */
public abstract class AbstractQualitativeColorMap extends AbstractColorMap {

	protected static AbstractColorMap instance;
	protected Color[] colors;

	protected AbstractQualitativeColorMap() {
		setColors();
	}

	abstract public Color[] getColors(int count);

	abstract protected void setColors();

	@Override
	public void drawColormap(Rectangle2D rect, Graphics2D g, boolean reverse) {
		this.drawColormap(rect, g, 1.0f, "", "");
	}

	@Override
	public void drawColormap(Rectangle2D rect, Graphics2D g, float alpha, boolean reverse, String label1,
			String label2) {
		this.drawColormap(rect, g, alpha, label1, label2);
	}

	public void drawColormap(Rectangle2D rect, Graphics2D g, float alpha, boolean reverse) {
		// loop columns
		int w = (int) rect.getWidth();
		int h = (int) rect.getHeight();
		int clusterWidth = w / colors.length;
		int inc = reverse ? -1 : 1;
		for (int i = reverse ? colors.length : 0; i < colors.length; i = i + inc) {
			Color c = colors[i];
			g.setColor(
					new Color((float) c.getRed() / 255, (float) c.getGreen() / 255, (float) c.getBlue() / 255, alpha));
			g.fillRect(i * clusterWidth, 0, clusterWidth, h);
			g.setColor(Color.BLACK);
			g.drawRect(i * clusterWidth, 0, clusterWidth, h);
		}
	}

	public void drawColormap(Rectangle2D rect, Graphics2D g, float alpha, String label1, String label2) {
		this.drawColormap(rect, g, alpha, false);
		FontMetrics fm = g.getFontMetrics();

		if (label1 != null)
			g.drawString(label1, (int) rect.getMinX() + 5,
					(int) (rect.getMinY() + rect.getHeight() / 2 + fm.getHeight() / 2));
		if (label2 != null)
			g.drawString(label2, (int) rect.getMaxX() - 30,
					(int) (rect.getMinY() + rect.getHeight() / 2 + fm.getHeight() / 2));

	}

	@Override
	public ColorSpace getColorSpace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
}
