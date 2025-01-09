package com.github.TKnudsen.infoVis.view.painters.bufferedImage;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * Paints buffered images.
 * 
 * 2023-04-16: background paint in the constructor set to null per default.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2024
 * </p>
 * 
 * @version 1.17
 */
public class BufferedImagePainter extends ChartPainter {

	private BufferedImage bImage = null;
	private BufferedImage scaledBImage = null;
	private boolean keepRatio = false;

	public BufferedImagePainter(BufferedImage bImage) {
		if (bImage == null)
			return;

		this.bImage = bImage;
		this.setRectangle(bImage.getData().getBounds());
		this.setDrawOutline(false);
		// new since 1.16, as of 2023-04-16
		this.setBackgroundPaint(null);
	}

	public BufferedImagePainter(BufferedImage bImage, boolean keepRatio) {
		this(bImage);

		this.keepRatio = keepRatio;
	}

	@Override
	/**
	 * Scalability 1.000.000 iterations take ~2.000 ms. One iteration takes 0.002
	 * ms.
	 */
	public void draw(Graphics2D g2) {
		Stroke s = g2.getStroke();

		if (rectangle == null)
			return;

		super.draw(g2);

		if (scaledBImage == null)
			rescale();
		if (scaledBImage != null)
			g2.drawImage(scaledBImage,
					(int) (rectangle.getX() + 0.5 * (rectangle.getWidth() - scaledBImage.getWidth())),
					(int) (rectangle.getY() + 0.5 * (rectangle.getHeight() - scaledBImage.getHeight())), null, null);

		if (isDrawOutline()) {
			g2.setStroke(stroke);
			DisplayTools.drawRectangle(g2, rectangle, getPaint());
		}

		g2.setStroke(s);
	}

	@Override
	public void setRectangle(Rectangle2D rectangle) {
		super.setRectangle(rectangle);
		rescale();
	}

	private void rescale() {
		if (bImage == null || rectangle == null)
			return;

		if (Double.isNaN(rectangle.getX()) || Double.isNaN(rectangle.getY()) || Double.isNaN(rectangle.getWidth())
				|| Double.isNaN(rectangle.getHeight())) {
			System.err.println("BufferedImagePainter: rectangle with null entries. canceling rescale.");
			return;
		}

		if (rectangle.getWidth() <= 0 || rectangle.getHeight() <= 0)
			scaledBImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_INDEXED);
		else if (bImage.getData().getBounds().equals(rectangle)) {
			scaledBImage = bImage;
			return;
		} else if (scaledBImage.getData().getBounds().equals(rectangle)) {
			return;
		} else {
			int width;
			int height;
			if (keepRatio) {
				double ratio = Math.min(rectangle.getWidth() / bImage.getWidth(),
						rectangle.getHeight() / bImage.getHeight());
				width = (int) Math.max(Math.round(ratio * bImage.getWidth()), 1.0);
				height = (int) Math.max(Math.round(ratio * bImage.getHeight()), 1.0);
			} else {
				width = (int) rectangle.getWidth();
				height = (int) rectangle.getHeight();
			}
			scaledBImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics2D = scaledBImage.createGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics2D.drawImage(bImage, 0, 0, (int) rectangle.getWidth(), (int) rectangle.getHeight(), null);
		}
	}

	public void setData(BufferedImage bImage) {
		this.bImage = bImage;
	}

	/**
	 * @return the keepRatio
	 */
	public boolean isKeepRatio() {
		return keepRatio;
	}

	/**
	 * @param keepRatio the keepRatio to set
	 */
	public void setKeepRatio(boolean keepRatio) {
		this.keepRatio = keepRatio;
	}

}
