package com.github.TKnudsen.infoVis.view.visualChannels.color.impl.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.visualChannels.color.IColorEncodingFunction;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.ColorSpace;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.AbstractColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.Colormap2DPanel;
import com.github.TKnudsen.infoVis.view.visualChannels.color.colormaps.colorMaps2D.IColorMap2D;
import com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorMapEncodingFunction2D;

/**
 * <p>
 * Worked example for {@link ColorMapEncodingFunction2D}, the 2D
 * sibling of {@link com.github.TKnudsen.infoVis.view.visualChannels.color.impl.ColorMapEncodingFunction}.
 * Same two-layer idea, one more axis: a palette maps {@code (x, y) in
 * [0,1]x[0,1]} to a {@link Color}, and the adapter supplies two normalizers
 * -- one per axis -- to connect that palette to a domain type T.
 * 
 * This example also demonstrates {@link AbstractColorMap2D} itself: unlike
 * the legacy {@code ColorMapLib}'s {@code colorMaps2D.impl} package (which
 * only wraps colormaps from a third-party Fraunhofer IGD library and cannot
 * be published), {@code AbstractColorMap2D} needs nothing but a plain
 * {@code BiFunction<Double, Double, Color>} -- so a genuinely new,
 * publishable 2D colormap is just a few lines. (A Fraunhofer-derived
 * colormap still implements the same {@link IColorMap2D} interface and
 * plugs into this exact adapter the same way -- see {@code ColorMapLib}'s
 * own {@code FraunhoferColorMapEncodingFunction2DTest} for that side
 * of the story; its source just stays in that private library.)
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class ColorMapEncodingFunction2DTest {

	/**
	 * Two independent numeric fields -- exactly what a 2D palette needs. Values
	 * are raw, human-meaningful percentages, not pre-normalized.
	 */
	private static final class Country {
		final String name;
		final double gdpGrowthPercent;
		final double unemploymentPercent;

		Country(String name, double gdpGrowthPercent, double unemploymentPercent) {
			this.name = name;
			this.gdpGrowthPercent = gdpGrowthPercent;
			this.unemploymentPercent = unemploymentPercent;
		}
	}

	public static void main(String[] args) {

		// Step 1: author a 2D palette. This is the entire definition -- red
		// grows with x, blue grows with y, green stays fixed. Any BiFunction
		// works; this one happens to be a lambda instead of a named class.
		IColorMap2D palette = new AbstractColorMap2D((x, y) -> new Color((float) (double) x, 0.3f, (float) (double) y),
				ColorSpace.RGB) {
		};

		// Step 2: one rescale per axis, each to [0,1] via LinearNormalizationFunction
		// -- the shared rescale-to-[0,1] utility used throughout the ecosystem,
		// same as in the 1D example. Bounds here are the known, fixed range for
		// each indicator; use the Collection<Number> constructor instead when the
		// bounds are not known upfront and should be derived from the actual data.
		// Kept as named locals (rather than inlined) since the overlay dots below
		// reuse the exact same rescale.
		LinearNormalizationFunction gdpGrowthNormalization = new LinearNormalizationFunction(-3.0, 8.0);
		LinearNormalizationFunction unemploymentNormalization = new LinearNormalizationFunction(2.0, 16.0);

		// Step 3: compose extraction (per axis) + rescale + palette into a
		// domain-typed color encoding function in one call -- same shape and same
		// IColorEncodingFunction<T> contract as the 1D adapter.
		IColorEncodingFunction<Country> colorEncoding = new ColorMapEncodingFunction2D<>(c -> c.gdpGrowthPercent,
				gdpGrowthNormalization, c -> c.unemploymentPercent, unemploymentNormalization, palette);

		Country[] countries = { new Country("Overheating", 7.5, 3.0), new Country("Balanced", 2.5, 6.0),
				new Country("Recession", -2.0, 14.0), new Country("Stagflation risk", 0.5, 11.0) };

		for (Country c : countries) {
			Paint color = colorEncoding.apply(c);
			System.out.println(c.name + " (growth=" + c.gdpGrowthPercent + "%, unemployment="
					+ c.unemploymentPercent + "%) -> " + color);
		}

		// Visual: the palette's own drawColormap(...) renders the full 2D
		// gradient, so you can see where each country actually falls on it.
		Colormap2DPanel panel = new Colormap2DPanel(palette) {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				for (Country c : countries) {
					int px = (int) (gdpGrowthNormalization.apply(c.gdpGrowthPercent).doubleValue() * getWidth());
					int py = (int) (unemploymentNormalization.apply(c.unemploymentPercent).doubleValue()
							* getHeight());
					g2.setColor(Color.WHITE);
					g2.fillOval(px - 4, py - 4, 8, 8);
					g2.setColor(Color.BLACK);
					g2.drawOval(px - 4, py - 4, 8, 8);
				}
			}
		};
		panel.setPreferredSize(new Dimension(200, 200));

		JPanel wrapper = new JPanel();
		wrapper.add(panel);

		SVGFrameTools.dropSVGFrame(wrapper, "ColorMapEncodingFunction2D: (growth, unemployment) -> color", 260,
				260);
	}

}
