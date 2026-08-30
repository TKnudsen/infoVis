package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.impl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

import javax.imageio.ImageIO;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;

/**
 * <p>
 * Base for 2D colormaps backed by a pre-rendered lookup image bundled as a
 * classpath resource next to the subclass: {@code (x, y) in [0,1]x[0,1]}
 * picks a pixel exactly the way the reference JavaScript implementation this
 * class ports does -- {@code pixel = floor(coordinate * (dimension - 1))},
 * no interpolation. This lets colormaps whose design is only available as a
 * pixel table (not a closed-form formula) be re-implemented exactly, rather
 * than approximated. See the individual subclasses for the origin of their
 * specific image.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public abstract class LookupImageColorMap2D extends AbstractColorMap2D {

	private final BufferedImage image;

	protected LookupImageColorMap2D(String resourceName, ColorSpace colorSpace) {
		this(loadImage(resourceName), colorSpace);
	}

	private LookupImageColorMap2D(BufferedImage image, ColorSpace colorSpace) {
		super((x, y) -> sample(image, x, y), colorSpace);

		this.image = image;
	}

	private static BufferedImage loadImage(String resourceName) {
		try (InputStream in = LookupImageColorMap2D.class.getResourceAsStream(resourceName)) {
			if (in == null)
				throw new IllegalStateException(
						"LookupImageColorMap2D: resource not found on classpath: " + resourceName);
			return ImageIO.read(in);
		} catch (IOException e) {
			throw new UncheckedIOException("LookupImageColorMap2D: failed to load " + resourceName, e);
		}
	}

	private static Color sample(BufferedImage image, Double x, Double y) {
		Objects.requireNonNull(image);

		int px = clamp((int) (x * (image.getWidth() - 1)), image.getWidth() - 1);
		int py = clamp((int) (y * (image.getHeight() - 1)), image.getHeight() - 1);

		return new Color(image.getRGB(px, py));
	}

	private static int clamp(int value, int max) {
		return Math.max(0, Math.min(max, value));
	}

	/**
	 * the underlying lookup image, e.g. for rendering a preview without going
	 * through {@link #getColor(double, double)} pixel by pixel
	 */
	public BufferedImage getImage() {
		return image;
	}

}
