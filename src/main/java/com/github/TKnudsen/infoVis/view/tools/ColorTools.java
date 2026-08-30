package com.github.TKnudsen.infoVis.view.tools;

import java.awt.Color;
import java.awt.Paint;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;

/**
 * <p>
 * Little helpers when working with colors.
 * </p>
 *
 * @version 1.06
 * @since 2016
 */
public class ColorTools {

	/**
	 * accepts Paint objects even if only Color objects can be processed at the
	 * moment. Still, the more general modeling helps for Paint-oriented classes.
	 * 
	 * @param color color
	 * @param alpha alpha
	 * @return color
	 */
	public static Color setAlpha(Paint color, float alpha) {
		Objects.requireNonNull(color);

		if (alpha < 0.0 || alpha > 1.0)
			throw new IllegalArgumentException("invalid alpha value: " + alpha);

		if (color instanceof Color) {
			Color c = (Color) color;

			return new Color(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, alpha);
		}

		throw new IllegalArgumentException("ColorTools.setAlpha: Paint object (" + color + ") was not of type Color.");
	}

	public static Color setBrightness(Color c, int brightness) {
		float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		int hue = (int) (hsv[0] * 255);
		int saturation = (int) (hsv[1] * 255);
		return new Color(Color.HSBtoRGB((float) hue / 255, (float) saturation / 255, (float) brightness / 255));
	}

	/**
	 * TODO needs testing. appears to produce reddish colors
	 * 
	 * @param c          color
	 * @param brightness brightness
	 * @param saturation saturation
	 * @return color
	 */
	public static Color setBrightnessAndSaturation(Color c, int brightness, int saturation) {
		float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		int hue = (int) (hsv[0] * 255);
		return new Color(Color.HSBtoRGB((float) hue / 255, (float) saturation / 255, (float) brightness / 255));
	}

	/**
	 * merges two colors to a new color using RGB arithmetics.
	 * 
	 * @param color1 color
	 * @param color2 color
	 * @return color
	 */
	public static Color mergeColors(Color color1, Color color2) {
		return mergeColors(color1, color2, 0.5);
	}

	/**
	 * merges two colors to a new color using RGB arithmetics.
	 * 
	 * @param color1 first color
	 * @param color2 second color
	 * @param weight 0 means 100% first where as 1 means 100% second
	 * @return merged color
	 */
	public static Color mergeColors(Color color1, Color color2, double weight) {
		if (color1 == null && color2 == null)
			return null;
		if (color1 == null)
			return new Color(color2.getRed(), color2.getGreen(), color2.getBlue());
		if (color2 == null)
			return new Color(color1.getRed(), color1.getGreen(), color1.getBlue());

		double w = Double.isNaN(weight) ? 0.5 : Math.max(0.0, Math.min(1.0, weight));

		int r = clampRgb((int) (color1.getRed() * (1 - w) + color2.getRed() * w));
		int g = clampRgb((int) (color1.getGreen() * (1 - w) + color2.getGreen() * w));
		int b = clampRgb((int) (color1.getBlue() * (1 - w) + color2.getBlue() * w));

		return new Color(r, g, b);
	}

	/**
	 * 
	 * @param color1 color
	 * @param color2 color
	 * @return color
	 */
	public static Color mergeColors(Paint color1, Paint color2) {
		if (color1 == null || color2 == null)
			return null;

		if (color1 instanceof Color && color2 instanceof Color) {
			return mergeColors((Color) color1, (Color) color2);
		}

		throw new IllegalArgumentException(
				"ColorTools.mergeColors: a Paint object (" + color1 + " || " + color2 + ") was not of type Color.");
	}

	/**
	 * 
	 * @param colors the colors
	 * @return the color
	 */
	public static Color mergeColors(Collection<Color> colors) {
		if (colors == null || colors.isEmpty())
			return null;

		double r = 0;
		double g = 0;
		double b = 0;
		double alpha = 0;

		for (Color c : colors) {
			r += c.getRed();
			g += c.getGreen();
			b += c.getBlue();
			alpha += c.getAlpha();
		}

		return new Color((float) r / colors.size() / 255, (float) g / colors.size() / 255,
				(float) b / colors.size() / 255, (float) alpha / colors.size() / 255);
	}

	/**
	 * 
	 * @return color
	 */
	public static Color randomColor() {
		return new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
	}

	/**
	 * 
	 * @return color
	 */
	public static Color randomColor(Random random) {
		return new Color((float) random.nextFloat(), (float) random.nextFloat(), (float) random.nextFloat());
	}

	/**
	 * 
	 * @param color color
	 * @return rgb
	 */
	public static int getRGB(Color color) {
		if (color == null)
			return Color.BLACK.getRGB();

		return color.getRGB();
	}

	/**
	 * 
	 * @param rgb rgb
	 * @return color
	 */
	public static Color getColor(int rgb) {
		return new Color(rgb);
	}

	/**
	 * Relative luminance in [0..1], using WCAG formula with sRGB gamma correction.
	 */
	public static float calculateLuminance(Color color) {
		Objects.requireNonNull(color, "color required");
		float r = (float) gammaCorrect(color.getRed() / 255.0);
		float g = (float) gammaCorrect(color.getGreen() / 255.0);
		float b = (float) gammaCorrect(color.getBlue() / 255.0);
		return 0.2126f * r + 0.7152f * g + 0.0722f * b;
	}

	private static double gammaCorrect(double channel) {
		if (channel <= 0.03928) {
			return channel / 12.92;
		}
		return Math.pow((channel + 0.055) / 1.055, 2.4);
	}

	/**
	 * Calculates the brightness of a color using the NTSC formula.
	 *
	 * @param color The color to calculate brightness for.
	 * @return The brightness of the color as a float between 0.0 and 255.0.
	 */
	public static float calculateBrightness(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();

		return (0.299f * r + 0.587f * g + 0.114f * b);
	}

	/**
	 * Calculates the saturation of a color.
	 *
	 * @param color The color to calculate saturation for.
	 * @return The saturation of the color as a float between 0.0 and 255.0.
	 */
	public static float calculateSaturation(Color color) {
		float[] hsb = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);

		return hsb[1] * 255;
	}

	/**
	 * Calculates the hue of a color.
	 *
	 * @param color The color to calculate hue for.
	 * @return The hue of the color as a float in the range 0.0 - 360.0.
	 */
	public static float calculateHue(Color color) {
		float[] hsb = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);

		return hsb[0] * 360.0f;
	}

	/**
	 * 
	 * @param paint color
	 * @return color
	 */
	public static Color brighter(Paint paint) {
		if (paint == null)
			return null;

		if (paint instanceof Color)
			return ((Color) paint).brighter();

		System.err.println("ColorTools.brighter not successful for paint " + paint);
		return Color.BLACK;
	}

	/**
	 * 
	 * @param paint color
	 * @return color
	 */
	public static Color darker(Paint paint) {
		if (paint == null)
			return null;

		if (paint instanceof Color)
			return ((Color) paint).darker();

		System.err.println("ColorTools.darker not successful for paint " + paint);
		return Color.BLACK;
	}

	// ============================================================
	// Color math
	// ============================================================

	public static int clampRgb(int v) {
		return Math.max(0, Math.min(255, v));
	}
}
