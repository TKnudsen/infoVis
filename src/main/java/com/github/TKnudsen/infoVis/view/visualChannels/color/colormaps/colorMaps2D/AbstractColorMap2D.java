package com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Objects;
import java.util.function.BiFunction;

import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;

/**
 * <p>
 * Self-contained base for 2D colormaps: {@code (x, y) in
 * [0,1]x[0,1] -> Color}, supplied as a plain {@link BiFunction} rather than
 * requiring any particular third-party colormap library -- unlike {@code
 * com.JB.colormaps.colorMaps2D.ColorMap2D} in the legacy {@code ColorMapLib},
 * which only exists as a wrapper around Fraunhofer IGD's {@code
 * AbstractKnownColormap} and cannot be published. A colormap backed by such a
 * third-party library can still implement {@link IColorMap2D} directly and
 * interoperate with everything here (see the legacy library's own {@code
 * ColorMap2D}, retrofitted to implement this package's {@link IColorMap2D})
 * -- it just does not need this particular base class.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public abstract class AbstractColorMap2D implements IColorMap2D {

	private final BiFunction<Double, Double, Color> colorFunction;
	private final ColorSpace colorSpace;

	private BufferedImage colormapImage;

	protected AbstractColorMap2D(BiFunction<Double, Double, Color> colorFunction, ColorSpace colorSpace) {
		this.colorFunction = Objects.requireNonNull(colorFunction, "colorFunction must not be null");
		this.colorSpace = Objects.requireNonNull(colorSpace, "colorSpace must not be null");
	}

	@Override
	public Color getColor(double x, double y) {
		return colorFunction.apply(x, y);
	}

	@Override
	public Color getColor(float x, float y) {
		return getColor((double) x, (double) y);
	}

	@Override
	public ColorSpace getColorSpace() {
		return colorSpace;
	}

	@Override
	public void drawColormap(Rectangle2D rect, Graphics2D g, boolean reverse) {
		if (rect == null || rect.getWidth() <= 0 || rect.getHeight() <= 0)
			return;

		int width = (int) rect.getWidth();
		int height = (int) rect.getHeight();

		if (colormapImage == null || colormapImage.getWidth() != width || colormapImage.getHeight() != height) {
			colormapImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			WritableRaster raster = colormapImage.getRaster();

			for (int h = 0; h < height; h++)
				for (int w = 0; w < width; w++) {
					double x = (double) w / width;
					double y = (double) h / height;
					if (reverse) {
						x = 1.0 - x;
						y = 1.0 - y;
					}
					Color color = getColor(x, y);
					raster.setPixel(w, h, new int[] { color.getRed(), color.getGreen(), color.getBlue() });
				}
		}

		g.drawImage(colormapImage, (int) rect.getX(), (int) rect.getY(), width, height, null);
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public String getDescription() {
		return getClass().getSimpleName();
	}

}
