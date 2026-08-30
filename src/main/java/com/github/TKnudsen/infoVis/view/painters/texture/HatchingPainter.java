package com.github.TKnudsen.infoVis.view.painters.texture;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.painters.ChartPainter;
import com.github.TKnudsen.infoVis.view.tools.DisplayTools;

/**
 * <p>
 * HatchingPainter: Draws a hatched texture by creating a buffered tile image
 * once and then repeatedly drawing it over the target rectangle.
 * </p>
 *
 * @version 1.02
 * @since 2026
 */
public class HatchingPainter extends ChartPainter {

	// constructor attributes
	private Color[] colors;
	private double[] colorWidths;
	private float angle;

	// internal attributes
	private transient BufferedImage bufferedImage;
	private transient int cacheHash;

	/**
	 * @param colors      stripe colors
	 * @param colorwidths stripe widths (pixel units)
	 * @param angle       angle of line textures. 0 means horizontal, 90 vertical,
	 *                    positive means 1st quadrant, negative means 4th quadrant
	 */
	public HatchingPainter(Color[] colors, double[] colorwidths, float angle) {
		this.setBackgroundPaint(null);

		setHatching(colors, colorwidths, angle);
	}

	// =====================================================================
	// Configuration (minimal additions)
	// =====================================================================

	public void setHatching(Color[] colors, double[] colorwidths, float angle) {
		validateInputs(colors, colorwidths);
		this.colors = colors.clone();
		this.colorWidths = colorwidths.clone();
		this.angle = angle;

		invalidateCache();
	}

	public void setColors(Color[] colors) {
		validateInputs(colors, this.colorWidths);
		this.colors = colors.clone();
		invalidateCache();
	}

	public void setColorWidths(double[] colorwidths) {
		validateInputs(this.colors, colorwidths);
		this.colorWidths = colorwidths.clone();

		invalidateCache();
	}

	public void setAngle(float angle) {
		this.angle = angle;
		invalidateCache();
	}

	public double[] getColorwidths() {
		return colorWidths == null ? null : colorWidths.clone();
	}

	public Color[] getColors() {
		return colors == null ? null : colors.clone();
	}

	public float getAngle() {
		return angle;
	}

	private void invalidateCache() {
		bufferedImage = null;
		cacheHash = 0;
	}

	private static void validateInputs(Color[] colors, double[] widths) {
		Objects.requireNonNull(colors, "colors must not be null");
		Objects.requireNonNull(widths, "colorwidths must not be null");

		if (colors.length == 0) {
			throw new IllegalArgumentException("colors must not be empty");
		}
		if (widths.length == 0) {
			throw new IllegalArgumentException("colorwidths must not be empty");
		}
		if (colors.length != widths.length) {
			throw new IllegalArgumentException("colors.length must equal colorwidths.length");
		}
		for (double w : widths) {
			if (w <= 0.0) {
				throw new IllegalArgumentException("All colorwidths must be > 0");
			}
		}
	}

	private int computeCacheHash() {
		int result = 17;
		result = 31 * result + Arrays.hashCode(colors);
		result = 31 * result + Arrays.hashCode(colorWidths);
		result = 31 * result + Float.floatToIntBits(angle);
		return result;
	}

	// =====================================================================
	// Painting (original logic preserved)
	// =====================================================================

	@Override
	public void draw(Graphics2D g2) {

		super.draw(g2);

		if (rectangle == null || rectangle.getWidth() <= 0 || rectangle.getHeight() <= 0) {
			return;
		}

		Color oldColor = g2.getColor();

		ensureTexture();

		if (bufferedImage != null && bufferedImage.getWidth() > 0 && bufferedImage.getHeight() > 0) {
			for (int y = (int) rectangle.getMinY(); y < rectangle.getMaxY(); y += bufferedImage.getHeight()) {
				for (int x = (int) rectangle.getMinX(); x < rectangle.getMaxX(); x += bufferedImage.getWidth()) {

					BufferedImage dest = null;

					// right edge cropping
					if (x + bufferedImage.getWidth() > rectangle.getMaxX()) {
						int remainingW = (int) Math.round(Math.abs(rectangle.getMaxX() - x));
						if (remainingW < 1) {
							continue;
						}
						dest = bufferedImage.getSubimage(0, 0, remainingW, bufferedImage.getHeight());
					}

					// bottom edge cropping
					if (y + bufferedImage.getHeight() > rectangle.getMaxY()) {
						int remainingH = (int) Math.round(Math.abs(rectangle.getMaxY() - y));
						if (remainingH < 1) {
							continue;
						}
						if (dest == null) {
							dest = bufferedImage.getSubimage(0, 0, bufferedImage.getWidth(), remainingH);
						} else {
							dest = dest.getSubimage(0, 0, dest.getWidth(), remainingH);
						}
					}

					if (dest == null) {
						g2.drawImage(bufferedImage, null, x, y);
					} else {
						g2.drawImage(dest, null, x, y);
					}
				}
			}
		}

		if (isDrawOutline()) {
			g2.setPaint(getBorderPaint());
			DisplayTools.drawRectangle(g2, rectangle);
		}

		g2.setColor(oldColor);
	}

	private void ensureTexture() {
		int h = computeCacheHash();
		if (bufferedImage != null && h == cacheHash) {
			return;
		}
		cacheHash = h;

		int w = 0;
		for (int i = 0; i < colorWidths.length; i++) {
			w += (int) Math.round(colorWidths[i]);
		}
		if (w <= 0) {
			bufferedImage = null;
			return;
		}

		int sizeMultiple = (int) Math.max(1, MathFunctions.round(200 / (double) w, 0));
		bufferedImage = HatchingUtils.createTexture(colors, colorWidths, angle, sizeMultiple);
	}

}