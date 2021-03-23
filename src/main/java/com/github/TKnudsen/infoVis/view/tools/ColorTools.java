package com.github.TKnudsen.infoVis.view.tools;

import java.awt.Color;
import java.awt.Paint;
import java.util.Collection;
import java.util.Objects;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Little helpers when working with colors.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2021 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 */
public class ColorTools {

	/**
	 * accepts Paint objects even if only Color objects can be processed at the
	 * moment. Still, the more general modeling helps for Paint-oriented classes.
	 * 
	 * @param color
	 * @param alpha
	 * @return
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
	 * @param c
	 * @param brightness
	 * @param saturation
	 * @return
	 */
	public static Color setBrightnessAndSaturation(Color c, int brightness, int saturation) {
		float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		int hue = (int) (hsv[0] * 255);
		return new Color(Color.HSBtoRGB((float) hue / 255, (float) saturation / 255, (float) brightness / 255));
	}

	/**
	 * merges two colors to a new color using RGB arithmetics.
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	public static Color mergeColors(Color color1, Color color2) {
		if (color1 == null && color2 == null)
			return null;
		if (color1 == null)
			return new Color(color2.getRed(), color2.getGreen(), color2.getBlue());
		if (color2 == null)
			return new Color(color1.getRed(), color1.getGreen(), color1.getBlue());

		int r = color1.getRed() + color2.getRed();
		int g = color1.getGreen() + color2.getGreen();
		int b = color1.getBlue() + color2.getBlue();

		return new Color(r / 2, g / 2, b / 2);
	}

	/**
	 * 
	 * @param color1
	 * @param color2
	 * @return
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
	 * @param colors
	 * @return
	 */
	public static Color mergeColors(Collection<Color> colors) {
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
	 * @return
	 */
	public static Color randomColor() {
		return new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
	}

	/**
	 * 
	 * @param color
	 * @return
	 */
	public static int getRGB(Color color) {
		if (color == null)
			return Color.BLACK.getRGB();

		return color.getRGB();
	}

	/**
	 * 
	 * @param rgb
	 * @return
	 */
	public static Color getColor(int rgb) {
		return new Color(rgb);
	}

	/**
	 * 
	 * @param paint
	 * @return
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
	 * @param paint
	 * @return
	 */
	public static Color darker(Paint paint) {
		if (paint == null)
			return null;

		if (paint instanceof Color)
			return ((Color) paint).darker();

		System.err.println("ColorTools.darker not successful for paint " + paint);
		return Color.BLACK;
	}
}
